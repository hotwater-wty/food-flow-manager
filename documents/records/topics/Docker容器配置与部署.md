# Docker 容器配置与部署

> 文档状态：历史记录，已按当前部署配置校准关键结论
> 当前事实入口：[后端技术选型与当前依赖](../../architecture/backend/技术选型与依赖规划.md)
> 历史边界：本文保留部署方案形成过程；以根目录 `Dockerfile`、`docker-compose.yml` 和运行配置为准。

## 1. 背景与当前结论

本文记录项目引入 Docker Compose 的过程，以及部署时需要理解的网络、环境变量、数据卷和初始化 SQL 边界。早期版本曾要求在宿主机执行 Maven 打包并把 jar 复制到项目根目录；该流程已经失效。

当前 `Dockerfile` 使用**多阶段构建**：

```text
构建阶段（Java 17 JDK）
  -> 复制 Maven Wrapper、pom.xml 和 src
  -> 容器内执行 ./mvnw -B clean package -DskipTests
运行阶段（Java 17 JRE）
  -> 仅复制构建产物 jar
  -> 启动 Spring Boot 应用
```

因此，部署或更新时不需要在宿主机手动生成、复制 `food-flow-manager.jar`；Compose 构建应用镜像时会完成打包。

## 2. 当前项目如何使用

根目录包含以下运行时资产：

```text
Dockerfile
docker-compose.yml
assets/schema.sql
```

`docker-compose.yml` 编排三个服务：

- `mysql`：MySQL 8，持久化数据库文件，并在首次初始化时执行 `assets/schema.sql`。
- `redis`：Redis 8.6.2，启用 AOF 并持久化数据。
- `food-flow-manager`：由根目录 `Dockerfile` 构建的 Spring Boot 服务。

应用通过 Compose 服务名访问依赖：

```text
应用 -> mysql:3306
应用 -> redis:6379
```

本机直接启动应用时，开发配置仍使用本机地址；不要为容器部署修改本地开发配置。Compose 通过环境变量覆盖数据源和 Redis 连接参数。

## 3. 部署与更新流程

首次部署或代码更新后，在包含 `docker-compose.yml` 的项目根目录执行：

```bash
git pull
docker compose up -d --build
```

`--build` 会重新执行 Dockerfile 的构建阶段。依赖已健康检查通过后，应用容器才会启动。

验证顺序：

```bash
docker compose ps
docker compose logs -f food-flow-manager
docker compose logs -f mysql
docker compose logs -f redis
curl http://localhost:8080/actuator/health
```

同时应访问 Knife4j 的 `/doc.html`，并调用一个已授权的核心接口。容器“正在运行”不足以说明业务可用。

## 4. 配置边界

### 4.1 数据库初始化

MySQL 仅在数据目录首次初始化为空时执行：

```text
./assets/schema.sql -> /docker-entrypoint-initdb.d/01-schema.sql
```

已有数据卷时修改 `schema.sql` 不会自动重放。需要重置数据库前，必须先确认数据可以清除并处理备份；此文档不授权删除数据卷。

### 4.2 Compose 网络

容器中的 `localhost` 指向容器自身，因此应用不能用 `localhost:3306` 或 `localhost:6379` 连接 Compose 服务。服务名 `mysql`、`redis` 由 Compose 网络解析。

### 4.3 密钥与运行配置

当前 Compose 文件中的数据库密码是本地学习配置，不应视为生产密钥管理方案。部署到共享或生产环境时，应改用受保护的环境变量、密钥管理能力和非 root 数据库账号；这是候选增强项，尚未在当前仓库落实。

## 5. 常见排查

| 现象 | 优先检查 |
| --- | --- |
| 应用连接不上 MySQL | `mysql` 健康状态、数据源环境变量、`mysql:3306` 服务名 |
| 应用连接不上 Redis | `redis` 健康状态、`SPRING_DATA_REDIS_*` 环境变量、`redis:6379` 服务名 |
| 表不存在或初始化 SQL 未更新 | 数据卷是否已存在；首次初始化规则是否符合预期 |
| 构建失败 | `docker compose build food-flow-manager` 的构建日志；Maven Wrapper 是否可执行 |
| 应用启动后接口不可用 | 应用日志、`/actuator/health`、Knife4j 与实际鉴权令牌 |

## 6. 可复用结论

- Java 17 项目可以在 Docker 多阶段构建中完成 Maven Wrapper 打包，运行镜像只保留 JRE 和最终 jar。
- Compose 中使用服务名连接数据库和缓存；本地开发地址与容器地址分离。
- `schema.sql` 是运行时数据库初始化资产，仍应保留在 `assets/`，不属于项目文档。
- 数据卷首次初始化规则、健康检查和真实接口验收应写入部署检查项。
- CI/CD、镜像仓库、`.env` 密钥管理和非 root 数据库账号是未来候选项，不应表述为当前已配置能力。
