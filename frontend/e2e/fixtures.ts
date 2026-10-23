import { execSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import { test as base, expect } from '@playwright/test'

const backendDir = fileURLToPath(new URL('../../backend', import.meta.url))

// 每个测试开始前把测试库重置回种子状态，保证测试互不影响、跟执行顺序无关
export const test = base.extend({
  // eslint-disable-next-line no-empty-pattern
  page: async ({ page }, use) => {
    execSync('./sql/reset-test-db.sh', { cwd: backendDir, stdio: 'pipe' })
    await use(page)
  },
})

export { expect }
