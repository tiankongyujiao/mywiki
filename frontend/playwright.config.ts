import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  // 测试共享同一个数据库，先串行跑，简单可靠
  workers: 1,
  use: {
    baseURL: 'http://localhost:5173',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
  webServer: [
    {
      // 每次跑测试：先重置测试库，再以 test profile 启动后端（连 mywiki_test）
      command:
        'cd ../backend && ./sql/reset-test-db.sh && SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run',
      url: 'http://localhost:8080',
      timeout: 300_000,
      reuseExistingServer: false,
      stdout: 'ignore',
    },
    {
      command: 'pnpm dev',
      url: 'http://localhost:5173',
      timeout: 60_000,
      reuseExistingServer: true,
    },
  ],
})
