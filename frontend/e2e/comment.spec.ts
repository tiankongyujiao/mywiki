import { test, expect } from './fixtures'
import { loginViaUI } from './helpers'

test('登录后在文章详情页发表评论', async ({ page }) => {
    await loginViaUI(page)
    await page.goto('/articles/1')   // 种子文章一，已有 1 条种子评论

    await expect(page.getByText('种子评论')).toBeVisible()

    await page.getByPlaceholder('写下你的评论...').fill('e2e 新评论')
    await page.getByRole('button', { name: '发表评论' }).click()

    await expect(page.locator('.el-message--success')).toContainText('评论成功')
    await expect(page.getByText('e2e 新评论')).toBeVisible()
})

test('删除自己的评论后从列表消失', async ({ page }) => {
    await loginViaUI(page)
    await page.goto('/articles/1')

    await expect(page.getByText('种子评论')).toBeVisible()

    // 注意限定在评论项里：文章头部的"删除"按钮也可见，不限定会撞名
    await page.locator('.comment-item').getByRole('button', { name: '删除' }).click()
    await page.getByRole('button', { name: '确定' }).click()

    await expect(page.locator('.el-message--success')).toContainText('删除成功')
    await expect(page.getByText('种子评论')).toBeHidden()
})