package com.example.agentworkflow.data.remote

import com.example.agentworkflow.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AgentApiService {

    @GET("api/health")
    suspend fun checkHealth(): Map<String, Any>

    @GET("api/agent/tools")
    suspend fun listTools(): ToolRegistryResponse

    @POST("api/agent/intent")
    suspend fun analyzeIntent(@Body request: IntentRequest): IntentAnalysis

    @POST("api/agent/interact")
    suspend fun interact(@Body request: InteractRequest): AgentResponse
}
