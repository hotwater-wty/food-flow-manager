# 当前文档状态

> 更新日期:2026-08-28

## 当前主线

后端 V1/V2 核心业务和工程增强已完成并冻结扩张。前端一期(阶段 0-6,2026-08-24 收口)与**前端二期:布局重构与视觉统一(R1-R5,2026-08-27 收口)**均已完成:双布局骨架与设计令牌落地、管理端 Element Plus 整体改版(订单/会话工作台、资料维护五页拆分、店长/店员角色显隐)、顾客端 Vant 移动化(Tabbar 导航、点餐购物车栏、预约表单校验、账户中心)。**当前进行中:前端三期——体验完善与商户端仪表盘(2026-08-28 立项)**,切片 P3-R1 店长禁用限制、P3-R2 路由过渡、P3-R3 数据刷新机制、P3-R4 共享层整理、P3-R5 商户端仪表盘(含最小统计端点)、P3-R6 双端拆分成本评估,尚未开工任何切片;计划与审查结论详见 [`planning/frontend-phase3-plan.md`](planning/frontend-phase3-plan.md),二期复盘见 [`planning/frontend-redesign-plan.md`](planning/frontend-redesign-plan.md) 与 [`records/reviews/前端二期阶段复盘.md`](records/reviews/前端二期阶段复盘.md)。管理端使用独立员工 JWT。

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
| **当前阶段计划(前端三期)** | [`planning/frontend-phase3-plan.md`](planning/frontend-phase3-plan.md) | 进行中:问题记录审查、切片清单、范围边界与候选池 |

## 归档判断

- V1/V2 阶段计划、旧功能清单和 V2 候选任务已完成当前阶段职责，统一放在 `archive/backend/`。
- 初始项目计划书和草稿不删除，作为需求演化和范围收敛的历史证据。
- 归档文档不得被 README 或新任务当作当前计划引用；需要复用时先提炼到当前设计或前端计划。

## 下一次更新触发条件

- 启动新的前端或全栈阶段(先另立计划,不沿用已收口文档充当现状)。
- 后端接口、状态码、权限或数据字段发生变化。
- 组件库引入方式调整(如按需加载改造)导致视觉/工程事实变化。
- 发现本文档描述与代码不一致。
