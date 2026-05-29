package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ServiceLocator
import com.example.data.api.MetricResponse
import com.example.data.local.SpamRecord
import com.example.data.repository.SpamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PredictUiState {
    object Idle : PredictUiState
    object Loading : PredictUiState
    data class Success(
        val emailText: String,
        val prediction: String, // "spam" or "ham"
        val confidence: Double,
        val timestamp: Long
    ) : PredictUiState
    data class Error(val message: String) : PredictUiState
}

sealed interface MetricUiState {
    object Idle : MetricUiState
    object Loading : MetricUiState
    data class Success(val response: MetricResponse) : MetricUiState
    data class Error(val message: String) : MetricUiState
}

class SpamViewModel(
    application: Application,
    private val repository: SpamRepository
) : AndroidViewModel(application) {

    private val _predictUiState = MutableStateFlow<PredictUiState>(PredictUiState.Idle)
    val predictUiState: StateFlow<PredictUiState> = _predictUiState.asStateFlow()

    private val _metricUiState = MutableStateFlow<MetricUiState>(MetricUiState.Idle)
    val metricUiState: StateFlow<MetricUiState> = _metricUiState.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _isLoadingSample = MutableStateFlow(false)
    val isLoadingSample: StateFlow<Boolean> = _isLoadingSample.asStateFlow()

    val historyRecords: StateFlow<List<SpamRecord>> = repository.recentRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onEmailInputChanged(newText: String) {
        _emailInput.value = newText
    }

    fun fetchRandomSample(label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingSample.value = true
            _predictUiState.value = PredictUiState.Idle // Reset prediction visually
            try {
                val res = repository.getRandomSample(label)
                if (res.text != null) {
                    _emailInput.value = res.text
                }
            } catch (e: Exception) {
                // Ignore failure for sample to keep it simple, or set error
            } finally {
                _isLoadingSample.value = false
            }
        }
    }

    fun fetchMetric(metricKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _metricUiState.value = MetricUiState.Loading
            try {
                val res = repository.getMetric(metricKey)
                _metricUiState.value = MetricUiState.Success(res)
            } catch (e: Exception) {
                _metricUiState.value = MetricUiState.Error(
                    e.localizedMessage ?: "Failed to fetch metric."
                )
            }
        }
    }

    fun classifyEmail() {
        val trimmedText = _emailInput.value.trim()
        if (trimmedText.isEmpty()) {
            _predictUiState.value = PredictUiState.Error("Email field cannot be empty. Please type or paste email body!")
            return
        }

        _predictUiState.value = PredictUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiResponse = repository.classifyEmail(trimmedText)
                
                val label = apiResponse.label ?: apiResponse.prediction ?: "ham"
                val spamProb = apiResponse.spam_probability ?: apiResponse.probability ?: 0.0
                val confidence = apiResponse.confidence ?: (spamProb * 100.0)
                
                val record = SpamRecord(
                    emailText = trimmedText,
                    prediction = label.lowercase(),
                    confidence = confidence,
                    timestamp = System.currentTimeMillis()
                )
                
                // Insert into room database (which automatically prunes to the last 10)
                repository.insertRecord(record)
                
                _predictUiState.value = PredictUiState.Success(
                    emailText = record.emailText,
                    prediction = record.prediction,
                    confidence = record.confidence, // This will be percentage
                    timestamp = record.timestamp
                )
            } catch (e: Exception) {
                _predictUiState.value = PredictUiState.Error(
                    e.localizedMessage ?: "Network error. Failed to reach server. Please check your internet connection and try again."
                )
            }
        }
    }

    fun resetPredictionState() {
        _predictUiState.value = PredictUiState.Idle
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    // Direct Factory class
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = ServiceLocator.getRepository(application)
            return SpamViewModel(application, repository) as T
        }
    }
}
