# 当前文档状态

> 更新日期:2026-08-28

## 当前主线

后端 V1/V2 核心业务和工程增强已完成并冻结扩张(三期新增的只读统计端点除外)。前端一期(阶段 0-6,2026-08-24 收口)、二期(布局重构与视觉统一,R1-R5,2026-08-27 收口)与**三期:体验完善与商户端仪表盘(P3-R1 至 P3-R6,2026-08-28 收口)**均已完成。三期交付:店长禁用限制(前端标灰+后端守卫,`disableEmployee` 拒绝店长与自我禁用)、路由切换过渡(管理端 fade-transform/顾客端轻量 fade)、数据自动刷新(`useAutoRefresh` 组合式函数:20 秒轮询+可见性暂停+聚焦刷新,静默加载不惊扰用户)、共享层整理(`formatPrice`/状态标签映射收敛、工作台统一 `usePagedList`、分类下拉取全量)、商户端仪表盘(`GET /api/admin/statistics/overview` + 经营概览页,零依赖呈现)、双端拆分成本评估(`frontend/08-双端拆分准备评估.md`)。当前无进行中的开发阶段;候选池事项见三期计划第 5 节,不自动开工。进度与复盘详见 [`planning/frontend-phase3-plan.md`](planning/frontend-phase3-plan.md)。管理端使用独立员工 JWT。

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
| 双端拆分准备评估 | [`frontend/08-双端拆分准备评估.md`](frontend/08-双端拆分准备评估.md) | 耦合点清单、拆分步骤草案与待决策项 |

## 归档判断

- V1/V2 阶段计划、旧功能清单和 V2 候选任务已完成当前阶段职责，统一放在 `archive/backend/`。
- 初始项目计划书和草稿不删除，作为需求演化和范围收敛的历史证据。
- 归档文档不得被 README 或新任务当作当前计划引用；需要复用时先提炼到当前设计或前端计划。

## 下一次更新触发条件

- 启动新的前端或全栈阶段(先另立计划,不沿用已收口文档充当现状)。
- 后端接口、状态码、权限或数据字段发生变化。
- 组件库引入方式调整(如按需加载改造)导致视觉/工程事实变化。
- 发现本文档描述与代码不一致。
