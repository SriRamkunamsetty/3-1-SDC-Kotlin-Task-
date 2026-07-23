import re
from typing import Dict, Any, List

class CoherenceEvaluator:
    """
    Evaluates text coherence, sentence transitions, discourse markers,
    and structural flow.
    """

    DISCOURSE_CONNECTIVES = {
        "addition": ["furthermore", "moreover", "in addition", "additionally", "also", "besides"],
        "contrast": ["however", "nevertheless", "on the other hand", "conversely", "in contrast", "but", "yet"],
        "causation": ["therefore", "consequently", "as a result", "thus", "hence", "because", "so"],
        "illustration": ["for example", "for instance", "specifically", "namely", "such as"],
        "conclusion": ["in conclusion", "to summarize", "overall", "finally", "in summary"]
    }

    def evaluate_coherence(self, text: str) -> Dict[str, Any]:
        """
        Evaluates coherence score (0.0 to 100.0%) based on sentence structure,
        transition density, and semantic continuity.
        """
        if not text or not text.strip():
            return {
                "coherence_score": 0.0,
                "sentence_count": 0,
                "avg_sentence_length": 0.0,
                "discourse_density": 0.0,
                "structural_flow_score": 0.0,
                "rating": "N/A"
            }

        sentences = [s.strip() for s in re.split(r"[.!?]+", text) if len(s.strip()) > 2]
        sentence_count = len(sentences)
        
        words = re.findall(r"\b[a-zA-Z0-9_]+\b", text)
        total_words = len(words)
        
        if total_words == 0:
            return {
                "coherence_score": 0.0,
                "sentence_count": 0,
                "avg_sentence_length": 0.0,
                "discourse_density": 0.0,
                "structural_flow_score": 0.0,
                "rating": "N/A"
            }

        avg_sent_len = round(total_words / max(1, sentence_count), 2)

        # Count discourse connectives
        text_lower = text.lower()
        connective_matches = 0
        categories_found = set()

        for cat, markers in self.DISCOURSE_CONNECTIVES.items():
            for m in markers:
                if m in text_lower:
                    connective_matches += 1
                    categories_found.add(cat)

        # Density of transition markers
        discourse_density = round((connective_matches / max(1, sentence_count)), 2)

        # Score components
        # 1. Sentence length variance score (optimal average 10-25 words)
        if 8 <= avg_sent_len <= 30:
            length_score = 35.0
        elif 5 <= avg_sent_len < 8 or 30 < avg_sent_len <= 45:
            length_score = 25.0
        else:
            length_score = 15.0

        # 2. Transition & Connective score (up to 40 pts)
        transition_score = min(40.0, connective_matches * 10.0 + len(categories_found) * 5.0)
        if sentence_count <= 2:
            transition_score = max(transition_score, 25.0)

        # 3. Sentence flow & capitalization consistency score (up to 25 pts)
        flow_score = 25.0
        if not text[0].isupper():
            flow_score -= 5.0
        if not text.endswith((".", "!", "?", "```")):
            flow_score -= 5.0

        total_coherence = round(min(100.0, length_score + transition_score + flow_score), 2)

        # Qualitative rating
        if total_coherence >= 85.0:
            rating = "HIGHLY COHERENT (Smooth Flow)"
        elif total_coherence >= 70.0:
            rating = "COHERENT (Good Structure)"
        elif total_coherence >= 50.0:
            rating = "MODERATE (Basic Flow)"
        else:
            rating = "DISJOINTED (Poor Coherence)"

        return {
            "coherence_score": total_coherence,
            "sentence_count": sentence_count,
            "avg_sentence_length": avg_sent_len,
            "discourse_density": discourse_density,
            "connective_count": connective_matches,
            "connective_categories": list(categories_found),
            "structural_flow_score": round(flow_score + length_score, 2),
            "rating": rating
        }
