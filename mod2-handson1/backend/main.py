from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

from persona_manager import PersonaManager
from chat_engine import ChatEngine

app = FastAPI(
    title="UI Chatbot Interface API",
    description="Conversational Flow, Persona Switching, and Message Generation Engine",
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

persona_manager = PersonaManager()
chat_engine = ChatEngine()


# --- Request/Response Pydantic Models ---

class ChatMessageRequest(BaseModel):
    message: str = Field(..., json_schema_extra={"example": "Hello! Explain conversational flow in Kotlin apps."})
    persona_id: Optional[str] = Field(default="HELPFUL_ASSISTANT", json_schema_extra={"example": "HELPFUL_ASSISTANT"})


# --- API Routes ---

@app.get("/api/health")
def health_check():
    return {
        "status": "online",
        "service": "UI Chatbot Interface API",
        "active_personas": len(persona_manager.PERSONAS)
    }

@app.get("/api/chat/personas")
def list_personas():
    """
    Returns available AI bot personas, system roles, colors, and avatar metadata.
    """
    return {
        "count": len(persona_manager.PERSONAS),
        "personas": persona_manager.get_all_personas()
    }

@app.get("/api/chat/suggestions")
def get_suggestions(persona_id: str = "HELPFUL_ASSISTANT"):
    """
    Returns quick reply suggestion chips for active persona.
    """
    return {
        "persona_id": persona_id,
        "suggestions": chat_engine._get_suggestions(persona_id)
    }

@app.post("/api/chat/message")
def process_message(req: ChatMessageRequest):
    """
    Main Conversational Endpoint:
    Receives user input, applies persona system prompt, and returns bot response.
    """
    try:
        return chat_engine.process_message(req.message, req.persona_id or "HELPFUL_ASSISTANT")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
