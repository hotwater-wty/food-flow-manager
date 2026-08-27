// 应用入口:创建 Pinia、恢复两类认证状态,注册 Element Plus,再挂载 Vue 根组件。
// 这个文件只负责组装基础设施,不承载具体页面业务。
import { createApp } from 'vue'
// Element Plus 二期 R2 起引入:首期选择全量引入(app.use 注册全部组件),
// 省去逐组件按需注册的构建配置;体积优化留作后续可选任务。
// 组件样式必须先于设计令牌加载,tokens.css 里的 --el-* 覆盖才能在同等优先级下生效。
import 'element-plus/dist/index.css'
// 先引设计令牌再引全局样式,保证 style.css 里的 var(--xxx) 有定义。
import './styles/tokens.css'
import './style.css'
import ElementPlus from 'element-plus'
// 默认语言是英文;引入官方中文语言包,分页"共 N 条"、弹窗按钮等文案才是中文。
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'
import { useAdminAuthStore } from './stores/admin-auth'

// createPinia 返回一个应用级状态容器;把同一个实例传给 Store 和 app,
// 才能让路由守卫、请求拦截器和组件访问到同一份响应式状态。
const pinia = createPinia()

// restore 是同步读取 localStorage 的初始化动作,必须发生在挂载前,
// 否则首个路由判断时可能暂时看不到已经保存的登录状态。
useAuthStore(pinia).restore()
useAdminAuthStore(pinia).restore()

// createApp 创建 Vue 应用实例;use 注册插件;mount 把组件树挂到 index.html 的 #app 节点。
// Element Plus 通过插件选项接收语言包。
createApp(App).use(pinia).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
