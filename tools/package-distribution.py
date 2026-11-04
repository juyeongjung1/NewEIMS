"""更新済みPDFとソースを配布先へ同期し、ZIP内の全ファイルを照合する。"""
from pathlib import Path
import hashlib
import json
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
EXCLUDED = {"target", "node_modules", ".git", ".settings", "__pycache__"}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def copy_pdf(source, destinations):
    source = ROOT / source
    if not source.is_file():
        raise FileNotFoundError(f"PDFを先に生成してください: {source}")
    for destination in destinations:
        target = ROOT / destination
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
        assert digest(target.read_bytes()) == digest(source.read_bytes())


def archive(source, destination, prefix, project=False):
    source, destination = ROOT / source, ROOT / destination
    project_roots = {"src", ".mvn", "mvnw", "mvnw.cmd", "pom.xml", "はじめにお読みください.txt"}
    files = []
    for path in sorted(source.rglob("*")):
        relative = path.relative_to(source)
        if not path.is_file() or any(part in EXCLUDED for part in relative.parts):
            continue
        if "test_bk" in relative.parts or path.suffix in {".zip", ".class", ".jar", ".log"}:
            continue
        if path.name.startswith("~$"):
            continue
        if project and relative.parts[0] not in project_roots:
            continue
        files.append((path, str(Path(prefix) / relative).replace("\\", "/")))
    with zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED) as output:
        for path, member in files:
            output.write(path, member)
    with zipfile.ZipFile(destination) as output:
        assert output.testzip() is None
        assert len(output.infolist()) == len(files)
        for path, member in files:
            assert output.read(member) == path.read_bytes(), member
    return {"path": str(destination.relative_to(ROOT)).replace("\\", "/"),
            "files": len(files), "sha256": digest(destination.read_bytes())}


copy_pdf("output/pdf/EIMS_Comprehensive_Exercise_Guide.pdf", [
    "EIMS_Comprehensive_Exercise_Guide.pdf",
    "EIMS_受講者配布資料/アプリ総合演習ガイド(共通機能).pdf",
])
copy_pdf("追加機能の設計書/output/EIMS_追加機能設計書.pdf", [
    "EIMS_追加機能設計書.pdf", "EIMS_受講者配布資料/アプリ総合演習ガイド(追加機能設計書).pdf",
])
archives = [
    archive("EIMS_SpringBoot_Basic", "EIMS_SpringBoot_Basic(解答).zip", "EIMS_SpringBoot_Basic", True),
    archive("EIMS_SpringBoot_Additional", "EIMS_SpringBoot_Additional(追加機能解答).zip", "EIMS_SpringBoot_Additional", True),
    archive("EIMS_TestCode", "EIMS_TestCode(解答).zip", "EIMS_TestCode"),
    archive("DBSET", "DBSET.zip", "DBSET"),
    archive("EIMS_受講者配布資料/EIMS_StarterKits", "EIMS_StarterKits.zip", "EIMS_StarterKits"),
    archive("EIMS_受講者配布資料/テスト関連のテンプレート", "テスト関連のテンプレート.zip", "テスト関連のテンプレート"),
    archive("EIMS_受講者配布資料", "EIMS_受講者配布資料/EIMS_受講者配布資料.zip", ""),
]
pdfs = ["EIMS_Comprehensive_Exercise_Guide.pdf", "EIMS_追加機能設計書.pdf"]
manifest = {"pdfs": [{"path": name, "sha256": digest((ROOT / name).read_bytes())} for name in pdfs],
            "archives": archives}
(ROOT / "docs/配布物照合結果.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
for item in archives:
    print(f"{item['path']}: {item['files']}ファイルを照合済み")
