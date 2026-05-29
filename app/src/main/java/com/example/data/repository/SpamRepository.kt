package com.example.data.repository

import android.util.Log
import com.example.data.api.PredictRequest
import com.example.data.api.PredictResponse
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

    suspend fun classifyEmail(emailText: String): PredictResponse {
        val request = PredictRequest(email = emailText)
        
        // Custom URL list for robustness
        val urls = listOf(
            "https://final-year-project-production.up.railway.app/predict",
            "https://oyebintan-email-spam-classifier.hf.space/predict"
        )
        
        var lastException: Exception? = null
        
        for (url in urls) {
            try {
                Log.d("SpamRepository", "Attempting classification with endpoint: $url")
                val response = RetrofitClient.spamApiService.predict(url, request)
                Log.d("SpamRepository", "Successful response from: $url -> ${response.prediction} (${response.confidence})")
                return response
            } catch (e: Exception) {
                Log.w("SpamRepository", "Failed classification on: $url style: ${e.localizedMessage}")
                lastException = e
            }
        }
        
        throw lastException ?: Exception("Unknown error in Spam classification pipeline")
    }
}
