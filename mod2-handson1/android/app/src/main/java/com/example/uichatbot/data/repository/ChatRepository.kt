package com.example.uichatbot.data.repository

import com.example.uichatbot.data.local.LocalChatEngine
import com.example.uichatbot.data.model.*
import com.example.uichatbot.data.remote.ChatApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatRepository(
    private var baseUrl: String = "http://10.0.2.2:8000/" // Android Emulator localhost
) {

    private val localEngine = LocalChatEngine()
    private var apiService: ChatApiService? = null

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

            apiService = retrofit.create(ChatApiService::class.java)
        } catch (e: Exception) {
            apiService = null
        }
    }

    suspend fun sendMessage(message: String, personaId: String): ChatResponse {
        return try {
            apiService?.sendMessage(ChatMessageRequest(message, personaId))
                ?: localEngine.processMessage(message, personaId)
        } catch (e: Exception) {
            localEngine.processMessage(message, personaId)
        }
    }

    suspend fun listPersonas(): PersonasResponse {
        return try {
            apiService?.listPersonas()
                ?: localEngine.getPersonas()
        } catch (e: Exception) {
            localEngine.getPersonas()
        }
    }

    suspend fun getSuggestions(personaId: String): List<String> {
        return try {
            apiService?.getSuggestions(personaId)?.suggestions
                ?: localEngine.getSuggestions(personaId)
        } catch (e: Exception) {
            localEngine.getSuggestions(personaId)
        }
    }
}
