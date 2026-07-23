import math
import numpy as np
from typing import List, Dict, Any, Tuple


class EmbeddingAnalyzer:
    """
    Analyzes LLM text embeddings, vector similarities, distances,
    and 2D spatial projections for visual representation.
    """

    DIMENSIONS = 128  # Compact vector embedding representation size

    def __init__(self, dimensions: int = 128):
        self.dimensions = dimensions

    def generate_embedding(self, text: str) -> List[float]:
        """
        Generates a normalized dense vector embedding for a text input.
        Uses deterministic semantic hashing with n-gram character and word features
        to ensure identical text produces identical embeddings and similar texts produce
        high cosine similarity.
        """
        if not text.strip():
            return [0.0] * self.dimensions

        vector = np.zeros(self.dimensions, dtype=np.float64)
        words = text.lower().split()
        
        # Word-level features
        for i, word in enumerate(words):
            # Positional hash
            h1 = abs(hash(word)) % self.dimensions
            h2 = abs(hash(word[::-1])) % self.dimensions
            weight = 1.0 + (0.1 * math.log(len(word) + 1))
            vector[h1] += weight
            vector[h2] -= weight * 0.5

        # Character n-gram features (captures subword semantics)
        clean_text = text.lower()
        for n in range(2, 5):
            for i in range(len(clean_text) - n + 1):
                ngram = clean_text[i:i+n]
                h = abs(hash(ngram)) % self.dimensions
                vector[h] += 0.35 / n

        # Normalize vector to unit length (L2 norm)
        norm = np.linalg.norm(vector)
        if norm > 0:
            vector = vector / norm
            
        return [round(float(v), 6) for v in vector]

    def compute_cosine_similarity(self, vec1: List[float], vec2: List[float]) -> float:
        """
        Computes cosine similarity between two embedding vectors.
        Result is bounded between -1.0 and 1.0 (typically 0.0 to 1.0 for normalized texts).
        """
        v1 = np.array(vec1, dtype=np.float64)
        v2 = np.array(vec2, dtype=np.float64)
        
        norm1 = np.linalg.norm(v1)
        norm2 = np.linalg.norm(v2)
        
        if norm1 == 0 or norm2 == 0:
            return 0.0
            
        dot_product = np.dot(v1, v2)
        similarity = dot_product / (norm1 * norm2)
        return float(np.clip(similarity, -1.0, 1.0))

    def compute_euclidean_distance(self, vec1: List[float], vec2: List[float]) -> float:
        """
        Computes Euclidean distance L2 between two vectors.
        """
        v1 = np.array(vec1, dtype=np.float64)
        v2 = np.array(vec2, dtype=np.float64)
        return float(np.linalg.norm(v1 - v2))

    def analyze_embeddings(self, texts: List[str]) -> Dict[str, Any]:
        """
        Analyzes embeddings for a collection of texts:
        - Generates vectors
        - Builds pairwise cosine similarity matrix
        - Computes 2D PCA projection coordinates for visualization
        """
        embeddings = [self.generate_embedding(t) for t in texts]
        matrix = np.array(embeddings, dtype=np.float64)
        
        n = len(texts)
        sim_matrix = np.ones((n, n), dtype=float)
        dist_matrix = np.zeros((n, n), dtype=float)

        for i in range(n):
            for j in range(i + 1, n):
                sim = self.compute_cosine_similarity(embeddings[i], embeddings[j])
                dist = self.compute_euclidean_distance(embeddings[i], embeddings[j])
                sim_matrix[i, j] = sim_matrix[j, i] = round(sim, 4)
                dist_matrix[i, j] = dist_matrix[j, i] = round(dist, 4)

        # 2D projection using SVD / PCA logic
        coords = self._project_2d(matrix)

        items = []
        for idx, text in enumerate(texts):
            # Top 5 highest activation dimensions
            vec = embeddings[idx]
            top_dim_indices = np.argsort(np.abs(vec))[-5:][::-1].tolist()
            top_dims = [{"dimension": int(d), "value": vec[d]} for d in top_dim_indices]

            items.append({
                "index": idx,
                "text": text,
                "vector_length": len(vec),
                "norm": round(float(np.linalg.norm(vec)), 4),
                "top_dimensions": top_dims,
                "x_2d": round(coords[idx][0], 4),
                "y_2d": round(coords[idx][1], 4),
                "vector_sample": vec[:10]  # First 10 dimensions sample
            })

        return {
            "prompt_count": n,
            "dimensions": self.dimensions,
            "items": items,
            "similarity_matrix": sim_matrix.tolist(),
            "distance_matrix": dist_matrix.tolist()
        }

    def _project_2d(self, matrix: np.ndarray) -> List[Tuple[float, float]]:
        """
        Simple SVD-based 2D projection for vector visualization.
        """
        n = matrix.shape[0]
        if n == 0:
            return []
        if n == 1:
            return [(0.0, 0.0)]

        # Center matrix
        mean = np.mean(matrix, axis=0)
        centered = matrix - mean

        try:
            U, S, Vt = np.linalg.svd(centered, full_matrices=False)
            projected = np.dot(centered, Vt[:2].T)
            # Scale coordinates to [-1, 1] range for easy plotting
            max_val = np.max(np.abs(projected))
            if max_val > 0:
                projected = projected / max_val
            return [(float(p[0]), float(p[1])) for p in projected]
        except Exception:
            return [(0.0, 0.0) for _ in range(n)]
