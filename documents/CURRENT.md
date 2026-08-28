# 当前文档状态

> 更新日期:2026-08-28

## 当前主线

后端 V1/V2 核心业务和工程增强已完成并冻结扩张(三期新增的只读统计端点除外)。前端一期(纵向业务闭环)、二期(布局重构与视觉统一)、三期(体验完善与商户端仪表盘)均已收口。**前端已于 2026-08-28 拆分为 pnpm workspace 双应用**:`apps/customer`(顾客端,Vant,dev 5174)与 `apps/admin`(商户端,Element Plus,dev 5173),共享契约包 `packages/shared`(types/api + utils);两端独立路由与请求层,旧地址 `/customer/**`、`/admin/**` 自动去前缀重定向,互通链接已全部移除;生产双域名部署(nginx)待单独授权。当前无进行中的开发阶段;候选事项见三期计划第 5 节与各复盘,不自动开工。进度详见 [`planning/frontend-split-plan.md`](planning/frontend-split-plan.md)(拆分)与 [`planning/frontend-phase3-plan.md`](planning/frontend-phase3-plan.md)(三期)。管理端使用独立员工 JWT。

## 当前权威资料

| 主题 | 权威位置 | 说明 |
| --- | --- | --- |
| 项目完成范围和启动方式 | 项目根目录 [`README.md`](../README.md) | 以代码和运行结果为准 |
| 后端接口、状态和数据模型 | [`architecture/backend/`](architecture/backend/) | 设计稿；发现差异时以代码为准 |
| 前端产品与技术决策 | [`frontend/01-前端项目决策总览.md`](frontend/01-前端项目决策总览.md) | 产品与技术目标规格 |
| 前端页面路由与守卫 | [`frontend/03-页面地图与交互状态.md`](frontend/03-页面地图与交互状态.md) | 二期收口后按实际路由树重写 |
| 前端视觉令牌与组件规则 | [`frontend/04-视觉设计与组件规则.md`](frontend/04-视觉设计与组件规则.md) | 规则+落地现状(令牌位置、主题桥接、已知取舍) |
| 前端接口契约 | [`frontend/05-前端工程与接口契约.md`](frontend/05-前端工程与接口契约.md) | 请求、鉴权和数据约定 |
| AI 协作边界 | [`frontend/07-AI协作与验收规则.md`](frontend/07-AI协作与验收规则.md) | 任务边界、证据和人工控制点 |
| 一期开发过程记录 | [`planning/frontend-development-plan.md`](planning/frontend-development-plan.md) | 一期历史记录,已标注收口 |
| 二期切片计划与审查结论 | [`planning/frontend-redesign-plan.md`](planning/frontend-redesign-plan.md) | 已收口的历史计划,含首版覆盖审查结论 |
| 三期计划与问题记录审查 | [`planning/frontend-phase3-plan.md`](planning/frontend-phase3-plan.md) | 已收口:切片清单、验收证据索引与候选池 |
| **前端双应用拆分计划** | [`planning/frontend-split-plan.md`](planning/frontend-split-plan.md) | 已收口:workspace 结构、端口约定与遗留事项 |
| 双端拆分准备评估 | [`frontend/08-双端拆分准备评估.md`](frontend/08-双端拆分准备评估.md) | 已执行;耦合点清单与决策项留档 |

## 归档判断

- V1/V2 阶段计划、旧功能清单和 V2 候选任务已完成当前阶段职责，统一放在 `archive/backend/`。
- 初始项目计划书和草稿不删除，作为需求演化和范围收敛的历史证据。
- 归档文档不得被 README 或新任务当作当前计划引用；需要复用时先提炼到当前设计或前端计划。

## 下一次更新触发条件

- 启动新的前端或全栈阶段(先另立计划,不沿用已收口文档充当现状)。
- 后端接口、状态码、权限或数据字段发生变化。
- 组件库引入方式调整(如按需加载改造)导致视觉/工程事实变化。
- 发现本文档描述与代码不一致。
