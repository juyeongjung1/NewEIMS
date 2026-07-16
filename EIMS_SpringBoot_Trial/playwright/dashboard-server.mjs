import { createReadStream, existsSync, readFileSync } from 'node:fs';
import { createServer } from 'node:http';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawn } from 'node:child_process';

const root = dirname(fileURLToPath(import.meta.url));
const dashboardPath = join(root, 'dashboard.html');
const reportPath = join(root, 'results', 'eims-report.html');
const featureSettings = {
  all: { label: '検索・登録・更新・削除', total: 126 },
  search: { label: '検索機能', total: 42 },
  registration: { label: '登録機能', total: 34 },
  update: { label: '更新機能', total: 35 },
  delete: { label: '削除機能', total: 15 },
};
const requestedFeature = process.argv[2] ?? 'all';
const feature = Object.hasOwn(featureSettings, requestedFeature) ? requestedFeature : 'all';
const settings = featureSettings[feature];
const clients = new Set();
const history = [];
let running = false;
let finished = false;
let commonFailures = 0;
let standardFailures = 0;

function publish(event) {
  const normalized = { time: new Date().toISOString(), ...event };
  history.push(normalized);
  if (history.length > 300) history.shift();
  const message = `data: ${JSON.stringify(normalized)}\n\n`;
  clients.forEach((client) => client.write(message));
}

function readProgress(stream) {
  let pending = '';
  stream.setEncoding('utf8');
  stream.on('data', (chunk) => {
    pending += chunk;
    const lines = pending.split(/\r?\n/);
    pending = lines.pop() ?? '';
    for (const line of lines) {
      const marker = line.indexOf('@@EIMS@@');
      if (marker < 0) continue;
      try {
        const event = JSON.parse(line.slice(marker + 8));
        if (event.result === 'failed' && event.level === 'common') commonFailures += 1;
        if (event.result === 'failed' && event.level === 'standard') standardFailures += 1;
        publish(event);
      } catch {
        // Playwright以外の出力は画面へ表示しない。
      }
    }
  });
}

function startDiagnostics() {
  if (running || finished) return;
  running = true;
  publish({ percent: 1, phase: '開始', message: `${settings.label}の診断を開始しました。`, status: 'running', total: settings.total });

  const child = spawn('powershell.exe', [
    '-NoLogo', '-NoProfile', '-ExecutionPolicy', 'Bypass',
    '-File', join(root, 'run-search-tests.ps1'), '-NoOpenReport', '-Feature', feature,
  ], { cwd: root, windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] });

  readProgress(child.stdout);
  readProgress(child.stderr);
  child.on('error', (error) => {
    running = false;
    finished = true;
    publish({ percent: 100, phase: '起動エラー', message: error.message, status: 'failed', reportReady: existsSync(reportPath) });
  });
  child.on('exit', (code) => {
    running = false;
    finished = true;
    const status = commonFailures > 0 || (code !== 0 && standardFailures === 0)
      ? 'failed'
      : standardFailures > 0
        ? 'simplified'
        : 'completed';
    publish({
      percent: 100,
      phase: status === 'completed' ? '完了' : status === 'simplified' ? '簡易仕様達成' : '要確認',
      message: status === 'completed'
        ? '一般仕様まで、すべての確認が完了しました。'
        : status === 'simplified'
          ? '簡易仕様は達成できましたが、一般仕様には未達成の項目があります。'
          : '確認中に問題が見つかりました。結果報告書をご覧ください。',
      status,
      reportReady: existsSync(reportPath),
    });
    setTimeout(() => server.close(), 60 * 60 * 1000).unref();
  });
}

const server = createServer((request, response) => {
  const url = new URL(request.url ?? '/', 'http://127.0.0.1');
  if (request.method === 'GET' && url.pathname === '/') {
    response.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8', 'Cache-Control': 'no-store' });
    response.end(readFileSync(dashboardPath));
    return;
  }
  if (request.method === 'GET' && url.pathname === '/settings') {
    response.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
    response.end(JSON.stringify({ feature, ...settings }));
    return;
  }
  if (request.method === 'GET' && url.pathname === '/events') {
    response.writeHead(200, {
      'Content-Type': 'text/event-stream; charset=utf-8',
      'Cache-Control': 'no-cache',
      Connection: 'keep-alive',
    });
    history.forEach((event) => response.write(`data: ${JSON.stringify(event)}\n\n`));
    clients.add(response);
    request.on('close', () => clients.delete(response));
    return;
  }
  if (request.method === 'POST' && url.pathname === '/start') {
    startDiagnostics();
    response.writeHead(202, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ started: true }));
    return;
  }
  if (request.method === 'GET' && url.pathname === '/report' && existsSync(reportPath)) {
    response.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8', 'Cache-Control': 'no-store' });
    createReadStream(reportPath).pipe(response);
    return;
  }
  response.writeHead(404);
  response.end('Not found');
});

server.listen(0, '127.0.0.1', () => {
  const address = server.address();
  const port = typeof address === 'object' && address ? address.port : 0;
  const pageUrl = `http://127.0.0.1:${port}/`;
  console.log(pageUrl);
  if (process.env.EIMS_DASHBOARD_NO_OPEN !== '1') {
    spawn('powershell.exe', ['-NoLogo', '-NoProfile', '-Command', `Start-Process '${pageUrl}'`], { windowsHide: true });
  }
});
