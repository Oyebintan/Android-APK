package com.example.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class PredictRequest(
    val email: String
)

@JsonClass(generateAdapter = true)
data class PredictResponse(
    val prediction: String, // "spam" or "ham"
    val confidence: Double
)

interface SpamApiService {
    @POST
    suspend fun predict(
        @Url url: String,
        @Body request: PredictRequest
    ): PredictResponse
}
