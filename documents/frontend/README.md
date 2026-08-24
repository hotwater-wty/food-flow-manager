# 膳畅管家前端立项空间

这里记录 `food-flow-manager` 前端补建之前的产品、交互、工程和 AI 协作决策。

> 文档状态：当前目标规格
> 实现状态：根目录 `frontend/` 已完成 Vue 3 + TypeScript + Vite 初始化，并已接入顾客认证、受保护路由、桌位查询和预约创建提交链路；预约查询与取消等后续切片尚未实现
> 当前后端事实入口：[`../architecture/backend/核心接口清单设计.md`](../architecture/backend/核心接口清单设计.md)

正式前端工程位于项目根目录的 `frontend/`，当前源码和依赖配置已存在；实现状态以 `documents/planning/frontend-development-plan.md` 与真实代码为准。

## 当前结论

- 技术：Vue 3、TypeScript、Vite、Pinia、Vue Router、Axios。
- 顾客端：手机优先，使用 Vant。
- 商户端：桌面优先，使用 Element Plus。
- 首版以一条可演示业务闭环为验收单位：顾客登录 -> 预约/模拟扫码 -> 点餐 -> 订单；店员登录 -> 处理订单 -> 清台。
- 扫码首版使用 `/customer/scan?tableId=...` 模拟，不实现真实二维码部署。
- AI 按规格分步实现，每个任务必须有文件边界和可操作验收证据。

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
cd frontend
pnpm install
pnpm dev
```

后端默认运行在 `http://localhost:8080`，Vite 开发服务器会通过代理转发 `/api` 请求。
