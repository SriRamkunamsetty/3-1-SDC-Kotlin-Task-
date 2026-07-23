import re
from typing import Dict, Any, List, Tuple

class IntentInterpreter:
    """
    Analyzes user prompts, identifies intent categories, calculates confidence scores,
    and extracts relevant parameter entities for agent tool execution.
    """

    INTENT_CATEGORIES = [
        "KNOWLEDGE_QUERY",
        "MATH_CALCULATION",
        "CODE_GENERATION",
        "TEXT_SUMMARIZATION",
        "SYSTEM_DIAGNOSTIC",
        "TASK_SCHEDULING",
        "GENERAL_CONVERSATION"
    ]

    def __init__(self):
        # Keyword & pattern rules for intent recognition
        self.rules = {
            "MATH_CALCULATION": [
                r"\b(calculate|compute|math|add|subtract|multiply|divide|square root|sum|eval)\b",
                r"(\d+\s*[\+\-\*\/\^]\s*\d+)"
            ],
            "CODE_GENERATION": [
                r"\b(write|create|generate|code|function|class|script|kotlin|python|java|algorithm|bug|fix)\b",
                r"\b(how to code|build an app|implement|snippet)\b"
            ],
            "TEXT_SUMMARIZATION": [
                r"\b(summarize|summary|concise|tldr|bullet points|key points|shorten|overview)\b"
            ],
            "KNOWLEDGE_QUERY": [
                r"\b(what is|explain|define|tell me about|how does|who is|history of|concept|meaning|why)\b"
            ],
            "SYSTEM_DIAGNOSTIC": [
                r"\b(status|health|diagnostic|memory|tools|system|ping|server|agent state)\b"
            ],
            "TASK_SCHEDULING": [
                r"\b(schedule|remind|timer|task|todo|calendar|event|plan)\b"
            ]
        }

    def interpret(self, prompt: str) -> Dict[str, Any]:
        """
        Interprets user prompt, returning primary intent, confidence score,
        extracted parameters, and alternative candidate intents.
        """
        if not prompt or not prompt.strip():
            return {
                "prompt": prompt,
                "primary_intent": "GENERAL_CONVERSATION",
                "confidence": 1.0,
                "extracted_parameters": {},
                "all_scores": {"GENERAL_CONVERSATION": 1.0}
            }

        scores = {intent: 0.1 for intent in self.INTENT_CATEGORIES}
        text_lower = prompt.lower()

        # Score matching
        for intent, patterns in self.rules.items():
            for pat in patterns:
                matches = re.findall(pat, text_lower)
                if matches:
                    scores[intent] += 0.35 * len(matches)

        # Math expression regex check
        if re.search(r"\d+\s*[\+\-\*\/\^]\s*\d+", text_lower):
            scores["MATH_CALCULATION"] += 0.5

        # Code keywords check
        if any(kw in text_lower for kw in ["kotlin", "python", "java", "code", "function", "class"]):
            scores["CODE_GENERATION"] += 0.4

        # Normalize scores to probabilities
        total = sum(scores.values())
        normalized_scores = {intent: round(score / total, 3) for intent, score in scores.items()}

        # Find primary intent
        primary_intent = max(normalized_scores, key=normalized_scores.get)
        confidence = normalized_scores[primary_intent]

        # Extract parameters based on primary intent
        params = self._extract_parameters(prompt, primary_intent)

        return {
            "prompt": prompt,
            "primary_intent": primary_intent,
            "confidence": confidence,
            "extracted_parameters": params,
            "all_scores": normalized_scores
        }

    def _extract_parameters(self, prompt: str, intent: str) -> Dict[str, Any]:
        params = {}
        text = prompt.strip()

        if intent == "MATH_CALCULATION":
            # Extract math expression
            match = re.search(r"[\d\.\s\+\-\*\/\^\(\)]+", text)
            if match and len(match.group(0).strip()) > 1:
                params["expression"] = match.group(0).strip()
            else:
                params["expression"] = text

        elif intent == "CODE_GENERATION":
            # Extract programming language if specified
            langs = ["kotlin", "python", "java", "javascript", "typescript", "c++", "sql"]
            found_lang = "kotlin"  # default for user's context
            for l in langs:
                if l in text.lower():
                    found_lang = l
                    break
            params["language"] = found_lang
            params["topic"] = text

        elif intent == "TEXT_SUMMARIZATION":
            params["text_to_summarize"] = text
            params["target_length"] = "concise"

        elif intent == "KNOWLEDGE_QUERY":
            params["query"] = text

        elif intent == "TASK_SCHEDULING":
            params["task_description"] = text

        return params
