import time
import random
from typing import Dict, Any, List
from persona_manager import PersonaManager

class ChatEngine:
    """
    Handles conversational flow, multi-turn dialogue context,
    response generation based on active persona, and quick suggestions.
    """

    def __init__(self):
        self.persona_manager = PersonaManager()

    def process_message(self, message: str, persona_id: str = "HELPFUL_ASSISTANT") -> Dict[str, Any]:
        """
        Processes incoming user message and returns structured bot response.
        """
        persona = self.persona_manager.get_persona(persona_id)
        msg_lower = message.lower().strip()

        # Simulated response generation based on persona and topic
        if persona_id == "CODE_MENTOR":
            if "kotlin" in msg_lower or "app" in msg_lower or "compose" in msg_lower:
                reply = """Here is a clean Jetpack Compose UI snippet in Kotlin:

```kotlin
@Composable
fun ChatMessageBubble(text: String, isUser: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) PrimaryBlue else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = Color.White
        )
    }
}
```

This component renders user and bot messages with custom Material 3 surface styling."""
            else:
                reply = f"""Here is the code architecture approach for '{message}':

```python
# Python Service Handler
def handle_chat_flow(user_input: str) -> dict:
    print(f"Processing input: {user_input}")
    return {"status": "success", "response": "Contextual AI Output"}
```

Always separate your UI components, ViewModels, and Data Repositories for scalable architecture."""

        elif persona_id == "CONCISE_SUMMARIZER":
            reply = f"""• **Core Intent**: {message[:30]}...
• **Status**: Processed successfully by BriefAI engine.
• **Action**: Conversational flow updated with zero fluff."""

        elif persona_id == "CREATIVE_WRITER":
            reply = f"✨ Ah, what a captivating prompt! When contemplating '{message}', imagination unfolds like a tapestry of endless possibilities. Ideas shimmer like stars in a twilight sky..."

        else:  # HELPFUL_ASSISTANT
            if "hello" in msg_lower or "hi" in msg_lower or "hey" in msg_lower:
                reply = f"Hello there! I'm Nova, your AI Chatbot Assistant. How can I assist you with your project or questions today?"
            elif "architecture" in msg_lower or "flow" in msg_lower:
                reply = "A complete mobile conversational flow consists of:\n1. Interactive message list with scroll state.\n2. Typing indicators & loading states.\n3. Persona/System prompt switching.\n4. Local offline storage & repository fallback."
            else:
                reply = f"I've processed your message: \"{message}\". How would you like to build upon this conversational flow next?"

        # Quick follow-up suggestions
        suggestions = self._get_suggestions(persona_id)

        return {
            "status": "success",
            "persona": persona,
            "user_message": message,
            "bot_response": reply,
            "timestamp": time.strftime("%H:%M"),
            "suggestions": suggestions
        }

    def _get_suggestions(self, persona_id: str) -> List[str]:
        if persona_id == "CODE_MENTOR":
            return ["Explain ViewModel state", "Kotlin Coroutines example", "Retrofit API setup"]
        elif persona_id == "CONCISE_SUMMARIZER":
            return ["Summarize key features", "TL;DR Architecture", "Bullet points please"]
        elif persona_id == "CREATIVE_WRITER":
            return ["Tell me a sci-fi story", "Write an inspiring quote", "Brainstorm ideas"]
        else:
            return ["Show code snippet", "Explain conversational flow", "Switch AI Persona"]
