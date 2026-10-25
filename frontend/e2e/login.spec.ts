import { test, expect } from './fixtures'
import { TEST_USER, TEST_PASSWORD } from './helpers'

test('正确账号密码，登录成功跳转文章列表', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('请输入用户名').fill(TEST_USER)
  await page.getByPlaceholder('请输入密码').fill(TEST_PASSWORD)
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/articles$/)
  await expect(page.locator('.el-message--success')).toContainText('登录成功')
})

test('错误密码，登录失败并提示错误信息', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('请输入用户名').fill(TEST_USER)
  await page.getByPlaceholder('请输入密码').fill('错误的密码')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page.locator('.el-message--error')).toContainText('用户名或密码错误')
  await expect(page).toHaveURL(/\/login/)
})
