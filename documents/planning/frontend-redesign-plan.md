# 前端二期计划:布局重构与视觉统一

> 立项日期:2026-08-27
> 状态:**已收口**——R1-R5 全部完成,复盘见 [`../records/reviews/前端二期阶段复盘.md`](../records/reviews/前端二期阶段复盘.md);本文件转为历史记录,后置候选不自动开工
> 上游文档:[`frontend-development-plan.md`](frontend-development-plan.md)(一期已收口)、[`../frontend/04-视觉设计与组件规则.md`](../frontend/04-视觉设计与组件规则.md)
> 分工决策:AI 为主实现,用户浏览器验收 + 阅读代码;每片完成后输出"改了什么 / 为什么这样组织 / 涉及知识点 / 建议补的课程"。

## 1. 背景:一期收口审查结论

2026-08-27 对前端首版做了一次后端覆盖审查,结论:

- **接口覆盖属实**:后端 17 个控制器、54 个业务端点,前端已接入 53 个。唯一未接的 `POST /api/admin/auth/register` 与已接入的 `POST /api/admin/employees` 功能重复(同为店长权限、同用一套注册 DTO),判定非缺口。后端不存在支付、统计、库存、文件上传等前端漏做的模块。
- **能力已接、界面未露出的质量缺口**(本阶段顺带修复):
  1. 管理端会话详情、资料维护各资源的"详情"只调接口不渲染数据;
  2. 管理端会话列表(状态/桌位/预约过滤)、顾客订单列表(状态过滤)的筛选能力未露出;
  3. 店员(STAFF)角色不控制界面,店长专属操作(员工管理、删除桌位)对店员可见,点击后收到 403 空响应体,报错体验差;
  4. 菜品图片字段采集了但从未渲染;
  5. 管理端无登出按钮、无 404 路由、首页文案为脚手架期过期内容、顾客登录不支持 `?redirect` 回跳、破坏性操作用原生 `window.confirm`。
- **文档与代码偏差**:`04-视觉设计与组件规则` 的目标(管理端左侧导航+顶部员工信息+详情抽屉、顾客端移动优先+底部购物车、Vant + Element Plus、陶土橙令牌)从未落地;`03-页面地图` 路由与实际不一致;`guides/开发流程手册` 有过期句;验收清单中的 `pnpm lint` 命令不存在。

## 2. 已确认决策

| 决策项 | 结论 |
| --- | --- |
| 组件库 | 管理端 Element Plus,顾客端 Vant(首期全量引入,构建体积优化后置) |
| 画风 | 暖色餐饮风:陶土橙 `#C85A3F` 主色、顾客端暖白 `#FAF8F4`、管理端浅灰 `#F5F6F8`,以 `04-视觉设计与组件规则` 既定令牌为基准;open-design 的 Airbnb/Editorial 类 DESIGN.md 仅作参考调优 |
| 布局 | 双布局:管理端桌面工作台(左侧菜单树+顶栏右上角员工信息/登出);顾客端移动优先(顶栏品牌+右上角账户入口+底部导航);登录页与 404 独立无布局;现有 URL 全部不变 |

## 3. 切片清单(每片一个可验收检查点,完成后 git 提交并停点等用户确认)

### R1 立项 + 双布局骨架 + 路由重构(已完成,2026-08-27)

- 新建本计划文档;`CURRENT.md` 声明二期启动。
- 新建 `src/styles/tokens.css`(色彩/间距/圆角/布局尺寸 CSS 变量),`style.css` 全量迁移到令牌并切换为陶土橙配色,中文字体优先。
- 新建 `src/layouts/AdminLayout.vue`(左侧两级菜单树 + 顶栏页名/员工姓名/角色标签/登出,窄屏折叠为横向菜单)、`src/layouts/CustomerLayout.vue`(顶栏 + 底部四项导航);`App.vue` 瘦身为纯路由出口。
- 路由改嵌套结构;新增 404 兜底路由与 `NotFoundView`。
- 顺带修复:管理端登出按钮、首页过期文案(改为顾客入口页)、顾客登录 `?redirect` 回跳。
- 验收:`vue-tsc --noEmit` 与 `vite build` 通过;浏览器 1440x900 与 390x844 双视口走查通过(路由可达、布局正确、守卫/回跳/登出/404 生效,详见 `../records/reviews/前端二期R1布局骨架验收记录.md`)。

### R2 Element Plus 引入 + 订单工作台试点(已完成,2026-08-27)

- 安装 `element-plus@2.14.5`、`@element-plus/icons-vue@2.3.2`(pnpm 11.19.0,lockfile 同步);`main.ts` 全量引入并配置中文语言包。
- `tokens.css` 增加 Element Plus 主题桥接段:`--el-color-primary` 系映射为陶土橙色阶,success/danger 映射语义令牌,圆角与字体对齐项目规范。
- 订单工作台改版:ElTable(六列+状态 ElTag+固定操作列)、ElPagination(中文"共 N 条")、ElSelect 筛选(空字符串哨兵表示"全部")、ElDrawer 详情(订单信息 ElDescriptions+菜品明细表+备注列表)、刷新按钮(图标包);终态"完成订单"用 ElMessageBox.confirm 拦截误触,推进成功用 ElMessage 提示;时间展示把 ISO 的 T 换为空格。
- 验收:`vue-tsc --noEmit` 与 `vite build` 通过(全量引入后主包 gzip 约 322KB,优化后置);浏览器真实数据走查通过:表格渲染、筛选(含空状态 ElEmpty)、详情抽屉(含备注)、完整状态推进链(已下单→制作中→已上齐→已完成,终态确认弹窗)、移动 390 视口表格横滚+固定操作列。验收数据已清理(会话关闭、桌位释放)。详见 [`../records/reviews/前端二期R2订单工作台试点验收记录.md`](../records/reviews/前端二期R2订单工作台试点验收记录.md)。

### R3 管理端其余页 + 资料维护拆分(已完成,2026-08-27)

- 新建 `src/composables/use-pagedList.ts`:把各资源页重复的"页码+总数+加载锁+错误信息"分页状态机收敛为组合式函数;`utils/status.ts` 扩展为全量状态字典(会话/桌位/菜品/员工角色与状态),消除三个视图里的重复映射表。
- 服务层 `getAdminSessions` 支持 status 筛选参数(`status !== undefined` 时携带,修复原 `status ?` 对 0 值的漏传)。
- 会话工作台 EP 改版:状态筛选下拉、表格(会话/桌位双状态列)、详情抽屉真正渲染数据(替代原"拼接一行反馈文本")、取消等待/清台带确认弹窗。
- 资料维护拆为五个子路由页 `/admin/resources/{tables|categories|dishes|reservations|employees}`(旧 `/admin/resources` 重定向到桌位页):统一 ElTable+ElPagination+ElDialog 表单(声明式校验 rules)+删除/启停确认弹窗;菜品页新增 ElImage 图片渲染(带失败占位)、分类下拉选择(替代手填 ID)、价格"元↔分"边界换算;预约页详情改抽屉。原 186 行五合一 `AdminResourcesView.vue` 删除,详情按钮从"调用后提示已读取"改为真实渲染。
- 角色显隐:`RouteMeta.requiresManager` + 路由守卫(店员访问店长页重定向回订单页);侧栏菜单 computed 按角色过滤,店员看不到"桌位维护/员工管理"入口。后端拦截器仍是最终防线。
- 验收:`vue-tsc --noEmit` 与 `vite build` 通过;店长/店员双账号浏览器走查通过(店员菜单缺省两项+URL 直达被拦、店长七项菜单齐全、员工页两账号渲染、会话筛选含空状态、详情抽屉、桌位新增弹窗校验与真实创建、删除确认链)。验收数据已清理(测试桌位与店员账号已删)。详见 [`../records/reviews/前端二期R3管理端拆分验收记录.md`](../records/reviews/前端二期R3管理端拆分验收记录.md)。

### R4 Vant 引入 + 顾客端全部页面(已完成,2026-08-27)

- 安装 `vant@4.10.0`(全量引入);`main.ts` 注册 Vant 插件;`tokens.css` 增加 Vant 主题桥接(`--van-primary-color` 等映射项目令牌)。图标用 Vant 内置字体图标名(字符串),未引入独立图标包(`@vant/icons` 是字体配置包、`@vant/icons-vue` 不存在)。
- `index.html` 修正 `lang="zh-CN"`、标题"膳畅管家"、`viewport-fit=cover` 与 `theme-color`;清理无引用的 `hero.png/vite.svg/vue.svg/icons.svg`。
- CustomerLayout:底部导航改 VanTabbar(四 Tab 带图标,`placeholder` 占位,"首页"精确匹配高亮)。
- 菜单点餐页:VanTabs 分类、菜品卡片(VanImage 失败占位+VanStepper 圆形加减+`@click.stop` 防误触详情)、底部 VanSubmitBar 购物车栏(`bottom=50` 叠在 Tabbar 之上,明细+合计+下单)、无会话 VanNoticeBar 提示(点击跳开台)、菜品详情 VanActionSheet 半屏、下单结果面板。
- 预约创建页:桌位卡片选择+VanForm/VanField rules 校验(人数容量/时间未来)+原生 datetime-local 内嵌 Field、结果面板。
- 会话页:会话恢复面板与桌位卡片+开台按钮、Toast 反馈。
- 我的预约/我的订单:卡片+VanTag 状态、详情内嵌 cell-group、showConfirmDialog 替代原生 confirm、空状态 VanEmpty 带引导按钮。
- 账户页:从"认证验证页"升级为账户中心(头像昵称卡+服务入口 cell+用户 ID+退出登录确认)。
- 验收中发现并修复两个缺陷:1) 预约页模板残留内联 TS 断言表达式导致组件渲染中断;2) VanField 校验器绑定在无 v-model 的字段上拿不到表单值(时间校验恒失败)。
- 验收:`vue-tsc --noEmit` 与 `vite build` 通过;390x844 全流程浏览器走查通过:首页/Tabbar 图标、菜单页(提示条→开台→返回→加购 2 份→SubmitBar 合计 ¥24.68→下单成功面板)、我的订单(6 笔+明细展开)、账户中心(头像/服务入口/退出)、我的预约列表、预约创建完整链(选桌→填时间→创建成功含编号)。验收数据已清理(会话已清台、测试预约已取消)。详见 [`../records/reviews/前端二期R4顾客端Vant验收记录.md`](../records/reviews/前端二期R4顾客端Vant验收记录.md)。

### R5 收尾 + 文档同步(已完成,2026-08-27)

- 两端双视口全局走查(命令+浏览器),走查中修复四类问题:`type-check` 脚本改为真验证 `vue-tsc -b` 并修复暴露的 4 个类型错误;VanTabbar 高亮改为路由推导(修"选中态恒为首页");新增 `utils/format.ts` 统一四处 ISO 时间展示;菜品页分类选项挂载预载(修首屏"分类 N"兜底文案)。
- 文档同步:03 页面地图按实际路由重写、04 视觉设计补落地现状节、开发流程手册清过期句、一期计划标注二期收口、`CURRENT.md` 收口;新建 [`../records/reviews/前端二期R5收尾文档同步验收记录.md`](../records/reviews/前端二期R5收尾文档同步验收记录.md) 与 [`../records/reviews/前端二期阶段复盘.md`](../records/reviews/前端二期阶段复盘.md)。
- 验收:`vue-tsc -b && vite build && node scripts/smoke-api.mjs` 全部通过(主包 gzip 约 351KB);两端双视口走查通过,本轮无业务数据写入。

## 4. 范围与边界

- 允许修改:`frontend/src/**`、`frontend/index.html`、`frontend/package.json`(+lockfile)、`documents/**`、根 `README.md` 前端章节。
- 新增依赖仅限:`element-plus`、`@element-plus/icons-vue`、`vant`。
- 禁止:修改 `backend/**`、Docker、数据库脚本;不新增业务功能;接口 URL 语义不变。
- 已知风险:组件库样式与现有全局 CSS 冲突(逐页迁移控制);confirm→Dialog、详情改抽屉属交互变化需逐一验收;全量引入使 bundle 增大(可接受,优化后置);资料页拆分是最大单项改动(单独成片便于回滚)。

## 5. 验收证据索引

- R1:`vue-tsc --noEmit` 退出码 0(当时尚未发现该命令为空验证);`vite build` 成功(dist 产物正常);docker compose 后端 `{"status":"UP"}`;浏览器双视口走查(顾客端首页/登录/守卫回跳/点餐页、404、管理端守卫/登录/菜单树导航/资源页/登出、移动端侧栏折叠)全部通过。
- R2/R3/R4:详见各自验收记录(`records/reviews/`)中的命令输出与浏览器路径;其中"type-check 通过"结论由 R5 的 `vue-tsc -b` 回溯确认后仍然成立(4 个新报错已修复,不涉及其它页面行为)。
- R5:`vue-tsc -b` / `vite build` / `scripts/smoke-api.mjs` 三条全通过;浏览器 390x844 与 1440x900 全局走查通过;证据见 [`../records/reviews/前端二期R5收尾文档同步验收记录.md`](../records/reviews/前端二期R5收尾文档同步验收记录.md)。
