import pdfplumber as pp

async def read_pdf(filepath):
    data = {}
    with pp.open(filepath) as pdf:
        for page in pdf.pages:
            print(page.extract_text())
    
    return data

async def read_compare_document(filepath: str) -> dict | None:
    if filepath.endswith('.pdf'):
        data = await read_pdf(filepath)
    else:
        data = None
    return data