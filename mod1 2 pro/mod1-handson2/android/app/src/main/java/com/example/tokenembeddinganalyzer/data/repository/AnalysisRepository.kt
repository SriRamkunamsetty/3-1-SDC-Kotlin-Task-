package com.example.tokenembeddinganalyzer.data.repository

import com.example.tokenembeddinganalyzer.data.local.LocalFallbackEngine
import com.example.tokenembeddinganalyzer.data.model.*
import com.example.tokenembeddinganalyzer.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AnalysisRepository(
    private var baseUrl: String = "http://10.0.2.2:8000/" // Android Emulator localhost
) {

    private val localEngine = LocalFallbackEngine()
    private var apiService: ApiService? = null

    init {
        createRetrofitService()
    }

    fun updateBaseUrl(newUrl: String) {
        var formatted = newUrl.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://$formatted"
        }
        if (!formatted.endsWith("/")) {
            formatted = "$formatted/"
        }
        this.baseUrl = formatted
        createRetrofitService()
    }

    private fun createRetrofitService() {
        try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        } catch (e: Exception) {
            apiService = null
        }
    }

    suspend fun analyzeTokens(text: String, encodingName: String): TokenizationResponse {
        return try {
            apiService?.analyzeTokens(TokenizeRequest(text, encodingName))
                ?: localEngine.tokenize(text, encodingName)
        } catch (e: Exception) {
            localEngine.tokenize(text, encodingName)
        }
    }

    suspend fun analyzeEmbeddings(prompts: List<String>): EmbeddingAnalysisResponse {
        return try {
            apiService?.analyzeEmbeddings(EmbeddingsRequest(prompts))
                ?: localEngine.analyzeEmbeddings(prompts)
        } catch (e: Exception) {
            localEngine.analyzeEmbeddings(prompts)
        }
    }

    suspend fun comparePrompts(promptA: String, promptB: String, encodingName: String): PromptComparisonResponse {
        return try {
            apiService?.comparePrompts(ComparePromptsRequest(promptA, promptB, encodingName))
                ?: localEngine.comparePrompts(promptA, promptB, encodingName)
        } catch (e: Exception) {
            localEngine.comparePrompts(promptA, promptB, encodingName)
        }
    }
}
