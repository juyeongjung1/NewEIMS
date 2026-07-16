import { expect, Page, test } from '@playwright/test';
import {
  caseTitle, completeRegistration, deleteEmployeeIfExists, diagnosticEmployee, fillEmployeeForm,
  findEmployeeNumber, openRegistration, registerEmployee, submitInputForm, type EmployeeInput,
} from './helpers';

async function expectRegistrationError(page: Page, field?: keyof EmployeeInput) {
  await expect(page.locator('[name="lastName"]'), '入力エラー時は登録画面へ戻ります。').toBeVisible();
  const errors = field
    ? page.locator(`[name="${field}"]`).first().locator('xpath=ancestor::div[contains(@class,"mb-3") or contains(@class,"mb-4")][1]').locator('.text-danger')
    : page.locator('.text-danger');
  await expect(errors.filter({ hasText: /.+/ }).first(), '入力項目の近くに分かりやすいエラーメッセージが必要です。').toBeVisible();
}

async function openConfirm(page: Page, values = diagnosticEmployee('c')) {
  await openRegistration(page);
  await fillEmployeeForm(page, values);
  await submitInputForm(page);
  await expect(page.getByRole('heading', { name: /登録.*確認/ }), '一般仕様では登録確認画面を表示します。').toBeVisible();
  return values;
}

async function cleanupByName(page: Page, lastName: string) {
  try {
    await deleteEmployeeIfExists(page, await findEmployeeNumber(page, lastName));
  } catch {
    // 登録されなかった場合は後片付け不要。
  }
}

test(caseTitle('registration', 'TC001'), async ({ page }) => {
  await page.goto('/');
  await page.getByRole('link', { name: /登録/ }).first().click();
  await expect(page.locator('[name="lastName"]')).toBeVisible();
});

test(caseTitle('registration', 'TC002'), async ({ page }) => {
  await openRegistration(page);
  for (const name of ['lastName', 'firstName', 'lastKana', 'firstKana', 'password', 'gender', 'deptNo']) {
    await expect(page.locator(`[name="${name}"]`).first(), `${name}の入力欄がありません。`).toBeVisible();
  }
  await expect(page.locator('[name="empNo"]'), '登録画面に社員番号入力欄は不要です。').toHaveCount(0);
});

test(caseTitle('registration', 'TC003'), async ({ page }) => {
  await openRegistration(page);
  const select = page.locator('select[name="deptNo"]');
  await expect(select, '一般仕様では部署をプルダウン表示します。').toBeVisible();
  for (const department of ['人事部', '経理部', '営業部', '総務部', '開発部', '企画部']) {
    await expect(select.locator('option', { hasText: department }), `${department}がありません。`).toHaveCount(1);
  }
});

test(caseTitle('registration', 'TC004'), async ({ page }) => {
  await openRegistration(page);
  const radios = page.locator('[name="gender"][type="radio"]');
  await expect(radios, '一般仕様では性別をラジオボタンで表示します。').toHaveCount(2);
  expect(await radios.evaluateAll((elements) => elements.every((element) => !(element as HTMLInputElement).checked))).toBeTruthy();
});

test(caseTitle('registration', 'TC005'), async ({ page }) => {
  const values = await openConfirm(page);
  await expect(page.locator('body')).toContainText(values.lastName);
});

test(caseTitle('registration', 'TC006'), async ({ page }) => {
  const values = await openConfirm(page);
  for (const value of [values.lastName, values.firstName, values.lastKana, values.firstKana, '男性', '人事部']) {
    await expect(page.locator('body'), `${value}が確認画面にありません。`).toContainText(value);
  }
});

test(caseTitle('registration', 'TC007'), async ({ page }) => {
  await openConfirm(page);
  await expect(page.locator('body')).not.toContainText('pass1234');
  await expect(page.locator('body')).toContainText(/\*{4,}|非表示/);
});

test(caseTitle('registration', 'TC008'), async ({ page }) => {
  const values = diagnosticEmployee('8');
  let empNo: string | undefined;
  try {
    await completeRegistration(page, values);
    empNo = await findEmployeeNumber(page, values.lastName);
    expect(Number(empNo)).toBeGreaterThan(10020);
  } finally {
    await deleteEmployeeIfExists(page, empNo);
  }
});

test(caseTitle('registration', 'TC009'), async ({ page }) => {
  const created = await registerEmployee(page, diagnosticEmployee('9'));
  try {
    await expect(page.locator('body')).toContainText(created.values.lastName);
  } finally {
    await deleteEmployeeIfExists(page, created.empNo);
  }
});

test(caseTitle('registration', 'TC010'), async ({ page }) => {
  await openRegistration(page);
  await page.getByRole('link', { name: 'メニューに戻る' }).click();
  await expect(page.getByRole('heading', { name: /EIMS|社員情報管理/ })).toBeVisible();
});

test(caseTitle('registration', 'TC011'), async ({ page }) => {
  const values = await openConfirm(page);
  await page.getByRole('button', { name: /修正/ }).click();
  await expect(page.locator('[name="lastName"]')).toHaveValue(values.lastName);
  await expect(page.locator('[name="deptNo"]')).toHaveValue(values.deptNo);
});

test(caseTitle('registration', 'TC012'), async ({ page }) => {
  const values = diagnosticEmployee('12');
  try {
    await completeRegistration(page, values);
    await page.getByRole('link', { name: /続けて登録/ }).click();
    await expect(page.locator('[name="lastName"]')).toHaveValue('');
    await expect(page.locator('[name="gender"]:checked')).toHaveCount(0);
  } finally {
    await cleanupByName(page, values.lastName);
  }
});

test(caseTitle('registration', 'TC013'), async ({ page }) => {
  const values = diagnosticEmployee('13');
  try {
    await completeRegistration(page, values);
    await page.getByRole('link', { name: 'メニューに戻る' }).click();
    await expect(page.getByRole('heading', { name: /EIMS|社員情報管理/ })).toBeVisible();
  } finally {
    await cleanupByName(page, values.lastName);
  }
});

test(caseTitle('registration', 'TC014'), async ({ page }) => {
  await openRegistration(page);
  await submitInputForm(page);
  await expectRegistrationError(page);
});

for (const item of [
  ['TC015', 'lastName', 'あ'.repeat(11)], ['TC016', 'firstName', 'あ'.repeat(11)],
  ['TC017', 'lastKana', 'ア'.repeat(21)], ['TC018', 'firstKana', 'ア'.repeat(21)],
  ['TC019', 'password', 'abc'], ['TC020', 'password', 'a'.repeat(17)],
] as const) {
  test(caseTitle('registration', item[0]), async ({ page }) => {
    await openRegistration(page);
    await fillEmployeeForm(page, { ...diagnosticEmployee(item[0]), [item[1]]: item[2] });
    await submitInputForm(page);
    await expectRegistrationError(page, item[1]);
  });
}

test(caseTitle('registration', 'TC021'), async ({ page }) => {
  await openRegistration(page);
  await fillEmployeeForm(page, { ...diagnosticEmployee('21'), gender: '' });
  await expect(
    page.locator('[name="gender"]:checked'),
    '性別未選択テストの送信前に、ラジオボタンが選択されたままです。',
  ).toHaveCount(0);
  await submitInputForm(page);
  await expectRegistrationError(page, 'gender');
});

test(caseTitle('registration', 'TC022'), async ({ page }) => {
  await openRegistration(page);
  await fillEmployeeForm(page, { ...diagnosticEmployee('22'), deptNo: '' });
  await submitInputForm(page);
  await expectRegistrationError(page, 'deptNo');
});

test(caseTitle('registration', 'TC023'), async ({ page }) => {
  const values = { ...diagnosticEmployee('23'), lastName: '' };
  await openRegistration(page);
  await fillEmployeeForm(page, values);
  await submitInputForm(page);
  await expectRegistrationError(page, 'lastName');
  await expect(page.locator('[name="firstName"]')).toHaveValue(values.firstName);
  await expect(page.locator('[name="deptNo"]')).toHaveValue(values.deptNo);
});

test(caseTitle('registration', 'TC024'), async () => test.skip(true, 'DB停止を伴うため自動実行しません。'));

for (const item of [
  ['TC025', 'lastName'], ['TC026', 'firstName'], ['TC027', 'lastKana'],
  ['TC028', 'firstKana'], ['TC029', 'password'],
] as const) {
  test(caseTitle('registration', item[0]), async ({ page }) => {
    await openRegistration(page);
    await fillEmployeeForm(page, { ...diagnosticEmployee(item[0]), [item[1]]: '' });
    await submitInputForm(page);
    await expectRegistrationError(page, item[1]);
  });
}

test(caseTitle('registration', 'TC030'), async ({ page }) => {
  await openRegistration(page);
  await fillEmployeeForm(page, { lastName: ' ', firstName: ' ', lastKana: ' ', firstKana: ' ', password: ' ', gender: '1', deptNo: '100' });
  await submitInputForm(page);
  await expectRegistrationError(page);
});

test(caseTitle('registration', 'TC031'), async () => test.skip(true, '画面外からの改ざん送信は対象外です。'));
test(caseTitle('registration', 'TC032'), async () => test.skip(true, '画面外からの改ざん送信は対象外です。'));

test(caseTitle('registration', 'TC033'), async ({ page }) => {
  await openConfirm(page);
  await expect(page.locator('body')).toContainText('以下の内容で登録します。よろしいですか？');
});

test(caseTitle('registration', 'TC034'), async ({ page }) => {
  const values = diagnosticEmployee('34');
  try {
    await completeRegistration(page, values);
    await expect(page.locator('body')).toContainText('登録が完了しました');
    await expect(page.locator('body')).toContainText('新しい社員情報が正常にデータベースに保存されました。');
  } finally {
    await cleanupByName(page, values.lastName);
  }
});
