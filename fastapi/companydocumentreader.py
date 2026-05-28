from bs4 import BeautifulSoup
import ollama
import pdfplumber as pp
import re
import datetime

keywords = {
    "trumpalaikis turtas": [
        "trumpalaikis turtas",
        "trumpalaikis turtas iš viso",
        "trumpalaikio turto suma",
        "trumpalaikio turto iš viso",
        "trumpalaikis turtas (viso)"
    ],

    "atsargos": [
        "atsargos",
        "atsargų suma",
        "atsargų likutis",
        "prekių atsargos",
        "žaliavų atsargos",
        "medžiagų atsargos",
        "produkcijos atsargos",
        "nebaigta gamyba"
    ],

    "trumpalaikiai įsipareigojimai": [
        "trumpalaikiai įsipareigojimai",
        "trumpalaikiai įsipareigojimai iš viso",
        "trumpalaikių įsipareigojimų suma",
        "trumpalaikiai įsipareigojimai (viso)",
        "mokėtinos sumos",
        "trumpalaikės skolos",
        "per vienerius metus mokėtinos sumos"
    ],

    "nuosavas kapitalas": [
        "nuosavas kapitalas",
        "nuosavas kapitalas iš viso",
        "nuosavo kapitalo suma",
        "akcininkų nuosavybė",
        "kapitalas ir rezervai",
        "savas kapitalas"
    ],

    "visas turtas": [
        "visas turtas",
        "turtas iš viso",
        "turto suma",
        "viso turto",
        "bendras turtas",
        "turtas (viso)"
    ],

    "grynas pelnas": [
        "grynas pelnas",
        "grynasis pelnas",
        "grynasis laikotarpio pelnas",
        "grynasis rezultatas",
        "pelno (nuostolių) rezultatas",
        "ataskaitinio laikotarpio pelnas"
    ],

    "palūkanos": [
        "palūkanos",
        "palūkanų sąnaudos",
        "palūkanų išlaidos",
        "sumokėtos palūkanos",
        "finansinės sąnaudos"
    ],

    "sumokėti mokesčiai": [
        "sumokėti mokesčiai",
        "sumokėti pelno mokesčiai",
        "sumokėtas pelno mokestis",
        "pelno mokestis",
        "pelno mokesčio sąnaudos",
        "mokesčių išlaidos"
    ],

    "nusidėvėjimas": [
        "nusidėvėjimas",
        "ilgalaikio turto nusidėvėjimas",
        "nusidėvėjimo sąnaudos"
    ],

    "amortizacija": [
        "amortizacija",
        "nematerialiojo turto amortizacija",
        "amortizacijos sąnaudos"
    ],

    "finansiniai įsipareigojimai": [
        "finansiniai įsipareigojimai",
        "finansinės skolos",
        "skolos finansinėms institucijoms",
        "paskolos",
        "įsipareigojimai kredito įstaigoms"
    ],

    "grynieji pinigai": [
        "grynieji pinigai",
        "pinigai ir pinigų ekvivalentai",
        "pinigų likutis",
        "grynųjų pinigų likutis"
    ],

    "pardavimų pajamos": [
        "pardavimų pajamos",
        "pardavimo pajamos",
        "pardavimo pajamų suma",
        "įplaukos",
        "apyvarta",
        "grynosios pardavimo pajamos"
    ],

    "pardavimų pajamos praeitų metų": [
        "pardavimų pajamos praėjusiais metais",
        "praėjusių metų pajamos",
        "ankstesnių metų pajamos",
        "praeito laikotarpio pajamos",
        "lyginamasis laikotarpis"
    ]
}

from dataclasses import dataclass, field

@dataclass
class Candidate:
    value: float
    scale: int
    source: str        # "rule_table", "llm_table", "llm_paragraph"
    synonym: str
    confidence: float

CONFIDENCE = {
    "rule_table":    1.0,
    "llm_table":     0.6,
    "llm_paragraph": 0.35,
}

def pick_best_candidate(candidates: list[Candidate]) -> float | None:
    if not candidates:
        return None
    if len(candidates) == 1:
        return candidates[0].value

    def same_value(a: float, b: float) -> bool:
        if a == 0 and b == 0:
            return True
        return abs(a - b) / max(abs(a), abs(b)) < 0.005

    groups: list[list[Candidate]] = []
    for c in candidates:
        for g in groups:
            if same_value(g[0].value, c.value):
                g.append(c)
                break
        else:
            groups.append([c])

    def group_score(g: list[Candidate]) -> float:
        return sum(c.confidence for c in g) + 0.5 * (len(g) - 1)

    best_group = max(groups, key=group_score)
    best = max(best_group, key=lambda c: c.confidence)
    print("  picked %.4f from '%s' via %s (group size %d, total candidates %d)",
                 best.value, best.synonym, best.source, len(best_group), len(candidates))
    return best.value

# ------------------------------------------------------------------------------------------------------

def detect_year_xhtml(soup) -> str:
    for attr in ('date', 'dcterms.date', 'dc.date', 'year', 'publication-date'):
        tag = soup.find('meta', attrs={'name': re.compile(attr, re.IGNORECASE)})
        if tag:
            match = re.search(r'(20\d{2})', tag.get('content', ''))
            if match:
                return match.group(1)

    title = soup.find('title')
    if title:
        match = re.search(r'(20\d{2})', title.get_text())
        if match:
            return match.group(1)

    for tag in soup.find_all(['h1', 'h2', 'h3'])[:5]:
        match = re.search(r'(20\d{2})', tag.get_text())
        if match:
            return match.group(1)

    body = soup.find('body')
    if body:
        text = body.get_text()[:2000]
        match = re.search(r'\b(20\d{2})\b', text)
        if match:
            return match.group(1)
    
    return str(datetime.date.today().year)

def format_table(table: list[list[str]]) -> str:
    if not table:
        return ""

    # force all cells to plain strings
    table = [
        [
            re.sub(r'\s+', ' ', c.get_text(separator=" ", strip=True) if hasattr(c, 'get_text') else str(c or "")).strip()
            for c in row
        ]
        for row in table
    ]

    col_count = max(len(row) for row in table)

    # find non-empty columns
    keep_cols = [
        i for i in range(col_count)
        if any(i < len(row) and row[i] for row in table)
    ]

    # build cleaned rows
    cleaned_rows = []
    for row in table:
        cells = [row[i] if i < len(row) else "" for i in keep_cols]
        if any(cells):
            cleaned_rows.append(cells)

    if not cleaned_rows:
        return ""

    # merge multi-row headers: collapse consecutive rows where first cell is empty
    merged_rows = []
    for row in cleaned_rows:
        if merged_rows and not row[0] and not merged_rows[-1][0]:
            # merge into previous row by concatenating non-empty cells per column
            prev = merged_rows[-1]
            merged = [
                " ".join(filter(None, [prev[i] if i < len(prev) else "", row[i] if i < len(row) else ""])).strip()
                for i in range(max(len(prev), len(row)))
            ]
            merged_rows[-1] = merged
        else:
            merged_rows.append(row)

    # merge broken parentheses across cells
    final_rows = []
    for row in merged_rows:
        merged = []
        i = 0
        while i < len(row):
            cell = row[i]
            if cell.count('(') > cell.count(')') and i + 1 < len(row):
                cell = cell + row[i + 1]
                i += 1
            merged.append(cell)
            i += 1
        final_rows.append(merged)

    # strip noise columns (AVR only or all empty in data rows)
    col_count_final = max(len(r) for r in final_rows)
    meaningful_cols = [
        i for i in range(col_count_final)
        if any(
            i < len(row) and row[i] and row[i] not in ('AVR',)
            for row in final_rows[1:]
        )
    ]

    display_rows = []
    for row in final_rows:
        cells = [row[i] if i < len(row) else "" for i in meaningful_cols]
        display_rows.append(cells)

    return display_rows

def format_table_for_llm(table):
    if not table:
        return ""

    # force all cells to plain strings
    table = [
        [
            re.sub(r'\s+', ' ', c.get_text(separator=" ", strip=True) if hasattr(c, 'get_text') else str(c or "")).strip()
            for c in row
        ]
        for row in table
    ]

    col_count = max(len(row) for row in table)

    # find non-empty columns
    keep_cols = [
        i for i in range(col_count)
        if any(i < len(row) and row[i] for row in table)
    ]

    # build cleaned rows
    cleaned_rows = []
    for row in table:
        cells = [row[i] if i < len(row) else "" for i in keep_cols]
        if any(cells):
            cleaned_rows.append(cells)

    if not cleaned_rows:
        return ""

    # merge multi-row headers: collapse consecutive rows where first cell is empty
    merged_rows = []
    for row in cleaned_rows:
        if merged_rows and not row[0] and not merged_rows[-1][0]:
            # merge into previous row by concatenating non-empty cells per column
            prev = merged_rows[-1]
            merged = [
                " ".join(filter(None, [prev[i] if i < len(prev) else "", row[i] if i < len(row) else ""])).strip()
                for i in range(max(len(prev), len(row)))
            ]
            merged_rows[-1] = merged
        else:
            merged_rows.append(row)

    # merge broken parentheses across cells
    final_rows = []
    for row in merged_rows:
        merged = []
        i = 0
        while i < len(row):
            cell = row[i]
            if cell.count('(') > cell.count(')') and i + 1 < len(row):
                cell = cell + row[i + 1]
                i += 1
            merged.append(cell)
            i += 1
        final_rows.append(merged)

    # strip noise columns (AVR only or all empty in data rows)
    col_count_final = max(len(r) for r in final_rows)
    meaningful_cols = [
        i for i in range(col_count_final)
        if any(
            i < len(row) and row[i] and row[i] not in ('AVR',)
            for row in final_rows[1:]
        )
    ]

    display_rows = []
    for row in final_rows:
        cells = [row[i] if i < len(row) else "" for i in meaningful_cols]
        display_rows.append(cells)
    # pad for alignment
    col_widths = [
        max((len(row[i]) if i < len(row) else 0) for row in display_rows)
        for i in range(len(meaningful_cols))
    ]

    lines = []
    for row in display_rows:
        padded = [
            (row[i] if i < len(row) else "").ljust(col_widths[i])
            for i in range(len(meaningful_cols))
        ]
        lines.append(" | ".join(padded).rstrip())

    if len(lines) > 1:
        separator = "-+-".join("-" * w for w in col_widths)
        lines.insert(1, separator)

    return "\n".join(lines)

def detect_year(pdf) -> str:
    for page in pdf.pages[:5]:  # check first 5 pages only
        text = page.extract_text() or ""
        # matches "Year ended December 31, 2024" or "FY2023" etc.
        match = re.search(r'\b(?:FY|fiscal year\s*)?(20\d{2})\b', text, re.IGNORECASE)
        if match:
            return match.group(1)
    return str(datetime.date.today().year)

# ------------------------------------------------------------------------------------------------------
# tables
# ------------------------------------------------------------------------------------------------------

def clean_cell(text: str) -> str:
    text = text.replace("\xa0", " ")  # non-breaking space
    text = re.sub(r'[\n\t]+', ' ', text)  # newlines and tabs → single space
    text = re.sub(r' +', ' ', text)  # multiple spaces → one
    return text.strip()

def detect_scale(page_text):
    if "tūkst" in page_text.lower():
        return 1_000
    if "mln" in page_text.lower():
        return 1_000_000
    return 1  # assume units

def find_year_index(table, year):
    year = str(year)
    for row in table:
        for i, cell in enumerate(row):
            if year in cell:
                return i
    return 2

UNIT_INDICATORS = {'twh', 'mwh', 'gwh', 'kwh', 'mw', 'km', 'km²', 't', 'kt', 'vnt', 'avg'}

def find_keyword_row(table, keyword):
    keyword_clean = re.sub(r'\s+', ' ', keyword.strip())
    exact_match = None
    partial_match = None
    empty_value_match = None
    print(f"    looking for: '{keyword_clean}'")
    for i, row in enumerate(table):
        if not row:
            continue
        label = re.sub(r'\s+', ' ', str(row[0]).strip().lower())
        if label:
            print(f"    row {i}: '{label}'")

    for i, row in enumerate(table):
        if not row:
            continue
        label = re.sub(r'\s+', ' ', str(row[0]).strip().lower())
        row_cells = {cell.strip().lower() for cell in row}
        if row_cells & UNIT_INDICATORS:
            continue

        is_match = label == keyword_clean
        is_partial = keyword_clean in label

        if is_match or is_partial:
            has_numbers = any(parse_number(cell) is not None for cell in row[1:] if cell.strip())
            if has_numbers:
                if is_match and exact_match is None:
                    exact_match = i
                elif is_partial and partial_match is None:
                    partial_match = i
            else:
                if empty_value_match is None:
                    empty_value_match = i

    if exact_match is not None:
        return exact_match
    if partial_match is not None:
        return partial_match

    if empty_value_match is not None:
        for i in range(empty_value_match + 1, len(table)):
            row = table[i]
            if not row:
                continue
            label = str(row[0]).strip()
            non_empty = [cell.strip() for cell in row if cell.strip()]
            all_numbers = all(parse_number(c) is not None for c in non_empty if c != label)
            # stop if we hit another labeled row
            if not label and all_numbers:
                return i

    return None

def parse_number(cell: str) -> float | None:
    clean = (cell.strip()
             .replace(" ", "")
             .replace(",", ".")
             .replace("(", "-")
             .replace(")", ""))
    try:
        return float(clean)
    except ValueError:
        return None

def get_number_at(row, index, scale) -> float | None:
    # if row contains a unit cell, don't apply financial scale
    row_units = {cell.strip().lower() for cell in row}
    has_units = bool(row_units & UNIT_INDICATORS)

    for i in [index, index-1, index+1]:
        if 0 <= i < len(row):
            cell = row[i].strip()
            if not cell or cell in ('n/a', '-', ''):
                continue
            val = parse_number(cell)
            if val is not None and not (cell.isdigit() and len(cell) <= 2):
                return val if has_units else (val * scale if scale else val)
    return None

def extract_value(table, row_index, year_index, scale):
    # first try the keyword row itself
    value = get_number_at(table[row_index], year_index, scale)
    if value is not None:
        return value

    # scan following rows until we hit another labeled row
    for i in range(row_index + 1, min(row_index + 10, len(table))):
        row = table[i]
        if not row:
            continue

        label = re.sub(r'\s+', ' ', str(row[0]).strip().lower())

        # stop if we hit a new labeled row (not empty, not a continuation)
        if label and label != re.sub(r'\s+', ' ', str(table[row_index][0]).strip().lower()):
            break

        value = get_number_at(row, year_index, scale)
        if value is not None:
            return value

    return None

def try_read_from_table(data, table, keyword, synonym, year, scale):
    year_index = find_year_index(table, year)
    print(f"  year_index: {year_index} (year={year})")
    
    target_row_index = find_keyword_row(table, synonym)
    print(f"  target_row_index: {target_row_index} (keyword={synonym})")
    
    if target_row_index is None:
        data[keyword] = None
        return
    
    row = table[target_row_index]
    print(f"  row: {row}")
    print(f"  value at year_index: {row[year_index] if year_index and year_index < len(row) else 'OUT OF RANGE'}")
    
    result = extract_value(
        table,
        target_row_index,
        year_index if year_index is not None else 2,
        scale
    )
    print(f"  extracted: {result}")
    data[keyword] = result

def strip_empty_columns(formatted: str) -> str:
    lines = formatted.split('\n')
    if not lines:
        return formatted
    
    # split into cells
    rows = [line.split(' | ') for line in lines if not all(c == '-' or c == '+' for c in line.replace(' | ', '').replace('-+-', ''))]
    separator_indices = [i for i, line in enumerate(lines) if set(line.replace(' | ', '').replace('-+-', '')) <= {'-', '+', ''}]
    
    if not rows:
        return formatted
    
    col_count = max(len(row) for row in rows)
    
    # find cols where ALL data rows are empty (skip header)
    empty_cols = [
        i for i in range(col_count)
        if all(
            i >= len(row) or not row[i].strip()
            for row in rows[1:]  # skip header
        )
    ]
    
    # rebuild without empty cols
    clean_lines = []
    for i, line in enumerate(lines):
        if i in separator_indices:
            clean_lines.append(line)  # keep separator as-is
            continue
        row = line.split(' | ')
        cells = [row[j] for j in range(len(row)) if j not in empty_cols]
        clean_lines.append(' | '.join(cells))
    
    return '\n'.join(clean_lines)

def query_llm_table(chunk: str, keyword: str) -> str:
    response = ollama.chat(
        model="llama3.2",
        messages=[
            {
                "role": "system",
                "content": (
                    "You are a financial data extractor. "
                    "You receive a financial table in Lithuanian. "
                    "Find the row matching the indicator. "
                    "If the row has a direct value, return it. "
                    "If the value must be summed from component rows, calculate and return the sum. "
                    "Always return ONLY the final number on the last line, nothing else. "
                    "No explanation, no units, no working — just the number."
                )
            },
            {
                "role": "user",
                "content": (
                    f"Indicator: {keyword}\n"
                    f"TABLE:\n{chunk}\n\n"
                    f"Final number:"
                )
            }
        ],
        options={"temperature": 0, "num_ctx": 2048}
    )
    
    raw = response["message"]["content"].strip()
    
    # always take the last line — model puts the answer there
    last_line = raw.strip().split('\n')[-1].strip()
    
    # extract number from it
    match = re.search(r'-?[\d\s]+[,.]?\d*', last_line)
    return match.group(0).strip() if match else 'null'

def query_llm_paragraph(paragraph: str, keyword: str) -> str:
    response = ollama.chat(
        model="llama3.2",
        messages=[
            {
                "role": "system",
                "content": (
                    "You are a financial data extractor. "
                    "You receive a paragraph of financial text. "
                    "The indicator name may be in Lithuanian or another language — match by meaning. "
                    "If the indicator is not clearly mentioned, return null. "
                    "Do not guess or infer. "
                    "Return only the current period value as a plain number, no units, no explanation. "
                    "If not found, return null."
                )
            },
            {
                "role": "user",
                "content": (
                    f"Indicator: {keyword}\n\n"
                    f"PARAGRAPH:\n{paragraph}\n\n"
                    f"Value:"
                )
            }
        ],
        options={"temperature": 0, "num_ctx": 2048}
    )
    return response["message"]["content"].strip()

def is_hallucinated(value: str, source_text: str) -> bool:
    if not value or value == 'null':
        return False

    val = parse_number(value)
    if val is None:
        return True

    numbers_in_text = re.findall(r'-?[\d\s]+[,.]?\d*', source_text)
    parsed_numbers = [parse_number(n) for n in numbers_in_text if parse_number(n) is not None]

    return not any(abs(val - n) < 1e-3 for n in parsed_numbers)

# ------------------------------------------------------------------------------------------------------

async def read_xhtml(filepath: str) -> dict | None:
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            soup = BeautifulSoup(f, features="lxml")
    except IOError as e:
        print(e)
        return None

    year = detect_year_xhtml(soup)
    candidates: dict[str, list[Candidate]] = {kw: [] for kw in keywords}

    for keyword, synonyms in keywords.items():
        for synonym in synonyms:
            results = soup.find_all(string=re.compile(re.escape(synonym), re.IGNORECASE))
            for result in results:
                table_tag = result.parent.find_parent("table")

                if table_tag is not None:
                    table_text = table_tag.get_text(separator=" ", strip=True)
                    context = table_tag.find_previous(string=True)
                    if context:
                        table_text += " " + str(context)
                    scale = detect_scale(table_text)

                    full_table = []
                    for row in table_tag.find_all("tr"):
                        cells = [clean_cell(td.get_text(separator=" ", strip=True))
                                 for td in row.find_all(["td", "th"])]
                        if any(c.strip() for c in cells):
                            full_table.append(cells)

                    if not full_table:
                        continue

                    # rule-based
                    data = {}
                    try_read_from_table(data, full_table, keyword, synonym, year, scale)
                    if data.get(keyword) is not None:
                        print(f"Rule-based hit: {synonym} = {data[keyword]}")
                        candidates[keyword].append(Candidate(
                            value=data[keyword], scale=scale, source="rule_table",
                            synonym=synonym, confidence=CONFIDENCE["rule_table"]
                        ))
                        continue  # skip LLM for this occurrence, but keep scanning others

                    # llm table fallback
                    formatted = strip_empty_columns(format_table_for_llm(full_table))
                    print(f"Querying LLM (table) for: {synonym}")
                    response = query_llm_table(formatted, synonym)
                    print(f"LLM table response for {synonym}: {response}")
                    if response and response != "null" and not is_hallucinated(response, formatted):
                        val = parse_number(response)
                        if val is not None:
                            candidates[keyword].append(Candidate(
                                value=val * scale, scale=scale, source="llm_table",
                                synonym=synonym, confidence=CONFIDENCE["llm_table"]
                            ))

                else:
                    para = result.parent.get_text(separator=" ", strip=True)
                    if not para:
                        continue
                    scale = detect_scale(para)
                    print(f"Querying LLM (paragraph) for: {synonym}")
                    response = query_llm_paragraph(para, synonym)
                    print(f"LLM paragraph response for {synonym}: {response}")
                    if response and response != "null" and not is_hallucinated(response, para):
                        val = parse_number(response)
                        if val is not None:
                            candidates[keyword].append(Candidate(
                                value=val * scale, scale=scale, source="llm_paragraph",
                                synonym=synonym, confidence=CONFIDENCE["llm_paragraph"]
                            ))

    result = {kw: pick_best_candidate(cs) for kw, cs in candidates.items()}
    missing = [kw for kw, v in result.items() if v is None]
    if missing:
        print(f"Missing values for: {missing}")
    return result

async def read_pdf(filepath: str) -> dict:
    candidates: dict[str, list[Candidate]] = {kw: [] for kw in keywords}

    with pp.open(filepath) as pdf:
        year = detect_year(pdf)

        for keyword, synonyms in keywords.items():
            for synonym in synonyms:
                for page in pdf.pages:
                    text = page.extract_text() or ""
                    if synonym.lower() not in text.lower():
                        continue
                    scale = detect_scale(text)
                    found_in_table = False

                    for table in page.extract_tables():
                        cleaned = format_table(table)
                        if not cleaned:
                            continue

                        # skip irrelevant tables
                        table_text = " ".join(cell for row in cleaned for cell in row if cell)
                        if synonym.lower() not in table_text.lower():
                            continue

                        # rule-based
                        data = {}
                        try_read_from_table(data, cleaned, keyword, synonym, year, scale)
                        if data.get(keyword) is not None:
                            print(f"Rule-based hit: {synonym} = {data[keyword]}")
                            candidates[keyword].append(Candidate(
                                value=data[keyword], scale=scale, source="rule_table",
                                synonym=synonym, confidence=CONFIDENCE["rule_table"]
                            ))
                            found_in_table = True
                            continue  # skip LLM for this table, keep scanning others

                        # llm table fallback
                        formatted = strip_empty_columns(format_table_for_llm(cleaned))
                        print(f"Querying LLM (table) for: {synonym}")
                        response = query_llm_table(formatted, synonym)
                        print(f"LLM table response for {synonym}: {response}")
                        if response and response != "null" and not is_hallucinated(response, formatted):
                            val = parse_number(response)
                            if val is not None:
                                candidates[keyword].append(Candidate(
                                    value=val * scale, scale=scale, source="llm_table",
                                    synonym=synonym, confidence=CONFIDENCE["llm_table"]
                                ))
                                found_in_table = True

                    # paragraph fallback only if no table candidate found on this page
                    if found_in_table:
                        continue

                    paragraphs = [
                        p.strip() for p in re.split(r'\n{2,}', text)
                        if synonym.lower() in p.lower() and p.strip()
                    ]
                    for para in paragraphs:
                        print(f"Querying LLM (paragraph) for: {synonym}")
                        response = query_llm_paragraph(para, synonym)
                        print(f"LLM paragraph response for {synonym}: {response}")
                        if response and response != "null" and not is_hallucinated(response, para):
                            val = parse_number(response)
                            if val is not None:
                                candidates[keyword].append(Candidate(
                                    value=val * scale, scale=scale, source="llm_paragraph",
                                    synonym=synonym, confidence=CONFIDENCE["llm_paragraph"]
                                ))

    result = {kw: pick_best_candidate(cs) for kw, cs in candidates.items()}
    missing = [kw for kw, v in result.items() if v is None]
    if missing:
        print(f"Missing values for: {missing}")
    return result

async def read_document(filepath: str) -> dict | None:
    if filepath.endswith(('.xhtml', '.html')):
        data = await read_xhtml(filepath)
    elif filepath.endswith('.pdf'):
        data = await read_pdf(filepath)
    else:
        data = None
    return data