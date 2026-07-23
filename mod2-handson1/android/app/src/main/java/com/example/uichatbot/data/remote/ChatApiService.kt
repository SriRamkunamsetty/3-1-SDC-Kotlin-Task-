package com.example.uichatbot.data.remote

import com.example.uichatbot.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApiService {

    @GET("api/health")
    suspend fun checkHealth(): Map<String, Any>

    @GET("api/chat/personas")
    suspend fun listPersonas(): PersonasResponse

    @GET("api/chat/suggestions")
    suspend fun getSuggestions(@Query("persona_id") personaId: String): SuggestionsResponse

    @POST("api/chat/message")
    suspend fun sendMessage(@Body request: ChatMessageRequest): ChatResponse
}
