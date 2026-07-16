import fs from 'node:fs';
import path from 'node:path';
import type { FullConfig, FullResult, Reporter, TestCase, TestResult } from '@playwright/test/reporter';
import { maintenanceTestCases } from './maintenance-test-cases';
import { searchTestCases, type EimsTestCase, type FeatureId } from './test-cases';

const allTestCases = [...searchTestCases, ...maintenanceTestCases];
const testCaseMap = new Map(allTestCases.map((testCase) => [`${testCase.feature}:${testCase.id}`, testCase]));
const featureLabels: Record<FeatureId, string> = {
  search: '検索機能', registration: '登録機能', update: '更新機能', delete: '削除機能',
};
const requestedFeature = process.env.EIMS_FEATURE ?? 'all';
const selectedFeature = requestedFeature === 'all' || requestedFeature in featureLabels ? requestedFeature : 'all';
const selectedTestCases = selectedFeature === 'all'
  ? allTestCases
  : allTestCases.filter((testCase) => testCase.feature === selectedFeature);
const assessmentLabel = selectedFeature === 'all' ? '検索・登録・更新・削除' : featureLabels[selectedFeature as FeatureId];

type CaseResult = {
  definition: EimsTestCase;
  status: 'passed' | 'failed' | 'skipped' | 'notRun';
  duration: number;
  error: string;
  screenshot?: string;
};

const ansiPattern = new RegExp(`${String.fromCharCode(27)}\\[[0-9;]*m`, 'g');

function escapeHtml(value: unknown): string {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function imageDataUrl(filePath: string | undefined): string | undefined {
  if (!filePath || !fs.existsSync(filePath)) return undefined;
  return `data:image/png;base64,${fs.readFileSync(filePath).toString('base64')}`;
}

function causedByOnly(value: string): string {
  const causes = value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => /^Caused by:/i.test(line))
    .filter((line, index, lines) => lines.indexOf(line) === index);
  return causes.slice(-8).join('\n');
}

function springBootLog(): string {
  return ['spring-boot.out.log', 'spring-boot.err.log']
    .map((fileName) => path.resolve(process.cwd(), 'results', fileName))
    .filter((filePath) => fs.existsSync(filePath))
    .map((filePath) => fs.readFileSync(filePath, 'utf8'))
    .join('\n');
}

function displayStatus(result: CaseResult): { label: string; className: string; icon: string } {
  if (result.status === 'passed') return { label: '合格', className: 'pass', icon: '✓' };
  if (result.status === 'skipped') return { label: '対象外', className: 'skip', icon: '−' };
  if (result.status === 'notRun') return { label: '未実施', className: 'skip', icon: '−' };
  if (result.definition.level === 'standard') return { label: '一般仕様未達', className: 'warning', icon: '!' };
  if (result.definition.level === 'reference') return { label: '参考確認', className: 'reference', icon: 'i' };
  return { label: '要修正', className: 'fail', icon: '×' };
}

function levelLabel(level: EimsTestCase['level']): string {
  if (level === 'common') return '簡易実装';
  if (level === 'standard') return '一般仕様';
  return '参考確認';
}

class EimsReporter implements Reporter {
  private startedAt = new Date();
  private results = new Map<string, CaseResult>();
  private outputFile = path.resolve(process.cwd(), 'results', 'eims-report.html');
  private completedCount = 0;
  private logOffsets = new Map<string, number>();

  private sendProgress(payload: Record<string, unknown>): void {
    console.log(`@@EIMS@@${JSON.stringify(payload)}`);
  }

  onBegin(_config: FullConfig): void {
    this.startedAt = new Date();
    this.completedCount = 0;
    this.logOffsets.clear();
    fs.mkdirSync(path.dirname(this.outputFile), { recursive: true });
    this.sendProgress({ percent: 30, phase: assessmentLabel, message: `全${selectedTestCases.length}件の確認を開始します。`, completed: 0, total: selectedTestCases.length });
  }

  onTestBegin(test: TestCase): void {
    this.logOffsets.set(test.id, springBootLog().length);
    const match = test.title.match(/\[(search|registration|update|delete):(TC\d{3})\]/);
    const definition = match ? testCaseMap.get(`${match[1]}:${match[2]}`) : undefined;
    if (!definition) return;

    this.sendProgress({
      percent: 30 + Math.round((this.completedCount / selectedTestCases.length) * 65),
      phase: featureLabels[definition.feature],
      message: definition.title,
      current: this.completedCount + 1,
      completed: this.completedCount,
      total: selectedTestCases.length,
    });
  }

  onTestEnd(test: TestCase, result: TestResult): void {
    const match = test.title.match(/\[(search|registration|update|delete):(TC\d{3})\]/);
    const definition = match ? testCaseMap.get(`${match[1]}:${match[2]}`) : undefined;
    if (!definition) return;

    const screenshot = result.attachments.find((attachment) => attachment.contentType === 'image/png');
    const status = result.status === 'passed'
      ? 'passed'
      : result.status === 'skipped'
        ? 'skipped'
        : 'failed';
    const log = springBootLog();
    const logOffset = this.logOffsets.get(test.id) ?? log.length;
    this.logOffsets.delete(test.id);

    this.results.set(`${definition.feature}:${definition.id}`, {
      definition,
      status,
      duration: result.duration,
      error: status === 'failed'
        ? causedByOnly([
          result.errors.map((error) => error.message ?? error.value ?? '').join('\n'),
          log.slice(logOffset),
        ].join('\n').replace(ansiPattern, ''))
        : '',
      screenshot: imageDataUrl(screenshot?.path),
    });

    this.completedCount += 1;
    this.sendProgress({
      percent: 30 + Math.round((this.completedCount / selectedTestCases.length) * 65),
      phase: featureLabels[definition.feature],
      message: definition.title,
      completed: this.completedCount,
      total: selectedTestCases.length,
      result: status,
    });
  }

  async onEnd(_result: FullResult): Promise<{ status: FullResult['status'] }> {
    if (this.completedCount === 0) return { status: 'passed' };

    const allResults = selectedTestCases.map((definition) => this.results.get(`${definition.feature}:${definition.id}`) ?? ({
      definition,
      status: 'notRun' as const,
      duration: 0,
      error: '',
    }));

    fs.writeFileSync(this.outputFile, this.buildHtml(allResults), 'utf8');
    this.sendProgress({ percent: 98, phase: '結果作成', message: 'HTML結果報告書を作成しました。', completed: selectedTestCases.length, total: selectedTestCases.length });

    const commonFailed = allResults.some((item) => item.definition.level === 'common' && item.status !== 'passed');
    return { status: commonFailed ? 'failed' : 'passed' };
  }

  private buildHtml(results: CaseResult[]): string {
    const subject = selectedFeature === 'all' ? '共通機能' : assessmentLabel;
    const common = results.filter((item) => item.definition.level === 'common');
    const standard = results.filter((item) => item.definition.level === 'standard');
    const reference = results.filter((item) => item.definition.level === 'reference');
    const simplifiedPassed = common.filter((item) => item.status === 'passed').length;
    const standardPassed = standard.filter((item) => item.status === 'passed').length;
    const referencePassed = reference.filter((item) => item.status === 'passed').length;
    const simplifiedComplete = simplifiedPassed === common.length;
    const standardComplete = standardPassed === standard.length;
    const overall = simplifiedComplete && standardComplete
      ? { label: '一般仕様達成', className: 'pass', description: `${subject}は一般仕様まで実装できています。` }
      : simplifiedComplete
        ? { label: '簡易実装達成', className: 'warning', description: `難易度を抑えた${subject}として完成しています。黄色の項目へ進むと一般仕様を目指せます。` }
        : { label: '要確認', className: 'fail', description: `簡易実装として必要な${subject}に問題があります。赤い項目から確認してください。` };
    const elapsed = Math.max(0, Date.now() - this.startedAt.getTime());
    const updatedAt = new Intl.DateTimeFormat('ja-JP', { dateStyle: 'long', timeStyle: 'medium' }).format(new Date());
    const featureButtons = selectedFeature === 'all'
      ? '<button data-filter="search">検索</button><button data-filter="registration">登録</button><button data-filter="update">更新</button><button data-filter="delete">削除</button>'
      : '';

    const screenshotCounts = new Map<string, number>();
    results.forEach((item) => {
      if (item.screenshot) screenshotCounts.set(item.screenshot, (screenshotCounts.get(item.screenshot) ?? 0) + 1);
    });

    const cards = results.map((item) => {
      const status = displayStatus(item);
      const detailOpen = item.status === 'failed' ? ' open' : '';
      const errorBlock = item.error
        ? `<details class="technical"><summary>技術的な詳細を見る</summary><pre>${escapeHtml(item.error)}</pre></details>`
        : '';
      const sameScreenshotCount = item.screenshot ? screenshotCounts.get(item.screenshot) ?? 1 : 0;
      const duplicateScreenshotNote = sameScreenshotCount > 1
        ? `<p class="duplicate-note">この画面と同じ状態で停止した確認が、全部で${sameScreenshotCount}件あります。画像の使い回しではなく、異なる操作が同じ画面で失敗した結果です。</p>`
        : '';
      const screenshot = item.screenshot
        ? `<div class="screenshot">
            <p>問題が発生したときの画面</p>
            <div class="shot-context"><strong>この画像の確認内容</strong><span>${escapeHtml(item.definition.input)} → ${escapeHtml(item.definition.expected)}</span></div>
            <a class="shot-frame" href="${item.screenshot}" target="_blank">
              <strong class="shot-label">確認${escapeHtml(item.definition.id.slice(3))}・${escapeHtml(item.definition.title)}</strong>
              <img src="${item.screenshot}" alt="${escapeHtml(item.definition.id)}の失敗時スクリーンショット">
            </a>
            ${duplicateScreenshotNote}
          </div>`
        : '';
      const hints = item.definition.hints.map((hint) => `<li>${escapeHtml(hint)}</li>`).join('');
      return `
        <article class="case ${status.className}" data-status="${status.className}" data-level="${item.definition.level}" data-feature="${item.definition.feature}">
          <details${detailOpen}>
            <summary>
              <span class="status-icon">${status.icon}</span>
              <span class="case-title"><small>${featureLabels[item.definition.feature]}・確認${escapeHtml(item.definition.id.slice(3))}・${levelLabel(item.definition.level)}</small>${escapeHtml(item.definition.title)}</span>
              <span class="status-label">${status.label}</span>
            </summary>
            <div class="case-body">
              <div class="explanation"><span>確認内容</span><p>${escapeHtml(item.definition.description)}</p></div>
              <div class="two-column">
                <div><span>入力・操作</span><p>${escapeHtml(item.definition.input)}</p></div>
                <div><span>期待する結果</span><p>${escapeHtml(item.definition.expected)}</p></div>
              </div>
              <div class="hint"><strong>確認する場所の候補</strong><ul>${hints}</ul></div>
              <p class="note">仕様書との対応：${escapeHtml(item.definition.note)}</p>
              ${screenshot}${errorBlock}
            </div>
          </details>
        </article>`;
    }).join('');

    return `<!doctype html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>EIMS 共通機能 実装診断レポート</title>
  <style>
    :root { --navy:#17324d; --blue:#2563eb; --green:#17834f; --amber:#b66b00; --red:#c43232; --muted:#607080; --line:#d9e2ea; --bg:#f4f7fa; }
    * { box-sizing:border-box; }
    body { margin:0; color:#1f2937; background:var(--bg); font-family:"Yu Gothic UI","Meiryo",system-ui,sans-serif; line-height:1.7; }
    header { color:white; background:linear-gradient(135deg,#102b45,#285d8d); padding:42px 24px 76px; }
    header .inner, main { width:min(1120px,calc(100% - 32px)); margin:auto; }
    header p { margin:8px 0 0; color:#dbeafe; }
    h1 { margin:0; font-size:clamp(25px,4vw,38px); letter-spacing:.02em; }
    main { margin-top:-48px; padding-bottom:60px; }
    .hero { display:grid; grid-template-columns:1.25fr 2fr; gap:20px; margin-bottom:22px; }
    .panel { background:white; border:1px solid #e5ebf0; border-radius:18px; box-shadow:0 10px 30px #17324d16; }
    .overall { padding:28px; display:flex; flex-direction:column; justify-content:center; }
    .overall .badge { align-self:flex-start; font-size:24px; font-weight:800; border-radius:999px; padding:8px 18px; }
    .overall.pass .badge { color:#086b3d; background:#dcfce7; }
    .overall.warning .badge { color:#8a4b00; background:#fff2c7; }
    .overall.fail .badge { color:#a51d1d; background:#fee2e2; }
    .overall p { margin:14px 0 0; color:#4b5d6d; }
    .scores { padding:22px; display:grid; grid-template-columns:repeat(3,1fr); gap:14px; }
    .score { border-radius:14px; padding:18px; background:#f8fafc; border:1px solid var(--line); }
    .score strong { display:block; font-size:27px; color:var(--navy); }
    .score span { font-size:13px; color:var(--muted); }
    .bar { height:7px; background:#dfe7ee; border-radius:99px; overflow:hidden; margin-top:12px; }
    .bar i { display:block; height:100%; background:var(--blue); }
    .toolbar { display:flex; flex-wrap:wrap; align-items:center; gap:9px; padding:15px 18px; margin-bottom:15px; }
    button { border:1px solid var(--line); background:white; color:#334155; border-radius:999px; padding:8px 14px; cursor:pointer; font-weight:700; }
    button:hover, button.active { color:white; border-color:var(--blue); background:var(--blue); }
    .toolbar .updated { margin-left:auto; color:var(--muted); font-size:13px; }
    .case { background:white; border:1px solid var(--line); border-left:6px solid var(--line); border-radius:14px; margin:10px 0; box-shadow:0 3px 12px #17324d0a; overflow:hidden; }
    .case.pass { border-left-color:var(--green); } .case.warning { border-left-color:var(--amber); }
    .case.fail { border-left-color:var(--red); } .case.reference { border-left-color:#64748b; }
    summary { display:flex; align-items:center; gap:14px; list-style:none; cursor:pointer; padding:16px 18px; }
    summary::-webkit-details-marker { display:none; }
    .status-icon { display:grid; place-items:center; width:32px; height:32px; flex:none; border-radius:50%; color:white; font-weight:900; background:#64748b; }
    .pass .status-icon { background:var(--green); } .warning .status-icon { background:var(--amber); }
    .fail .status-icon { background:var(--red); }
    .case-title { display:flex; flex-direction:column; font-weight:800; }
    .case-title small { font-size:12px; color:var(--muted); font-weight:600; }
    .status-label { margin-left:auto; font-size:13px; font-weight:800; border-radius:999px; padding:5px 10px; background:#edf2f7; white-space:nowrap; }
    .case-body { border-top:1px solid #edf1f4; padding:20px 24px 24px 70px; }
    .case-body span { display:block; color:var(--muted); font-size:12px; font-weight:800; letter-spacing:.04em; }
    .case-body p { margin:2px 0 14px; }
    .two-column { display:grid; grid-template-columns:1fr 1fr; gap:18px; }
    .two-column > div { padding:14px 16px; background:#f8fafc; border-radius:10px; }
    .hint { margin-top:16px; padding:14px 18px; background:#eff6ff; border-radius:10px; color:#244664; }
    .hint ul { margin:5px 0 0; padding-left:20px; }
    .note { margin-top:14px!important; color:var(--muted); font-size:13px; }
    .screenshot { margin-top:18px; padding:14px; background:#111827; border-radius:12px; }
    .screenshot p { color:white; margin:0 0 9px; font-weight:700; }
    .shot-context { display:flex; gap:10px; align-items:baseline; margin:0 0 10px; padding:10px 12px; color:#dbeafe; background:#1e3a5f; border-radius:8px; }
    .shot-context strong { flex:none; color:white; }
    .shot-context span { color:#dbeafe; font-size:13px; }
    .shot-frame { position:relative; display:block; }
    .shot-label { position:absolute; z-index:1; top:10px; left:10px; max-width:calc(100% - 20px); padding:7px 11px; color:white; background:#b42318e8; border-radius:7px; box-shadow:0 2px 8px #0008; }
    .screenshot img { display:block; max-width:100%; max-height:560px; margin:auto; border-radius:7px; background:white; }
    .screenshot .duplicate-note { margin:10px 0 0; padding:9px 12px; color:#fde68a; background:#422006; border-radius:8px; font-size:13px; font-weight:600; }
    .technical { margin-top:14px; padding:10px 14px; background:#fff5f5; border-radius:9px; }
    .technical summary { padding:0; font-size:13px; color:#8b2525; font-weight:700; }
    pre { overflow:auto; white-space:pre-wrap; font:12px/1.6 Consolas,monospace; }
    .legend { color:var(--muted); font-size:13px; text-align:center; margin:22px 0; }
    [hidden] { display:none!important; }
    @media (max-width:760px) { .hero,.two-column { grid-template-columns:1fr; } .scores { grid-template-columns:1fr; } .case-body { padding:18px; } .toolbar .updated { width:100%; margin-left:0; } }
    @media print { header { padding-bottom:30px; } main { margin-top:0; } .toolbar { display:none; } details { display:block; } }
  </style>
</head>
<body>
  <header><div class="inner"><h1>EIMS 共通機能 実装診断レポート</h1><p>${assessmentLabel}</p></div></header>
  <main>
    <section class="hero">
      <div class="panel overall ${overall.className}"><span class="badge">${overall.label}</span><p>${overall.description}</p></div>
      <div class="panel scores">
        ${this.scoreCard('簡易実装', simplifiedPassed, common.length)}
        ${this.scoreCard('一般仕様', standardPassed, standard.length)}
        ${this.scoreCard('参考確認', referencePassed, reference.length)}
      </div>
    </section>
    <section class="panel toolbar">
      <button class="active" data-filter="all">すべて</button>
      <button data-filter="problem">確認が必要</button>
      <button data-filter="common">簡易実装</button>
      <button data-filter="standard">一般仕様</button>
      <button data-filter="reference">参考確認</button>
      ${featureButtons}
      <span class="updated">${escapeHtml(updatedAt)}・${(elapsed / 1000).toFixed(1)}秒</span>
    </section>
    <section id="cases">${cards}</section>
    <p class="legend">簡易実装を達成したあと、黄色の項目へ進むと一般仕様を目指せます。参考確認は総合判定に影響しません。</p>
  </main>
  <script>
    const buttons = document.querySelectorAll('[data-filter]');
    const cases = document.querySelectorAll('.case');
    buttons.forEach((button) => button.addEventListener('click', () => {
      buttons.forEach((item) => item.classList.remove('active'));
      button.classList.add('active');
      const filter = button.dataset.filter;
      cases.forEach((item) => {
        const problem = ['fail','warning','reference'].includes(item.dataset.status);
        item.hidden = !(filter === 'all' || item.dataset.level === filter || item.dataset.feature === filter || (filter === 'problem' && problem));
      });
    }));
  </script>
</body>
</html>`;
  }

  private scoreCard(label: string, passed: number, total: number): string {
    const percent = total === 0 ? 0 : Math.round((passed / total) * 100);
    return `<div class="score"><span>${label}</span><strong>${passed} / ${total}</strong><div class="bar"><i style="width:${percent}%"></i></div></div>`;
  }
}

export default EimsReporter;
