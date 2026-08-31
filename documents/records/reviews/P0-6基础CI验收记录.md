# P0-6 基础 CI 验收记录

> 验收日期：2026-08-31
> 范围：GitHub Actions 后端与前端基础质量门禁
> 计划：[`../../planning/project-development-plan.md`](../../planning/project-development-plan.md)

## 1. 实现内容

新增 `.github/workflows/ci.yml`，在 `main` push、面向 `main` 的 Pull Request 和手动触发时运行：

- `backend-test`：Java 17、Maven 缓存、Testcontainers MySQL/Redis 和 `./mvnw -B -q test`。
- `frontend-quality`：pnpm 11.19.0、Node.js 22、锁文件安装、type-check、lint、format-check、workspace build 和 unit test。
- 两个 Job 使用 Ubuntu 托管 Runner，声明最小 `contents: read` 权限，并对同一分支的新提交取消旧运行。

## 2. 本地等价验证

```bash
pnpm exec prettier --check .github/workflows/ci.yml
pnpm type-check
pnpm lint
pnpm format:check
pnpm -r build
pnpm test
cd backend && ./mvnw -B -q test
```

以上检查均通过；后端测试使用 Testcontainers 自动启动隔离 MySQL/Redis。

## 3. 当前边界

- 当前工作流尚未运行 `pnpm test:e2e`。现有冒烟脚本依赖预置顾客账号、员工账号以及后端和两个 Vite 服务，需要先设计 CI 专用数据初始化和服务编排。
- P0-6 本身未将 Nginx/前端静态镜像纳入 CI；本地 Nginx 演练已在后续部署切片完成，镜像发布和 Nginx 配置校验仍应在后续 CI/CD 切片中加入。
