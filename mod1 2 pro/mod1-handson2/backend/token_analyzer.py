import re
from typing import List, Dict, Any, Optional

try:
    import tiktoken
    TIKTOKEN_AVAILABLE = True
except ImportError:
    TIKTOKEN_AVAILABLE = False


class TokenAnalyzer:
    """
    Analyzes text tokenization across different LLM tokenizers.
    Supports tiktoken encodings (cl100k_base, o200k_base, p50k_base, r50k_base)
    and fallback subword tokenization rules.
    """

    SUPPORTED_ENCODINGS = [
        "cl100k_base", # GPT-4, GPT-3.5-Turbo
        "o200k_base",  # GPT-4o
        "p50k_base",   # Codex, Davinci
        "wordpiece_sim", # Simulated BERT/WordPiece
        "char_level"   # Character-level baseline
    ]

    def __init__(self):
        self.encoders = {}
        if TIKTOKEN_AVAILABLE:
            for enc_name in ["cl100k_base", "o200k_base", "p50k_base"]:
                try:
                    self.encoders[enc_name] = tiktoken.get_encoding(enc_name)
                except Exception:
                    pass

    def tokenize(self, text: str, encoding_name: str = "cl100k_base") -> Dict[str, Any]:
        """
        Tokenizes the input text and returns comprehensive token metadata.
        """
        if not text:
            return {
                "text": text,
                "encoding_name": encoding_name,
                "token_count": 0,
                "character_count": 0,
                "tokens": [],
                "compression_ratio": 0.0,
                "avg_bytes_per_token": 0.0
            }

        tokens = []

        if TIKTOKEN_AVAILABLE and encoding_name in self.encoders:
            encoder = self.encoders[encoding_name]
            token_ids = encoder.encode(text)
            
            # Map token IDs back to string tokens and bytes
            char_cursor = 0
            for idx, tid in enumerate(token_ids):
                token_bytes = encoder.decode_bytes([tid])
                try:
                    token_str = token_bytes.decode("utf-8", errors="replace")
                except Exception:
                    token_str = str(token_bytes)
                
                # Estimate character range
                token_len = len(token_str)
                tokens.append({
                    "id": tid,
                    "text": token_str,
                    "index": idx,
                    "byte_count": len(token_bytes),
                    "char_start": char_cursor,
                    "char_end": char_cursor + token_len,
                    "type": "subword" if idx > 0 and not token_str.startswith(" ") else "word"
                })
                char_cursor += token_len
        elif encoding_name == "char_level":
            for idx, ch in enumerate(text):
                tokens.append({
                    "id": ord(ch),
                    "text": ch,
                    "index": idx,
                    "byte_count": len(ch.encode("utf-8")),
                    "char_start": idx,
                    "char_end": idx + 1,
                    "type": "char"
                })
        else:
            # Fallback subword regex tokenizer
            # Matches words, punctuation, spaces, numbers separately
            matches = re.finditer(r"'s|'t|'re|'ve|'m|'ll|'d| ?[a-zA-Z]+| ?[0-9]+| ?[^\s\a-zA-Z0-9]+|\s+", text)
            
            token_list = []
            for m in matches:
                word = m.group(0)
                # Split longer words into subword chunks of ~3-4 chars
                if len(word) > 4 and word.strip().isalnum():
                    chunks = [word[i:i+3] for i in range(0, len(word), 3)]
                    token_list.extend(chunks)
                else:
                    token_list.append(word)
            
            char_cursor = 0
            for idx, tok in enumerate(token_list):
                # Generate deterministic hash token id
                tid = abs(hash(tok)) % 100000
                tok_bytes = tok.encode("utf-8")
                tok_len = len(tok)
                tokens.append({
                    "id": tid,
                    "text": tok,
                    "index": idx,
                    "byte_count": len(tok_bytes),
                    "char_start": char_cursor,
                    "char_end": char_cursor + tok_len,
                    "type": "subword" if idx > 0 and not tok.startswith(" ") else "word"
                })
                char_cursor += tok_len

        token_count = len(tokens)
        char_count = len(text)
        total_bytes = len(text.encode("utf-8"))
        
        # Token efficiency ratio: characters per token (higher is generally more efficient)
        compression_ratio = round(char_count / token_count, 2) if token_count > 0 else 0.0
        avg_bytes_per_token = round(total_bytes / token_count, 2) if token_count > 0 else 0.0

        return {
            "text": text,
            "encoding_name": encoding_name,
            "token_count": token_count,
            "character_count": char_count,
            "total_bytes": total_bytes,
            "tokens": tokens,
            "compression_ratio": compression_ratio,
            "avg_bytes_per_token": avg_bytes_per_token
        }

    def compare_encodings(self, text: str) -> Dict[str, Any]:
        """
        Compares how multiple encodings process the exact same text.
        """
        results = {}
        for enc in self.SUPPORTED_ENCODINGS:
            results[enc] = self.tokenize(text, enc)
        return {
            "text": text,
            "encodings": results
        }
