# 膳畅管家前端事实入口

这里记录 `food-flow-manager` 双前端当前的产品、交互、工程和协作约定。

> 文档状态：当前事实（2026-08-29 更新）
> 实现状态：前端为 pnpm workspace 双应用：`apps/customer`（顾客端，Vant，dev 5174）与 `apps/admin`（商户端，Element Plus，dev 5173），共享契约包 `packages/shared`；已完成首页选座开台、点餐/预约/订单、经营概览、订单/会话工作台、资料维护、统一写反馈和布局级 SSE
> 当前后端事实入口：[`../architecture/backend/核心接口清单设计.md`](../architecture/backend/核心接口清单设计.md)

前端工程位于仓库根目录 `apps/` 与 `packages/shared/`；当前计划见 [`../planning/project-development-plan.md`](../planning/project-development-plan.md)，历史阶段资料见 [`../archive/frontend/`](../archive/frontend/)。

## 当前结论

- 技术：Vue 3、TypeScript、Vite、Pinia、Vue Router、Axios。
- 顾客端：手机优先，使用 Vant；顾客 JWT、路由和请求层独立。
- 商户端：桌面优先，使用 Element Plus；员工 JWT、路由和请求层独立。
- Pinia 保存认证、顾客购物车、管理端通知和操作反馈状态；跨端只共享类型、格式化和状态映射。
- 当前顾客闭环：登录 -> 首页选择空闲桌位或预约到店 -> 点餐 -> 查看订单。
- 当前商户闭环：员工登录 -> 经营概览/订单/会话 -> 资料维护 -> 清台。
- 二维码和摄像头扫码方案已暂停；首页直接选座是开发/演示入口，生产化前需补店内证明或员工确认机制。
- 管理端按钮写操作由 HTTP 结果负责即时反馈，SSE 只通知外部业务变化并触发 REST 回查。

## 文档索引

1. [前端项目决策总览](./01-前端项目决策总览.md)
2. [产品范围与用户旅程](./02-产品范围与用户旅程.md)
3. [页面地图与交互状态](./03-页面地图与交互状态.md)
4. [视觉设计与组件规则](./04-视觉设计与组件规则.md)
5. [前端工程与接口契约](./05-前端工程与接口契约.md)
6. [AI 协作与验收规则](./07-AI协作与验收规则.md)
7. [当前项目发展计划](../planning/project-development-plan.md)

## 开发入口

```bash
pnpm install
pnpm dev:customer   # 顾客端 http://localhost:5174
pnpm dev:admin      # 商户端 http://localhost:5173
```

常用验收：

```bash
pnpm type-check
pnpm lint
pnpm format:check
pnpm -r build
pnpm test
pnpm test:e2e
```

后端默认运行在 `http://localhost:8080`，两个 Vite 开发服务器通过代理转发 `/api` 请求。
