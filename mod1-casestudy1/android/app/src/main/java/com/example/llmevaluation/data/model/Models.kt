package com.example.llmevaluation.data.model

import com.google.gson.annotations.SerializedName

data class PerplexityMetrics(
    val perplexity: Double,
    val entropy: Double,
    @SerializedName("fluency_score") val fluencyScore: Double,
    @SerializedName("word_count") val wordCount: Int,
    @SerializedName("unique_words") val uniqueWords: Int,
    @SerializedName("repetition_penalty") val repetitionPenalty: Double,
    val rating: String
)

data class CoherenceMetrics(
    @SerializedName("coherence_score") val coherenceScore: Double,
    @SerializedName("sentence_count") val sentenceCount: Int,
    @SerializedName("avg_sentence_length") val avgSentenceLength: Double,
    @SerializedName("discourse_density") val discourseDensity: Double,
    @SerializedName("connective_count") val connectiveCount: Int,
    @SerializedName("connective_categories") val connectiveCategories: List<String>,
    @SerializedName("structural_flow_score") val structuralFlowScore: Double,
    val rating: String
)

data class AccuracyMetrics(
    @SerializedName("accuracy_score") val accuracyScore: Double,
    @SerializedName("rouge_l_f1") val rougeLF1: Double,
    @SerializedName("bleu_score") val bleuScore: Double,
    @SerializedName("keyword_match_pct") val keywordMatchPct: Double,
    val rating: String
)

data class EfficiencyMetrics(
    @SerializedName("estimated_tokens") val estimatedTokens: Int,
    @SerializedName("estimated_latency_ms") val estimatedLatencyMs: Int,
    @SerializedName("tokens_per_second") val tokensPerSecond: Double
)

data class SingleEvalResponse(
    @SerializedName("task_category") val taskCategory: String?,
    @SerializedName("generated_text") val generatedText: String,
    @SerializedName("reference_text") val referenceText: String?,
    @SerializedName("overall_quality_score") val overallQualityScore: Double,
    @SerializedName("perplexity_metrics") val perplexityMetrics: PerplexityMetrics,
    @SerializedName("coherence_metrics") val coherenceMetrics: CoherenceMetrics,
    @SerializedName("accuracy_metrics") val accuracyMetrics: AccuracyMetrics,
    @SerializedName("efficiency_metrics") val efficiencyMetrics: EfficiencyMetrics
)

data class ModelEvalSummary(
    val name: String,
    val text: String,
    @SerializedName("overall_score") val overallScore: Double,
    val perplexity: Double,
    val coherence: Double,
    val accuracy: Double
)

data class ModelComparisonResponse(
    @SerializedName("model_a") val modelA: ModelEvalSummary,
    @SerializedName("model_b") val modelB: ModelEvalSummary,
    @SerializedName("benchmark_winner") val benchmarkWinner: String,
    @SerializedName("score_difference") val scoreDifference: Double,
    val recommendation: String
)

data class BenchmarkTask(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    @SerializedName("reference_text") val referenceText: String
)

data class BenchmarkTasksResponse(
    val count: Int,
    val tasks: List<BenchmarkTask>
)

// Request DTOs

data class SingleEvalRequest(
    @SerializedName("generated_text") val generatedText: String,
    @SerializedName("reference_text") val referenceText: String = "",
    @SerializedName("task_category") val taskCategory: String = "Summarization"
)

data class CompareModelsRequest(
    @SerializedName("model_a_name") val modelAName: String = "Model A",
    @SerializedName("model_a_text") val modelAText: String,
    @SerializedName("model_b_name") val modelBName: String = "Model B",
    @SerializedName("model_b_text") val modelBText: String,
    @SerializedName("reference_text") val referenceText: String = ""
)
