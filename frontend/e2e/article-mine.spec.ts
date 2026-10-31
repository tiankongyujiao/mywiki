import { test, expect } from './fixtures'
import { loginViaUI } from './helpers'

test('登录后勾选我的文章，只看到自己的', async ({ page }) => {
    await loginViaUI(page)          // 登录后落在 /articles

    const rows = page.locator('.el-table__row')
    await expect(rows).toHaveCount(4)             // 别人的那篇也能看到
    await expect(page.getByText('别人的文章')).toBeVisible()

    // Element Plus 把真实 input 隐藏了，点可见的标签容器才能触发勾选
    await page.locator('.el-checkbox').filter({ hasText: '我的文章' }).click()

    await expect(rows).toHaveCount(4)             // 只剩自己的 4 篇
    await expect(page.getByText('别人的文章')).toBeHidden()
    await expect(page.getByText('我的草稿')).toBeVisible()
})

test('未登录勾选我的文章，被踢去登录页', async ({ page }) => {
    await page.goto('/articles')

    await page.locator('.el-checkbox').filter({ hasText: '我的文章' }).click()

    // 后端 401 → 拦截器跳 /login（修 B 后 401 的语义现在是正确的）
    await expect(page).toHaveURL(/\/login/)
})