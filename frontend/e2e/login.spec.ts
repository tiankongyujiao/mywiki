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

test('错误密码，留在登录页且不落 token', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('请输入用户名').fill(TEST_USER)
  await page.getByPlaceholder('请输入密码').fill('错误的密码')
  await page.getByRole('button', { name: '登录' }).click()

  // 后端登录失败返回 401，前端拦截器直接跳回 /login，不弹错误提示
  await expect(page).toHaveURL(/\/login/)
  const token = await page.evaluate(() => localStorage.getItem('token'))
  expect(token).toBeNull()
})
