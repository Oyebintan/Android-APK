package com.example.data.repository

import android.util.Log
import com.example.data.api.PredictRequest
import com.example.data.api.PredictResponse
import com.example.data.api.SampleResponse
import com.example.data.api.MetricResponse
import com.example.data.api.RetrofitClient
import com.example.data.local.SpamDao
import com.example.data.local.SpamRecord
import kotlinx.coroutines.flow.Flow

class SpamRepository(private val spamDao: SpamDao) {

    val recentRecords: Flow<List<SpamRecord>> = spamDao.getRecentRecords()

    suspend fun insertRecord(record: SpamRecord) {
        spamDao.insertRecord(record)
        spamDao.pruneRecords() // keeps exactly the last 10 elements in DB
    }

    suspend fun clearHistory() {
        spamDao.clearAllRecords()
    }
    
    private val apiBaseUrl = "https://Lammyde-email-spam-classifier.hf.space"

    suspend fun classifyEmail(emailText: String): PredictResponse {
        val request = PredictRequest(text = emailText)
        val url = "$apiBaseUrl/predict"
        
        return try {
            Log.d("SpamRepository", "Attempting classification with endpoint: $url")
            RetrofitClient.spamApiService.predict(url, request)
        } catch (e: Exception) {
            Log.w("SpamRepository", "Failed classification on: $url style: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun getRandomSample(label: String): SampleResponse {
        val url = "$apiBaseUrl/sample"
        return try {
            Log.d("SpamRepository", "Fetching random sample from: $url")
            RetrofitClient.spamApiService.getSample(url, label)
        } catch (e: Exception) {
            Log.w("SpamRepository", "Failed fetching sample style: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun getMetric(metricKey: String): MetricResponse {
        val url = "$apiBaseUrl/metrics/$metricKey"
        return try {
            Log.d("SpamRepository", "Fetching metric from: $url")
            RetrofitClient.spamApiService.getMetric(url)
        } catch (e: Exception) {
            Log.w("SpamRepository", "Failed fetching metric style: ${e.localizedMessage}")
            throw e
        }
    }
}
