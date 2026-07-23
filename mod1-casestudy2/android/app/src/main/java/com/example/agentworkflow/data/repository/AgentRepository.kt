package com.example.agentworkflow.data.repository

import com.example.agentworkflow.data.local.LocalAgentEngine
import com.example.agentworkflow.data.model.*
import com.example.agentworkflow.data.remote.AgentApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AgentRepository(
    private var baseUrl: String = "http://10.0.2.2:8000/" // Android Emulator localhost
) {

    private val localEngine = LocalAgentEngine()
    private var apiService: AgentApiService? = null

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

            apiService = retrofit.create(AgentApiService::class.java)
        } catch (e: Exception) {
            apiService = null
        }
    }

    suspend fun interact(prompt: String): AgentResponse {
        return try {
            apiService?.interact(InteractRequest(prompt))
                ?: localEngine.processRequest(prompt)
        } catch (e: Exception) {
            localEngine.processRequest(prompt)
        }
    }

    suspend fun analyzeIntent(prompt: String): IntentAnalysis {
        return try {
            apiService?.analyzeIntent(IntentRequest(prompt))
                ?: localEngine.processRequest(prompt).intentAnalysis
        } catch (e: Exception) {
            localEngine.processRequest(prompt).intentAnalysis
        }
    }

    suspend fun listTools(): ToolRegistryResponse {
        return try {
            apiService?.listTools()
                ?: localEngine.getRegisteredTools()
        } catch (e: Exception) {
            localEngine.getRegisteredTools()
        }
    }
}
