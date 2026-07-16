import { expect, Page, test } from '@playwright/test';
import { caseTitle } from './helpers';

const noResultMessage = '検索条件に一致する社員は見つかりませんでした。';

function employeeNumberForm(page: Page) {
  return page.locator('form').filter({ has: page.locator('[name="empNo"]') });
}

function employeeNameForm(page: Page) {
  return page.locator('form').filter({ has: page.locator('[name="keyword"]') });
}

function departmentForm(page: Page) {
  return page.locator('form').filter({ has: page.locator('[name="deptNo"]') });
}

async function openSearchPage(page: Page) {
  await page.goto('/search');
  await expect(page.locator('[name="empNo"]'), '検索画面に社員番号入力欄が必要です。').toBeVisible();
  await expect(page.locator('[name="keyword"]'), '検索画面に社員名入力欄が必要です。').toBeVisible();
  await expect(page.locator('[name="deptNo"]'), '検索画面に部署入力欄が必要です。').toBeVisible();
}

async function clickSearchButton(form: ReturnType<Page['locator']>) {
  await form.getByRole('button', { name: /検索/ }).click();
  await form.page().waitForLoadState('domcontentloaded');
}

async function searchByEmployeeNumber(page: Page, value: string) {
  await openSearchPage(page);
  const form = employeeNumberForm(page);
  await form.locator('[name="empNo"]').fill(value);
  await clickSearchButton(form);
}

async function searchByName(page: Page, keyword: string) {
  await openSearchPage(page);
  const form = employeeNameForm(page);
  await form.locator('[name="keyword"]').fill(keyword);
  await clickSearchButton(form);
}

async function specifyDepartment(page: Page, deptNo: string) {
  const field = departmentForm(page).locator('[name="deptNo"]');
  if ((await field.evaluate((element) => element.tagName)) === 'SELECT') {
    await field.selectOption(deptNo);
  } else {
    await field.fill(deptNo);
  }
}

async function searchByDepartment(page: Page, deptNo: string) {
  await openSearchPage(page);
  await specifyDepartment(page, deptNo);
  await clickSearchButton(departmentForm(page));
}

async function expectSearchPage(page: Page, message: string) {
  await expect(page.locator('[name="empNo"]'), message).toBeVisible();
  await expect(page.locator('[name="keyword"]'), message).toBeVisible();
}

async function expectNoServerError(page: Page) {
  await expect(
    page.locator('body'),
    'サーバーエラー画面が表示されました。Spring Bootのログも確認してください。',
  ).not.toContainText(/Internal Server Error|Whitelabel Error Page|HTTP Status 500/i);
}

test(caseTitle('search', 'TC001'), async ({ page }) => {
  await page.goto('/');
  const searchLink = page.getByRole('link', { name: /社員.*検索|検索/ }).first();
  await expect(searchLink, 'トップページに検索画面へのリンクが見つかりません。').toBeVisible();
  await searchLink.click();
  await expectSearchPage(page, '検索リンクを押した後に検索画面が表示されませんでした。');
});

test(caseTitle('search', 'TC002'), async ({ page }) => {
  await openSearchPage(page);
  const select = departmentForm(page).locator('select[name="deptNo"]');
  await expect(select, '一般仕様では部署検索をプルダウンで表示します。').toBeVisible();
  const options = await select.locator('option').evaluateAll((elements) =>
    elements.map((element) => ({ value: (element as HTMLOptionElement).value, text: element.textContent?.trim() ?? '' })),
  );
  for (const expected of [
    { value: '100', text: '人事部' }, { value: '200', text: '経理部' },
    { value: '300', text: '営業部' }, { value: '400', text: '総務部' },
    { value: '500', text: '開発部' }, { value: '600', text: '企画部' },
  ]) {
    expect(options, `部署「${expected.text}(${expected.value})」が選択肢にありません。`).toContainEqual(expected);
  }
});

test(caseTitle('search', 'TC003'), async ({ page }) => {
  await searchByEmployeeNumber(page, '10001');
  await expect(page, '社員番号検索では詳細画面へ直接遷移します。').toHaveURL(/\/detail\/10001|\/selectByEmpNo/);
  await expect(page.locator('body'), '社員番号10001の詳細情報が表示されていません。').toContainText('10001');
  await expect(page.getByRole('heading', { name: /社員.*詳細/ }), '社員詳細画面の見出しが見つかりません。').toBeVisible();
});

test(caseTitle('search', 'TC004'), async ({ page }) => {
  await searchByEmployeeNumber(page, '99999');
  await expect(page.getByText(noResultMessage, { exact: false }), '0件メッセージが表示されていません。').toBeVisible();
});

test(caseTitle('search', 'TC005'), async ({ page }) => {
  await openSearchPage(page);
  await clickSearchButton(employeeNumberForm(page));
  await expectSearchPage(page, '社員番号が未入力のときは検索画面に留まります。');
});

test(caseTitle('search', 'TC006'), async ({ page }) => {
  await openSearchPage(page);
  const input = employeeNumberForm(page).locator('[name="empNo"]');
  try {
    await input.fill('ABC');
  } catch {
    return;
  }
  await clickSearchButton(employeeNumberForm(page));
  await expect(page, '数値以外から社員詳細画面へ遷移してはいけません。').not.toHaveURL(/\/detail\//);
  await expectNoServerError(page);
});

test(caseTitle('search', 'TC007'), async ({ page }) => {
  await searchByName(page, '田');
  const rows = page.locator('table tbody tr');
  expect(await rows.count(), '「田」の部分一致検索結果が0件でした。').toBeGreaterThan(0);
  for (const text of await rows.allTextContents()) {
    expect(text, '検索条件「田」を含まない社員が結果に混ざっています。').toContain('田');
  }
});

test(caseTitle('search', 'TC008'), async ({ page }) => {
  await searchByName(page, '陽');
  await expect(page.locator('table tbody'), '名に「陽」を含む社員が検索結果にありません。').toContainText('陽');
});

test(caseTitle('search', 'TC009'), async ({ page }) => {
  await searchByName(page, '中');
  expect(await page.locator('table tbody tr').count(), '複数件検索になるキーワードで2件以上表示されませんでした。').toBeGreaterThanOrEqual(2);
});

test(caseTitle('search', 'TC010'), async ({ page }) => {
  await searchByName(page, '存在しない文字列');
  await expect(page.getByText(noResultMessage, { exact: false }), '氏名検索0件時のメッセージが表示されていません。').toBeVisible();
  await expect(page.locator('table'), '一般仕様では0件時に空の表を表示しません。').toHaveCount(0);
});

test(caseTitle('search', 'TC011'), async ({ page }) => {
  await openSearchPage(page);
  await clickSearchButton(employeeNameForm(page));
  await expectSearchPage(page, '氏名が未入力のときは検索画面に留まります。');
});

test(caseTitle('search', 'TC012'), async ({ page }) => {
  await openSearchPage(page);
  await employeeNameForm(page).locator('[name="keyword"]').fill(' ');
  await clickSearchButton(employeeNameForm(page));
  await expectSearchPage(page, '空白だけの氏名は未入力として扱い、検索画面に留まります。');
});

test(caseTitle('search', 'TC013'), async ({ page }) => {
  await searchByDepartment(page, '100');
  const rows = page.locator('table tbody tr');
  const rowCount = await rows.count();
  expect(rowCount, '人事部の検索結果が0件でした。').toBeGreaterThan(0);
  for (let index = 0; index < rowCount; index++) {
    await expect(rows.nth(index).locator('td').last(), '人事部以外の社員が検索結果に含まれています。').toHaveText('人事部');
  }
});

test(caseTitle('search', 'TC014'), async ({ page }) => {
  await searchByDepartment(page, '300');
  const rows = page.locator('table tbody tr');
  const rowCount = await rows.count();
  expect(rowCount, '営業部の検索結果が0件でした。').toBeGreaterThan(0);
  for (let index = 0; index < rowCount; index++) {
    await expect(rows.nth(index).locator('td').last(), '営業部以外の社員が検索結果に含まれています。').toHaveText('営業部');
  }
});

test(caseTitle('search', 'TC015'), async ({ page }) => {
  await openSearchPage(page);
  await clickSearchButton(departmentForm(page));
  await expectSearchPage(page, '部署が未選択のときは検索画面に留まります。');
});

test(caseTitle('search', 'TC016'), async ({ page }) => {
  await searchByName(page, '田');
  const headers = (await page.locator('table thead th').allTextContents()).map((text) => text.replace(/\s/g, ''));
  expect(headers, '検索結果の列数は4列です。').toHaveLength(4);
  expect(headers[0], '1列目は社員番号です。').toContain('社員番号');
  expect(headers[1], '2列目は氏名とカナです。').toMatch(/氏名.*カナ/);
  expect(headers[2], '3列目は性別です。').toContain('性別');
  expect(headers[3], '4列目は部署名です。').toContain('部署名');
});

test(caseTitle('search', 'TC017'), async ({ page }) => {
  await searchByName(page, '田');
  const link = page.locator('table tbody a[href*="/detail/"]').first();
  await expect(link, '検索結果の氏名に詳細画面へのリンクがありません。').toBeVisible();
  const href = await link.getAttribute('href');
  await link.click();
  await expect(page, '選択した社員の詳細画面へ遷移していません。').toHaveURL(new RegExp(href?.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') ?? '/detail/'));
  await expect(page.getByRole('heading', { name: /社員.*詳細/ }), '社員詳細画面が表示されていません。').toBeVisible();
});

test(caseTitle('search', 'TC018'), async ({ page }) => {
  await searchByName(page, '田');
  await page.getByRole('link', { name: /検索画面.*戻る|検索画面へ/ }).click();
  await expectSearchPage(page, '検索結果から検索画面へ戻れませんでした。');
});

test(caseTitle('search', 'TC019'), async ({ page }) => {
  await searchByName(page, '田');
  await page.getByRole('link', { name: 'メニューに戻る' }).click();
  await expect(page.getByRole('heading', { name: /EIMS|社員情報管理/ }), 'トップページへ戻れませんでした。').toBeVisible();
});

test(caseTitle('search', 'TC020'), async ({ page }) => {
  await searchByName(page, '存在しない文字列');
  await expect(page.getByText(noResultMessage, { exact: false }), '0件メッセージが表示されていません。').toBeVisible();
  await expect(page.locator('table'), '0件時に検索結果テーブルが表示されています。').toHaveCount(0);
});

test(caseTitle('search', 'TC021'), async ({ page }) => {
  await openSearchPage(page);
  await employeeNumberForm(page).locator('[name="empNo"]').fill('10001');
  await employeeNameForm(page).locator('[name="keyword"]').fill('存在しない文字列');
  await clickSearchButton(employeeNumberForm(page));
  await expect(page.locator('body'), '社員番号側の検索ボタンを押した結果が使用されていません。').toContainText('10001');
  await expect(page.getByRole('heading', { name: /社員.*詳細/ }), '意図しない複合検索になっています。').toBeVisible();
});

test(caseTitle('search', 'TC022'), async () => {
  test.skip(true, 'DB停止を伴うため、この自動テストでは実施しません。');
});

test(caseTitle('search', 'TC023'), async ({ page }) => {
  await searchByEmployeeNumber(page, '-1');
  await expect(page, '負数から社員詳細画面へ遷移してはいけません。').not.toHaveURL(/\/detail\//);
  await expectNoServerError(page);
});

test(caseTitle('search', 'TC024'), async ({ page }) => {
  await openSearchPage(page);
  const input = employeeNumberForm(page).locator('[name="empNo"]');
  try {
    await input.fill('10001.5');
  } catch {
    return;
  }
  await clickSearchButton(employeeNumberForm(page));
  await expect(page, '小数から社員詳細画面へ遷移してはいけません。').not.toHaveURL(/\/detail\//);
});

test(caseTitle('search', 'TC025'), async ({ page }) => {
  const response = await page.goto('/selectByDeptNo?deptNo=999');
  expect(response?.status() ?? 500, '存在しない部署番号で500エラーになりました。').toBeLessThan(500);
  await expectNoServerError(page);
});

test(caseTitle('search', 'TC026'), async ({ page, request }) => {
  const response = await page.goto('/selectByDeptNo?deptNo=abc');
  expect(response?.status() ?? 500, '不正な部署番号は400系または安全な画面表示で処理してください。').toBeLessThan(500);
  const healthCheck = await request.get('/');
  expect(healthCheck.status(), '不正リクエスト後にアプリへアクセスできません。').toBeLessThan(500);
});

test(caseTitle('search', 'TC027'), async ({ page }) => {
  await searchByName(page, 'あ'.repeat(256));
  await expect(page, '長い検索文字列から社員詳細画面へ遷移してはいけません。').not.toHaveURL(/\/detail\//);
  await expectNoServerError(page);
});

test(caseTitle('search', 'TC028'), async ({ page }) => {
  await searchByName(page, '田');
  await expect(
    page.locator('table tbody'),
    '氏名とカナが「氏 名 (氏カナ 名カナ)」の形式で表示されていません。',
  ).toContainText('中田 結衣 (ナカタ ユイ)');
});

test(caseTitle('search', 'TC029'), async ({ page }) => {
  await openSearchPage(page);
  await page.getByRole('link', { name: 'メニューに戻る' }).click();
  await expect(page.getByRole('heading', { name: /EIMS|社員情報管理/ }), '検索画面からトップページへ戻れませんでした。').toBeVisible();
});
