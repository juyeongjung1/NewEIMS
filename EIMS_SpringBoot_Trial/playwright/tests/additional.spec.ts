import { expect, Page, test } from '@playwright/test';
import { additionalTestCaseMap } from '../additional-test-cases';

function title(id: string): string {
  const definition = additionalTestCaseMap.get(id);
  if (!definition) throw new Error(`${id} が定義されていません。`);
  return `[additional:${id}] ${definition.title}`;
}

async function login(page: Page, empNo: string, password = 'password'): Promise<void> {
  await page.goto('/login');
  await page.locator('[name="empNo"]').fill(empNo);
  await page.locator('[name="password"]').fill(password);
  await page.getByRole('button', { name: 'ログイン' }).click();
  await page.waitForLoadState('domcontentloaded');
}

async function loginAdmin(page: Page): Promise<void> {
  await login(page, '10001');
  await expect(page, '管理者でログインできませんでした。').toHaveURL(/\/index(?:;jsessionid=[^/?#]+)?$/i);
}

async function loginGeneral(page: Page): Promise<void> {
  await login(page, '10002');
  await expect(page, '一般ユーザーでログインできませんでした。').toHaveURL(/\/index(?:;jsessionid=[^/?#]+)?$/i);
}

function sidebar(page: Page) {
  return page.locator('nav').filter({ has: page.getByRole('link', { name: /トップページ/ }) });
}

function actionArea(page: Page) {
  return page.locator('main');
}

async function expectCommonLayout(page: Page): Promise<void> {
  await expect(page.locator('nav.navbar'), '共通ヘッダーが表示されていません。').toBeVisible();
  await expect(sidebar(page), '共通サイドバーが表示されていません。').toBeVisible();
  await expect(page.locator('main'), 'メイン領域が表示されていません。').toBeVisible();
}

async function registerDiagnosticEmployee(page: Page, suffix: string): Promise<{ empNo: string; lastName: string }> {
  const lastName = `診断追加${suffix}`;
  await page.goto('/input');
  await page.locator('[name="lastName"]').fill(lastName);
  await page.locator('[name="firstName"]').fill('太郎');
  await page.locator('[name="lastKana"]').fill(`シンダンツイカ${suffix}`);
  await page.locator('[name="firstKana"]').fill('タロウ');
  await page.locator('[name="password"]').fill('pass1234');
  await page.locator('[name="gender"][value="1"]').check();
  await page.locator('[name="deptNo"]').selectOption('100');
  await page.getByRole('button', { name: /登録確認/ }).click();
  await page.getByRole('button', { name: /登録確定/ }).click();
  const body = await page.locator('body').innerText();
  const empNo = body.match(/社員番号\s*(\d+)/)?.[1];
  if (!empNo) throw new Error('診断用社員の社員番号を登録完了画面から取得できませんでした。');
  return { empNo, lastName };
}

async function retireEmployee(page: Page, empNo: string): Promise<void> {
  await loginAdmin(page);
  await page.goto(`/deleteConfirm/${empNo}`);
  await page.getByRole('button', { name: /退職処理を実行/ }).click();
  await expect(page.locator('body'), '退職処理が完了しませんでした。').toContainText(/退職処理.*完了/);
}

let lifecycle: { empNo: string; lastName: string } | undefined;
let lifecycleRetired = false;

async function ensureLifecycleEmployee(page: Page): Promise<{ empNo: string; lastName: string }> {
  if (!lifecycle) lifecycle = await registerDiagnosticEmployee(page, 'A');
  return lifecycle;
}

async function ensureLifecycleRetired(page: Page): Promise<{ empNo: string; lastName: string }> {
  const employee = await ensureLifecycleEmployee(page);
  if (!lifecycleRetired) {
    await retireEmployee(page, employee.empNo);
    lifecycleRetired = true;
  }
  return employee;
}

test(title('AT001'), async ({ page }) => {
  await page.goto('/login');
  await expect(page.locator('[name="empNo"]'), '社員番号入力欄がありません。').toBeVisible();
  await expect(page.locator('[name="password"]'), 'パスワード入力欄がありません。').toBeVisible();
  await expect(page.getByRole('button', { name: 'ログイン' }), 'ログインボタンがありません。').toBeVisible();
});

test(title('AT002'), async ({ page }) => {
  await page.goto('/login');
  await page.getByRole('button', { name: 'ログイン' }).click();
  const body = page.locator('body');
  await expect(body, '社員番号の必須エラーがありません。').toContainText(/社員番号.*入力/);
  await expect(body, 'パスワードの必須エラーがありません。').toContainText(/パスワード.*入力/);
});

test(title('AT003'), async ({ page }) => {
  await login(page, '10001', 'abc');
  await expect(page.locator('body'), 'パスワード下限のエラーがありません。').toContainText(/4文字以上.*16文字以内/);
});

test(title('AT004'), async ({ page }) => {
  await login(page, '10001', '12345678901234567');
  await expect(page.locator('body'), 'パスワード上限のエラーがありません。').toContainText(/4文字以上.*16文字以内/);
});

test(title('AT005'), async ({ page }) => {
  await login(page, '99999');
  await expect(page, '存在しない社員でログイン画面から遷移しました。').toHaveURL(/\/login$/);
  await expect(page.locator('body'), '認証失敗メッセージがありません。').toContainText(/社員番号またはパスワード.*正しくありません/);
});

test(title('AT006'), async ({ page }) => {
  await login(page, '10001', 'wrong-pass');
  await expect(page, '誤ったパスワードでログインできてしまいました。').toHaveURL(/\/login$/);
  await expect(page.locator('body'), '認証失敗メッセージがありません。').toContainText(/正しくありません/);
});

test(title('AT007'), async ({ page }) => {
  await loginAdmin(page);
  await expect(page.locator('nav.navbar'), '管理者のログイン表示がありません。').toContainText('ようこそ、');
  await expect(page.locator('nav.navbar').getByRole('link', { name: '長嶋 陽翔' }), '管理者の氏名がヘッダーに表示されていません。').toBeVisible();
});

test(title('AT008'), async ({ page }) => {
  await loginGeneral(page);
  await expect(page.locator('nav.navbar'), '一般ユーザーのログイン表示がありません。').toContainText('ようこそ、');
  await expect(page.locator('nav.navbar').getByRole('link', { name: '中田 結衣' }), '一般ユーザーの氏名がヘッダーに表示されていません。').toBeVisible();
});

test(title('AT009'), async ({ page }) => {
  await loginAdmin(page);
  await page.getByRole('link', { name: /社員情報検索/ }).click();
  await expect(page.locator('nav.navbar'), '画面遷移後にログイン情報が消えました。').toContainText('長嶋 陽翔');
  await expect(page.getByRole('button', { name: 'ログアウト' }), 'ログアウトボタンがありません。').toBeVisible();
});

test(title('AT010'), async ({ page }) => {
  await loginAdmin(page);
  await page.getByRole('button', { name: 'ログアウト' }).click();
  await expect(page, 'ログアウト後にログイン画面へ戻りません。').toHaveURL(/\/login$/);
  await expect(page.locator('body'), 'ログアウト後も氏名が表示されています。').not.toContainText('ようこそ、');
});

test(title('AT011'), async ({ page }) => {
  await page.goto('/');
  await expectCommonLayout(page);
});

test(title('AT012'), async ({ page }) => {
  for (const url of ['/search', '/input', '/detail/10002']) {
    await page.goto(url);
    await expectCommonLayout(page);
  }
});

test(title('AT013'), async ({ page }) => {
  await page.goto('/selectByEmpName?keyword=田');
  await expect(page.locator('table thead'), '検索結果の表見出しがありません。').toBeVisible();
  expect(await page.locator('table tbody tr').count(), '検索結果が表に表示されていません。').toBeGreaterThan(0);
});

test(title('AT014'), async ({ page }) => {
  await page.goto('/selectByEmpName?keyword=中田');
  const image = page.locator('img[src$="/images/10002.png"]');
  await expect(image, '社員10002の顔写真が検索結果にありません。').toBeVisible();
  expect(await image.evaluate((img: HTMLImageElement) => img.naturalWidth), '顔写真ファイルを読み込めません。').toBeGreaterThan(0);
});

test(title('AT015'), async ({ page }) => {
  await loginAdmin(page);
  await page.goto('/employeeList');
  const rows = page.locator('table tbody tr');
  expect(await rows.count(), '社員一覧に社員が表示されていません。').toBeGreaterThan(0);
  expect(await rows.locator('img[src*="/images/"]').count(), '各社員行に顔写真がありません。').toBe(await rows.count());
});

test(title('AT016'), async ({ page }) => {
  await loginAdmin(page);
  await page.goto('/employeeList');
  const box = await page.locator('table tbody img').first().boundingBox();
  expect(box?.width ?? 0, '一覧の顔写真幅が約60pxではありません。').toBeGreaterThanOrEqual(55);
  expect(box?.width ?? 999, '一覧の顔写真幅が約60pxではありません。').toBeLessThanOrEqual(70);
  expect(box?.height ?? 0, '一覧の顔写真高さが約70pxではありません。').toBeGreaterThanOrEqual(65);
  expect(box?.height ?? 999, '一覧の顔写真高さが約70pxではありません。').toBeLessThanOrEqual(80);
});

test(title('AT017'), async ({ page }) => {
  await page.goto('/detail/10002');
  const image = page.locator('img[src$="/images/10002.png"]');
  await expect(image, '社員10002の顔写真が詳細画面にありません。').toBeVisible();
  expect(await image.evaluate((img: HTMLImageElement) => img.naturalWidth)).toBeGreaterThan(0);
});

test(title('AT018'), async ({ page }) => {
  await page.goto('/selectByEmpName?keyword=田');
  const headers = (await page.locator('table thead th').allTextContents()).map((text) => text.trim());
  for (const label of ['顔写真', '社員番号', '氏名', '部署', '権限', '状態']) {
    expect(headers, `検索結果に「${label}」列がありません。`).toContain(label);
  }
});

test(title('AT019'), async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/selectByEmpName?keyword=田');
  const responsive = page.locator('.table-responsive');
  await expect(responsive, '表を囲むレスポンシブ領域がありません。').toBeVisible();
  const dimensions = await responsive.evaluate((element) => ({ client: element.clientWidth, scroll: element.scrollWidth }));
  expect(dimensions.scroll, '狭い画面で表の内容を保持できていません。').toBeGreaterThanOrEqual(dimensions.client);
  expect(await page.evaluate(() => document.documentElement.scrollWidth), 'ページ全体が画面幅から大きくはみ出しています。').toBeLessThanOrEqual(410);
});

test(title('AT020'), async ({ page }) => {
  await loginAdmin(page);
  await page.goto('/retireeList');
  await expectCommonLayout(page);
  await expect(page.getByRole('heading', { name: /退職者管理/ }), '退職者管理のメイン領域がありません。').toBeVisible();
});

test(title('AT021'), async ({ page }) => {
  await loginAdmin(page);
  await expect(sidebar(page).getByRole('link', { name: /全社員一覧/ }), '管理者に全社員一覧メニューがありません。').toBeVisible();
  await expect(sidebar(page).getByRole('link', { name: /退職者管理/ }), '管理者に退職者管理メニューがありません。').toBeVisible();
});

test(title('AT022'), async ({ page }) => {
  await loginGeneral(page);
  await expect(sidebar(page).getByRole('link', { name: /全社員一覧/ }), '一般ユーザーに全社員一覧が表示されています。').toHaveCount(0);
  await expect(sidebar(page).getByRole('link', { name: /退職者管理/ }), '一般ユーザーに退職者管理が表示されています。').toHaveCount(0);
});

test(title('AT023'), async ({ page }) => {
  await page.goto('/');
  await expect(sidebar(page).getByRole('link', { name: /全社員一覧|退職者管理/ }), '未ログイン時に管理者メニューが表示されています。').toHaveCount(0);
});

test(title('AT024'), async ({ page }) => {
  await loginGeneral(page);
  await page.locator('nav.navbar').getByRole('link', { name: '中田 結衣' }).click();
  await expect(page, 'ログイン中社員本人の詳細へ遷移していません。').toHaveURL(/\/detail\/10002$/);
});

test(title('AT025'), async ({ page }) => {
  await page.goto('/detail/10001');
  await expect(page.locator('main'), '社員10001の権限が管理者と表示されていません。').toContainText('管理者');
});

test(title('AT026'), async ({ page }) => {
  await page.goto('/detail/10002');
  await expect(page.locator('main'), '社員10002の権限が一般ユーザーと表示されていません。').toContainText('一般ユーザー');
});

test(title('AT027'), async ({ page }) => {
  await page.goto('/detail/10002');
  const row = page.locator('tr').filter({ hasText: '退職状態' });
  await expect(row, '在籍社員の退職状態が「在籍」ではありません。').toContainText('在籍');
});

test(title('AT028'), async ({ page }) => {
  await loginAdmin(page);
  await page.goto('/detail/10001');
  await expect(actionArea(page).getByRole('link', { name: '変更' }), '管理者本人に変更ボタンがありません。').toBeVisible();
  await expect(actionArea(page).getByRole('link', { name: /退職処理/ }), '管理者本人に退職処理が表示されています。').toHaveCount(0);
});

test(title('AT029'), async ({ page }) => {
  await loginAdmin(page);
  await page.goto('/detail/10002');
  await expect(actionArea(page).getByRole('link', { name: '変更' }), '管理者に他社員の変更ボタンがありません。').toBeVisible();
  await expect(actionArea(page).getByRole('link', { name: /退職処理/ }), '管理者に他社員の退職処理がありません。').toBeVisible();
});

test(title('AT030'), async ({ page }) => {
  await loginGeneral(page);
  await page.goto('/detail/10002');
  await expect(actionArea(page).getByRole('link', { name: '変更' }), '一般ユーザー本人に変更ボタンがありません。').toBeVisible();
  await expect(actionArea(page).getByRole('link', { name: /退職処理/ }), '一般ユーザー本人に退職処理が表示されています。').toHaveCount(0);
});

test(title('AT031'), async ({ page }) => {
  await loginGeneral(page);
  await page.goto('/detail/10003');
  await expect(actionArea(page).getByRole('link', { name: '変更' }), '一般ユーザーに他社員の変更が表示されています。').toHaveCount(0);
  await expect(actionArea(page).getByRole('link', { name: /退職処理/ }), '一般ユーザーに他社員の退職処理が表示されています。').toHaveCount(0);
});

test(title('AT032'), async ({ page }) => {
  await page.goto('/detail/10002');
  await expect(actionArea(page).getByRole('link', { name: '変更' }), '未ログイン時に変更が表示されています。').toHaveCount(0);
  await expect(actionArea(page).getByRole('link', { name: /退職処理/ }), '未ログイン時に退職処理が表示されています。').toHaveCount(0);
});

test(title('AT033'), async ({ page }) => {
  await page.goto('/input');
  await expect(page.locator('[name="role"], [name="deleteFlg"]'), '登録画面に権限または退職状態の入力欄があります。').toHaveCount(0);
  await loginGeneral(page);
  await page.goto('/changeInput/10002');
  await expect(page.locator('[name="role"], [name="deleteFlg"]'), '変更画面に権限または退職状態の入力欄があります。').toHaveCount(0);
});

test(title('AT034'), async ({ page }) => {
  const employee = await ensureLifecycleEmployee(page);
  await page.goto(`/detail/${employee.empNo}`);
  await expect(page.locator('main'), '新規社員が一般ユーザーになっていません。').toContainText('一般ユーザー');
  await expect(page.locator('tr').filter({ hasText: '退職状態' }), '新規社員が在籍になっていません。').toContainText('在籍');
});

test(title('AT035'), async ({ page }) => {
  const employee = await ensureLifecycleEmployee(page);
  await loginAdmin(page);
  await page.goto(`/detail/${employee.empNo}`);
  await page.getByRole('link', { name: /退職処理/ }).click();
  await expect(page, '退職処理確認画面のURLへ遷移していません。').toHaveURL(new RegExp(`/deleteConfirm/${employee.empNo}$`));
  await expect(page.getByRole('heading', { name: /退職処理.*確認/ }), '退職処理確認画面が表示されていません。').toBeVisible();
});

test(title('AT036'), async ({ page }) => {
  const employee = await ensureLifecycleEmployee(page);
  await retireEmployee(page, employee.empNo);
  lifecycleRetired = true;
  await expect(page.locator('body'), '退職処理完了画面が表示されていません。').toContainText(/退職処理.*完了/);
});

test(title('AT037'), async ({ page }) => {
  const employee = await ensureLifecycleRetired(page);
  await page.goto(`/selectByEmpName?keyword=${encodeURIComponent(employee.lastName)}`);
  await expect(page.locator('main'), '通常検索に退職者が表示されています。').not.toContainText(employee.empNo);
  await expect(page.locator('main'), '退職者除外後の0件表示がありません。').toContainText(/存在しません|見つかりません/);
});

test(title('AT038'), async ({ page }) => {
  const employee = await ensureLifecycleRetired(page);
  await loginAdmin(page);
  await page.goto('/employeeList');
  await expect(page.locator('table tbody'), '全社員一覧に退職者が表示されています。').not.toContainText(employee.empNo);
});

test(title('AT039'), async ({ page }) => {
  const employee = await ensureLifecycleRetired(page);
  await loginAdmin(page);
  await page.goto('/retireeList');
  await expect(page.locator('table tbody'), '退職者一覧に対象社員が表示されていません。').toContainText(employee.empNo);
});

test(title('AT040'), async ({ page }) => {
  const employee = await ensureLifecycleRetired(page);
  await page.goto(`/detail/${employee.empNo}`);
  await expect(page.locator('tr').filter({ hasText: '退職状態' }), '退職者の状態が「退職」ではありません。').toContainText('退職');
});

test(title('AT041'), async ({ page }) => {
  const employee = await ensureLifecycleRetired(page);
  await login(page, employee.empNo, 'pass1234');
  await expect(page, '退職者がログインできてしまいました。').toHaveURL(/\/login$/);
  await expect(page.locator('body'), '退職者ログイン時の認証失敗メッセージがありません。').toContainText(/正しくありません/);
});

test(title('AT042'), async ({ page }) => {
  const employee = await ensureLifecycleRetired(page);
  await loginAdmin(page);
  await page.goto('/retireeList');
  const row = page.locator('tr').filter({ hasText: employee.empNo });
  await row.getByRole('button', { name: '復元' }).click();
  lifecycleRetired = false;
  await expect(page.locator('main'), '復元した社員が退職者一覧に残っています。').not.toContainText(employee.empNo);
});

test(title('AT043'), async ({ page }) => {
  const employee = await ensureLifecycleEmployee(page);
  await page.goto(`/selectByEmpName?keyword=${encodeURIComponent(employee.lastName)}`);
  await expect(page.locator('table tbody'), '復元した社員が通常検索に表示されません。').toContainText(employee.empNo);
});

test(title('AT044'), async ({ page }) => {
  const employee = await ensureLifecycleEmployee(page);
  await login(page, employee.empNo, 'pass1234');
  await expect(page, '復元後の社員でログインできません。').toHaveURL(/\/index(?:;jsessionid=[^/?#]+)?$/i);
  await expect(page.locator('nav.navbar'), '復元後の社員名がヘッダーに表示されません。').toContainText(employee.lastName);
});

test(title('AT045'), async ({ page }) => {
  const employee = await registerDiagnosticEmployee(page, 'B');
  await retireEmployee(page, employee.empNo);
  await page.goto('/retireeList');
  const row = page.locator('tr').filter({ hasText: employee.empNo });
  await row.getByRole('button', { name: '完全削除' }).click();
  await expect(page.locator('main'), '完全削除した社員が退職者一覧に残っています。').not.toContainText(employee.empNo);
  const response = await page.goto(`/selectByEmpNo?empNo=${employee.empNo}`);
  expect(response?.status() ?? 500, '完全削除後の検索でサーバーエラーになりました。').toBeLessThan(500);
  await expect(page.locator('main'), '完全削除した社員が検索できてしまいます。').not.toContainText(employee.lastName);
});

test(title('AT046'), async ({ page }) => {
  await loginGeneral(page);
  await expect(sidebar(page).getByRole('link', { name: /退職者管理/ }), '一般ユーザーに退職者管理メニューが表示されています。').toHaveCount(0);
  await page.goto('/detail/10003');
  await expect(page.getByRole('link', { name: /退職処理/ }), '一般ユーザーに退職処理ボタンが表示されています。').toHaveCount(0);
});

test(title('AT047'), async () => {
  test.skip(true, 'URL直接入力のサーバー側制限は、設計書で発展オプションとされているため対象外です。');
});
