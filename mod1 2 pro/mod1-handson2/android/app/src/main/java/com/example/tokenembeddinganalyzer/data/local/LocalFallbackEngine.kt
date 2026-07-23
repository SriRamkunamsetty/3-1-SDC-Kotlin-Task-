package com.example.tokenembeddinganalyzer.data.local

import com.example.tokenembeddinganalyzer.data.model.*
import kotlin.math.*

class LocalFallbackEngine {

    private val dimensions = 128

    fun tokenize(text: String, encodingName: String): TokenizationResponse {
        if (text.isBlank()) {
            return TokenizationResponse(
                text = text,
                encodingName = encodingName,
                tokenCount = 0,
                characterCount = 0,
                totalBytes = 0,
                tokens = emptyList(),
                compressionRatio = 0.0,
                avgBytesPerToken = 0.0
            )
        }

        val tokensList = mutableListOf<TokenItem>()
        // Regex word & subword splitting
        val regex = Regex("""'s|'t|'re|'ve|'m|'ll|'d| ?[a-zA-Z]+| ?[0-9]+| ?[^\s\a-zA-Z0-9]+|\s+""")
        val matches = regex.findAll(text).map { it.value }.toList()

        var cursor = 0
        var tokenIdx = 0

        for (match in matches) {
            if (match.length > 4 && match.trim().all { it.isLetterOrDigit() }) {
                val chunks = match.chunked(3)
                for (chunk in chunks) {
                    val bytes = chunk.toByteArray(Charsets.UTF_8)
                    tokensList.add(
                        TokenItem(
                            id = abs(chunk.hashCode()) % 100000,
                            text = chunk,
                            index = tokenIdx++,
                            byteCount = bytes.size,
                            charStart = cursor,
                            charEnd = cursor + chunk.length,
                            type = if (chunk.startsWith(" ")) "word" else "subword"
                        )
                    )
                    cursor += chunk.length
                }
            } else {
                val bytes = match.toByteArray(Charsets.UTF_8)
                tokensList.add(
                    TokenItem(
                        id = abs(match.hashCode()) % 100000,
                        text = match,
                        index = tokenIdx++,
                        byteCount = bytes.size,
                        charStart = cursor,
                        charEnd = cursor + match.length,
                        type = if (match.startsWith(" ")) "word" else "subword"
                    )
                )
                cursor += match.length
            }
        }

        val tokenCount = tokensList.size
        val charCount = text.length
        val totalBytes = text.toByteArray(Charsets.UTF_8).size
        val compression = if (tokenCount > 0) (charCount.toDouble() / tokenCount).roundTo(2) else 0.0
        val avgBytes = if (tokenCount > 0) (totalBytes.toDouble() / tokenCount).roundTo(2) else 0.0

        return TokenizationResponse(
            text = text,
            encodingName = "$encodingName (Offline Engine)",
            tokenCount = tokenCount,
            characterCount = charCount,
            totalBytes = totalBytes,
            tokens = tokensList,
            compressionRatio = compression,
            avgBytesPerToken = avgBytes
        )
    }

    fun generateEmbedding(text: String): DoubleArray {
        val vector = DoubleArray(dimensions) { 0.0 }
        if (text.isBlank()) return vector

        val words = text.lowercase().split(Regex("""\s+"""))
        for (word in words) {
            val h1 = abs(word.hashCode()) % dimensions
            val h2 = abs(word.reversed().hashCode()) % dimensions
            val weight = 1.0 + 0.1 * ln((word.length + 1).toDouble())
            vector[h1] += weight
            vector[h2] -= weight * 0.5
        }

        val cleanText = text.lowercase()
        for (n in 2..4) {
            for (i in 0..(cleanText.length - n)) {
                val ngram = cleanText.substring(i, i + n)
                val h = abs(ngram.hashCode()) % dimensions
                vector[h] += 0.35 / n
            }
        }

        var norm = 0.0
        for (v in vector) norm += v * v
        norm = sqrt(norm)

        if (norm > 0) {
            for (i in vector.indices) vector[i] /= norm
        }

        return vector
    }

    fun computeCosineSimilarity(vec1: DoubleArray, vec2: DoubleArray): Double {
        var dot = 0.0
        var n1 = 0.0
        var n2 = 0.0
        for (i in vec1.indices) {
            dot += vec1[i] * vec2[i]
            n1 += vec1[i] * vec1[i]
            n2 += vec2[i] * vec2[i]
        }
        n1 = sqrt(n1)
        n2 = sqrt(n2)
        if (n1 == 0.0 || n2 == 0.0) return 0.0
        return (dot / (n1 * n2)).coerceIn(-1.0, 1.0)
    }

    fun analyzeEmbeddings(prompts: List<String>): EmbeddingAnalysisResponse {
        val vectors = prompts.map { generateEmbedding(it) }
        val n = prompts.size
        val simMatrix = List(n) { DoubleArray(n) { 1.0 } }
        val distMatrix = List(n) { DoubleArray(n) { 0.0 } }

        for (i in 0 until n) {
            for (j in (i + 1) until n) {
                val sim = computeCosineSimilarity(vectors[i], vectors[j]).roundTo(4)
                val dist = sqrt(vectors[i].indices.sumOf { (vectors[i][it] - vectors[j][it]).pow(2) }).roundTo(4)
                simMatrix[i][j] = sim
                simMatrix[j][i] = sim
                distMatrix[i][j] = dist
                distMatrix[j][i] = dist
            }
        }

        val items = prompts.mapIndexed { idx, text ->
            val vec = vectors[idx]
            val topDims = vec.mapIndexed { index, value -> index to value }
                .sortedByDescending { abs(it.second) }
                .take(5)
                .map { TopDimension(it.first, it.second.roundTo(4)) }

            val x = sin(idx.toDouble() * 1.5).roundTo(4)
            val y = cos(idx.toDouble() * 1.5).roundTo(4)

            EmbeddingItem(
                index = idx,
                text = text,
                vectorLength = vec.size,
                norm = 1.0,
                topDimensions = topDims,
                x2d = x,
                y2d = y,
                vectorSample = vec.take(10).map { it.roundTo(4) }
            )
        }

        return EmbeddingAnalysisResponse(
            promptCount = n,
            dimensions = dimensions,
            items = items,
            similarityMatrix = simMatrix.map { it.toList() },
            distanceMatrix = distMatrix.map { it.toList() }
        )
    }

    fun comparePrompts(promptA: String, promptB: String, encodingName: String): PromptComparisonResponse {
        val tA = tokenize(promptA, encodingName)
        val tB = tokenize(promptB, encodingName)

        val vA = generateEmbedding(promptA)
        val vB = generateEmbedding(promptB)

        val cosSim = computeCosineSimilarity(vA, vB).roundTo(4)
        val tokenDiff = tB.tokenCount - tA.tokenCount
        val savings = if (tA.tokenCount > 0) ((tA.tokenCount - tB.tokenCount).toDouble() / tA.tokenCount * 100).roundTo(2) else 0.0

        val setA = tA.tokens.map { it.id }.toSet()
        val setB = tB.tokens.map { it.id }.toSet()
        val overlap = if (setA.union(setB).isNotEmpty()) (setA.intersect(setB).size.toDouble() / setA.union(setB).size).roundTo(4) else 1.0

        val rec = if (savings > 10.0 && cosSim >= 0.85) {
            "Prompt B saves $savings% tokens with high semantic similarity (${(cosSim * 100).toInt()}%). Recommended!"
        } else if (cosSim < 0.70) {
            "Prompts differ significantly in vector representation (Cosine Sim: $cosSim)."
        } else {
            "Both prompts are semantically close (Cosine Similarity: $cosSim)."
        }

        return PromptComparisonResponse(
            promptA = PromptStats(promptA, tA.tokenCount, tA.characterCount, tA.compressionRatio, listOf(0.2, -0.3)),
            promptB = PromptStats(promptB, tB.tokenCount, tB.characterCount, tB.compressionRatio, listOf(0.4, -0.1)),
            metrics = ComparisonMetrics(
                cosineSimilarity = cosSim,
                jaccardTokenOverlap = overlap,
                tokenDifference = tokenDiff,
                tokenSavingsPercent = savings,
                semanticDistance = (1.0 - cosSim).roundTo(4)
            ),
            recommendation = rec
        )
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}
