from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

from token_analyzer import TokenAnalyzer
from embedding_analyzer import EmbeddingAnalyzer

app = FastAPI(
    title="Token & Embedding Analysis API",
    description="LLM Tokenization, Embedding Vectors, and Prompt Representation Analysis Service",
    version="1.0.0"
)

# Enable CORS for mobile app requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

token_analyzer = TokenAnalyzer()
embedding_analyzer = EmbeddingAnalyzer(dimensions=128)

# --- Request/Response Pydantic Models ---

class TokenizeRequest(BaseModel):
    text: str = Field(..., json_schema_extra={"example": "Hello world! How are tokens represented in LLMs?"})
    encoding_name: str = Field(default="cl100k_base", json_schema_extra={"example": "cl100k_base"})

class CompareEncodingsRequest(BaseModel):
    text: str = Field(..., json_schema_extra={"example": "Analyzing subwords in tokenization"})

class EmbeddingsRequest(BaseModel):
    prompts: List[str] = Field(..., min_length=1, json_schema_extra={"example": ["Prompt option A", "Prompt option B"]})

class ComparePromptsRequest(BaseModel):
    prompt_a: str = Field(..., json_schema_extra={"example": "Summarize the article in 3 sentences."})
    prompt_b: str = Field(..., json_schema_extra={"example": "Write a concise 3-sentence summary of the following document."})
    encoding_name: str = Field(default="cl100k_base")


# --- API Routes ---

@app.get("/api/health")
def health_check():
    return {
        "status": "online",
        "service": "Token & Embedding Analysis API",
        "supported_encodings": TokenAnalyzer.SUPPORTED_ENCODINGS
    }

@app.post("/api/analyze-tokens")
def analyze_tokens(req: TokenizeRequest):
    """
    Tokenizes text input and returns detailed token breakdown, IDs, character bounds, and compression metrics.
    """
    try:
        return token_analyzer.tokenize(req.text, req.encoding_name)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/compare-encodings")
def compare_encodings(req: CompareEncodingsRequest):
    """
    Compares tokenization of a single text across multiple LLM tokenizer algorithms.
    """
    try:
        return token_analyzer.compare_encodings(req.text)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/analyze-embeddings")
def analyze_embeddings(req: EmbeddingsRequest):
    """
    Generates embedding vectors, cosine similarity matrix, and 2D spatial projections for prompts.
    """
    try:
        return embedding_analyzer.analyze_embeddings(req.prompts)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/compare-prompts")
def compare_prompts(req: ComparePromptsRequest):
    """
    Provides an in-depth comparative analysis between Prompt A and Prompt B:
    - Token count difference & token savings %
    - Cosine similarity score
    - Token ID overlap %
    - Character compression efficiency ratio
    - Recommendation on prompt optimization
    """
    try:
        tokens_a = token_analyzer.tokenize(req.prompt_a, req.encoding_name)
        tokens_b = token_analyzer.tokenize(req.prompt_b, req.encoding_name)

        embed_result = embedding_analyzer.analyze_embeddings([req.prompt_a, req.prompt_b])
        items = embed_result["items"]
        
        sim_matrix = embed_result["similarity_matrix"]
        cosine_sim = sim_matrix[0][1] if len(sim_matrix) > 1 else 1.0

        # Token set overlap
        ids_a = set(t["id"] for t in tokens_a["tokens"])
        ids_b = set(t["id"] for t in tokens_b["tokens"])
        
        intersection = ids_a.intersection(ids_b)
        union = ids_a.union(ids_b)
        jaccard_overlap = round(len(intersection) / len(union), 4) if union else 1.0

        token_diff = tokens_b["token_count"] - tokens_a["token_count"]
        pct_savings = 0.0
        if tokens_a["token_count"] > 0:
            pct_savings = round(((tokens_a["token_count"] - tokens_b["token_count"]) / tokens_a["token_count"]) * 100, 2)

        # Recommendation logic
        if pct_savings > 10.0 and cosine_sim >= 0.85:
            recommendation = f"Prompt B saves {pct_savings}% tokens while maintaining {round(cosine_sim*100, 1)}% semantic similarity to Prompt A. Recommended for cost reduction."
        elif cosine_sim < 0.70:
            recommendation = f"Prompts differ significantly in meaning (Cosine Similarity: {round(cosine_sim, 2)}). Verify prompt intent."
        else:
            recommendation = f"Both prompts show high semantic alignment (Cosine Similarity: {round(cosine_sim, 2)})."

        return {
            "prompt_a": {
                "text": req.prompt_a,
                "token_count": tokens_a["token_count"],
                "character_count": tokens_a["character_count"],
                "compression_ratio": tokens_a["compression_ratio"],
                "vector_2d": [items[0]["x_2d"], items[0]["y_2d"]]
            },
            "prompt_b": {
                "text": req.prompt_b,
                "token_count": tokens_b["token_count"],
                "character_count": tokens_b["character_count"],
                "compression_ratio": tokens_b["compression_ratio"],
                "vector_2d": [items[1]["x_2d"], items[1]["y_2d"]]
            },
            "metrics": {
                "cosine_similarity": cosine_sim,
                "jaccard_token_overlap": jaccard_overlap,
                "token_difference": token_diff,
                "token_savings_percent": pct_savings,
                "semantic_distance": round(1.0 - cosine_sim, 4)
            },
            "recommendation": recommendation
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
