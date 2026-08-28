# 前端与实时能力下一阶段开发计划

> 计划版本: 2026-08-28 v1
> 状态: P1、P5、P6 已完成；P2-P4 等待桌码契约评审；P7-P8 待推进
> 上游基线: `documents/CURRENT.md`、`documents/frontend/05-前端工程与接口契约.md`、`documents/planning/frontend-split-plan.md`
> 维护规则: 一次只启动一个可验收切片; 启动前核对真实后端契约; 完成后补验收记录并建立 Git 检查点

## 1. 阶段定位

本阶段承接双应用拆分收口，重点从前端页面可运行推进到桌位扫码可落地、商户端通知可实时感知、工程边界可持续维护。

当前基线包含 `apps/customer`(Vant, 5174)、`apps/admin`(Element Plus, 5173) 和 `packages/shared`；顾客登录/预约/模拟开台/点餐/订单、员工登录/仪表盘/订单与会话工作台/资料维护均已完成。管理端工作台仍默认使用 20 秒轮询，顾客端仅在聚焦或恢复可见时刷新。真实二维码、图片生成、摄像头/图片解码和 WebSocket/SSE 均未完成。

另已发现管理端路由过渡疑似回归：`AdminLayout.vue` 仍使用 `fade-transform`，但 `apps/admin/src/style.css` 只定义起止状态，缺少 active transition 声明；顾客端 `fade` 样式完整但只有 0.18 秒透明度变化。

## 2. 目标与非目标

### 2.1 目标

1. 修复并验证双端路由切换过渡。
2. 定义不暴露数据库 ID 的桌码契约，完成开发期真实二维码图片闭环。
3. 后端支持后完成顾客端桌码解析、状态确认和开台闭环。
4. 以 SSE 作为商户端单向通知方案，使用短期连接 ticket 兼容当前 Bearer JWT。
5. SSE 只触发静默刷新，断线时保留手动刷新和低频兜底。
6. 处理分页、husky、Element Plus 弹层残留等已确认工程事项。

### 2.2 不自动实施

- 不直接修改后端核心业务、数据库、Docker 或 nginx；后端切片须单独授权。
- 不把 `tableId` 明文、长期 JWT 或可伪造桌号作为生产占座凭证。
- 不用前端假接口替代真实二维码、SSE 或占座业务。
- 不扩展到支付、聊天、多门店、库存或复杂报表。

## 3. 设计决策

### 3.1 桌码

二维码内容建议为顾客端地址加不可猜测的 code：

```text
https://customer.example.com/scan/table/<opaque-code>
```

`opaque-code` 由后端生成并与桌位绑定，可轮换。二维码只是桌位线索，开台接口仍由后端原子校验状态。顾客扫码后保存待处理 code，登录成功再解析并确认。

以下是待后端评审的接口提案，不是当前契约：

```text
GET  /api/user/table-codes/{code}        解析桌码并返回桌号、容量、状态
POST /api/user/tables/{tableId}/sessions 开台，后端再次校验 FREE 状态
```

生成使用 [node-qrcode](https://github.com/soldair/node-qrcode)；顾客端解码首选 [html5-qrcode](https://github.com/mebjas/html5-qrcode)，轻量备选 [qr-scanner](https://github.com/nimiq/qr-scanner)，多格式备选 [@zxing/browser](https://github.com/zxing-js/browser)。原生 `BarcodeDetector` 只作增强，不能作为唯一实现。

### 3.2 SSE 与鉴权

当前 Axios 通过 `Authorization: Bearer <JWT>` 鉴权，而原生 `EventSource` 不能自定义 Authorization 请求头。因此不把长期 JWT 放进 SSE URL。

推荐短期一次性 ticket：

```text
POST /api/admin/notifications/ticket   Bearer JWT
        -> 30~60 秒有效的一次性 ticket
GET  /api/admin/notifications/stream?ticket=...
        -> 校验 ticket，建立 SseEmitter
        -> 推送 new-order / reservation-check-in
        -> 前端重新 GET 订单/会话列表
```

ticket 绑定员工身份并设置 TTL，消费后不可复用。SSE 事件只做变化提示，业务详情以 REST 查询为准。选择 SSE 是因为当前需求是服务端到商户端的单向通知；只有出现聊天、双向协作等需求时才重新评估 WebSocket。

## 4. 执行顺序与切片

| 编号 | 切片 | 依赖 | 允许修改 | 验收出口 |
| --- | --- | --- | --- | --- |
| P0 | 计划与契约预检 | 无 | `documents/**` | 事实、接口、风险已登记；不编码 |
| P1 | 管理端路由过渡修复 | P0 | `apps/admin/src/style.css`及验收记录 | **已完成**：补齐 active transition，检查通过 |
| P2 | 桌码契约与后端预研 | P0 | 先限文档；后端需授权 | code 生命周期、解析权限、状态和并发错误确定 |
| P3 | 二维码生成与开发期模拟 | P2 | 授权后 `backend/**`；`apps/admin/**`、`apps/customer/**`、必要 shared 类型 | 商户端下载 PNG；顾客端上传 PNG 解码；固定 code 可复验 |
| P4 | 顾客端扫码开台闭环 | P3 | `apps/customer/**`、`packages/shared/**`；后端按授权 | 扫码、登录回跳、解析、空闲确认、占用/禁用/并发失败提示 |
| P5 | SSE 鉴权最小纵向切片 | P0 | 后端通知模块按授权；`apps/admin/**`、shared 契约 | **已完成**：JWT 换 60 秒一次性 ticket、连接、测试事件、组件卸载关闭 |
| P6 | SSE 接入订单/预约事件 | P5 | 后端事件发布按授权；`apps/admin/**` | **已完成**：订单/预约/到店事件触发工作台静默刷新 |
| P7 | 轮询降级与可观测性 | P6 | `apps/admin/**`、文档 | SSE 正常时降低轮询；断线启用低频兜底；无请求堆积 |
| P8 | 分页与工程收尾 | 可并行 | 后端授权后前端；`package.json`/husky；必要资料页 | 顾客分页真实生效；提交前检查；弹层残留有结论 |

每个切片启动前必须写任务简报：背景、唯一目标、允许/禁止修改、已有契约、验收路径、AI/用户分工。未完成当前切片不得顺手推进下一切片。

## 5. 重点验收

### SSE

- 不在 URL 或日志中使用长期 JWT；ticket 过期、重复使用、员工失效均失败。
- 员工登录后建立连接，测试事件到达；离开页面、退出登录、组件卸载后 `close()`。
- 外部创建订单或预约到店后无需点击刷新即可看到更新。
- 重复事件不造成加载堆积；断线可手动刷新并启用低频兜底。

### 桌码

- code 在轮换前稳定，禁用/轮换后旧 code 不能开台。
- 扫码后登录回跳保留 code；不能靠修改 URL 的 `tableId` 绕过校验。
- 空闲、占用、禁用、不存在分别展示后端消息；并发确认最多一个成功。
- 摄像头不可用时可上传 PNG；权限拒绝仍可退出或使用测试输入。

## 6. 现有事项处理

| 事项 | 当前结论 | 计划处理 |
| --- | --- | --- |
| F1/F2/F3/F5/F6/F7/F10 | 已完成 | 回归检查 |
| F3b husky | 未完成 | P8 |
| F4/B1 顾客端分页 | 后端当前忽略分页，预约接口也未分页 | 后端先行后入 P8 |
| F8 视觉遗留 | SubmitBar 已修；`el-dialog` DOM 残留待结论 | P8 |
| F9 仪表盘趋势、B2 | 需统计端点和图表依赖 | 暂缓 |
| B3 通知机制 | 已确定 SSE + ticket | P5-P7 |
| B4-B10、D1-D3 | 候选或未授权 | 保留，不自动开工 |

## 7. 风险

- 当前后端桌位接口只返回 `FREE`，桌码解析必须补指定桌位状态查询，不能从空闲列表反推。
- ticket 的 TTL、一次性消费、断线清理和多实例共享需明确；多实例时再接 Redis Pub/Sub 或消息队列。
- SSE 经 nginx 时需关闭响应缓冲并配置长连接超时，这属于部署切片。
- 原生 EventSource 自动重连不等于业务幂等，重复事件必须允许安全重复 GET。
- HTTPS、摄像头权限、移动端后台挂起和 `prefers-reduced-motion` 都是验收条件。

## 8. 计划维护

启动 P1-P8 任一切片时，在本文件标记进行中，并建立对应 `documents/records/reviews/` 验收记录。完成后记录文件、命令、浏览器路径、成功/失败场景和遗留风险；阶段收口时更新 `documents/CURRENT.md` 与 `documents/planning/README.md`。
