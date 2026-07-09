import { spawn } from "node:child_process";
import { mkdirSync, rmSync, writeFileSync } from "node:fs";
import { request } from "node:http";
import { join } from "node:path";
import { setTimeout as wait } from "node:timers/promises";

const chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
const baseUrl = "http://localhost:8080";
const port = 9222;
const outputDir = "C:\\work\\NewEIMS\\NewEIMS\\追加機能の設計書\\images";
const userDataDir = "C:\\work\\NewEIMS\\NewEIMS\\.tmp-additional-screenshot-profile";

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
  "--window-size=1366,900",
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
  const wsUrl = await waitForChrome();
  const cdp = await connectCdp(wsUrl);

  await cdp.send("Page.enable");
  await cdp.send("Runtime.enable");
  await cdp.send("Emulation.setDeviceMetricsOverride", {
    width: 1366,
    height: 900,
    deviceScaleFactor: 1.25,
    mobile: false,
  });

  async function navigate(path) {
    const loaded = cdp.once("Page.loadEventFired");
    await cdp.send("Page.navigate", { url: `${baseUrl}${path}` });
    await loaded;
    await wait(900);
  }

  async function evaluate(expression) {
    await cdp.send("Runtime.evaluate", { expression, awaitPromise: true });
  }

  async function screenshot(fileName) {
    const result = await cdp.send("Page.captureScreenshot", {
      format: "png",
      fromSurface: true,
      captureBeyondViewport: false,
    });
    writeFileSync(join(outputDir, fileName), Buffer.from(result.data, "base64"));
    console.log(fileName);
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

  await navigate("/login");
  await screenshot("AF01_login.png");

  await login(10001);
  await navigate("/index");
  await screenshot("AF02_top_admin.png");
  await navigate("/employeeList");
  await screenshot("AF02_employee_list.png");
  await navigate("/selectByDeptNo?deptNo=100");
  await screenshot("AF02_search_result.png");
  await navigate("/retireeList");
  await screenshot("AF03_retiree_list.png");
  await navigate("/detail/10003");
  await screenshot("AF05_detail_admin_other.png");
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
  .finally(() => {
    chrome.kill();
    try {
      rmSync(userDataDir, { recursive: true, force: true, maxRetries: 5, retryDelay: 300 });
    } catch {
      // Chromeの終了直後はプロファイルがロックされることがあるため、削除失敗は無視する。
    }
  });
