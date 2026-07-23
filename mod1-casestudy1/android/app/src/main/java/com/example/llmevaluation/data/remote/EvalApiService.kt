package com.example.llmevaluation.data.remote

import com.example.llmevaluation.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EvalApiService {

    @GET("api/health")
    suspend fun checkHealth(): Map<String, Any>

    @GET("api/eval/benchmark-tasks")
    suspend fun listBenchmarkTasks(): BenchmarkTasksResponse

    @POST("api/eval/single")
    suspend fun evaluateSingle(@Body request: SingleEvalRequest): SingleEvalResponse

    @POST("api/eval/compare")
    suspend fun compareModels(@Body request: CompareModelsRequest): ModelComparisonResponse
}
