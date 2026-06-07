# food-flow-manager 膳畅管家

`food-flow-manager` 是一个基于 Spring Boot 的餐饮预约与堂食管理后端项目。项目中文名为“膳畅管家”，V1 阶段聚焦单门店场景，目标是打通从用户预约、到店开台、堂食下单、商户处理订单到清台释放桌位的完整业务闭环。

## 1. 项目定位

本项目主要用于后端项目实践和求职作品沉淀。V1 不追求复杂营销、支付、排班、消息通知等外围能力，而是优先训练后端开发中最核心的能力：

- 需求拆分与版本范围控制。
- 业务流程建模与状态流转。
- 领域模型、数据库表、接口设计。
- Spring Boot + MyBatis-Plus 项目开发。
- JWT 鉴权、角色权限、统一异常与统一响应。
- 事务控制、跨模块业务协作和接口测试。

## 2. 技术栈

- Java 17
- Spring Boot 3.5.x
- MyBatis-Plus 3.5.x
- MySQL 8.x
- JWT
- BCrypt 密码加密
- Knife4j / OpenAPI
- Maven

## 3. 核心业务流程

```text
用户注册登录
  -> 查看桌位
  -> 创建预约或直接扫码占座
  -> 到店扫码开台
  -> 查看菜品
  -> 创建堂食订单
  -> 商户处理订单
  -> 店员清台释放桌位
```

核心状态流转：

- 桌位：`FREE -> RESERVED -> WAITING -> DINING -> FREE`
- 预约：`WAITING_CHECK_IN -> CHECKED_IN / CANCELED`
- 开台会话：`WAITING -> DINING -> COMPLETED / CANCELED`
- 订单：`PLACED -> COOKING -> SERVED -> COMPLETED`

## 4. 模块说明

- 用户认证：用户注册、登录、JWT 签发。
- 员工认证与管理：员工登录、新增员工、启用/禁用员工、店长权限控制。
- 桌位管理：桌位 CRUD、启用/禁用、用户查看空闲桌位。
- 预约管理：用户预约、取消预约、预约到店核验、商户异常取消。
- 开台会话：预约扫码开台、非预约扫码占座、查看当前开台、释放等待中桌位、清台。
- 菜品与分类：菜品分类管理、菜品 CRUD、菜品上下架、用户查看启售菜品。
- 堂食订单：创建订单、订单明细、用户查看订单、商户处理订单状态。

## 5. 本地启动

### 5.1 准备环境

- JDK 17+
- Maven Wrapper，项目已包含 `mvnw.cmd`
- MySQL 8.x

### 5.2 初始化数据库

执行：

```sql
source assets/schema.sql;
```

或在 MySQL 客户端中运行 [assets/schema.sql](assets/schema.sql)。

初始化脚本会创建 `food_flow_manager` 数据库、V1 核心表，并插入默认店长账号和基础菜品分类。

### 5.3 修改本地配置

根据本机 MySQL 配置修改：

```text
src/main/resources/application-dev.yaml
```

需要重点确认：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 5.4 启动项目

```powershell
.\mvnw.cmd spring-boot:run
```

默认端口：

```text
http://localhost:8080
```

### 5.5 构建验证

```powershell
.\mvnw.cmd clean test
```

## 6. 接口文档

项目已引入 Knife4j / OpenAPI 依赖。启动项目后可访问：

```text
http://localhost:8080/doc.html
```

OpenAPI JSON 地址通常为：

```text
http://localhost:8080/v3/api-docs
```

当前接口文档的业务设计稿见：

```text
assets/核心接口清单设计.md
assets/接口规范设计.md
```

## 7. V1 已完成功能

- 用户注册、登录。
- 员工登录、员工基础管理。
- JWT 鉴权、用户端/商户端隔离、店长权限控制。
- 禁用账号请求拦截。
- 桌位管理与用户查看空闲桌位。
- 用户预约、取消预约、查看预约。
- 预约扫码开台、非预约扫码占座。
- 用户查看当前开台。
- 菜品分类和菜品管理。
- 用户查看启售菜品。
- 用户创建堂食订单、查看订单列表和详情。
- 商户查看订单列表和详情。
- 商户按顺序更新订单状态。
- 店员释放等待中桌位。
- 店员清台并释放桌位。

## 8. V2 规划

V2 不推翻 V1 模型，而是在 V1 主流程稳定的基础上增强工程能力。重点方向包括：

- 并发控制和活跃开台唯一性约束。
- 编号生成策略升级。
- 分页查询。
- Redis 缓存与验证码。
- 预约超时自动处理和定时任务。
- 消息队列通知。
- 操作日志。
- Spring Security 权限体系升级。
- Docker 化部署。

详细待办见：

```text
assets/V2待办任务清单.md
assets/V2业务增强功能清单.md
```

## 9. 过程资产

项目开发过程中的问题、决策和专题笔记保存在：

```text
assets/过程资产/
```

其中：

- `技术实现问题记录.md`：具体 Bug、实现坑和修复方式。
- `工程设计决策记录.md`：工程边界、职责划分和方案取舍。
- `专题文档/`：可复用技术专题。
- `V1阶段项目复盘.md`：V1 阶段复盘和面试表达素材。

