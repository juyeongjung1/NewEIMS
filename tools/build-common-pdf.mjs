import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { execFileSync } from "node:child_process";
import { dirname, join } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { createRequire } from "node:module";
import { chromium } from "playwright";

const toolsDir = dirname(fileURLToPath(import.meta.url));
const root = join(toolsDir, "..");
const temporary = join(root, "tmp", "pdfs", "common-guide");
const output = join(root, "output", "pdf", "EIMS_Comprehensive_Exercise_Guide.pdf");
mkdirSync(temporary, { recursive: true });
mkdirSync(dirname(output), { recursive: true });
const html = join(temporary, "guide.html");
execFileSync("pandoc", [
  join(root, "EIMS_Comprehensive_Exercise_Guide.md"), "--from=gfm", "--to=html5",
  "--standalone", "--metadata", "pagetitle=EIMS アプリ総合演習ガイド",
  "--metadata", "lang=ja-JP", "--css", pathToFileURL(join(toolsDir, "common-guide.css")).href,
  "--output", html,
], { cwd: root, stdio: "inherit" });
writeFileSync(html, readFileSync(html, "utf8").replace("<head>",
  `<head><base href="${pathToFileURL(root + "/").href}">`));
const browser = await chromium.launch({ channel: "chrome", headless: true });
try {
  const page = await browser.newPage({ viewport: { width: 700, height: 1000 } });
  await page.goto(pathToFileURL(html).href);
  await page.emulateMedia({ media: "print" });
  await page.evaluate(() => {
    for (const divider of document.querySelectorAll('div[style*="page-break-before"]')) {
      if (divider.textContent.trim() || divider.children.length) continue;
      let next = divider.nextElementSibling;
      while (next && (next.tagName === "HR" || (next.tagName === "P" && !next.textContent.trim()))) {
        const following = next.nextElementSibling;
        if (next.tagName === "HR") next.remove();
        next = following;
      }
      if (next) next.style.breakBefore = "page";
      divider.remove();
    }
    for (const paragraph of document.querySelectorAll("p")) {
      if (!paragraph.textContent.trim() && paragraph.querySelector("a[id]")) {
        const next = paragraph.nextElementSibling;
        if (next) for (const anchor of paragraph.querySelectorAll("a[id]")) next.prepend(anchor);
        paragraph.remove();
      }
    }
    for (const paragraph of document.querySelectorAll("p:has(>img)")) {
      const content = document.createDocumentFragment();
      let text = document.createElement("p");
      for (const child of [...paragraph.childNodes]) {
        if (child.nodeName === "IMG") {
          if (text.textContent.trim()) content.append(text);
          const figure = document.createElement("figure");
          const caption = document.createElement("figcaption");
          caption.textContent = child.alt;
          figure.append(child, caption);
          content.append(figure);
          text = document.createElement("p");
        } else text.append(child);
      }
      if (text.textContent.trim()) content.append(text);
      paragraph.replaceWith(content);
    }
    for (const block of document.querySelectorAll("pre.mermaid")) {
      const diagram = document.createElement("div");
      diagram.className = "mermaid";
      diagram.textContent = block.textContent;
      block.replaceWith(diagram);
    }
  });
  const require = createRequire(import.meta.url);
  await page.addScriptTag({ path: require.resolve("mermaid/dist/mermaid.min.js") });
  await page.evaluate(async () => {
    mermaid.initialize({ startOnLoad: false, securityLevel: "loose", theme: "neutral",
      fontFamily: '"Yu Gothic", "Meiryo", sans-serif',
      flowchart: { htmlLabels: true }, sequence: { useMaxWidth: true } });
    await mermaid.run({ querySelector: ".mermaid" });
    await document.fonts.ready;
  });
  await page.evaluate(() => {
    for (const svg of document.querySelectorAll(".mermaid svg")) {
      const diagram = svg.parentElement;
      const viewBox = svg.viewBox.baseVal;
      if (viewBox.width > 1250) diagram.classList.add("wide-diagram");
      const preceding = [];
      let sibling = diagram.previousElementSibling;
      while (sibling && sibling.tagName === "P") {
        preceding.unshift(sibling);
        sibling = sibling.previousElementSibling;
      }
      if (sibling && ["H3", "H4"].includes(sibling.tagName)) preceding.unshift(sibling);
      for (const element of preceding.reverse()) {
        if (element.style.breakBefore === "page") {
          element.style.breakBefore = "auto";
          diagram.style.breakBefore = "page";
        }
        diagram.prepend(element);
      }
    }
  });
  const failedImages = await page.locator("img").evaluateAll(images =>
    images.filter(image => !image.complete || image.naturalWidth === 0).map(image => image.src));
  if (failedImages.length) throw new Error(`画像の読み込み失敗: ${failedImages.join(", ")}`);
  const figures = await page.locator(".mermaid svg").count();
  const diagnostics = await page.locator(".mermaid svg").evaluateAll(figures => figures.map((svg, i) => ({
    number: i + 1, viewBox: svg.getAttribute("viewBox"), width: svg.getBoundingClientRect().width,
    height: svg.getBoundingClientRect().height,
  })));
  writeFileSync(join(temporary, "diagrams.json"), JSON.stringify(diagnostics, null, 2));
  await page.pdf({ path: output, format: "A4", printBackground: true, preferCSSPageSize: true,
    displayHeaderFooter: true, headerTemplate: "<span></span>",
    footerTemplate: '<div style="width:100%;text-align:center;font-size:8px;color:#777"><span class="pageNumber"></span></div>',
    outline: true, tagged: true });
  console.log(`共通仕様PDFを生成しました。図: ${figures}件、出力: ${output}`);
} finally { await browser.close(); }
