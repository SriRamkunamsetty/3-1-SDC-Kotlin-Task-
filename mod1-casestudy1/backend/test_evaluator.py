import unittest
from perplexity_evaluator import PerplexityEvaluator
from coherence_evaluator import CoherenceEvaluator
from accuracy_evaluator import AccuracyEvaluator

class TestLLMEvaluatorBackend(unittest.TestCase):

    def setUp(self):
        self.ppl = PerplexityEvaluator()
        self.coh = CoherenceEvaluator()
        self.acc = AccuracyEvaluator()

    def test_perplexity_fluent_text(self):
        fluent_text = "The quick brown fox jumps over the lazy dog."
        res = self.ppl.calculate_perplexity(fluent_text)
        self.assertGreater(res["perplexity"], 0.0)
        self.assertGreater(res["fluency_score"], 50.0)

    def test_coherence_evaluation(self):
        text = "AI agents process inputs. Furthermore, they execute tools. Therefore, tasks are completed."
        res = self.coh.evaluate_coherence(text)
        self.assertGreater(res["coherence_score"], 60.0)
        self.assertIn("addition", res["connective_categories"])

    def test_accuracy_rouge_bleu(self):
        gen = "An AI agent interprets user intent and executes tools."
        ref = "An AI agent interprets user intent and executes tool actions."
        res = self.acc.evaluate_accuracy(gen, ref)
        self.assertGreater(res["accuracy_score"], 70.0)
        self.assertGreater(res["rouge_l_f1"], 0.7)
        self.assertGreater(res["bleu_score"], 0.5)

    def test_accuracy_mismatch(self):
        gen = "Chocolate chip cookies recipe in oven."
        ref = "Kotlin mobile application architecture."
        res = self.acc.evaluate_accuracy(gen, ref)
        self.assertLess(res["accuracy_score"], 40.0)

if __name__ == "__main__":
    unittest.main()
