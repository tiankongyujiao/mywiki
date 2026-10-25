import { test, expect } from './fixtures'

// 种子数据：分类A 2 篇（文章一、二），分类B 1 篇（文章三），共 3 篇
test('文章列表展示种子数据', async ({ page }) => {
  await page.goto('/articles')

  const rows = page.locator('.el-table__row')
  await expect(rows).toHaveCount(4)
  await expect(page.getByText('种子文章一')).toBeVisible()
  await expect(page.getByText('种子文章二')).toBeVisible()
  await expect(page.getByText('种子文章三')).toBeVisible()
  await expect(page.getByText('别人的文章')).toBeVisible()
})

test('按分类筛选：分类A 下只有 2 篇', async ({ page }) => {
  await page.goto('/articles')

  // Element Plus 的 select 把 placeholder 渲染成 span，不是 input 的 placeholder 属性
  await page.locator('.el-select').filter({ hasText: '按分类筛选' }).click()
  await page
    .locator('.el-select-dropdown__item', { hasText: '测试分类A' })
    .click()

  const rows = page.locator('.el-table__row')
  await expect(rows).toHaveCount(2)
  await expect(page.getByText('种子文章一')).toBeVisible()
  await expect(page.getByText('种子文章三')).toBeHidden()
})
