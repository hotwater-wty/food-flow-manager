# 项目文档中心

本目录是 `food-flow-manager` 的项目文档中心。它借鉴了 `personal-planning` 的“当前内容、单一事实源、历史归档”做法，但按软件项目的生命周期和交付边界取舍，不承载个人规划内容。

## 快速入口

| 需要查看 | 文件或目录 |
| --- | --- |
| 当前文档状态和维护边界 | [`CURRENT.md`](CURRENT.md) |
| 前端立项、产品范围和开发计划 | [`frontend/`](frontend/) |
| 后端当前架构、接口和数据设计 | [`architecture/backend/`](architecture/backend/) |
| 开发流程、模板和文档规范 | [`guides/`](guides/) |
| 复盘、决策、问题和专题记录 | [`records/`](records/) |
| 已完成阶段、旧需求和草稿 | [`archive/`](archive/) |

## 目录规则

```text
documents/
├── CURRENT.md
├── README.md
├── frontend/                  # 当前前端产品、交互、工程和切片计划
├── architecture/backend/     # 当前以后端代码和 README 为准的设计资料
├── guides/                    # 开发流程、格式、模板和维护手册
├── records/                   # 复盘、决策、问题、实践和技术专题
└── archive/                   # 不再主动维护但保留参考价值的历史资料
    ├── backend/plans/
    ├── backend/tasks/
    ├── backend/requirements/
    └── drafts/
```

仓库代码边界：`backend/` 是当前后端 Maven 工程，`frontend/` 是已预留但尚未初始化的前端工程目录；根目录 `docker-compose.yml` 负责跨服务编排。

## 单一事实源

- 可运行行为、接口实际语义和完成范围：以 `backend/` 当前代码、测试、`README.md` 和 `assets/schema.sql` 为准。
- 前端当前范围和验收方式：以 [`frontend/`](frontend/) 为准。
- 后端架构设计：以 [`architecture/backend/`](architecture/backend/) 为准；若与代码不一致，先修正设计文档或标注差异。
- 过程记录解释结论如何形成，但不替代当前设计文档。
- 归档资料只用于追溯，不作为当前开发依据。

## 新文档放置规则

1. 当前仍会影响开发决策的产品、接口或工程方案，放入 `frontend/` 或 `architecture/backend/`。
2. 可复用的流程、模板和维护约定，放入 `guides/`。
3. 已发生的复盘、取舍、排错和专题沉淀，放入 `records/`。
4. 版本已结束、被新结论替代或仅保留参考的内容，按主题放入 `archive/`。
5. 不在 `documents/` 根目录堆放新的 Markdown；不把计划、事实、问题记录和教程混写在同一文件中。

## 运行时资产

`assets/schema.sql` 仍保留在 `assets/` 根目录，因为 Docker Compose 和 README 直接引用该路径。`assets/` 不再作为 Markdown 文档中心。
