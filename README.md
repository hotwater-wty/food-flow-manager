# food-flow-manager 膳畅管家

`food-flow-manager` 是一个基于 Spring Boot 3 的餐饮预约与堂食管理后端项目，中文名为“膳畅管家”。项目从单门店场景出发，覆盖用户预约、到店开台、堂食点餐、商户处理订单、店员清台释放桌位的完整主流程，并持续补强权限、并发、缓存、分页和部署能力。

这个项目更偏向真实后端工程实践，而不是简单 CRUD 练习。开发过程中重点关注了需求收敛、状态流转、角色鉴权、跨模块协作、接口文档、缓存设计和 Docker 部署。

> 当前边界：后端业务、缓存和部署已形成可运行基线；根目录 `frontend/` 已初始化 Vue 3 + TypeScript + Vite，并已接入顾客认证、鉴权、桌位查询和预约创建提交链路，后续预约查询/取消、开台、点餐和商户端仍在开发。V1/V2 完成状态只指后端范围，候选功能不作为当前已完成功能。

## 仓库结构

```text
food-flow-manager/
├── backend/                  # 当前 Spring Boot 单体 Maven 工程
│   ├── src/
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   ├── .mvn/
│   ├── .dockerignore
│   └── Dockerfile
├── frontend/                 # Vue 3 + TypeScript + Vite 前端工程
├── docker-compose.yml        # 根目录统一编排 MySQL、Redis 和后端
├── assets/schema.sql         # 数据库初始化运行时资产
├── documents/                # 当前、规划、指南、记录和归档
└── .gitignore / .gitattributes
```

当前只完成后端工程目录分离，仍是按业务包组织的单体应用；未来是否转为 Maven 多模块，需要单独建立迁移计划，不属于本次整理范围。

## 项目简介

V1 的目标是先做成一个可运行、可测试、可复盘的最小业务闭环。

核心业务场景：

- 用户注册登录后查看桌位并创建预约。
- 用户到店后可通过预约扫码开台，或直接扫码占座。
- 用户在有效开台会话下查看启售菜品并创建堂食订单。
- 商户端可查看订单并按流程更新订单状态。
- 店员可在用餐结束后清台，释放桌位。
- 店长可管理员工、桌位、菜品分类等高权限功能。

## 核心业务流程

```text
用户注册登录
  -> 查看空闲桌位
  -> 创建预约 / 直接扫码占座
  -> 到店扫码开台
  -> 查看启售菜品
  -> 创建堂食订单
  -> 商户处理订单
  -> 店员清台释放桌位
```

核心状态流转：

- 桌位：`FREE -> RESERVED -> WAITING -> DINING -> FREE`
- 预约：`WAITING_CHECK_IN -> CHECKED_IN / CANCELED`
- 开台会话：`WAITING -> DINING -> COMPLETED / CANCELED`
- 订单：`PLACED -> COOKING -> SERVED -> COMPLETED`

## 技术栈

- Java 17
- Spring Boot 3.5.x
- MyBatis-Plus 3.5.x
- MySQL 8.x
- Redis
- Docker / Docker Compose
- Maven Wrapper
- JWT
- BCrypt
- Knife4j / OpenAPI

## 项目亮点

- 完成用户端、商户端、店长端的接口分层设计，覆盖预约、开台、点餐、订单处理、清台等核心链路。
- 使用 JWT 完成登录认证，并通过 `loginType` 区分用户和员工，通过 `role` 区分店员和店长。
- 自定义拦截器完成用户端、管理端、店长权限隔离，并支持账号禁用后旧 token 立即失效。
- 引入 `dining_session` 作为预约、桌位、订单之间的业务上下文，避免只靠桌位状态承载复杂业务。
- 在 Service 层统一校验实体归属和业务状态，减少跨模块调用带来的状态穿透。
- 实体内部使用状态枚举进行业务判断，接口边界使用数字 code 传输，兼顾可读性和兼容性。
- 通过 MyBatis-Plus 分页插件统一管理端列表查询方式。
- 使用 Redis 落地启售菜品缓存、菜品详情缓存、菜品分类缓存、账号状态缓存和防重复提交令牌。
- 使用 Docker Compose 编排 MySQL、Redis 和后端服务，支持 Linux 服务器部署。
- 配套保留设计文档、过程资产、V2 待办清单和复盘文档，便于后续迭代与面试表达。

## 功能模块

- 用户认证：用户注册、登录、JWT 签发。
- 员工认证与管理：员工登录、员工新增、启用、禁用、店长权限控制。
- 桌位管理：桌位新增、修改、删除、启用、禁用、用户查看空闲桌位。
- 预约管理：用户创建预约、取消预约、查看预约，商户异常取消预约。
- 开台会话：预约扫码开台、非预约扫码占座、查看当前开台、释放等待中桌位、清台。
- 菜品分类：商户端菜品分类基础维护，用户端启用分类缓存。
- 菜品管理：商户端菜品 CRUD、上下架，用户端查看启售菜品和菜品详情。
- 堂食订单：用户创建订单、查看订单列表和详情，商户查看订单并更新订单状态。
- Redis 支持：启售菜品缓存、菜品详情缓存、分类缓存、账号状态缓存、防重复提交令牌。
- 分页查询：管理端桌位、预约、开台会话、菜品、菜品分类、员工、订单分页查询。

## 快速开始

本节用于本地开发启动。如果使用 Docker 部署，可直接查看后面的“Docker 部署”。

### 1. 环境要求

- JDK 17+
- MySQL 8.x
- Redis
- Maven Wrapper，位于 `backend/mvnw.cmd`

### 2. 初始化数据库

在 MySQL 客户端中执行：

```sql
source assets/schema.sql;
```

也可以直接运行项目中的初始化脚本：

```text
assets/schema.sql
```

脚本会创建 `food_flow_manager` 数据库、V1/V2 基础表，并插入默认店长账号和基础菜品分类。默认账号信息以 `schema.sql` 中的数据为准。

### 3. 修改本地配置

根据本机 MySQL 和 Redis 配置修改：

```text
backend/src/main/resources/application-dev.yaml
```

重点确认：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.data.redis.host`
- `spring.data.redis.port`

### 4. 启动项目

macOS / Linux：

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

默认访问地址：

```text
http://localhost:8080
```

### 5. 构建验证

macOS / Linux：

```bash
cd backend
./mvnw clean test
```

Windows PowerShell：

```powershell
cd backend
.\mvnw.cmd clean test
```

## Docker 部署

当前 Docker 部署采用 Docker 多阶段构建。部署者只需要拉取仓库并执行 `docker compose up -d --build`，Docker 会在构建阶段自动完成 Maven 打包，不再需要手动生成、复制或改名 jar 包。

### 1. 服务器环境要求

- Ubuntu 24.04 或其他 Linux 服务器。
- 已安装 Docker。
- 已安装 Docker Compose。
- 服务器能访问 Git 仓库。
- 服务器能拉取基础镜像，例如 `eclipse-temurin:17-jdk-jammy`、`eclipse-temurin:17-jre-jammy`、`mysql:8`、`redis:8.6.2`。

### 2. 拉取项目

建议将项目放在固定部署目录，例如：

```bash
cd /opt
git clone https://github.com/hotwater-wty/food-flow-manager.git food-flow-manager
cd food-flow-manager
```

如果目录已经存在，则进入项目目录后拉取最新代码：

```bash
git pull
```

### 3. 确认项目目录权限

Ubuntu 下推荐将项目目录所有者设置为当前用户，避免后续 `git pull` 或 Docker 构建时出现权限问题：

```bash
sudo chown -R $(whoami):$(whoami) /opt/food-flow-manager
```

当前多阶段 Dockerfile 会在容器构建阶段执行 Maven Wrapper。部署者通常不需要手动执行 `chmod +x mvnw`、`./mvnw clean package` 或复制 jar 包。

项目根目录中应存在：

```text
docker-compose.yml
assets/schema.sql
backend/Dockerfile
```

### 4. 一键构建并启动容器

在 `docker-compose.yml` 所在目录执行：

```bash
docker compose up -d --build
```

该命令会自动完成：

- 使用 `backend/` 作为构建上下文并构建后端应用镜像。
- 在构建阶段执行 Maven 打包。
- 启动 MySQL、Redis 和后端服务容器。
- 挂载 `assets/schema.sql` 作为 MySQL 初始化脚本。

Compose 位于根目录，后端 Dockerfile 位于 `backend/Dockerfile`；如果调整任一位置，必须同步检查 `build.context`、Dockerfile 路径和 SQL 挂载路径。

### 5. 验证部署

查看容器状态：

```bash
docker compose ps
```

查看应用日志：

```bash
docker compose logs -f food-flow-manager
```

访问健康检查：

```bash
curl http://localhost:8080/actuator/health
```

访问 Knife4j 接口文档：

```text
http://服务器IP:8080/doc.html
```

## 接口文档

启动项目后访问 Knife4j：

```text
http://localhost:8080/doc.html
```

OpenAPI JSON 地址：

```text
http://localhost:8080/v3/api-docs
```

业务接口设计稿：

```text
documents/architecture/backend/核心接口清单设计.md
documents/architecture/backend/接口规范设计.md
```

## 前端文档

前端目标规格采用 Vue 3、TypeScript、Vite、Pinia、Vue Router 和 Axios；顾客端计划使用 Vant，商户端计划使用 Element Plus。当前 `frontend/` 已初始化并完成顾客认证、鉴权、桌位查询和预约创建提交链路，UI 组件库仍未引入。

前端文档入口：

```text
documents/frontend/README.md
documents/frontend/06-纵向切片开发计划.md
```

前端首版计划以“顾客登录 -> 预约或模拟扫码 -> 点餐 -> 查看订单；店员登录 -> 处理订单 -> 清台”的真实后端闭环为验收目标。

## 数据库设计说明

V1/V2 核心表包括：

- `user`：普通用户。
- `employee`：员工账号。
- `dining_table`：餐桌。
- `reservation`：预约。
- `dining_session`：开台会话。
- `dish_category`：菜品分类。
- `dish`：菜品。
- `dining_order`：堂食订单。
- `dining_order_item`：订单明细。

数据库初始化脚本：

```text
assets/schema.sql
```

领域模型和表设计说明：

```text
documents/architecture/backend/领域模型与核心表设计.md
```

## V1 已完成功能

- 用户注册、登录。
- 员工登录和员工基础管理。
- JWT 鉴权、用户端/管理端隔离、店长权限控制。
- 禁用用户或员工后，旧 token 请求被拦截。
- 桌位管理与用户查看空闲桌位。
- 用户预约、取消预约、查看预约。
- 预约扫码开台、非预约扫码占座。
- 用户查看当前开台会话。
- 菜品分类和菜品管理。
- 用户查看启售菜品。
- 用户创建堂食订单、查看订单列表和详情。
- 商户查看订单列表和详情。
- 商户按顺序更新订单状态。
- 店员释放等待中桌位。
- 店员清台并释放桌位。

## V2 已完成内容

- 关键并发场景改为条件更新或数据库约束兜底处理。
- 活跃开台会话增加唯一性约束思路，并在业务层做双重校验。
- 管理端核心列表接口改为分页查询。
- Redis 落地启售菜品缓存、菜品详情缓存、分类缓存、账号状态缓存、防重复提交令牌。
- 缓存访问逻辑统一抽取到 `CacheUtil` 和各业务缓存 Client。
- 新增防重复提交令牌的接口与使用方式。
- Docker Compose 支持 MySQL、Redis 和后端服务协同启动。
- 补充了大量接口注解，Knife4j / OpenAPI 可展示更完整的接口信息。

## 后续候选方向

V2 的主要增强已经完成，后续更多属于继续迭代的方向：

- 预约时间策略和超时处理。
- 操作日志。
- Spring Security 权限体系升级。
- 消息队列与异步通知。
- 更完整的自动化测试。

历史候选池仅供追溯，不作为当前任务来源；需要启动后端增强时，先在 `documents/planning/` 建立当前阶段计划并校准源码。

```text
documents/archive/backend/tasks/V2待办任务清单.md
documents/archive/backend/requirements/V2业务增强功能清单.md
```

## 项目文档索引

文档总入口：

- `documents/README.md`
- `documents/CURRENT.md`

后端架构文档：

- `documents/architecture/backend/核心业务流程设计.md`
- `documents/architecture/backend/领域模型与核心表设计.md`
- `documents/architecture/backend/核心接口清单设计.md`
- `documents/architecture/backend/接口规范设计.md`
- `documents/architecture/backend/技术选型与依赖规划.md`
- `documents/architecture/backend/项目包结构设计.md`

历史收尾与迭代记录：

- `documents/archive/backend/plans/V1收尾阶段行动计划.md`
- `documents/archive/backend/plans/v1阶段开发计划.md`
- `documents/archive/backend/plans/v2阶段开发计划.md`
- `documents/archive/backend/tasks/V2待办任务清单.md`
- `documents/archive/backend/requirements/`

过程资产：

- `documents/records/reviews/V1阶段项目复盘.md`
- `documents/records/reviews/V2阶段项目复盘.md`
- `documents/records/issues/技术实现问题记录.md`
- `documents/records/decisions/工程设计决策记录.md`
- `documents/records/topics/`

历史归档：

- `documents/archive/backend/`
- `documents/archive/drafts/`

文档维护规则：

- `documents/guides/项目文档格式说明.md`
- `documents/guides/过程资产维护手册.md`
