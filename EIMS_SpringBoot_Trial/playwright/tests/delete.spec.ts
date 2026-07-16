import { expect, Page, test } from '@playwright/test';
import { caseTitle, deleteEmployeeIfExists, diagnosticEmployee, registerEmployee } from './helpers';

async function createForDelete(page: Page, suffix: string) {
  return registerEmployee(page, diagnosticEmployee(suffix));
}

async function openDeleteConfirm(page: Page, empNo: string) {
  await page.goto(`/detail/${empNo}`);
  await page.getByRole('link', { name: /削除/ }).first().click();
  await expect(page.getByRole('heading', { name: /削除.*確認/ }), '削除確認画面が表示されません。').toBeVisible();
}

async function confirmDelete(page: Page) {
  await page.getByRole('button', { name: /削除確定|削除する/ }).click();
  await page.waitForLoadState('domcontentloaded');
}

test(caseTitle('delete', 'TC001'), async ({ page }) => {
  const created = await createForDelete(page, 'd1');
  try {
    await openDeleteConfirm(page, created.empNo);
  } finally {
    await deleteEmployeeIfExists(page, created.empNo);
  }
});

test(caseTitle('delete', 'TC002'), async ({ page }) => {
  const created = await createForDelete(page, 'd2');
  try {
    await openDeleteConfirm(page, created.empNo);
    for (const value of [created.empNo, created.values.lastName, created.values.firstName, created.values.lastKana, created.values.firstKana, '男性', '人事部']) {
      await expect(page.locator('body'), `${value}が削除確認画面にありません。`).toContainText(value);
    }
    await expect(page.locator('input:not([type="hidden"]), select, textarea'), '削除確認画面は読み取り専用です。').toHaveCount(0);
  } finally {
    await deleteEmployeeIfExists(page, created.empNo);
  }
});

test(caseTitle('delete', 'TC003'), async ({ page }) => {
  const created = await createForDelete(page, 'd3');
  try {
    await openDeleteConfirm(page, created.empNo);
    await expect(page.locator('body')).toContainText(/削除.*(します|よろしい|取り消せません)/);
  } finally {
    await deleteEmployeeIfExists(page, created.empNo);
  }
});

test(caseTitle('delete', 'TC004'), async ({ page }) => {
  const created = await createForDelete(page, 'd4');
  try {
    await openDeleteConfirm(page, created.empNo);
    await page.getByRole('link', { name: /詳細に戻る/ }).click();
    await expect(page).toHaveURL(new RegExp(`/detail/${created.empNo}$`));
    await expect(page.locator('body')).toContainText(created.values.lastName);
  } finally {
    await deleteEmployeeIfExists(page, created.empNo);
  }
});

test(caseTitle('delete', 'TC005'), async ({ page }) => {
  const created = await createForDelete(page, 'd5');
  await openDeleteConfirm(page, created.empNo);
  await confirmDelete(page);
  await expect(page.locator('body')).toContainText(/削除.*完了/);
});

test(caseTitle('delete', 'TC006'), async ({ page }) => {
  const created = await createForDelete(page, 'd6');
  await openDeleteConfirm(page, created.empNo);
  await confirmDelete(page);
  await page.goto(`/selectByEmpNo?empNo=${created.empNo}`);
  await expect(page.locator('body')).toContainText(/見つかりません|存在しません|対象.*なし/);
});

test(caseTitle('delete', 'TC007'), async ({ page }) => {
  const created = await createForDelete(page, 'd7');
  await openDeleteConfirm(page, created.empNo);
  await confirmDelete(page);
  await page.getByRole('link', { name: 'メニューに戻る' }).click();
  await expect(page.getByRole('heading', { name: /EIMS|社員情報管理/ })).toBeVisible();
});

test(caseTitle('delete', 'TC008'), async ({ page }) => {
  const created = await createForDelete(page, 'd8');
  await openDeleteConfirm(page, created.empNo);
  await confirmDelete(page);
  await page.getByRole('link', { name: /検索画面に戻る/ }).click();
  await expect(page.locator('[name="keyword"]')).toBeVisible();
});

test(caseTitle('delete', 'TC009'), async ({ page }) => {
  const response = await page.goto('/deleteConfirm/99999');
  expect(response?.status() ?? 500).toBeLessThan(500);
  await expect(page.locator('body')).toContainText(/存在しない|削除できません|見つかりません/);
});

test(caseTitle('delete', 'TC010'), async ({ page }) => {
  const created = await createForDelete(page, 'd10');
  await openDeleteConfirm(page, created.empNo);
  await confirmDelete(page);
  const response = await page.goto(`/deleteConfirm/${created.empNo}`);
  expect(response?.status() ?? 500).toBeLessThan(500);
  await expect(page.locator('body')).toContainText(/存在しない|削除できません|見つかりません/);
});

test(caseTitle('delete', 'TC011'), async () => test.skip(true, 'DB停止を伴うため自動実行しません。'));

test(caseTitle('delete', 'TC012'), async ({ request }) => {
  const response = await request.post('/deleteEmployee', { form: {} });
  expect(response.status(), '社員番号なしの削除で500エラーになりました。').toBeLessThan(500);
});

test(caseTitle('delete', 'TC013'), async ({ request }) => {
  const response = await request.post('/deleteEmployee', { form: { empNo: '99999' } });
  expect(response.status(), '存在しない社員の削除で500エラーになりました。').toBeLessThan(500);
});

test(caseTitle('delete', 'TC014'), async ({ request }) => {
  const response = await request.post('/deleteEmployee', { form: { empNo: 'abc' } });
  expect(response.status(), '不正形式の社員番号は400系として処理してください。').toBeLessThan(500);
});

test(caseTitle('delete', 'TC015'), async ({ page }) => {
  const created = await createForDelete(page, 'd15');
  await openDeleteConfirm(page, created.empNo);
  await confirmDelete(page);
  await expect(page.locator('body')).toContainText('削除が完了しました');
  await expect(page.locator('body')).toContainText('社員情報がシステムから正常に削除されました。');
});
