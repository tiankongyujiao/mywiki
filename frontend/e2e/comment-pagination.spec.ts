import { test, expect } from './fixtures'

// 契约：评论区每页 10 条、倒序；翻页后展示旧评论
test('评论区默认展示第一页，翻页可见旧评论', async ({ page }) => {
    await page.goto('/articles/2')

    const items = page.locator('.comment-item')
    await expect(items).toHaveCount(10)
    await expect(page.getByText('共 12 条')).toBeVisible()
    await expect(page.getByText('第一条评论')).toBeHidden()   // 最旧的在第 2 页

    await page.getByRole('listitem', { name: '第 2 页' }).click()

    await expect(items).toHaveCount(2)
    await expect(page.getByText('第一条评论')).toBeVisible()
    await expect(page.getByText('第二条评论')).toBeVisible()
})