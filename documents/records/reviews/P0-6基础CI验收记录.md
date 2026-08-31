# P0-6 基础 CI 验收记录

> 验收日期：2026-08-31
> 范围：GitHub Actions 后端、前端与 Nginx 静态部署质量门禁
> 计划：[`../../planning/project-development-plan.md`](../../planning/project-development-plan.md)

## 1. 实现内容

新增 `.github/workflows/ci.yml`，在 `main` push、面向 `main` 的 Pull Request 和手动触发时运行：

- `backend-test`：Java 17、Maven 缓存、Testcontainers MySQL/Redis 和 `./mvnw -B -q test`。
- `frontend-quality`：pnpm 11.19.0、Node.js 22、锁文件安装、type-check、lint、format-check、workspace build 和 unit test。
- `frontend-quality` 在生成两个 `dist` 后构建 Nginx 镜像，执行 `nginx -t`，等待镜像内 `HEALTHCHECK` 变为 `healthy`，再验证 customer/admin 两个 Host 和 `/menu`、`/orders` 的 SPA 回退。
- 两个 Job 使用 Ubuntu 托管 Runner，声明最小 `contents: read` 权限，并对同一分支的新提交取消旧运行。

## 2. 本地等价验证

```bash
pnpm exec prettier --check .github/workflows/ci.yml
pnpm type-check
pnpm lint
pnpm format:check
pnpm -r build
docker build -f deploy/nginx/Dockerfile -t food-flow-manager-nginx:ci .
./scripts/verify-nginx-image.sh food-flow-manager-nginx:ci
pnpm test
cd backend && ./mvnw -B -q test
```

以上检查均通过；后端测试使用 Testcontainers 自动启动隔离 MySQL/Redis。

## 3. 当前边界

- 当前工作流尚未运行 `pnpm test:e2e`。现有冒烟脚本依赖预置顾客账号、员工账号以及后端和两个 Vite 服务，需要先设计 CI 专用数据初始化和服务编排。
- Nginx CI 当前验证静态镜像、配置语法、容器健康、双域名分流和 SPA 回退；该 Job 不启动后端，因此不把 `/api` 与 SSE 代理链路记为通过。它们属于后续多服务 E2E。
- CI 只验证镜像可用，不推送镜像、不更新服务器；发布仍属于后续 CD。
