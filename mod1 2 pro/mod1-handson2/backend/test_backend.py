import sys
import unittest
from token_analyzer import TokenAnalyzer
from embedding_analyzer import EmbeddingAnalyzer

class TestTokenAndEmbeddingBackend(unittest.TestCase):

    def setUp(self):
        self.token_analyzer = TokenAnalyzer()
        self.embedding_analyzer = EmbeddingAnalyzer(dimensions=128)

    def test_tokenization_basic(self):
        text = "Hello world! Tokenization analysis in LLMs."
        res = self.token_analyzer.tokenize(text, "cl100k_base")
        self.assertIn("token_count", res)
        self.assertGreater(res["token_count"], 0)
        self.assertEqual(len(res["tokens"]), res["token_count"])
        self.assertGreater(res["compression_ratio"], 0)

    def test_encoding_comparison(self):
        text = "Compare tiktoken encoders"
        res = self.token_analyzer.compare_encodings(text)
        self.assertIn("encodings", res)
        self.assertIn("char_level", res["encodings"])

    def test_embedding_generation_and_norm(self):
        text1 = "How to write Kotlin code for Android apps?"
        vec1 = self.embedding_analyzer.generate_embedding(text1)
        self.assertEqual(len(vec1), 128)
        
        # Test Cosine Similarity of identical text is 1.0
        sim_self = self.embedding_analyzer.compute_cosine_similarity(vec1, vec1)
        self.assertAlmostEqual(sim_self, 1.0, places=3)

    def test_cosine_similarity_different_texts(self):
        t1 = "Kotlin mobile application development"
        t2 = "Building Android apps with Kotlin programming language"
        t3 = "Recipe for cooking chocolate chip cookies in oven"
        
        v1 = self.embedding_analyzer.generate_embedding(t1)
        v2 = self.embedding_analyzer.generate_embedding(t2)
        v3 = self.embedding_analyzer.generate_embedding(t3)

        sim_related = self.embedding_analyzer.compute_cosine_similarity(v1, v2)
        sim_unrelated = self.embedding_analyzer.compute_cosine_similarity(v1, v3)

        # Related topics should have higher similarity than unrelated topics
        self.assertGreater(sim_related, sim_unrelated)

    def test_embeddings_analysis_matrix(self):
        prompts = ["Prompt A test", "Prompt B test", "Unrelated prompt test"]
        res = self.embedding_analyzer.analyze_embeddings(prompts)
        self.assertEqual(res["prompt_count"], 3)
        self.assertEqual(len(res["similarity_matrix"]), 3)
        self.assertEqual(len(res["items"]), 3)

if __name__ == "__main__":
    unittest.main()
