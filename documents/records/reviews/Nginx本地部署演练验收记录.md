# Nginx 本地部署演练验收记录

> 验收日期：2026-08-31
> 范围：双 Vite 应用构建产物由单个 Nginx 容器提供服务

## 1. 实现内容

- `docker-compose.nginx.local.yml` 使用目录挂载方式提供本地演练：两个 `dist` 目录和 `deploy/nginx/local.conf` 挂载到同一个 Nginx 容器。
- `local.conf` 通过 `customer.localhost` 和 `admin.localhost` 两个 `server` 区分顾客端和商户端，使用 `try_files` 支持 Vue Router 刷新，并将 `/api` 和管理端 SSE 转发给宿主机 IDEA 后端。
- 完整 `docker-compose.yml` 预留 `web` profile；启用后使用 `deploy/nginx/Dockerfile` 将两个 dist 和 Compose 内部代理配置构建进 Nginx 镜像。
- Nginx 镜像带有容器级 `HEALTHCHECK`；CI 使用 `scripts/verify-nginx-image.sh` 验证配置、健康状态、双 Host 静态入口和 SPA 回退。

## 2. 使用边界

| 场景 | Nginx 上游 | 静态文件来源 |
| --- | --- | --- |
| 本地 IDEA Debug | `host.docker.internal:8080` | 宿主机目录挂载 |
| 完整 Compose | `food-flow-manager:8080` | Nginx 镜像 `COPY` |

本地文件不能直接作为完整 Compose 的代理配置，因为容器内的 `localhost`/宿主机地址与 Compose 服务网络不是同一语义；因此两套配置保持相同路由规则，但使用不同上游地址。

## 3. 验收命令

```bash
pnpm -r build
docker compose -f docker-compose.nginx.local.yml up -d
docker exec food-flow-manager-nginx-local nginx -t
```

浏览器检查：

- `http://customer.localhost:8081/` 显示顾客端；
- `http://admin.localhost:8081/` 显示商户端；
- 刷新 `/menu`、`/orders` 等前端子路由不返回 404；
- `/api` 请求到达 IDEA 后端；
- 管理端 SSE 使用关闭缓冲和长读取超时配置。
