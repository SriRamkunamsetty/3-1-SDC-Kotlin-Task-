from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

from intent_interpreter import IntentInterpreter
from action_dispatcher import ActionDispatcher

app = FastAPI(
    title="Agent Workflow Assistant API",
    description="Agent Intent Interpretation, Tool Dispatching, and Reasoning Trace Service",
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

dispatcher = ActionDispatcher()
interpreter = IntentInterpreter()


# --- Request Models ---

class InteractRequest(BaseModel):
    prompt: str = Field(..., json_schema_extra={"example": "Calculate 25 * 4 + 150"})

class IntentRequest(BaseModel):
    prompt: str = Field(..., json_schema_extra={"example": "Write a Kotlin function for agent workflows"})


# --- API Routes ---

@app.get("/api/health")
def health_check():
    return {
        "status": "online",
        "service": "Agent Workflow Assistant API",
        "supported_intents": IntentInterpreter.INTENT_CATEGORIES,
        "active_tools": [t["name"] for t in dispatcher.get_registered_tools()]
    }

@app.get("/api/agent/tools")
def list_tools():
    """
    Returns registered agent tool suite capabilities and parameter schemas.
    """
    return {
        "tool_count": len(dispatcher.tools),
        "tools": dispatcher.get_registered_tools()
    }

@app.post("/api/agent/intent")
def analyze_intent(req: IntentRequest):
    """
    Analyzes prompt intent, extracts parameters, and computes intent confidence scores.
    """
    try:
        return interpreter.interpret(req.prompt)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/agent/interact")
def agent_interact(req: InteractRequest):
    """
    Main Agent Workflow Endpoint:
    Processes user input, interprets intent, selects & dispatches action tool,
    and returns step-by-step reasoning trace (Thought -> Action -> Observation -> Final Answer).
    """
    try:
        return dispatcher.process_request(req.prompt)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
