from collections import defaultdict
from pdf2image import convert_from_path
import pytesseract
import re
import unicodedata

def strip_diacritics(text: str) -> str:
    return ''.join(
        c for c in unicodedata.normalize('NFD', text)
        if unicodedata.category(c) != 'Mn'
    )

def normalize(text: str) -> str:
    text = text.replace("\n", " ")
    text = re.sub(r"\s+", " ", text)
    text = strip_diacritics(text)
    return text.lower()


def ocr_to_words(data):
    words = []
    for i, txt in enumerate(data["text"]):
        if txt.strip():
            words.append({
                "text": txt,
                "x0": data["left"][i],
                "top": data["top"][i],
            })
    return words

def group_lines(words, y_tol=5):
    lines = defaultdict(list)
    for w in words:
        key = round(w["top"] / y_tol)
        lines[key].append(w)
    # Sort by vertical position so lines are in reading order
    return [lines[k] for k in sorted(lines.keys())]

def line_to_text(line):
    return " ".join(
        w["text"] for w in sorted(line, key=lambda x: x["x0"])
    )

def extract_revenue(text):
    text = normalize(text)
    tokens = text.split()

    for i, token in enumerate(tokens):
        # Find "pajamos" preceded by "pagrindines" within 5 tokens
        if token == "pajamos":
            preceding = tokens[max(0, i-5):i]
            if "pagrindines" not in preceding:
                continue

            # Look at tokens strictly after "pajamos"
            after = tokens[i+1:i+10]
            after_text = " ".join(after)

            # Remove year patterns like "2024" or "uz 2024 metus"
            after_text = re.sub(r"\buz\s+\d{4}\s+metus\b", "", after_text)
            after_text = re.sub(r"\b\d{4}-\d{2}-\d{2}\b", "", after_text)

            numbers = re.findall(r"\b\d{1,3}(?:\s\d{3})*\b", after_text)
            if numbers:
                clean = numbers[0].replace(" ", "")
                return {"revenue": int(clean)}

    return None

def read_pdf(pdf_path):
    images = convert_from_path(pdf_path, dpi=300)
    img = images[0].convert("RGB")
    ocr_data = pytesseract.image_to_data(img, output_type=pytesseract.Output.DICT)
    words = ocr_to_words(ocr_data)
    lines = group_lines(words)
    text = "\n".join(line_to_text(line) for line in lines)
    normalized_text = normalize(text)
    print("=== FULL OCR TEXT ===")
    print(normalized_text)
    print("=== TOKEN SEARCH ===")
    tokens = normalized_text.split()
    print("'pagrindines' found:", "pagrindines" in tokens)
    print("'pajamos' found:", "pajamos" in tokens)
    # Show any token that's close, in case OCR mangled the spelling
    for t in tokens:
        if t.startswith("pagrind") or t.startswith("pajam"):
            print("  close match:", t)
    print("OCR text:\n", normalized_text)
    return extract_revenue(normalized_text)


async def read_compare_document(filepath: str):
    if filepath.endswith(".pdf"):
        return read_pdf(filepath)
    return None