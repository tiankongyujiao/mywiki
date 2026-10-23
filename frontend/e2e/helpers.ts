import { expect, type Page } from '@playwright/test'

// 种子数据里的固定账号，见 backend/sql/seed.sql
export const TEST_USER = 'e2e_test'
export const TEST_PASSWORD = 'Test123456'

export async function loginViaUI(page: Page) {
  await page.goto('/login')
  await page.getByPlaceholder('请输入用户名').fill(TEST_USER)
  await page.getByPlaceholder('请输入密码').fill(TEST_PASSWORD)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/articles$/)
}
