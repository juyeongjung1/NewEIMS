from __future__ import annotations

import io
import re
import shutil
import subprocess
from pathlib import Path

from pypdf import PdfReader, PdfWriter
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.cidfonts import UnicodeCIDFont
from reportlab.pdfgen import canvas


ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "EIMS_Comprehensive_Exercise_Guide_PDF_Page_Alignment_20260615.md"
CSS = ROOT / "pdf変換及び装飾関連" / "alignment-report.css"
TMP = ROOT / "tmp" / "pdfs" / "alignment-report"
OUTPUT = ROOT / "output" / "pdf" / "EIMS_Comprehensive_Exercise_Guide_PDF_Page_Alignment_20260615.pdf"
RAW_PDF = TMP / "alignment-report-raw.pdf"
HTML = TMP / "alignment-report.html"


def find_edge() -> Path:
    candidates = [
        Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"),
        Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return candidate
    raise FileNotFoundError("Microsoft Edge が見つかりません。")


def convert_markdown() -> str:
    source_text = SOURCE.read_text(encoding="utf-8")
    start = source_text.index("## 2. ページ増減の概要")
    end = source_text.index("## 5. 関連コミット")
    source_text = source_text[start:end].rstrip()
    heading_numbers = {"2": "1", "3": "2", "4": "3"}
    source_text = re.sub(
        r"^## ([234])\.",
        lambda match: f"## {heading_numbers[match.group(1)]}.",
        source_text,
        flags=re.MULTILINE,
    )
    source_text = source_text.replace(
        "## 1. ページ増減の概要",
        "## 1. ページ増減の概要\n\n"
        "> **任意項目について**：コンポーネントテストと発展課題は必須ではありません。"
        "基本機能の実装とシステムテストを優先し、時間に余裕がある場合に取り組むオプションです。",
        1,
    )
    source_text = source_text.replace(
        "| コンポーネントテスト | +2ページ |",
        "| コンポーネントテスト（任意） | +2ページ |",
    )
    source_text = source_text.replace(
        "| 発展課題 | +2ページ |",
        "| 発展課題（任意） | +2ページ |",
    )
    source_text = source_text.replace(
        "テスト環境設定とテストケースの拡充",
        "任意項目として、テスト環境設定とテストケースを拡充",
    )
    source_text = source_text.replace(
        "二重ポスト対策と社員一覧ソートを新設",
        "任意の発展課題として、二重ポスト対策と社員一覧ソートを新設",
    )
    source_text = source_text.replace(
        "完了画面の再読み込み等でPOSTが再実行される問題",
        "【任意】完了画面の再読み込み等でPOSTが再実行される問題",
    )
    source_text = source_text.replace(
        "社員一覧を社員番号昇順などで安定して表示する",
        "【任意】社員一覧を社員番号昇順などで安定して表示する",
    )
    source_text = source_text.replace(
        "テスト環境、テストケース、共通メッセージ",
        "【任意】コンポーネントテスト：テスト環境、テストケース、共通メッセージ",
    )
    source_text = source_text.replace(
        "新設された発展課題",
        "【任意】発展課題：二重ポスト対策、社員一覧ソート",
    )

    result = subprocess.run(
        [
            "pandoc",
            "--from=gfm",
            "--to=html5",
            "--standalone",
            "--metadata",
            "lang=ja-JP",
            "--metadata",
            "title=アプリ総合演習ガイド PDFページ差分・整合表",
            "--css",
            str(CSS),
        ],
        cwd=ROOT,
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
        input=source_text,
    )
    return result.stdout


def decorate_html(html: str) -> str:
    html = re.sub(
        r'<header id="title-block-header">.*?</header>',
        "",
        html,
        count=1,
        flags=re.DOTALL,
    )

    table_classes = [
        "delta-table",
        "alignment-table",
        "focus-table",
    ]
    table_index = 0

    def add_table_class(match: re.Match[str]) -> str:
        nonlocal table_index
        class_name = table_classes[table_index] if table_index < len(table_classes) else ""
        table_index += 1
        return f'<table class="{class_name}">' if class_name else "<table>"

    html = re.sub(r"<table>", add_table_class, html)
    html = html.replace("<td>大</td>", '<td><span class="badge badge-major">大</span></td>')
    html = html.replace("<td>中</td>", '<td><span class="badge badge-medium">中</span></td>')
    html = html.replace("<td>小</td>", '<td><span class="badge badge-minor">小</span></td>')
    html = html.replace("<td>新規</td>", '<td><span class="badge badge-new">新規</span></td>')

    optional_terms = (
        "<td>コンポーネントテスト（任意）</td>",
        "<td>発展課題（任意）</td>",
        "コンポーネントテスト仕様（任意）",
        "【任意】",
    )

    def mark_optional_row(match: re.Match[str]) -> str:
        row = match.group(0)
        if not any(term in row for term in optional_terms):
            return row
        if 'class="' in row.split(">", 1)[0]:
            return row.replace('class="', 'class="optional-row ', 1)
        return row.replace("<tr", '<tr class="optional-row"', 1)

    html = re.sub(r"<tr(?:\s[^>]*)?>.*?</tr>", mark_optional_row, html, flags=re.DOTALL)
    return html


def render_pdf() -> None:
    TMP.mkdir(parents=True, exist_ok=True)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    HTML.write_text(decorate_html(convert_markdown()), encoding="utf-8")

    edge = find_edge()
    subprocess.run(
        [
            str(edge),
            "--headless",
            "--disable-gpu",
            "--no-pdf-header-footer",
            f"--print-to-pdf={RAW_PDF}",
            HTML.resolve().as_uri(),
        ],
        cwd=ROOT,
        check=True,
    )
    if not RAW_PDF.exists():
        raise RuntimeError("PDFの生成に失敗しました。")


def add_footer() -> None:
    reader = PdfReader(RAW_PDF)
    writer = PdfWriter()
    total = len(reader.pages)

    try:
        pdfmetrics.registerFont(UnicodeCIDFont("HeiseiKakuGo-W5"))
        font_name = "HeiseiKakuGo-W5"
    except Exception:
        font_name = "Helvetica"

    for page_number, page in enumerate(reader.pages, start=1):
        width = float(page.mediabox.width)
        height = float(page.mediabox.height)
        packet = io.BytesIO()
        layer = canvas.Canvas(packet, pagesize=(width, height))
        layer.setStrokeColorRGB(0.84, 0.88, 0.91)
        layer.setLineWidth(0.45)
        layer.line(34, 23, width - 34, 23)
        layer.setFillColorRGB(0.38, 0.45, 0.51)
        layer.setFont(font_name, 7.5)
        layer.drawString(34, 11, "EIMS アプリ総合演習ガイド - PDFページ差分・整合表")
        layer.drawRightString(width - 34, 11, f"{page_number} / {total}")
        layer.save()
        packet.seek(0)
        page.merge_page(PdfReader(packet).pages[0])
        writer.add_page(page)

    writer.add_metadata(
        {
            "/Title": "アプリ総合演習ガイド PDFページ差分・整合表",
            "/Subject": "2026年6月15日版と現行版のPDFページ対応および変更内容",
            "/Author": "NewEIMS",
        }
    )
    with OUTPUT.open("wb") as stream:
        writer.write(stream)


def main() -> None:
    render_pdf()
    add_footer()
    reader = PdfReader(OUTPUT)
    print(f"PDF: {OUTPUT}")
    print(f"Pages: {len(reader.pages)}")
    print(f"Size: {OUTPUT.stat().st_size:,} bytes")


if __name__ == "__main__":
    main()
