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
    result = subprocess.run(
        [
            "pandoc",
            str(SOURCE),
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
    )
    return result.stdout


def decorate_html(html: str) -> str:
    cover = """
<section class="cover">
  <div class="cover-copy">
    <div class="eyebrow">EIMS DOCUMENT CHANGE REPORT</div>
    <h1>アプリ総合演習ガイド<span>PDFページ差分・整合表</span></h1>
    <p class="cover-lead">
      2026年6月15日版と現行版をPDFページ単位で照合し、
      どのページがどこへ移動し、仕様がどう変わったかを整理した比較資料です。
    </p>
  </div>
  <div class="cover-aside">
    <div class="cover-stat"><strong>62 → 75</strong><span>PDFページ数</span></div>
    <div class="cover-stat"><strong>+13</strong><span>増加ページ</span></div>
    <div class="cover-stat"><strong>48</strong><span>ページ対応項目</span></div>
  </div>
  <div class="cover-note">
    <span>比較基準：2026年6月15日版 → 2026年7月22日現行版</span>
    <span>社員情報管理システム（EIMS）</span>
  </div>
</section>
"""
    html = re.sub(r"<h1[^>]*>.*?</h1>", cover, html, count=1, flags=re.DOTALL)
    # Pandocの文書タイトルとは別に、Markdown本文の先頭見出しが残るため除去する。
    html = re.sub(r'<h1 id="[^"]+">.*?</h1>', "", html, count=1, flags=re.DOTALL)

    table_classes = [
        "meta-table",
        "delta-table",
        "alignment-table",
        "focus-table",
        "commit-table",
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

    note = """
<div class="callout">
  <strong>ページ番号の見方：</strong>
  本資料では、PDFビューワーで表紙を1ページ目として数えた物理ページ番号を使用しています。
  本文下部に印刷される番号とは異なる場合があります。
</div>
"""
    html = re.sub(
        r"<p>本表のページ番号は、.*?</p>",
        note,
        html,
        count=1,
        flags=re.DOTALL,
    )
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
