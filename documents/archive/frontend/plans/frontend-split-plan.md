# 前端拆分阶段计划:双应用 workspace 化

> 立项日期:2026-08-28
> 状态:**已收口**——S1 至 S4 全部完成(2026-08-28),验收见 [`../records/reviews/前端拆分S1-S4验收记录.md`](../../../records/reviews/前端拆分S1-S4验收记录.md)
> 上游决策:用户确认"AI 完成、5173/5174 端口区分、暂不引入 nginx",基于 [`../frontend/08-双端拆分准备评估.md`](../assessments/双端拆分准备评估.md) 推荐路径执行

## 1. 背景与目标

三期 R6 评估确认拆分可行性后,用户提出三点要求:用户端与商户端拆成独立工程;浏览器访问用不同地址区分(现阶段端口、未来域名);砍断两端互通通道(如商户端品牌位跳顾客端且无法返回)。目标:两个可独立构建、独立运行、独立部署的前端应用 + 一个共享契约包。

## 2. 已确认决策

| 决策项 | 结论 |
| --- | --- |
| 仓库形态 | 单仓库 pnpm workspace(apps/* + packages/shared),不做双仓库 |
| 端口 | admin 5173、customer 5174(strictPort),nginx 双域名部署后置并需单独授权 |
| 共享范围 | 仅契约投影:types/api.ts、utils/format、utils/status、utils/order-status;UI 组件不共享(双 UI 库) |
| 旧地址 | 双端各自保留 `/{前缀}/**` 兜底重定向,旧书签不失效 |

## 3. 切片与完成情况

- **S1 workspace 脚手架(已完成)**:`pnpm-workspace.yaml` + 根 package.json 统一脚本;`frontend/` 整体 git mv 到 `apps/admin`;types/utils 迁入 `packages/shared`(TS 源码直引,`@foodflow/shared/*` 子路径导出);admin 改 45 处导入、tsconfig paths + vite alias。
- **S2 商户端归位(已完成)**:删除顾客视图/服务/Store/CustomerLayout;路由去掉 `/admin` 前缀(根路径进订单工作台);http.ts 删 URL 前缀嗅探(单员工 token);删除全部顾客端互通链接;style.css 剔除顾客端样式;移除 Vant 依赖。
- **S3 顾客端拆出(已完成)**:删除管理端文件;路由扁平化(`/menu`、`/orders`…),仅顾客守卫;http.ts 单顾客 token;main.ts 仅装 Vant;style.css/tokens.css 剔除管理端内容;5174 端口。
- **S4 验收(已完成)**:见验收记录;顺手修复菜单硬编码旧路径与 pathMatch 数组重定向两个缺陷。

## 4. 范围与边界

- 允许修改:`apps/**`、`packages/**`、根 `package.json`/`pnpm-workspace.yaml`、`documents/**`、根 `README.md`。
- 禁止:nginx/Docker/部署配置变更(后置)、后端代码变更、两端 UI 行为变化(仅结构重组)。

## 5. 遗留与后置

- 生产双域名部署(nginx server_name + 可选 CORS 核对):等部署需求出现时另立任务。
- customer 侧 `stores/app.ts` 为无引用占位,可在下次切片删除。
- `usePagedList` 目前仅 admin 使用;顾客端引入分页列表时再考虑移入 shared。
