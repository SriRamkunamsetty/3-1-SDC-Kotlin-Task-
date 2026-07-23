from typing import Dict, Any, List
from intent_interpreter import IntentInterpreter
from tools import (
    CalculatorTool,
    KnowledgeTool,
    CodeGeneratorTool,
    SummarizerTool,
    SystemDiagnosticTool
)

class ActionDispatcher:
    """
    Agent Reasoning & Dispatcher Loop.
    Formulates execution plans, selects tools, executes actions,
    and constructs ReAct reasoning traces (Thought -> Action -> Observation -> Final Answer).
    """

    def __init__(self):
        self.interpreter = IntentInterpreter()
        
        # Tool Registry
        self.tools = {
            "CalculatorTool": CalculatorTool(),
            "KnowledgeTool": KnowledgeTool(),
            "CodeGeneratorTool": CodeGeneratorTool(),
            "SummarizerTool": SummarizerTool(),
            "SystemDiagnosticTool": SystemDiagnosticTool()
        }

        # Intent to Tool mapping
        self.intent_tool_map = {
            "MATH_CALCULATION": "CalculatorTool",
            "CODE_GENERATION": "CodeGeneratorTool",
            "TEXT_SUMMARIZATION": "SummarizerTool",
            "KNOWLEDGE_QUERY": "KnowledgeTool",
            "SYSTEM_DIAGNOSTIC": "SystemDiagnosticTool",
            "GENERAL_CONVERSATION": "KnowledgeTool",
            "TASK_SCHEDULING": "KnowledgeTool"
        }

    def get_registered_tools(self) -> List[Dict[str, Any]]:
        return [
            {
                "name": tool.name,
                "description": tool.description,
                "parameters": tool.parameters
            }
            for tool in self.tools.values()
        ]

    def process_request(self, user_prompt: str) -> Dict[str, Any]:
        """
        Executes full Agent Reasoning Loop for user prompt.
        """
        reasoning_steps = []

        # Step 1: Intent Interpretation
        intent_res = self.interpreter.interpret(user_prompt)
        primary_intent = intent_res["primary_intent"]
        confidence = intent_res["confidence"]
        params = intent_res["extracted_parameters"]

        thought_1 = f"Interpreted intent as '{primary_intent}' with {round(confidence * 100, 1)}% confidence. Parameters: {params}"
        reasoning_steps.append({
            "step_type": "THOUGHT",
            "step_number": 1,
            "title": "Intent Interpretation",
            "content": thought_1
        })

        # Step 2: Tool Selection
        selected_tool_name = self.intent_tool_map.get(primary_intent, "KnowledgeTool")
        tool = self.tools[selected_tool_name]

        action_step = f"Selected tool '{tool.name}' to handle intent '{primary_intent}'."
        reasoning_steps.append({
            "step_type": "ACTION",
            "step_number": 2,
            "title": "Tool Selection & Dispatch",
            "content": action_step,
            "tool_used": tool.name
        })

        # Step 3: Tool Execution & Observation
        try:
            if selected_tool_name == "CalculatorTool":
                expr = params.get("expression", user_prompt)
                obs_data = tool.execute(expr)
            elif selected_tool_name == "CodeGeneratorTool":
                lang = params.get("language", "kotlin")
                topic = params.get("topic", user_prompt)
                obs_data = tool.execute(lang, topic)
            elif selected_tool_name == "SummarizerTool":
                text = params.get("text_to_summarize", user_prompt)
                obs_data = tool.execute(text)
            elif selected_tool_name == "SystemDiagnosticTool":
                obs_data = tool.execute()
            else:
                query = params.get("query", user_prompt)
                obs_data = tool.execute(query)

            tool_output = obs_data.get("output", "Tool executed successfully.")
            status = obs_data.get("status", "success")
        except Exception as e:
            tool_output = f"Tool execution error: {str(e)}"
            status = "error"
            obs_data = {"error": str(e)}

        reasoning_steps.append({
            "step_type": "OBSERVATION",
            "step_number": 3,
            "title": "Tool Execution Result",
            "content": f"Received output from {tool.name}:\n{tool_output}"
        })

        # Step 4: Final Synthesis
        final_answer = f"Agent Action Completed. Intent: {primary_intent} → Executed {tool.name}.\n\n{tool_output}"
        reasoning_steps.append({
            "step_type": "FINAL_ANSWER",
            "step_number": 4,
            "title": "Final Response",
            "content": final_answer
        })

        return {
            "prompt": user_prompt,
            "intent_analysis": intent_res,
            "tool_used": tool.name,
            "action_status": status,
            "reasoning_steps": reasoning_steps,
            "final_answer": final_answer,
            "tool_details": obs_data
        }
