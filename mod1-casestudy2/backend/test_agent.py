import unittest
from intent_interpreter import IntentInterpreter
from action_dispatcher import ActionDispatcher
from tools import CalculatorTool, KnowledgeTool, CodeGeneratorTool

class TestAgentWorkflowBackend(unittest.TestCase):

    def setUp(self):
        self.interpreter = IntentInterpreter()
        self.dispatcher = ActionDispatcher()

    def test_intent_math_calculation(self):
        res = self.interpreter.interpret("Calculate 50 * 3 + 20")
        self.assertEqual(res["primary_intent"], "MATH_CALCULATION")
        self.assertGreater(res["confidence"], 0.3)
        self.assertIn("expression", res["extracted_parameters"])

    def test_intent_code_generation(self):
        res = self.interpreter.interpret("Write a Kotlin function to format timestamps")
        self.assertEqual(res["primary_intent"], "CODE_GENERATION")
        self.assertEqual(res["extracted_parameters"].get("language"), "kotlin")

    def test_calculator_tool(self):
        tool = CalculatorTool()
        out = tool.execute("100 / 4 + 25")
        self.assertEqual(out["status"], "success")
        self.assertEqual(out["result"], 50.0)

    def test_full_agent_workflow_math(self):
        prompt = "Compute 15 * 8"
        res = self.dispatcher.process_request(prompt)
        self.assertEqual(res["tool_used"], "CalculatorTool")
        self.assertEqual(len(res["reasoning_steps"]), 4)
        self.assertEqual(res["reasoning_steps"][0]["step_type"], "THOUGHT")
        self.assertEqual(res["reasoning_steps"][1]["step_type"], "ACTION")
        self.assertEqual(res["reasoning_steps"][2]["step_type"], "OBSERVATION")
        self.assertEqual(res["reasoning_steps"][3]["step_type"], "FINAL_ANSWER")

    def test_full_agent_workflow_code(self):
        prompt = "Create a Python script for downloading files"
        res = self.dispatcher.process_request(prompt)
        self.assertEqual(res["tool_used"], "CodeGeneratorTool")
        self.assertIn("Python Agent Function", res["final_answer"])

if __name__ == "__main__":
    unittest.main()
