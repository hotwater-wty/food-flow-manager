# 膳畅管家前端立项空间

这里记录 `food-flow-manager` 前端补建之前的产品、交互、工程和 AI 协作决策。

> 文档状态：当前事实(2026-08-28 更新)
> 实现状态：前端已拆分为 pnpm workspace 双应用——`apps/customer`(顾客端,Vant,dev 5174)与 `apps/admin`(商户端,Element Plus,dev 5173),共享契约包 `packages/shared`;已完成顾客注册/登录、预约/开台、点餐购物车(已入 Pinia)、订单、经营概览仪表盘、订单/会话工作台与资料维护,并通过 E2E 冒烟
> 当前后端事实入口：[`../architecture/backend/核心接口清单设计.md`](../architecture/backend/核心接口清单设计.md)

前端工程位于仓库根目录 `apps/` 与 `packages/shared/`;当前计划见 `documents/planning/`,一期历史记录保留在 `06-纵向切片开发计划.md`。

## 当前结论

- 技术：Vue 3、TypeScript、Vite、Pinia、Vue Router、Axios。
- 顾客端：手机优先，使用 Vant。
- 商户端：桌面优先，使用 Element Plus。
- 首版以一条可演示业务闭环为验收单位：顾客登录 -> 预约/模拟扫码 -> 点餐 -> 订单；店员登录 -> 处理订单 -> 清台。
- 扫码首版使用 `/customer/scan?tableId=...` 模拟，不实现真实二维码部署。
- AI 按规格分步实现，每个任务必须有文件边界和可操作验收证据。
- 阶段 6 已接入桌位、菜品、分类、预约和员工资料维护工作台，并完成真实数据复验、测试和文档收尾；本阶段前端开发结束。

## 文档索引

1. [前端项目决策总览](./01-前端项目决策总览.md)
2. [产品范围与用户旅程](./02-产品范围与用户旅程.md)
3. [页面地图与交互状态](./03-页面地图与交互状态.md)
4. [视觉设计与组件规则](./04-视觉设计与组件规则.md)
5. [前端工程与接口契约](./05-前端工程与接口契约.md)
6. [纵向切片开发计划](./06-纵向切片开发计划.md)
7. [AI 协作与验收规则](./07-AI协作与验收规则.md)

## 开发入口

```bash
# 仓库根目录安装与启动(本机无全局 pnpm 时用 npx)
npx pnpm@11.19.0 install
npx pnpm@11.19.0 dev:customer   # 顾客端 http://localhost:5174
npx pnpm@11.19.0 dev:admin      # 商户端 http://localhost:5173
pnpm install
pnpm dev
```

后端默认运行在 `http://localhost:8080`，Vite 开发服务器会通过代理转发 `/api` 请求。
