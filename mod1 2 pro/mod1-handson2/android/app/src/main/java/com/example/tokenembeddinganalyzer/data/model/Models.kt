package com.example.tokenembeddinganalyzer.data.model

import com.google.gson.annotations.SerializedName

data class TokenItem(
    val id: Int,
    val text: String,
    val index: Int,
    @SerializedName("byte_count") val byteCount: Int,
    @SerializedName("char_start") val charStart: Int,
    @SerializedName("char_end") val charEnd: Int,
    val type: String
)

data class TokenizationResponse(
    val text: String,
    @SerializedName("encoding_name") val encodingName: String,
    @SerializedName("token_count") val tokenCount: Int,
    @SerializedName("character_count") val characterCount: Int,
    @SerializedName("total_bytes") val totalBytes: Int,
    val tokens: List<TokenItem>,
    @SerializedName("compression_ratio") val compressionRatio: Double,
    @SerializedName("avg_bytes_per_token") val avgBytesPerToken: Double
)

data class TopDimension(
    val dimension: Int,
    val value: Double
)

data class EmbeddingItem(
    val index: Int,
    val text: String,
    @SerializedName("vector_length") val vectorLength: Int,
    val norm: Double,
    @SerializedName("top_dimensions") val topDimensions: List<TopDimension>,
    @SerializedName("x_2d") val x2d: Double,
    @SerializedName("y_2d") val y2d: Double,
    @SerializedName("vector_sample") val vectorSample: List<Double>
)

data class EmbeddingAnalysisResponse(
    @SerializedName("prompt_count") val promptCount: Int,
    val dimensions: Int,
    val items: List<EmbeddingItem>,
    @SerializedName("similarity_matrix") val similarityMatrix: List<List<Double>>,
    @SerializedName("distance_matrix") val distanceMatrix: List<List<Double>>
)

data class PromptStats(
    val text: String,
    @SerializedName("token_count") val tokenCount: Int,
    @SerializedName("character_count") val characterCount: Int,
    @SerializedName("compression_ratio") val compressionRatio: Double,
    @SerializedName("vector_2d") val vector2d: List<Double>
)

data class ComparisonMetrics(
    @SerializedName("cosine_similarity") val cosineSimilarity: Double,
    @SerializedName("jaccard_token_overlap") val jaccardTokenOverlap: Double,
    @SerializedName("token_difference") val tokenDifference: Int,
    @SerializedName("token_savings_percent") val tokenSavingsPercent: Double,
    @SerializedName("semantic_distance") val semanticDistance: Double
)

data class PromptComparisonResponse(
    @SerializedName("prompt_a") val promptA: PromptStats,
    @SerializedName("prompt_b") val promptB: PromptStats,
    val metrics: ComparisonMetrics,
    val recommendation: String
)

// Request Data Classes

data class TokenizeRequest(
    val text: String,
    @SerializedName("encoding_name") val encodingName: String = "cl100k_base"
)

data class EmbeddingsRequest(
    val prompts: List<String>
)

data class ComparePromptsRequest(
    @SerializedName("prompt_a") val promptA: String,
    @SerializedName("prompt_b") val promptB: String,
    @SerializedName("encoding_name") val encodingName: String = "cl100k_base"
)
