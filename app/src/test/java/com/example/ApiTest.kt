import org.junit.Test
import kotlinx.coroutines.runBlocking
import com.example.data.api.RetrofitClient
import com.example.data.api.PredictRequest

class ApiTest {
    @Test
    fun testHf() = runBlocking {
        try {
            val response = RetrofitClient.spamApiService.predict(
                "https://oyebintan-email-spam-classifier.hf.space/predict",
                PredictRequest("Verify your account to win $500")
            )
            println("HF RESPONSE: ${response.prediction} ${response.confidence}")
        } catch (e: Exception) {
            println("HF ERROR: ${e.message}")
            e.printStackTrace()
        }
    }
}
