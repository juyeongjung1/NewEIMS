import { expect, Page, test } from '@playwright/test';
import {
  caseTitle, confirmAndSaveChange, deleteEmployeeIfExists, diagnosticEmployee, fillEmployeeForm,
  openChange, registerEmployee, submitInputForm, type EmployeeInput,
} from './helpers';

let employeeNumber = '';
const initial = diagnosticEmployee('up');

test.beforeAll(async ({ browser }) => {
  test.setTimeout(30_000);
  const page = await browser.newPage();
  try {
    employeeNumber = (await registerEmployee(page, initial)).empNo;
  } finally {
    await page.close();
  }
});

test.afterAll(async ({ browser }) => {
  test.setTimeout(30_000);
  const page = await browser.newPage();
  try {
    await deleteEmployeeIfExists(page, employeeNumber);
  } finally {
    await page.close();
  }
});

async function openChangeConfirm(page: Page, changes: Partial<EmployeeInput> = { firstName: '次郎' }) {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, changes);
  await submitInputForm(page);
  await expect(page.getByRole('heading', { name: /変更.*確認/ }), '一般仕様では変更確認画面を表示します。').toBeVisible();
}

async function expectChangeError(page: Page, field?: keyof EmployeeInput) {
  await expect(page.locator('[name="lastName"]'), '入力エラー時は変更画面へ戻ります。').toBeVisible();
  const errors = field
    ? page.locator(`[name="${field}"]`).first().locator('xpath=ancestor::div[contains(@class,"mb-3") or contains(@class,"mb-4")][1]').locator('.text-danger')
    : page.locator('.text-danger');
  await expect(errors.filter({ hasText: /.+/ }).first(), '入力項目の近くにエラーメッセージが必要です。').toBeVisible();
}

test(caseTitle('update', 'TC001'), async ({ page }) => {
  await page.goto(`/detail/${employeeNumber}`);
  await page.getByRole('link', { name: /変更/ }).click();
  await expect(page.locator('[name="lastName"]')).toBeVisible();
});

test(caseTitle('update', 'TC002'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await expect(page.locator('[name="lastName"]')).toHaveValue(initial.lastName);
  await expect(page.locator('[name="firstName"]')).toHaveValue(initial.firstName);
  await expect(page.locator('[name="deptNo"]')).toHaveValue(initial.deptNo);
});

test(caseTitle('update', 'TC003'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await expect(page.locator('input[name="empNo"]:not([type="hidden"])'), '社員番号を編集可能にしてはいけません。').toHaveCount(0);
  await expect(page.locator('input[type="hidden"][name="empNo"]')).toHaveValue(employeeNumber);
});

test(caseTitle('update', 'TC004'), async ({ page }) => {
  await openChange(page, employeeNumber);
  const select = page.locator('select[name="deptNo"]');
  await expect(select, '一般仕様では部署をプルダウン表示します。').toBeVisible();
  await expect(select.locator('option')).toHaveCount(7);
  await expect(select).toHaveValue(initial.deptNo);
});

test(caseTitle('update', 'TC005'), async ({ page }) => {
  await openChangeConfirm(page, { firstName: '次郎' });
  await expect(page.locator('body')).toContainText('次郎');
});

test(caseTitle('update', 'TC006'), async ({ page }) => {
  await openChangeConfirm(page, { password: 'newpass8' });
  await expect(page.locator('body')).not.toContainText('newpass8');
  await expect(page.locator('body')).toContainText(/\*{4,}|非表示/);
});

test(caseTitle('update', 'TC007'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { firstName: '次郎' });
  await confirmAndSaveChange(page);
});

test(caseTitle('update', 'TC008'), async ({ page }) => {
  await page.goto(`/detail/${employeeNumber}`);
  await expect(page.locator('body')).toContainText('次郎');
});

test(caseTitle('update', 'TC009'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { firstName: '未保存' });
  await page.getByRole('link', { name: /詳細に戻る/ }).click();
  await expect(page).toHaveURL(new RegExp(`/detail/${employeeNumber}$`));
  await expect(page.locator('body')).not.toContainText('未保存');
});

test(caseTitle('update', 'TC010'), async ({ page }) => {
  await openChangeConfirm(page, { firstName: '保持確認' });
  await page.getByRole('button', { name: /修正/ }).click();
  await expect(page.locator('[name="firstName"]')).toHaveValue('保持確認');
  await expect(page.locator('[name="deptNo"]')).toHaveValue(initial.deptNo);
});

test(caseTitle('update', 'TC011'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { firstName: '三郎' });
  await confirmAndSaveChange(page);
  await page.getByRole('link', { name: 'メニューに戻る' }).click();
  await expect(page.getByRole('heading', { name: /EIMS|社員情報管理/ })).toBeVisible();
});

test(caseTitle('update', 'TC012'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { firstName: '四郎' });
  await confirmAndSaveChange(page);
  await page.getByRole('link', { name: /検索画面に戻る/ }).click();
  await expect(page.locator('[name="keyword"]')).toBeVisible();
});

test(caseTitle('update', 'TC013'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { lastName: '', firstName: '', lastKana: '', firstKana: '', password: '', gender: '', deptNo: '' });
  await submitInputForm(page);
  await expectChangeError(page);
});

for (const item of [
  ['TC014', 'lastName', 'あ'.repeat(11)], ['TC015', 'firstName', 'あ'.repeat(11)],
  ['TC016', 'lastKana', 'ア'.repeat(21)], ['TC017', 'firstKana', 'ア'.repeat(21)],
  ['TC018', 'password', 'abc'], ['TC019', 'password', 'a'.repeat(17)],
] as const) {
  test(caseTitle('update', item[0]), async ({ page }) => {
    await openChange(page, employeeNumber);
    await fillEmployeeForm(page, { [item[1]]: item[2] });
    await submitInputForm(page);
    await expectChangeError(page, item[1]);
  });
}

test(caseTitle('update', 'TC020'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { gender: '' });
  await submitInputForm(page);
  await expectChangeError(page, 'gender');
});

test(caseTitle('update', 'TC021'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { deptNo: '' });
  await submitInputForm(page);
  await expectChangeError(page, 'deptNo');
});

test(caseTitle('update', 'TC022'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { lastName: '', firstName: '保持更新' });
  await submitInputForm(page);
  await expectChangeError(page, 'lastName');
  await expect(page.locator('[name="firstName"]')).toHaveValue('保持更新');
  await expect(page.locator('[name="deptNo"]')).toHaveValue(initial.deptNo);
});

test(caseTitle('update', 'TC023'), async () => test.skip(true, 'URL直接操作は対象外です。'));
test(caseTitle('update', 'TC024'), async () => test.skip(true, 'DB停止を伴うため自動実行しません。'));

for (const item of [
  ['TC025', 'lastName'], ['TC026', 'firstName'], ['TC027', 'lastKana'],
  ['TC028', 'firstKana'], ['TC029', 'password'],
] as const) {
  test(caseTitle('update', item[0]), async ({ page }) => {
    await openChange(page, employeeNumber);
    await fillEmployeeForm(page, { [item[1]]: '' });
    await submitInputForm(page);
    await expectChangeError(page, item[1]);
  });
}

test(caseTitle('update', 'TC030'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { lastName: ' ', firstName: ' ', lastKana: ' ', firstKana: ' ', password: ' ' });
  await submitInputForm(page);
  await expectChangeError(page);
});

test(caseTitle('update', 'TC031'), async () => test.skip(true, '画面外からの改ざん送信は対象外です。'));
test(caseTitle('update', 'TC032'), async () => test.skip(true, '画面外からの改ざん送信は対象外です。'));
test(caseTitle('update', 'TC033'), async () => test.skip(true, '画面外からの改ざん送信は対象外です。'));

test(caseTitle('update', 'TC034'), async ({ page }) => {
  await openChangeConfirm(page, { firstName: '確認文言' });
  await expect(page.locator('body')).toContainText('以下の内容に変更します。よろしいですか？');
});

test(caseTitle('update', 'TC035'), async ({ page }) => {
  await openChange(page, employeeNumber);
  await fillEmployeeForm(page, { firstName: '完了文言' });
  await confirmAndSaveChange(page);
  await expect(page.locator('body')).toContainText('変更が完了しました');
  await expect(page.locator('body')).toContainText('社員情報が正常に変更されました。');
});
