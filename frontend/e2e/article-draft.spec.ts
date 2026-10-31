import { test, expect } from './fixtures'
import { loginViaUI } from './helpers'

test('新增页存草稿，主列表不可见，我的文章里带草稿标记', async ({ page }) => {
    await loginViaUI(page)
    await page.goto('/articles/create')

    await page.getByPlaceholder('请输入标题').fill('e2e存的新草稿')
    await page.getByPlaceholder('请输入内容').fill('草稿内容')
    await page.getByRole('button', { name: '保存草稿' }).click()

    await expect(page).toHaveURL(/\/articles$/)
    await expect(page.locator('.el-message--success')).toContainText('已存草稿')

    // 主列表没有它，行数不变
    await expect(page.locator('.el-table__row')).toHaveCount(4)
    await expect(page.getByText('e2e存的新草稿')).toBeHidden()

    // 勾「我的文章」后可见，且带草稿标记（我的草稿 + 新草稿共 2 个）
    await page.locator('.el-checkbox').filter({ hasText: '我的文章' }).click()
    await expect(page.locator('.el-table__row')).toHaveCount(5)
    await expect(page.getByText('e2e存的新草稿')).toBeVisible()
    await expect(page.locator('.el-tag').filter({ hasText: '草稿' })).toHaveCount(2)
})

test('编辑草稿页点发布，主列表出现', async ({ page }) => {
    await loginViaUI(page)
    await page.goto('/articles/5/edit')

    // 草稿加载进表单
    await expect(page.getByPlaceholder('请输入标题')).toHaveValue('我的草稿')
    await page.getByRole('button', { name: '发布' }).click()

    await expect(page).toHaveURL(/\/articles$/)
    await expect(page.locator('.el-message--success')).toContainText('发布成功')

    // 主列表出现，且不再是草稿
    await expect(page.locator('.el-table__row')).toHaveCount(5)
    await expect(page.getByText('我的草稿')).toBeVisible()
    await expect(page.locator('.el-tag').filter({ hasText: '草稿' })).toHaveCount(0)
})