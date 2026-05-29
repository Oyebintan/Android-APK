package com.example.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class PredictRequest(
    val text: String
)

@JsonClass(generateAdapter = true)
data class PredictResponse(
    val prediction: String? = null,
    val label: String? = null,
    val confidence: Double? = null,
    val spam_probability: Double? = null,
    val probability: Double? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SampleResponse(
    val label: String? = null,
    val text: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class MetricResponse(
    val metric: String? = null,
    val value: Double? = null,
    val formula: String? = null,
    val error: String? = null
)

interface SpamApiService {
    @POST
    suspend fun predict(
        @Url url: String,
        @Body request: PredictRequest
    ): PredictResponse

    @GET
    suspend fun getSample(
        @Url url: String,
        @Query("label") label: String
    ): SampleResponse

    @GET
    suspend fun getMetric(
        @Url url: String
    ): MetricResponse
}
