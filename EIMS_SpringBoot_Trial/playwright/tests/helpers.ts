import { expect, Page } from '@playwright/test';
import { deleteTestCases, registrationTestCases, updateTestCases } from '../maintenance-test-cases';
import { searchTestCases, type FeatureId } from '../test-cases';

export type EmployeeInput = {
  lastName: string;
  firstName: string;
  lastKana: string;
  firstKana: string;
  password: string;
  gender: string;
  deptNo: string;
};

const maps = {
  search: new Map(searchTestCases.map((item) => [item.id, item])),
  registration: new Map(registrationTestCases.map((item) => [item.id, item])),
  update: new Map(updateTestCases.map((item) => [item.id, item])),
  delete: new Map(deleteTestCases.map((item) => [item.id, item])),
};

export function caseTitle(feature: FeatureId, id: string): string {
  const definition = maps[feature].get(id);
  if (!definition) throw new Error(`${feature}:${id} が定義されていません。`);
  return `[${feature}:${id}] ${definition.title}`;
}

export function diagnosticEmployee(suffix = ''): EmployeeInput {
  const token = (Date.now().toString(36) + suffix).slice(-5);
  return {
    lastName: `診断${token}`,
    firstName: '太郎',
    lastKana: `シンダン${token.toUpperCase()}`,
    firstKana: 'タロウ',
    password: 'pass1234',
    gender: '1',
    deptNo: '100',
  };
}

export async function fillEmployeeForm(page: Page, values: Partial<EmployeeInput>): Promise<void> {
  for (const name of ['lastName', 'firstName', 'lastKana', 'firstKana', 'password'] as const) {
    if (values[name] !== undefined) await page.locator(`[name="${name}"]`).fill(values[name] ?? '');
  }
  if (values.gender !== undefined) {
    const gender = page.locator('[name="gender"]');
    if (await gender.first().getAttribute('type') === 'radio') {
      if (values.gender) await page.locator(`[name="gender"][value="${values.gender}"]`).check();
      else await gender.evaluateAll((elements) => elements.forEach((element) => ((element as HTMLInputElement).checked = false)));
    } else {
      await gender.fill(values.gender);
    }
  }
  if (values.deptNo !== undefined) {
    const department = page.locator('[name="deptNo"]');
    if ((await department.evaluate((element) => element.tagName)) === 'SELECT') await department.selectOption(values.deptNo);
    else await department.fill(values.deptNo);
  }
}

export async function submitInputForm(page: Page): Promise<void> {
  await page.locator('form').filter({ has: page.locator('[name="lastName"]') })
    .getByRole('button', { name: /確認画面へ|登録|変更/ }).click();
  await page.waitForLoadState('domcontentloaded');
}

export async function openRegistration(page: Page): Promise<void> {
  await page.goto('/input');
  await expect(page.locator('[name="lastName"]'), '登録画面に氏の入力欄がありません。').toBeVisible();
}

export async function completeRegistration(page: Page, values: EmployeeInput): Promise<void> {
  await openRegistration(page);
  await fillEmployeeForm(page, values);
  await submitInputForm(page);
  const confirmButton = page.getByRole('button', { name: /登録確定/ });
  if (await confirmButton.count()) {
    await confirmButton.click();
    await page.waitForLoadState('domcontentloaded');
  }
  await expect(page.locator('body'), '登録完了画面が表示されませんでした。').toContainText(/登録.*完了|正常に.*保存/);
}

export async function findEmployeeNumber(page: Page, lastName: string): Promise<string> {
  await page.goto(`/selectByEmpName?keyword=${encodeURIComponent(lastName)}`);
  const link = page.locator('a[href*="/detail/"]').filter({ hasText: lastName }).first();
  await expect(link, '診断用に登録した社員を検索できませんでした。').toBeVisible();
  const empNo = (await link.getAttribute('href'))?.match(/\/detail\/(\d+)/)?.[1];
  if (!empNo) throw new Error('登録した社員番号を取得できませんでした。');
  return empNo;
}

export async function registerEmployee(page: Page, values = diagnosticEmployee()): Promise<{ empNo: string; values: EmployeeInput }> {
  await completeRegistration(page, values);
  const empNo = await findEmployeeNumber(page, values.lastName);
  return { empNo, values };
}

export async function deleteEmployeeIfExists(page: Page, empNo: string | undefined): Promise<void> {
  if (!empNo) return;
  try {
    const response = await page.goto(`/detail/${empNo}`);
    if (!response || response.status() >= 500) return;
    const deleteLink = page.getByRole('link', { name: /削除/ }).first();
    if (!(await deleteLink.count())) return;
    await deleteLink.click();
    const confirm = page.getByRole('button', { name: /削除確定|削除する/ }).first();
    if (await confirm.count()) await confirm.click();
  } catch {
    // テスト本体の結果を、後片付けの失敗で上書きしない。
  }
}

export async function openChange(page: Page, empNo: string): Promise<void> {
  await page.goto(`/changeInput/${empNo}`);
  await expect(page.locator('[name="lastName"]'), '変更画面に氏の入力欄がありません。').toBeVisible();
  const password = page.locator('[name="password"]');
  if (await password.count() && !(await password.inputValue())) await password.fill('pass1234');
}

export async function confirmAndSaveChange(page: Page): Promise<void> {
  await submitInputForm(page);
  const confirm = page.getByRole('button', { name: /変更確定/ });
  if (await confirm.count()) {
    await confirm.click();
    await page.waitForLoadState('domcontentloaded');
  }
  await expect(page.locator('body'), '変更完了画面が表示されませんでした。').toContainText(/変更.*完了|正常に変更/);
}
