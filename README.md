# food-flow-manager 膳畅管家

`food-flow-manager` 是一个基于 Spring Boot 3 的餐饮预约与堂食管理后端项目。项目中文名为“膳畅管家”，V1 阶段聚焦单门店场景，完成从用户预约、到店开台、堂食点餐、商户处理订单到清台释放桌位的主业务闭环。

项目定位不是简单 CRUD 练习，而是围绕后端开发中的真实问题进行实践：需求收敛、状态流转、角色鉴权、跨模块业务协作、接口测试和项目文档沉淀。

## 项目简介

V1 目标是先做成一个可运行、可测试、可复盘的最小业务闭环。

核心场景：

- 用户可以注册登录、查看桌位、创建预约。
- 用户到店后可以通过预约扫码开台，或未预约直接扫码占座。
- 用户在有效开台会话下查看菜品并创建堂食订单。
- 商户端员工可以查看订单、按状态处理订单。
- 店员可以在用餐结束后清台，释放桌位。
- 店长可以管理员工、删除桌位等高权限操作。

V1 暂不包含支付、排班、消息通知、复杂营销、前端页面和分布式部署，这些内容作为 V2 或后续增强方向。

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
- Maven Wrapper
- JWT
- BCrypt
- Knife4j / OpenAPI

## 项目亮点

- 完成用户端和商户端双端接口设计，覆盖预约、开台、点餐、订单处理、清台等核心链路。
- 使用 JWT 实现登录认证，并通过 `loginType` 区分普通用户和员工，通过 `role` 区分店员和店长。
- 自定义拦截器完成用户端、管理端、店长权限隔离，并支持禁用账号后旧 token 被拦截。
- 引入开台会话 `dining_session` 作为预约、桌位、订单之间的业务上下文，避免只依赖桌位状态承载复杂业务。
- 在 Service 层统一校验实体归属和业务状态，防止绕过 Controller 入口后破坏状态流转。
- 实体内部使用状态枚举进行业务判断，接口边界使用数字 code 传输，兼顾可读性和接口兼容性。
- 使用 Knife4j / OpenAPI 提供接口文档，并配合 Apifox 进行接口测试。
- 保留完整的设计文档、过程资产、V2 待办清单，便于后续复盘和迭代。

## 功能模块

- 用户认证：用户注册、登录、JWT 签发。
- 员工认证与管理：员工登录、员工新增、启用、禁用、店长权限控制。
- 桌位管理：桌位新增、修改、删除、启用、禁用、用户查看空闲桌位。
- 预约管理：用户创建预约、取消预约、查看预约，商户异常取消预约。
- 开台会话：预约扫码开台、非预约扫码占座、查看当前开台、释放等待中桌位、清台。
- 菜品分类：商户端菜品分类基础维护。
- 菜品管理：商户端菜品 CRUD、上下架，用户端查看启售菜品。
- 堂食订单：用户创建订单、查看订单列表和详情，商户查看订单并更新订单状态。

## 快速开始

本节用于本地开发启动。如果使用 Docker 部署，可直接查看后面的“Docker 部署”。

### 1. 环境要求

- JDK 17+
- MySQL 8.x
- Maven Wrapper，项目已包含 `mvnw.cmd`

### 2. 初始化数据库

在 MySQL 客户端中执行：

```sql
source assets/schema.sql;
```

也可以直接运行项目中的初始化脚本：

```text
assets/schema.sql
```

脚本会创建 `food_flow_manager` 数据库、V1 核心表，并插入默认店长账号和基础菜品分类。默认账号信息以 `schema.sql` 中的数据为准。

### 3. 修改本地配置

根据本机 MySQL 配置修改：

```text
src/main/resources/application-dev.yaml
```

重点确认：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 4. 启动项目

```powershell
.\mvnw.cmd spring-boot:run
```

默认访问地址：

```text
http://localhost:8080
```

### 5. 构建验证

```powershell
.\mvnw.cmd clean test
```

## Docker 部署

当前 Docker 部署采用“服务器本地打包 jar，再由 Docker Compose 构建并启动容器”的方式。

### 1. 服务器环境要求

- Ubuntu 24.04 或其他 Linux 服务器。
- 已安装 Docker。
- 已安装 Docker Compose。
- 服务器能访问 Git 仓库。
- 服务器能拉取基础镜像，例如 `eclipse-temurin:17-jre-jammy`、`mysql:8`。

### 2. 拉取项目

建议将项目放在固定部署目录，例如：

```bash
cd /opt
git clone <repository-url> food-flow-manager
cd food-flow-manager
```

如果目录已经存在，则进入项目目录后拉取最新代码：

```bash
git pull
```

### 3. 在服务器本地打包 jar

Linux 下执行 Maven Wrapper 前，需要确认 `mvnw` 具有可执行权限：

```bash
chmod +x mvnw
```

`chmod +x mvnw` 的作用是给 `mvnw` 脚本增加可执行权限。Windows 使用 `mvnw.cmd`，Linux 使用 `mvnw`。

执行打包：

```bash
./mvnw clean package -DskipTests
```

将 Maven 生成的 jar 复制到 Dockerfile 同级目录，并改名为 Dockerfile 中约定的名称：

```bash
cp target/food-flow-manager-0.0.1-SNAPSHOT.jar food-flow-manager.jar
```

最终项目根目录中应存在：

```text
Dockerfile
docker-compose.yml
food-flow-manager.jar
assets/schema.sql
```

### 4. 启动容器

在 `docker-compose.yml` 所在目录执行：

```bash
docker compose up -d --build
```

当前部署方式要求 `docker-compose.yml`、`Dockerfile`、`food-flow-manager.jar` 位于同一项目目录中。不要单独移动 `docker-compose.yml`，否则需要同步调整 `build.context`、Dockerfile 路径、jar 路径和 SQL 挂载路径。

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

如果后续补充了更完整的 Knife4j / OpenAPI 注解，接口文档会展示更完整的接口说明、参数信息和响应结构。

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
assets/核心接口清单设计.md
assets/接口规范设计.md
```

## 数据库设计说明

V1 核心表包括：

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
assets/领域模型与核心表设计.md
```

## V1 已完成功能

- 用户注册、登录。
- 员工登录和员工基础管理。
- JWT 鉴权、用户端/商户端隔离、店长权限控制。
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

## V2 规划

V2 不推翻 V1 模型，而是在主流程稳定后补强工程能力和业务严谨性。

重点方向：

- 预约、扫码开台、创建订单、清台的并发控制。
- 活跃开台会话唯一性约束。
- 预约编号、订单编号、开台编号生成策略升级。
- 分页查询。
- 账号状态缓存和菜品缓存。
- 预约时间策略和超时处理。
- 操作日志。
- Spring Security 权限体系升级。
- Docker 化部署。
- Redis、MQ 等中间件实践。

详细任务见：

```text
assets/V2待办任务清单.md
assets/V2业务增强功能清单.md
```

## 项目文档索引

正式设计文档：

- `assets/餐饮预约与堂食排班管理系统项目计划书.md`
- `assets/V1最小业务闭环功能清单.md`
- `assets/V2业务增强功能清单.md`
- `assets/核心业务流程设计.md`
- `assets/领域模型与核心表设计.md`
- `assets/核心接口清单设计.md`
- `assets/接口规范设计.md`
- `assets/技术选型与依赖规划.md`
- `assets/项目包结构设计.md`

收尾和迭代文档：

- `assets/V1收尾阶段行动计划.md`
- `assets/v1阶段开发计划.md`
- `assets/V2待办任务清单.md`

过程资产：

- `assets/过程资产/V1阶段项目复盘.md`
- `assets/过程资产/技术实现问题记录.md`
- `assets/过程资产/工程设计决策记录.md`
- `assets/过程资产/专题文档/`
