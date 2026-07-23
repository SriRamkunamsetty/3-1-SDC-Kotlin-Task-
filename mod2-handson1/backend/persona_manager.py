from typing import Dict, Any, List

class PersonaManager:
    """
    Manages AI chatbot personas, system prompts, avatar metadata,
    and conversational tone configurations.
    """

    PERSONAS = {
        "HELPFUL_ASSISTANT": {
            "id": "HELPFUL_ASSISTANT",
            "name": "Nova AI",
            "title": "Helpful AI Assistant",
            "avatar_icon": "SmartToy",
            "system_prompt": "You are Nova, a friendly, intelligent AI chatbot assistant. Provide clear, structured, helpful answers.",
            "greeting": "Hello! I'm Nova, your AI assistant. How can I help you today?",
            "color_hex": "#38BDF8"
        },
        "CODE_MENTOR": {
            "id": "CODE_MENTOR",
            "name": "DevBot",
            "title": "Senior Code Mentor",
            "avatar_icon": "Code",
            "system_prompt": "You are DevBot, a senior software architect. Provide clean code snippets in Kotlin/Python with explanation.",
            "greeting": "Hey developer! I'm DevBot. What code or architecture question can we solve together?",
            "color_hex": "#A855F7"
        },
        "CONCISE_SUMMARIZER": {
            "id": "CONCISE_SUMMARIZER",
            "name": "BriefAI",
            "title": "Concise Summarizer",
            "avatar_icon": "Compress",
            "system_prompt": "You are BriefAI. Keep all answers under 3 bullet points. Be extremely concise.",
            "greeting": "BriefAI active. Send text or questions for instant bullet-point summaries.",
            "color_hex": "#34D399"
        },
        "CREATIVE_WRITER": {
            "id": "CREATIVE_WRITER",
            "name": "Aria",
            "title": "Creative Storyteller",
            "avatar_icon": "AutoAwesome",
            "system_prompt": "You are Aria, an imaginative creative writer. Provide engaging, vivid responses.",
            "greeting": "Greetings! I'm Aria. What creative idea or story shall we explore today?",
            "color_hex": "#FBBF24"
        }
    }

    def get_all_personas(self) -> List[Dict[str, Any]]:
        return list(self.PERSONAS.values())

    def get_persona(self, persona_id: str) -> Dict[str, Any]:
        return self.PERSONAS.get(persona_id, self.PERSONAS["HELPFUL_ASSISTANT"])
