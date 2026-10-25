import { test, expect } from './fixtures'
import { loginViaUI } from './helpers'

test('登录后新增文章，列表出现新文章', async ({ page }) => {
  await loginViaUI(page)

  await page.goto('/articles/create')

  await page.getByPlaceholder('请输入标题').fill('e2e创建的文章')
  await page
    .locator('.el-select')
    .filter({ hasText: '请选择分类' })
    .click()
  await page
    .locator('.el-select-dropdown__item', { hasText: '测试分类A' })
    .click()
  await page.getByPlaceholder('请输入内容').fill('由 e2e 测试创建的内容')

  await page.getByRole('button', { name: '保存' }).click()

  await expect(page).toHaveURL(/\/articles$/)
  await expect(page.locator('.el-message--success')).toContainText('新增成功')

  // 4 篇种子文章 + 新建的 1 篇
  const rows = page.locator('.el-table__row')
  await expect(rows).toHaveCount(5)
  await expect(page.getByText('e2e创建的文章')).toBeVisible()
})

test('未登录访问新增文章页，跳转登录', async ({ page }) => {
  await page.goto('/articles/create')

  await expect(page).toHaveURL(/\/login/)
})
