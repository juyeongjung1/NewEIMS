import { execFileSync, spawn } from "node:child_process";
import { mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { request } from "node:http";
import { join } from "node:path";
import { setTimeout as wait } from "node:timers/promises";
import { deflateSync, inflateSync } from "node:zlib";

const chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
const baseUrl = "http://localhost:8080";
const port = 9222;
const outputDir = "C:\\work\\NewEIMS\\NewEIMS\\追加機能の設計書\\images";
const userDataDir = "C:\\work\\NewEIMS\\NewEIMS\\.tmp-additional-screenshot-profile";
let dbPrepared = false;

mkdirSync(outputDir, { recursive: true });
rmSync(userDataDir, { recursive: true, force: true });

const chrome = spawn(chromePath, [
  "--headless=new",
  "--disable-gpu",
  "--no-first-run",
  "--no-default-browser-check",
  "--hide-scrollbars",
  `--remote-debugging-port=${port}`,
  `--user-data-dir=${userDataDir}`,
  "--window-size=1180,740",
  "about:blank",
], { stdio: "ignore" });

function httpGetJson(url) {
  return new Promise((resolve, reject) => {
    request(url, (res) => {
      let body = "";
      res.setEncoding("utf8");
      res.on("data", (chunk) => body += chunk);
      res.on("end", () => resolve(JSON.parse(body)));
    }).on("error", reject).end();
  });
}

function setDeleteFlg(empNo, deleteFlg) {
  execFileSync("mysql", [
    "-ueimsuser",
    "-pPa$$w0rd",
    "eimsdb",
    "-e",
    `UPDATE employee SET delete_flg = ${deleteFlg} WHERE emp_no = ${empNo};`,
  ], { stdio: "ignore" });
}

const crcTable = new Uint32Array(256);
for (let i = 0; i < 256; i++) {
  let c = i;
  for (let j = 0; j < 8; j++) {
    c = (c & 1) ? (0xedb88320 ^ (c >>> 1)) : (c >>> 1);
  }
  crcTable[i] = c >>> 0;
}

function crc32(buffer) {
  let c = 0xffffffff;
  for (const byte of buffer) {
    c = crcTable[(c ^ byte) & 0xff] ^ (c >>> 8);
  }
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data = Buffer.alloc(0)) {
  const typeBuffer = Buffer.from(type, "ascii");
  const out = Buffer.alloc(12 + data.length);
  out.writeUInt32BE(data.length, 0);
  typeBuffer.copy(out, 4);
  data.copy(out, 8);
  out.writeUInt32BE(crc32(Buffer.concat([typeBuffer, data])), 8 + data.length);
  return out;
}

function addBlackBorder(filePath, border = 4) {
  const source = readFileSync(filePath);
  let offset = 8;
  let width = 0;
  let height = 0;
  const idat = [];
  const chunks = [];

  while (offset < source.length) {
    const length = source.readUInt32BE(offset);
    const type = source.subarray(offset + 4, offset + 8).toString("ascii");
    const data = source.subarray(offset + 8, offset + 8 + length);
    if (type === "IHDR") {
      width = data.readUInt32BE(0);
      height = data.readUInt32BE(4);
      if (data[8] !== 8 || data[9] !== 2) {
        throw new Error(`${filePath} は 8bit RGB PNG ではありません。`);
      }
    } else if (type === "IDAT") {
      idat.push(data);
    } else if (!["IEND", "gAMA", "cHRM", "sRGB", "iCCP"].includes(type)) {
      chunks.push(chunk(type, Buffer.from(data)));
    }
    offset += 12 + length;
  }

  const bytesPerPixel = 3;
  const stride = width * bytesPerPixel;
  const raw = inflateSync(Buffer.concat(idat));
  const pixels = Buffer.alloc(width * height * bytesPerPixel);
  let previous = Buffer.alloc(stride);
  let input = 0;

  for (let y = 0; y < height; y++) {
    const filter = raw[input++];
    const row = Buffer.from(raw.subarray(input, input + stride));
    input += stride;
    for (let x = 0; x < stride; x++) {
      const left = x >= bytesPerPixel ? row[x - bytesPerPixel] : 0;
      const up = previous[x];
      const upLeft = x >= bytesPerPixel ? previous[x - bytesPerPixel] : 0;
      if (filter === 1) row[x] = (row[x] + left) & 0xff;
      if (filter === 2) row[x] = (row[x] + up) & 0xff;
      if (filter === 3) row[x] = (row[x] + Math.floor((left + up) / 2)) & 0xff;
      if (filter === 4) {
        const p = left + up - upLeft;
        const pa = Math.abs(p - left);
        const pb = Math.abs(p - up);
        const pc = Math.abs(p - upLeft);
        row[x] = (row[x] + (pa <= pb && pa <= pc ? left : pb <= pc ? up : upLeft)) & 0xff;
      }
    }
    row.copy(pixels, y * stride);
    previous = row;
  }

  const newWidth = width + border * 2;
  const newHeight = height + border * 2;
  const newStride = newWidth * bytesPerPixel;
  const framed = Buffer.alloc((newStride + 1) * newHeight);
  let out = 0;

  for (let y = 0; y < newHeight; y++) {
    framed[out++] = 0;
    for (let x = 0; x < newWidth; x++) {
      const inBorder = x < border || y < border || x >= width + border || y >= height + border;
      if (inBorder) {
        framed[out++] = 0;
        framed[out++] = 0;
        framed[out++] = 0;
      } else {
        const src = ((y - border) * width + (x - border)) * bytesPerPixel;
        framed[out++] = pixels[src];
        framed[out++] = pixels[src + 1];
        framed[out++] = pixels[src + 2];
      }
    }
  }

  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(newWidth, 0);
  ihdr.writeUInt32BE(newHeight, 4);
  ihdr[8] = 8;
  ihdr[9] = 2;
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;
  writeFileSync(filePath, Buffer.concat([
    source.subarray(0, 8),
    chunk("IHDR", ihdr),
    ...chunks,
    chunk("IDAT", deflateSync(framed)),
    chunk("IEND"),
  ]));
}

async function waitForChrome() {
  for (let i = 0; i < 50; i++) {
    try {
      const pages = await httpGetJson(`http://localhost:${port}/json`);
      const page = pages.find((item) => item.type === "page" && item.webSocketDebuggerUrl);
      if (page) {
        return page.webSocketDebuggerUrl;
      }
    } catch {
      await wait(200);
    }
  }
  throw new Error("Chrome DevTools Protocol に接続できませんでした。");
}

function connectCdp(wsUrl) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(wsUrl);
    const callbacks = new Map();
    const listeners = new Map();
    let id = 0;

    ws.addEventListener("open", () => {
      resolve({
        send(method, params = {}) {
          const messageId = ++id;
          ws.send(JSON.stringify({ id: messageId, method, params }));
          return new Promise((res, rej) => callbacks.set(messageId, { res, rej }));
        },
        once(eventName) {
          return new Promise((res) => listeners.set(eventName, res));
        },
        close() {
          ws.close();
        },
      });
    });

    ws.addEventListener("message", async (event) => {
      let data = event.data;
      if (data instanceof ArrayBuffer) {
        data = Buffer.from(data).toString("utf8");
      } else if (ArrayBuffer.isView(data)) {
        data = Buffer.from(data.buffer).toString("utf8");
      } else if (typeof data !== "string" && data?.text) {
        data = await data.text();
      }
      const message = JSON.parse(data);
      if (message.id && callbacks.has(message.id)) {
        const { res, rej } = callbacks.get(message.id);
        callbacks.delete(message.id);
        if (message.error) {
          rej(new Error(message.error.message));
        } else {
          res(message.result);
        }
        return;
      }
      if (message.method && listeners.has(message.method)) {
        const listener = listeners.get(message.method);
        listeners.delete(message.method);
        listener(message.params);
      }
    });

    ws.addEventListener("error", reject);
  });
}

async function main() {
  setDeleteFlg(10002, 1);
  dbPrepared = true;

  const wsUrl = await waitForChrome();
  const cdp = await connectCdp(wsUrl);

  await cdp.send("Page.enable");
  await cdp.send("Runtime.enable");
  await cdp.send("Emulation.setDeviceMetricsOverride", {
    width: 1180,
    height: 740,
    deviceScaleFactor: 1.5,
    mobile: false,
  });

  async function navigate(path) {
    const loaded = cdp.once("Page.loadEventFired");
    await cdp.send("Page.navigate", { url: `${baseUrl}${path}` });
    await loaded;
    await wait(900);
  }

  async function evaluate(expression, returnByValue = false) {
    const result = await cdp.send("Runtime.evaluate", { expression, awaitPromise: true, returnByValue });
    return result.result?.value;
  }

  async function screenshot(fileName) {
    const clip = await evaluate(`
      (() => {
        const pad = 24;
        if (!document.querySelector('main')) {
          const card = document.querySelector('.card') || document.body;
          const rect = card.getBoundingClientRect();
          return {
            x: Math.max(0, Math.floor(rect.left - pad)),
            y: Math.max(0, Math.floor(rect.top - pad)),
            width: Math.min(window.innerWidth, Math.ceil(rect.width + pad * 2)),
            height: Math.min(window.innerHeight, Math.ceil(rect.height + pad * 2)),
            scale: 1
          };
        }

        const targets = [
          ...document.querySelectorAll('.navbar, main .card, main h2, main table, main .alert, main .mt-4')
        ];
        const bottom = Math.max(...targets.map((el) => el.getBoundingClientRect().bottom), 480);
        const right = Math.max(...targets.map((el) => el.getBoundingClientRect().right), 920);
        return {
          x: 0,
          y: 0,
          width: Math.min(window.innerWidth, Math.ceil(right + pad)),
          height: Math.min(window.innerHeight, Math.ceil(bottom + pad)),
          scale: 1
        };
      })()
    `, true);
    const result = await cdp.send("Page.captureScreenshot", {
      format: "png",
      fromSurface: true,
      captureBeyondViewport: false,
      clip,
    });
    writeFileSync(join(outputDir, fileName), Buffer.from(result.data, "base64"));
    addBlackBorder(join(outputDir, fileName));
    console.log(fileName);
  }

  async function screenshotSelector(fileName, selector, padding = 16) {
    const clip = await evaluate(`
      (() => {
        const target = document.querySelector('${selector}');
        const rect = target.getBoundingClientRect();
        const pad = ${padding};
        return {
          x: Math.max(0, Math.floor(rect.left - pad)),
          y: Math.max(0, Math.floor(rect.top - pad)),
          width: Math.min(window.innerWidth, Math.ceil(rect.width + pad * 2)),
          height: Math.min(window.innerHeight, Math.ceil(rect.height + pad * 2)),
          scale: 1
        };
      })()
    `, true);
    const result = await cdp.send("Page.captureScreenshot", {
      format: "png",
      fromSurface: true,
      captureBeyondViewport: false,
      clip,
    });
    writeFileSync(join(outputDir, fileName), Buffer.from(result.data, "base64"));
    addBlackBorder(join(outputDir, fileName));
    console.log(fileName);
  }

  async function submitInvalidLogin() {
    await navigate("/login");
    const loaded = cdp.once("Page.loadEventFired");
    await evaluate(`
      document.querySelector('input[name="empNo"]').value = '10001';
      document.querySelector('input[name="password"]').value = 'password1';
      document.querySelector('form').requestSubmit();
    `);
    await loaded;
    await wait(900);
  }

  async function login(empNo) {
    await navigate("/login");
    const loaded = cdp.once("Page.loadEventFired");
    await evaluate(`
      document.querySelector('input[name="empNo"]').value = '${empNo}';
      document.querySelector('input[name="password"]').value = 'password';
      document.querySelector('form').requestSubmit();
    `);
    await loaded;
    await wait(900);
  }

  async function logout() {
    await evaluate(`fetch('/logout', { method: 'POST' })`);
    await wait(500);
  }

  async function applyPhaseView(phase) {
    await evaluate(`
      (() => {
        const hideColumn = (label) => {
          const headers = [...document.querySelectorAll('table thead th')];
          const index = headers.findIndex((th) => th.textContent.trim() === label);
          if (index < 0) return;
          document.querySelectorAll('table tr').forEach((tr) => {
            const cell = tr.children[index];
            if (cell) cell.style.display = 'none';
          });
        };
        const hideMenu = (label) => {
          [...document.querySelectorAll('nav a')].forEach((a) => {
            if (a.textContent.includes(label)) {
              a.closest('li').style.display = 'none';
            }
          });
        };
        const phase = '${phase}';
        if (phase === 'designOnly') {
          hideColumn('権限');
          hideColumn('状態');
          hideMenu('退職者管理');
          document.querySelector('.navbar .ms-auto').style.visibility = 'hidden';
        }
        if (phase === 'afterRetiree') {
          hideColumn('権限');
        }
      })()
    `);
    await wait(300);
  }

  await navigate("/login");
  await screenshot("AF01_login.png");
  await submitInvalidLogin();
  await screenshot("AF01_login_error.png");

  await login(10001);
  await navigate("/index");
  await screenshotSelector("AF11_header_logout.png", ".navbar", 0);

  await navigate("/employeeList");
  await applyPhaseView("designOnly");
  await screenshot("AF20_design_only_list.png");

  await navigate("/employeeList");
  await applyPhaseView("afterRetiree");
  await screenshot("AF23_after_retiree_list.png");

  await navigate("/employeeList");
  await screenshot("AF24_after_role_list.png");

  await navigate("/employeeList");
  await navigate("/retireeList");
  await screenshot("AF03_retiree_list.png");
  await navigate("/detail/10003");
  await screenshot("AF05_detail_admin_other.png");
  await screenshot("AF25_after_detail.png");
  await navigate("/detail/10001");
  await screenshot("AF05_detail_admin_self.png");

  await logout();
  await login(10003);
  await navigate("/index");
  await screenshot("AF04_top_general.png");
  await navigate("/detail/10001");
  await screenshot("AF04_detail_general_other.png");

  cdp.close();
}

main()
  .finally(async () => {
    if (dbPrepared) {
      setDeleteFlg(10002, 0);
    }
    chrome.kill();
    await wait(1000);
    try {
      rmSync(userDataDir, { recursive: true, force: true, maxRetries: 5, retryDelay: 300 });
    } catch {
      // Chromeの終了直後はプロファイルがロックされることがあるため、削除失敗は無視する。
    }
  });
