import unittest
from persona_manager import PersonaManager
from chat_engine import ChatEngine

class TestChatbotBackend(unittest.TestCase):

    def setUp(self):
        self.persona_mgr = PersonaManager()
        self.chat_engine = ChatEngine()

    def test_persona_list(self):
        personas = self.persona_mgr.get_all_personas()
        self.assertGreaterEqual(len(personas), 4)
        self.assertEqual(personas[0]["id"], "HELPFUL_ASSISTANT")

    def test_helpful_assistant_response(self):
        res = self.chat_engine.process_message("Hello Nova", "HELPFUL_ASSISTANT")
        self.assertEqual(res["status"], "success")
        self.assertIn("Nova", res["bot_response"])
        self.assertGreater(len(res["suggestions"]), 0)

    def test_code_mentor_persona(self):
        res = self.chat_engine.process_message("How to build Kotlin compose UI?", "CODE_MENTOR")
        self.assertEqual(res["persona"]["id"], "CODE_MENTOR")
        self.assertIn("```kotlin", res["bot_response"])

    def test_concise_summarizer_persona(self):
        res = self.chat_engine.process_message("Explain AI chatbot interface flow", "CONCISE_SUMMARIZER")
        self.assertIn("•", res["bot_response"])

if __name__ == "__main__":
    unittest.main()
