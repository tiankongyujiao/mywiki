# CLAUDE.md

## 项目

mywiki：个人 wiki/博客。后端 Spring Boot 3.5 + MyBatis-Plus + MySQL 8（`backend/`），前端 Vue 3 + Element Plus + Pinia + Vite（`frontend/`）。MySQL 跑在本机 Docker 容器 `mysql8`（root/123456）。

## 常用命令

```bash
# 开发
cd frontend && pnpm dev                      # 前端（代理 /api → localhost:8080）
cd backend && ./mvnw spring-boot:run         # 后端（default profile 连开发库 mywiki）

# 测试（改完功能两个都要跑）
cd backend && ./mvnw test                    # 后端接口测试（Testcontainers 自起 MySQL）
cd frontend && pnpm exec playwright test     # e2e（自动重置测试库→起 test 后端→起 vite）

./backend/sql/reset-test-db.sh               # 手动重置 e2e 测试库（mywiki_test）
```

CI：push 触发 `.github/workflows/e2e.yml`，`e2e` 与 `backend-test` 两个并行 job。

## 新功能开发流程（已多次验证）

1. **契约先行**：动代码前先定行为规则；规则密集的功能用 `/grill-me` 拷问，产出契约表 + 测试清单
2. **数据先行**：开发库 `ALTER` + `schema.sql` 同步 + 种子数据；受契约变化影响的旧测试**合法更新**（断言从新契约推导，绝不从实际输出反推）
3. **红**：写接口测试，红灯必须"为正确的理由"失败，逐个核对原因
4. **绿**：最小实现转绿，`./mvnw test` 全量回归
5. **前端 + e2e**：UI 入口 + 每条用户路径一个 e2e，`playwright test` 全绿
6. **本地全绿后再 push**，CI 是复核不是第一轮

## 测试约定

- `backend/sql/schema.sql` 是表结构的唯一真相，e2e 与后端测试共用；**数据各自维护**：`backend/sql/seed.sql` 归 e2e（中文、为浏览器断言设计），`backend/src/test/resources/test-data.sql` 归后端测试（英文标题，避免 MockMvc 编码坑）
- 后端测试继承 `BaseApiTest`（单例 Testcontainers 容器 + MockMvc + `login()`）。不要退回 `@Container`/`@Testcontainers`——每个测试类起停容器会让 Spring 上下文缓存指向已销毁容器
- `backend/src/test/resources/docker-java.properties` 固定 `api.version=1.41`（CI 新 Docker 引擎最低要求 1.40，默认 1.32 会被拒）
- e2e 每个测试前自动重置库（`frontend/e2e/fixtures.ts`），固定账号 `e2e_test / Test123456`；spec 文件从 `./fixtures` 导入 `test/expect`，不要用 `@playwright/test`
- Element Plus 把选择框/勾选框的真实 input 隐藏了：用 `.el-select` / `.el-checkbox` 容器 + `filter({ hasText })` 点击，不要 `getByPlaceholder` / `getByRole('checkbox')`

## 关键约定与坑

- 后端路径**没有** `/api` 前缀（axios baseURL=`/api`，vite 代理会剥掉）；curl 直连后端别带 `/api`
- 测试库两条隔离路线：e2e 复用 `mysql8` 容器内的 `mywiki_test`（每次测试前重置）；后端测试用 Testcontainers 临时容器（用完即毁）
- 导入 SQL 时 mysql 客户端必须 `--default-character-set=utf8mb4`，否则中文双重编码（reset 脚本已处理）
- 错误响应约定：`@Valid` 校验失败 → HTTP 400 + code 10002；业务错误经 `ErrorCode` 自带正确 HTTP 状态；401 语义：登录失败（10004, 400）/ 未登录（10006, 401）
- 文章状态两态 `DRAFT`/`PUBLISHED`：主列表只含 PUBLISHED；`mine=true`（需登录）含本人全部含草稿；草稿对非作者 = 404（详情与评论）；编辑不回退状态（v1 无撤回）

## 提交约定

commit message 用中文，前缀式：【add】【fix】【test】。
