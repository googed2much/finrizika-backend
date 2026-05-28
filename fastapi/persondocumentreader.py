import pdfplumber as pp
import re

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

def extract_debt_blob(table: list[list[str]]) -> str:
    blob = ""
    collecting = False

    for row in table:
        if not row:
            continue
        label = str(row[0]).strip()
        col1 = str(row[1]).strip() if len(row) > 1 else ""

        if 'Detali informacija' in label:
            collecting = True
            continue

        if collecting and label and any(keyword in label for keyword in ('Mokėjimų', 'Teigiama', 'Užklausų', 'Kreditų')):
            break

        if collecting and col1:
            blob += ' ' + col1

    return blob.strip()

def parse_debt_details(table: list[list[str]]) -> dict:
    debts = []
    total = 0.0

    blob = extract_debt_blob(table)
    if not blob:
        return {'items': [], 'count': 0, 'total': 0.0}

    pattern = re.compile(
        r'(\d{4}-\d{2}-\d{2})\s+([\d.,]+)\s+(.+?)\s+(Paslaugos|Paskola|Lizingas|Kita)\s+(\d+)'
    )
    for match in pattern.finditer(blob):
        amount = float(match.group(2).replace('.', '').replace(',', '.'))
        total += amount
        debts.append({
            'date': match.group(1),
            'amount': amount,
            'creditor': match.group(3).strip(),
            'type': match.group(4),
            'id': match.group(5),
        })

    return {
        'items': debts,
        'count': len(debts),
        'total': round(total, 2),
    }

def extract_sold_debt_blob(table: list[list[str]]) -> str:
    blob = ""
    collecting = False

    for row in table:
        if not row:
            continue
        label = str(row[0]).strip()
        col1 = str(row[1]).strip() if len(row) > 1 else ""

        if 'Kitos skolos' in label:
            collecting = True
            continue

        if collecting and label and any(keyword in label for keyword in ('Ataskaitų', 'Sektoriai')):
            break

        if collecting and col1:
            blob += ' ' + col1
        elif collecting and label and 'Detali informacija' not in label and 'Mokėjimų skaičius' not in label:
            blob += ' ' + label

    return blob.strip()


def parse_sold_debts(table: list[list[str]]) -> dict:
    debts = []
    total = 0.0

    blob = extract_sold_debt_blob(table)
    if not blob:
        return {'items': [], 'count': 0, 'total': 0.0}

    # pattern: "2019-06-18 / 2021-03-09 370,26 Lorem Ipsum PARDUOTA 00000"
    pattern = re.compile(
        r'(\d{4}-\d{2}-\d{2})\s*/\s*(\d{4}-\d{2}-\d{2})\s+([\d.,]+)\s+(.+?)\s+(PARDUOTA|RESTRUKTŪRIZUOTA)\s+(\d+)',
        re.IGNORECASE
    )
    for match in pattern.finditer(blob):
        amount = float(match.group(3).replace('.', '').replace(',', '.'))
        total += amount
        debts.append({
            'date': match.group(1),
            'amount': amount,
            'creditor': match.group(4).strip(),
            'type': match.group(5).upper(),
            'id': match.group(6),
        })

    return {
        'items': debts,
        'count': len(debts),
        'total': round(total, 2),
    }

def parse_credit_report(table: list[list[str]]) -> dict:
    data = {}

    # extract simple key-value rows (value in col 4)
    simple_fields = {
        'skolų skaičius': 'debt_count',
        'paskutinės skolos data': 'last_debt_date',
        'vidutinė įsiskolinimų trukmė': 'avg_debt_duration',
        'mokėjimų skaičius': 'payment_count',
        'paskutinė skolos apmokėjimo data': 'last_payment_date',
        'vidutinis apmokėjimo terminas': 'avg_payment_term',
        'asmens statusas': 'blacklist_status',
    }

    for row in table:
        label = re.sub(r'\s+', ' ', str(row[0]).strip().lower())
        for key, field in simple_fields.items():
            if key in label:
                # value is in col 0 for inline ones like "asmens statusas: neregistruotas"
                if ':' in label:
                    data[field] = label.split(':', 1)[1].strip()
                elif len(row) > 4 and row[4].strip():
                    data[field] = row[4].strip()

    # extract risk class from col 6
    for row in table:
        val = str(row[6]).strip() if len(row) > 6 else ''
        if 'klasė' in val.lower() and '%' in val:
            # e.g. "E klasė (99,85%)"
            match = re.match(r'([A-E])\s*klasė\s*\(([\d,]+%)\)', val, re.IGNORECASE)
            if match:
                data['risk_class'] = match.group(1)
                data['risk_probability'] = match.group(2)

    # parse detailed debt list from the blob in col 1
    data['debts'] = parse_debt_details(table)
    data['sold_debts'] = parse_sold_debts(table)

    return data

async def read_pdf(filepath: str) -> dict:
    all_tables = []
    with pp.open(filepath) as pdf:
        for page in pdf.pages:
            tables = page.extract_tables()
            for table in tables:
                cleaned = format_table(table)
                all_tables.extend(cleaned)

    all_data = parse_credit_report(all_tables)
    data = all_data['debts']['items'] + all_data['sold_debts']['items']
    return data

async def read_document(filepath: str) -> dict | None:
    if filepath.endswith('.pdf'):
        data = await read_pdf(filepath)
    else:
        data = None
    return data