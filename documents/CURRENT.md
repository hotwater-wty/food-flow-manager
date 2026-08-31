# 当前文档状态

> 更新日期：2026-08-31

## 当前主线

后端 V1/V2 主业务和工程增强、前端一期至三期、双应用拆分、首页选座、管理端统一写操作反馈和布局级 SSE 均已完成。当前代码基线是 Spring Boot 单体 + MySQL/Redis，以及 `apps/customer`、`apps/admin`、`packages/shared` 组成的 pnpm workspace。

历史阶段已归档，当前进入“可靠性基线、后端能力补全、全栈新业务和生产部署”规划阶段。唯一活跃计划是 [`planning/project-development-plan.md`](planning/project-development-plan.md)。二维码方案继续暂停；首页直接选座是当前开发/演示入口，公开部署前需要补充店内证明或员工确认机制。

## 当前权威资料

| 主题 | 权威位置 | 说明 |
| --- | --- | --- |
| 项目完成范围和启动方式 | 根目录 [`README.md`](../README.md) | 以代码和运行结果为准 |
| 当前发展路线 | [`planning/project-development-plan.md`](planning/project-development-plan.md) | 唯一活跃计划，不自动开工 |
| 后端接口与状态 | [`architecture/backend/`](architecture/backend/) | 设计事实；冲突时以源码/OpenAPI 为准 |
| 前端产品、页面和契约 | [`frontend/`](frontend/) | 当前双应用事实 |
| 开发和验收规则 | 根目录 [`AGENTS.md`](../AGENTS.md)、[`guides/开发流程手册.md`](guides/开发流程手册.md) | 每次只推进一个切片 |
| 历史验收和复盘 | [`records/`](records/) | 解释结论如何形成，不替代当前事实 |
| 已完成前端阶段 | [`archive/frontend/`](archive/frontend/) | 只用于历史追溯 |

## 已知边界

- 后端核心业务自动化测试不足，当前只有少量上下文、响应契约和 SSE ticket 测试。
- 用户端订单与预约未分页；员工编辑/离职、顾客状态管理、审计日志、JWT 刷新/服务端登出未实现。
- 预约会立即锁定桌位，尚未支持真实的预约时间段、迟到过期和自动释放。
- SSE 当前为单实例内存实现；生产代理、多实例共享、心跳和监控未完成。
- 已新增本地 Nginx 演练 Compose，并在完整 Compose 中预留 `web` profile；CI 已验证 Nginx 静态镜像健康、双 Host 和 SPA 回退。正式域名/HTTPS、API/SSE 多服务 E2E、CD 发布、Secret 和数据库迁移治理仍未完成。

## 文档维护规则

- `planning/` 只保留当前计划；阶段完成后移入 `archive/`。
- 当前事实写入 `frontend/` 或 `architecture/backend/`，过程证据写入 `records/`。
- 归档资料中的路径和“当前”措辞只代表历史时点，新任务不得直接引用为事实。
- 启动或完成切片、接口/状态/权限变化、部署拓扑变化时同步更新本文。
