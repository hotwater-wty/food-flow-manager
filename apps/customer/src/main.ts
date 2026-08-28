// 顾客端应用入口:创建 Pinia、恢复顾客认证状态,再挂载 Vue 根组件。
// F1 起模板组件按需引入(unplugin-vue-components + VantResolver):
// 不再 app.use(Vant) 全量注册;函数式 API(showToast 系)的使用不受影响。
// 注:Vant 函数式组件的样式依赖保留全量 CSS 引入,收益与复杂度权衡后按整体引入处理。
import { createApp } from 'vue'
// 先引库样式再引令牌,tokens.css 里的 --van-* 覆盖才能在同等优先级下生效。
import 'vant/lib/index.css'
// 先引设计令牌再引全局样式,保证 style.css 里的 var(--xxx) 有定义。
import './styles/tokens.css'
import './style.css'
import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'

// createPinia 返回一个应用级状态容器;把同一个实例传给 Store 和 app,
// 才能让路由守卫、请求拦截器和组件访问到同一份响应式状态。
const pinia = createPinia()

// restore 是同步读取 localStorage 的初始化动作,必须发生在挂载前,
// 否则首个路由判断时可能暂时看不到已经保存的登录状态。
useAuthStore(pinia).restore()

// createApp 创建 Vue 应用实例;use 注册插件;mount 把组件树挂到 index.html 的 #app 节点。
createApp(App).use(pinia).use(router).mount('#app')
