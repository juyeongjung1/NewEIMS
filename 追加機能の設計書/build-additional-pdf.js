const fs = require("fs");
const path = require("path");
const { execFileSync, spawn } = require("child_process");
const { pathToFileURL } = require("url");

const docsDir = __dirname;
const root = path.resolve(docsDir, "..");
const input = path.join(docsDir, "追加機能設計書.md");
const tmpDir = path.join(root, ".tmp-additional-design-doc");
const outDir = path.join(docsDir, "output");
const cssPath = path.join(tmpDir, "guide.css");
const htmlPath = path.join(tmpDir, "additional-design-doc.html");
const pdfPath = path.join(outDir, "EIMS_追加機能設計書.pdf");
const tmpPdfPath = path.join(tmpDir, "EIMS_追加機能設計書.pdf");
const previewPath = path.join(tmpDir, "additional-design-doc-preview.png");
const chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
const logoPath = path.join(root, "images", "trainocate_logo.png");

const css = String.raw`
:root {
    --ink: #232323;
    --muted: #666f7d;
    --line: #ead8d2;
    --soft: #fff8f5;
    --brand: #e94b22;
    --brand-dark: #b9361c;
    --brand-soft: #fff1ec;
    --accent: #1d3994;
    --accent-soft: #eef3ff;
    --code-bg: #fffaf7;
    --guide-logo: none;
}

html {
    font-family: "Noto Sans JP", "Yu Gothic", "YuGothic", "Meiryo", sans-serif;
    color: var(--ink);
    font-size: 10.5pt;
    line-height: 1.78;
}

@page {
    size: A4;
    margin: 15mm 16mm 18mm 16mm;
}

body {
    max-width: none;
    margin: 0;
    padding: 0;
    word-break: normal;
    overflow-wrap: anywhere;
}

#title-block-header {
    min-height: 210mm;
    display: flex;
    flex-direction: column;
    justify-content: center;
    border-left: 10px solid var(--brand);
    padding-left: 18mm;
    break-after: page;
    position: relative;
}

#title-block-header::before {
    content: "";
    position: absolute;
    top: 10mm;
    left: 18mm;
    width: 52mm;
    height: 8mm;
    background: var(--guide-logo) left center / contain no-repeat;
}

#title-block-header .title {
    margin: 0;
    font-size: 30pt;
    line-height: 1.35;
    letter-spacing: 0;
    color: var(--brand);
}

#title-block-header .date {
    display: none;
}

body > h1:first-of-type {
    display: none;
}

a {
    color: var(--accent);
    text-decoration: none;
}

h1, h2, h3, h4 {
    color: var(--brand-dark);
    line-height: 1.35;
    break-after: avoid;
}

h1 {
    font-size: 21pt;
    margin: 0 0 9mm;
    padding-bottom: 4mm;
    border-bottom: 2px solid var(--brand);
    break-before: page;
}

#title-block-header .title {
    break-before: auto;
}

h2 {
    font-size: 15.5pt;
    margin: 7mm 0 4mm;
    padding: 2.5mm 4mm;
    background: var(--brand-soft);
    border-left: 5px solid var(--brand);
    break-before: auto;
    break-after: avoid;
}

h1 + h2 {
    margin-top: 0;
}

h3 {
    font-size: 13.2pt;
    margin: 6mm 0 3mm;
    padding-bottom: 1.5mm;
    border-bottom: 1px solid var(--line);
    break-before: auto;
    break-after: avoid;
}

h2 + h3 {
    margin-top: 0;
}

h4 {
    font-size: 11.2pt;
    margin: 6mm 0 2.5mm;
    color: var(--brand-dark);
    break-after: avoid;
}

p {
    margin: 2.5mm 0 4mm;
}

strong {
    color: var(--brand-dark);
    font-weight: 700;
}

ul, ol {
    margin: 2.5mm 0 5mm 7mm;
    padding-left: 5mm;
}

li {
    margin: 1.7mm 0;
}

blockquote {
    margin: 5mm 0;
    padding: 3.5mm 5mm;
    background: var(--brand-soft);
    border-left: 5px solid var(--brand);
    color: #562313;
    break-inside: avoid;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin: 4mm 0 7mm;
    font-size: 9.4pt;
    break-inside: avoid;
}

thead {
    display: table-header-group;
}

th {
    background: var(--brand);
    color: #ffffff;
    font-weight: 700;
}

th, td {
    border: 1px solid var(--line);
    padding: 2mm 2.5mm;
    vertical-align: top;
}

code {
    font-family: "Consolas", "BIZ UDGothic", "Meiryo", monospace;
    font-size: 9pt;
    background: var(--brand-soft);
    border-radius: 3px;
    padding: 0.2mm 1mm;
    word-break: break-all;
}

pre {
    margin: 4mm 0 6mm;
    padding: 3.5mm 4mm;
    background: var(--code-bg);
    border: 1px solid var(--line);
    border-left: 4px solid var(--brand);
    border-radius: 5px;
    overflow: visible;
    white-space: pre-wrap;
    break-inside: avoid;
}

pre code {
    display: block;
    background: transparent;
    padding: 0;
    line-height: 1.55;
    word-break: normal;
    overflow-wrap: anywhere;
}

img {
    display: block;
    max-width: 100%;
    max-height: 128mm;
    margin: 4mm auto 8mm;
    border: 1px solid var(--line);
    border-radius: 6px;
    box-shadow: 0 2px 10px rgba(29, 57, 148, 0.12);
    object-fit: contain;
    break-inside: avoid;
}

.layout-diagram-block {
    break-inside: avoid;
    page-break-inside: avoid;
    margin-bottom: 8mm;
}

hr {
    border: 0;
    border-top: 1px solid var(--line);
    margin: 8mm 0;
}

@media print {
    body {
        -webkit-print-color-adjust: exact;
        print-color-adjust: exact;
    }
}

#TOC {
    break-before: page;
    break-after: page;
    padding: 20mm 15mm;
    background-color: var(--brand-soft);
    border-radius: 8px;
    margin: 10mm 0;
    box-shadow: inset 0 0 20px rgba(233, 75, 34, 0.05);
}

#TOC h2#toc-title {
    font-size: 24pt;
    color: var(--brand-dark);
    font-weight: 800;
    border-bottom: 3px solid var(--brand);
    padding-bottom: 4mm;
    margin-bottom: 10mm;
    display: flex;
    align-items: center;
}

#TOC h2#toc-title::before {
    content: "INDEX";
    font-size: 10pt;
    font-weight: 700;
    background: var(--brand);
    color: #fff;
    padding: 1mm 3mm;
    border-radius: 4px;
    margin-right: 4mm;
    letter-spacing: 1px;
}

#TOC > ul {
    list-style: none;
    padding-left: 0;
    margin-left: 0;
}

#TOC > ul > li {
    margin-top: 6mm;
    border-left: 4px solid var(--brand);
    padding-left: 5mm;
    font-size: 12.5pt;
    font-weight: 700;
}

#TOC > ul > li > a {
    color: var(--brand-dark);
}

#TOC > ul > li > ul {
    list-style: none;
    padding-left: 0;
    margin-top: 2mm;
    margin-bottom: 3mm;
}

#TOC > ul > li > ul > li {
    font-size: 10.5pt;
    font-weight: 600;
    margin: 2mm 0;
    color: var(--ink);
    padding-left: 4mm;
    position: relative;
}

#TOC > ul > li > ul > li::before {
    content: "■";
    font-size: 6pt;
    color: var(--brand);
    position: absolute;
    left: 0;
    top: 0.5mm;
}

#TOC > ul > li > ul > li > a {
    color: var(--ink);
}

#TOC > ul > li > ul > li > ul {
    list-style: none;
    padding-left: 0;
    margin-top: 1mm;
    margin-bottom: 2mm;
    display: flex;
    flex-wrap: wrap;
    gap: 2mm 4mm;
}

#TOC > ul > li > ul > li > ul > li {
    font-size: 9.5pt;
    font-weight: 400;
    margin: 0;
    color: var(--muted);
    background: #ffffff;
    padding: 0.8mm 2.5mm;
    border: 1px solid var(--line);
    border-radius: 4px;
}
`;

function logoDataUri() {
    if (!fs.existsSync(logoPath)) {
        return "none";
    }
    const bytes = fs.readFileSync(logoPath);
    return `url("data:image/png;base64,${bytes.toString("base64")}")`;
}

function copyImages() {
    const srcImages = path.join(docsDir, "images");
    const destImages = path.join(tmpDir, "images");
    fs.rmSync(destImages, { recursive: true, force: true });
    fs.mkdirSync(destImages, { recursive: true });
    for (const file of fs.readdirSync(srcImages)) {
        fs.copyFileSync(path.join(srcImages, file), path.join(destImages, file));
    }
}

function runPandoc() {
    console.log("Preparing HTML...");
    fs.mkdirSync(tmpDir, { recursive: true });
    fs.mkdirSync(outDir, { recursive: true });
    copyImages();
    fs.writeFileSync(cssPath, css.replace("--guide-logo: none;", `--guide-logo: ${logoDataUri()};`), "utf8");
    execFileSync(
        "pandoc",
        [
            input,
            "--from=gfm",
            "--to=html5",
            "--standalone",
            "--toc",
            "--toc-depth=3",
            "--resource-path",
            docsDir,
            "--metadata",
            "title=EIMS 追加機能設計書",
            "--metadata",
            "lang=ja-JP",
            "--metadata",
            "toc-title=目次",
            "--css",
            cssPath,
            "--output",
            htmlPath,
        ],
        { cwd: docsDir, stdio: "inherit" }
    );
    console.log(`HTML: ${htmlPath}`);
}

function delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

async function removeDirWithRetry(target) {
    for (let i = 0; i < 12; i += 1) {
        try {
            fs.rmSync(target, { recursive: true, force: true });
            return;
        } catch (error) {
            if (error.code !== "EBUSY" && error.code !== "EPERM") {
                throw error;
            }
            await delay(250);
        }
    }
}

function waitForExit(process) {
    return new Promise((resolve) => {
        if (process.exitCode !== null) {
            resolve();
            return;
        }
        process.once("exit", resolve);
        setTimeout(resolve, 2000);
    });
}

async function waitForDevTools(port) {
    const endpoint = `http://127.0.0.1:${port}/json/version`;
    for (let i = 0; i < 80; i += 1) {
        try {
            const response = await fetch(endpoint);
            if (response.ok) {
                return;
            }
        } catch (error) {
            // Chrome is still starting.
        }
        await delay(250);
    }
    throw new Error("Chrome DevTools endpoint did not start.");
}

async function createPage(port) {
    const response = await fetch(`http://127.0.0.1:${port}/json/new?about:blank`, { method: "PUT" });
    if (!response.ok) {
        throw new Error(`Failed to create Chrome page: ${response.status} ${response.statusText}`);
    }
    return response.json();
}

function createCdpClient(webSocketUrl) {
    const socket = new WebSocket(webSocketUrl);
    let nextId = 1;
    const callbacks = new Map();
    const listeners = new Map();

    socket.addEventListener("message", (event) => {
        const message = JSON.parse(event.data);
        if (message.id && callbacks.has(message.id)) {
            const { resolve, reject } = callbacks.get(message.id);
            callbacks.delete(message.id);
            if (message.error) {
                reject(new Error(`${message.error.message}: ${message.error.data || ""}`));
            } else {
                resolve(message.result);
            }
            return;
        }

        if (message.method && listeners.has(message.method)) {
            for (const listener of listeners.get(message.method)) {
                listener(message.params || {});
            }
        }
    });

    const opened = new Promise((resolve, reject) => {
        socket.addEventListener("open", resolve, { once: true });
        socket.addEventListener("error", reject, { once: true });
    });

    function send(method, params = {}) {
        const id = nextId;
        nextId += 1;
        const payload = JSON.stringify({ id, method, params });
        return new Promise((resolve, reject) => {
            callbacks.set(id, { resolve, reject });
            socket.send(payload);
        });
    }

    function once(method) {
        return new Promise((resolve) => {
            const listener = (params) => {
                listeners.get(method).delete(listener);
                resolve(params);
            };
            if (!listeners.has(method)) {
                listeners.set(method, new Set());
            }
            listeners.get(method).add(listener);
        });
    }

    return {
        opened,
        send,
        once,
        close() {
            socket.close();
        },
    };
}

async function renderPdfWithOutline(fileUrl) {
    const port = 43000 + Math.floor(Math.random() * 1000);
    const chromeProfilePath = path.join(tmpDir, `chrome-profile-${process.pid}-${Date.now()}`);
    fs.mkdirSync(chromeProfilePath, { recursive: true });

    const chrome = spawn(chromePath, [
        "--headless=new",
        "--disable-gpu",
        "--no-first-run",
        "--no-default-browser-check",
        `--remote-debugging-port=${port}`,
        `--user-data-dir=${chromeProfilePath}`,
    ], { stdio: "ignore" });

    try {
        await waitForDevTools(port);
        const page = await createPage(port);
        const client = createCdpClient(page.webSocketDebuggerUrl);
        await client.opened;
        await client.send("Page.enable");
        await client.send("Runtime.enable");

        const loaded = client.once("Page.loadEventFired");
        await client.send("Page.navigate", { url: fileUrl });
        await loaded;
        await client.send("Emulation.setEmulatedMedia", { media: "print" });

        const pdf = await client.send("Page.printToPDF", {
            displayHeaderFooter: false,
            printBackground: true,
            preferCSSPageSize: true,
            generateTaggedPDF: true,
            generateDocumentOutline: true,
        });

        fs.writeFileSync(tmpPdfPath, Buffer.from(pdf.data, "base64"));
        client.close();
    } finally {
        chrome.kill();
        await waitForExit(chrome);
        await removeDirWithRetry(chromeProfilePath);
    }
}

async function renderPdf() {
    console.log("Rendering PDF...");
    const fileUrl = pathToFileURL(htmlPath).href;
    await renderPdfWithOutline(fileUrl);
    try {
        fs.copyFileSync(tmpPdfPath, pdfPath);
    } catch (error) {
        if (error.code === "EBUSY" || error.code === "EPERM") {
            throw new Error(`PDF is locked. Close the file and run this script again: ${pdfPath}`);
        }
        throw error;
    }

    console.log("Saving preview image...");
    execFileSync(chromePath, [
        "--headless=new",
        "--disable-gpu",
        "--no-first-run",
        "--no-default-browser-check",
        `--screenshot=${previewPath}`,
        "--window-size=794,1123",
        fileUrl,
    ], { stdio: "inherit" });
}

function inspectPdf() {
    const bytes = fs.readFileSync(pdfPath);
    const text = bytes.toString("latin1");
    const pageCount = (text.match(/\/Type\s*\/Page\b/g) || []).length;
    const sizeMb = (bytes.length / 1024 / 1024).toFixed(2);
    console.log(`PDF: ${pdfPath}`);
    console.log(`Preview: ${previewPath}`);
    console.log(`Pages: ${pageCount}`);
    console.log(`Size: ${sizeMb} MB`);
}

(async () => {
    runPandoc();
    await renderPdf();
    inspectPdf();
})();
