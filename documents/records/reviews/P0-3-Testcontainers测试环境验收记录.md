# P0-3 Testcontainers 测试环境验收记录

> 验收日期：2026-08-30
> 范围：后端集成测试从开发容器迁移到 Testcontainers
> 计划：[`../../planning/project-development-plan.md`](../../planning/project-development-plan.md)

## 1. 实现内容

- `backend/pom.xml` 增加 Testcontainers JUnit 5、MySQL 和通用容器测试依赖。
- 新增 `backend/src/test/java/com/foodflow/testsupport/IntegrationTestContainers.java`，统一管理 MySQL/Redis 容器生命周期。
- MySQL 使用随机映射端口并执行 `schema.sql`；Redis 使用随机映射端口并启用测试密码 `1234`。
- `@DynamicPropertySource` 将容器运行时地址注入 Spring；`@ActiveProfiles("test")` 标记测试环境。
- P0-2 的直接开台测试和 Spring 上下文测试继承测试基础类，业务断言保持不变。
- 当前将 `assets/schema.sql` 复制到 `backend/src/test/resources/schema.sql` 供 classpath 初始化；P0-7 数据库迁移治理时再消除双份 SQL。

## 2. 验收证据

执行前已停止开发 Compose：

```bash
docker compose -f docker-compose.dev.yml stop
```

执行：

```bash
cd backend
./mvnw test
```

结果：9 个后端测试全部通过。日志显示 Testcontainers 启动 `mysql:8.4` 和 `redis:8.6.2`，MySQL 使用随机端口并成功执行 `schema.sql`；未连接开发 Compose 的固定端口。

## 3. 后续边界

- 基础类使用测试 JVM 级共享容器。Spring 会跨测试类缓存 ApplicationContext，如果按测试类停止容器，后续测试可能复用已经失效的 DataSource；因此容器在 JVM 退出时由 Testcontainers/Ryuk 清理。
- Testcontainers 只解决依赖环境隔离，不替代业务测试、事务断言或 CI。
- P0-4 已沿用该基础类补充预约、下单、状态推进和清台测试。
