from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

from perplexity_evaluator import PerplexityEvaluator
from coherence_evaluator import CoherenceEvaluator
from accuracy_evaluator import AccuracyEvaluator

app = FastAPI(
    title="LLM Evaluation Study API",
    description="LLM Evaluation Metrics Engine (Perplexity, Coherence, Accuracy, Latency & Model Benchmarking)",
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

ppl_evaluator = PerplexityEvaluator()
coherence_evaluator = CoherenceEvaluator()
accuracy_evaluator = AccuracyEvaluator()


# --- Request/Response Pydantic Models ---

class SingleEvalRequest(BaseModel):
    generated_text: str = Field(..., json_schema_extra={"example": "AI Agents process inputs, interpret intent, and execute tools."})
    reference_text: Optional[str] = Field(default="", json_schema_extra={"example": "AI Agents parse user input and run tools."})
    task_category: Optional[str] = Field(default="Summarization", json_schema_extra={"example": "Summarization"})

class CompareModelsRequest(BaseModel):
    model_a_name: str = Field(default="Model A (GPT-4o)", json_schema_extra={"example": "Model A"})
    model_a_text: str = Field(..., json_schema_extra={"example": "AI agents take user goals, interpret intent, and execute tools to achieve targets."})
    model_b_name: str = Field(default="Model B (Llama-3)", json_schema_extra={"example": "Model B"})
    model_b_text: str = Field(..., json_schema_extra={"example": "Agents take goal inputs and call API tools dynamically."})
    reference_text: Optional[str] = Field(default="AI Agents process input and execute tools.", json_schema_extra={"example": "Gold Reference"})


# --- Benchmark Dataset ---

BENCHMARK_TASKS = [
    {
        "id": "task_1",
        "title": "Text Summarization Benchmark",
        "category": "Summarization",
        "description": "Evaluate concise text summarization accuracy and coherence.",
        "reference_text": "An AI agent is an autonomous system that interprets user intent and executes tool actions to complete tasks."
    },
    {
        "id": "task_2",
        "title": "Kotlin Code Synthesis Benchmark",
        "category": "Code Generation",
        "description": "Evaluate code correctness, readability, and structural coherence.",
        "reference_text": "fun processAgentTask(input: String): Map<String, Any> { return mapOf(\"status\" to \"completed\") }"
    },
    {
        "id": "task_3",
        "title": "Question Answering Benchmark",
        "category": "Q&A",
        "description": "Evaluate factual accuracy and ROUGE alignment against gold answer.",
        "reference_text": "Text perplexity measures language model fluency, where lower scores indicate higher predictability."
    }
]


# --- API Routes ---

@app.get("/api/health")
def health_check():
    return {
        "status": "online",
        "service": "LLM Evaluation Study API",
        "supported_metrics": ["Perplexity", "Coherence", "ROUGE-L Accuracy", "BLEU", "Latency Efficiency"]
    }

@app.get("/api/eval/benchmark-tasks")
def list_benchmark_tasks():
    return {
        "count": len(BENCHMARK_TASKS),
        "tasks": BENCHMARK_TASKS
    }

@app.post("/api/eval/single")
def evaluate_single(req: SingleEvalRequest):
    """
    Evaluates a single model output across Perplexity, Coherence, Accuracy, and Efficiency.
    """
    try:
        ppl_res = ppl_evaluator.calculate_perplexity(req.generated_text)
        coh_res = coherence_evaluator.evaluate_coherence(req.generated_text)
        acc_res = accuracy_evaluator.evaluate_accuracy(req.generated_text, req.reference_text or "")

        # Estimated generation latency & speed (ms per token)
        words = len(req.generated_text.split())
        est_tokens = int(words * 1.3)
        est_latency_ms = max(120, est_tokens * 18)

        overall_score = round(0.35 * acc_res["accuracy_score"] + 0.35 * coh_res["coherence_score"] + 0.30 * ppl_res["fluency_score"], 2)

        return {
            "task_category": req.task_category,
            "generated_text": req.generated_text,
            "reference_text": req.reference_text,
            "overall_quality_score": overall_score,
            "perplexity_metrics": ppl_res,
            "coherence_metrics": coh_res,
            "accuracy_metrics": acc_res,
            "efficiency_metrics": {
                "estimated_tokens": est_tokens,
                "estimated_latency_ms": est_latency_ms,
                "tokens_per_second": round((est_tokens / (est_latency_ms / 1000.0)), 1) if est_latency_ms > 0 else 0.0
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/eval/compare")
def compare_models(req: CompareModelsRequest):
    """
    Provides side-by-side comparative benchmark between Model A and Model B.
    Computes delta metrics and designates a winning model.
    """
    try:
        eval_a = evaluate_single(SingleEvalRequest(generated_text=req.model_a_text, reference_text=req.reference_text))
        eval_b = evaluate_single(SingleEvalRequest(generated_text=req.model_b_text, reference_text=req.reference_text))

        score_a = eval_a["overall_quality_score"]
        score_b = eval_b["overall_quality_score"]

        diff = round(abs(score_a - score_b), 2)
        if score_a > score_b:
            winner = req.model_a_name
            recommendation = f"{req.model_a_name} outperforms {req.model_b_name} by {diff} points (Higher Accuracy & Coherence)."
        elif score_b > score_a:
            winner = req.model_b_name
            recommendation = f"{req.model_b_name} outperforms {req.model_a_name} by {diff} points (Better Alignment & Lower Perplexity)."
        else:
            winner = "TIE"
            recommendation = "Both models achieved identical overall quality scores."

        return {
            "model_a": {
                "name": req.model_a_name,
                "text": req.model_a_text,
                "overall_score": score_a,
                "perplexity": eval_a["perplexity_metrics"]["perplexity"],
                "coherence": eval_a["coherence_metrics"]["coherence_score"],
                "accuracy": eval_a["accuracy_metrics"]["accuracy_score"]
            },
            "model_b": {
                "name": req.model_b_name,
                "text": req.model_b_text,
                "overall_score": score_b,
                "perplexity": eval_b["perplexity_metrics"]["perplexity"],
                "coherence": eval_b["coherence_metrics"]["coherence_score"],
                "accuracy": eval_b["accuracy_metrics"]["accuracy_score"]
            },
            "benchmark_winner": winner,
            "score_difference": diff,
            "recommendation": recommendation
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
