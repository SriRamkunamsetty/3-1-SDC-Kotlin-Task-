package com.example.llmevaluation.data.repository

import com.example.llmevaluation.data.local.LocalEvalEngine
import com.example.llmevaluation.data.model.*
import com.example.llmevaluation.data.remote.EvalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class EvaluationRepository(
    private var baseUrl: String = "http://10.0.2.2:8000/" // Android Emulator localhost
) {

    private val localEngine = LocalEvalEngine()
    private var apiService: EvalApiService? = null

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

            apiService = retrofit.create(EvalApiService::class.java)
        } catch (e: Exception) {
            apiService = null
        }
    }

    suspend fun evaluateSingle(generatedText: String, referenceText: String, taskCategory: String): SingleEvalResponse {
        return try {
            apiService?.evaluateSingle(SingleEvalRequest(generatedText, referenceText, taskCategory))
                ?: localEngine.evaluateSingle(generatedText, referenceText, taskCategory)
        } catch (e: Exception) {
            localEngine.evaluateSingle(generatedText, referenceText, taskCategory)
        }
    }

    suspend fun compareModels(
        modelAName: String, modelAText: String,
        modelBName: String, modelBText: String,
        referenceText: String
    ): ModelComparisonResponse {
        return try {
            apiService?.compareModels(CompareModelsRequest(modelAName, modelAText, modelBName, modelBText, referenceText))
                ?: localEngine.compareModels(modelAName, modelAText, modelBName, modelBText, referenceText)
        } catch (e: Exception) {
            localEngine.compareModels(modelAName, modelAText, modelBName, modelBText, referenceText)
        }
    }

    suspend fun listBenchmarkTasks(): BenchmarkTasksResponse {
        return try {
            apiService?.listBenchmarkTasks()
                ?: localEngine.getBenchmarkTasks()
        } catch (e: Exception) {
            localEngine.getBenchmarkTasks()
        }
    }
}
