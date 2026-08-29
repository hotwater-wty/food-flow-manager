# 前端二期 R4 顾客端 Vant 验收记录

> 日期:2026-08-27
> 对应计划:[`../../archive/frontend/plans/frontend-redesign-plan.md`](../../archive/frontend/plans/frontend-redesign-plan.md) 切片 R4
> 环境:docker compose 后端(health UP),前端 `vite` 开发服务器(5173),顾客账号 13900001234,主视口 390x844

## 一、变更清单

| 类别 | 文件 | 说明 |
| --- | --- | --- |
| 依赖 | `frontend/package.json`(+lockfile) | 新增 `vant@4.10.0` |
| 修改 | `src/main.ts` | 注册 Vant 插件;样式顺序:EP css → vant css → tokens.css → style.css |
| 修改 | `src/styles/tokens.css` | 新增 Vant 主题桥接段(主色/成功/危险/文字/边框/背景/按钮/圆角/Tabbar 高度) |
| 修改 | `index.html` | `lang="zh-CN"`、标题"膳畅管家"、`viewport-fit=cover`、`theme-color` |
| 删除 | `src/assets/hero.png`、`src/assets/vite.svg`、`src/assets/vue.svg`、`public/icons.svg` | 无引用的脚手架残留 |
| 重写 | `src/layouts/CustomerLayout.vue` | 底部导航改 VanTabbar(四 Tab 带内置图标;首页精确匹配高亮;placeholder 占位) |
| 重写 | `src/views/CustomerMenuView.vue` | VanTabs 分类/菜品卡片(VanImage+VanStepper)/VanSubmitBar 购物车栏/VanNoticeBar/VanActionSheet 详情/结果面板 |
| 重写 | `src/views/CustomerReservationCreateView.vue` | 桌位卡片+VanForm rules 校验+原生 datetime-local 内嵌/结果面板 |
| 重写 | `src/views/CustomerSessionView.vue` | 会话恢复面板/桌位卡片/开台按钮/Toast |
| 重写 | `src/views/CustomerReservationsView.vue` | 卡片+VanTag/详情 cell-group/showConfirmDialog 取代 window.confirm/到店开台 Dialog |
| 重写 | `src/views/CustomerOrdersView.vue` | 卡片+VanTag/明细 cell-group/空状态引导 |
| 重写 | `src/views/CustomerAccountView.vue` | 升级为账户中心(头像卡/服务入口/用户 ID/退出登录确认) |

## 二、命令级验收

- `npx -y pnpm@11.19.0 add vant` → 安装成功;图标方案定为 Vant 内置字体图标(`@vant/icons-vue` 不存在,`@vant/icons` 仅字体配置包,尝试后已移除)。
- `./node_modules/.bin/vue-tsc --noEmit` → 退出码 0。
- `./node_modules/.bin/vite build` → 构建成功(仍有主包体积警告,EP+Vant 双库全量引入的预期代价)。

## 三、浏览器验收(390x844 全流程)

1. 首页:Tabbar 四 Tab 图标正常(非乱码),"首页"精确高亮;顶栏右上角显示登录昵称;
2. 菜单页(未开台):VanNoticeBar 提示"当前没有用餐会话";VanTabs(全部/热菜/主食/饮品)选中态正常;菜品卡片图片占位+价格+Stepper;
3. 开台流程:进开台页选 Z99 → 确认开台 → 会话恢复面板 → "去点餐"返回菜单;
4. 点餐下单:Stepper 加 2 份 → SubmitBar 显示"自动验收菜 × 2 / 合计 ¥24.68"(叠在 Tabbar 上方,双层完整不遮挡)→ 确认下单 → 结果面板(订单号/桌位/金额);
5. 我的订单:列表 6 笔+状态 Tag,"查看明细"展开菜品明细与总额;
6. 账户中心:头像昵称卡、"我的服务"入口、用户 ID、退出登录(showConfirmDialog);
7. 我的预约:列表与状态 Tag 正常;
8. 预约创建完整链:选桌 Z99 → 填时间 2026-08-28 18:30 → 创建成功(结果面板含编号与"18:30:00"格式化时间)。

验收后清理:管理端清台会话 4(订单 3 已推进至完成后清台,桌位 Z99 释放)、测试预约(2092948477858885634)已取消;数据库保持整洁。

## 四、验收中发现并修复的缺陷

1. **模板内联 TS 断言导致渲染中断**:预约页 van-field 上残留 `@click="($event.currentTarget as HTMLInputElement | undefined)"`,模板表达式在运行时是非法 JS,整个表单区不渲染(桌位网格之后页面截止)。修复:删除该废弃表达式。教训:vue-tsc 只查 script/type,模板内联表达式的 TS 语法能在构建通过的情况下于运行时爆炸,模板断言必须依赖浏览器验收兜底。
2. **VanField 校验器拿不到表单值**:时间字段的 rules 绑定在未 v-model 的 Field 上,校验器收到空值恒报"请选择预约时间"。修复:Field 加 `v-model="reserveTime"`(内嵌 input 插槽保留双绑定)。教训:van-field 的 rules 校验的是 Field 的 modelValue,自定义 input 插槽不改变这一契约。

## 五、遗留与后续

- SubmitBar 的 `bottom=50` 与 Tabbar 高度(--tabbar-height: 56px)存在 6px 缝隙,视觉可接受;若追求像素级贴合可改用 CSS 变量统一。
- 菜单页购物车明细为纯文本拼接,后续可升级为 VanActionSheet 展开式购物车列表。
- 主包体积(EP+Vant 双库全量)优化后置,与 R2 记录一致。
- 顾客端整体统一完成;R5 做两端收尾走查与文档同步。
