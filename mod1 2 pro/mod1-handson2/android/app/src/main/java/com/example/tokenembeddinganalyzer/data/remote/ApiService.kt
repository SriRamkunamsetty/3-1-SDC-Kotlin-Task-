package com.example.tokenembeddinganalyzer.data.remote

import com.example.tokenembeddinganalyzer.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("api/health")
    suspend fun checkHealth(): Map<String, Any>

    @POST("api/analyze-tokens")
    suspend fun analyzeTokens(@Body request: TokenizeRequest): TokenizationResponse

    @POST("api/analyze-embeddings")
    suspend fun analyzeEmbeddings(@Body request: EmbeddingsRequest): EmbeddingAnalysisResponse

    @POST("api/compare-prompts")
    suspend fun comparePrompts(@Body request: ComparePromptsRequest): PromptComparisonResponse
}
