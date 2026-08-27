// 应用入口：创建 Pinia、恢复两类认证状态，再挂载 Vue 根组件。
// 这个文件只负责组装基础设施，不承载具体页面业务。
import { createApp } from 'vue'
// 先引设计令牌再引全局样式,保证 style.css 里的 var(--xxx) 有定义。
import './styles/tokens.css'
import './style.css'
import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'
import { useAdminAuthStore } from './stores/admin-auth'

// createPinia 返回一个应用级状态容器；把同一个实例传给 Store 和 app，
// 才能让路由守卫、请求拦截器和组件访问到同一份响应式状态。
const pinia = createPinia()

// restore 是同步读取 localStorage 的初始化动作，必须发生在挂载前，
// 否则首个路由判断时可能暂时看不到已经保存的登录状态。
useAuthStore(pinia).restore()
useAdminAuthStore(pinia).restore()

// createApp 创建 Vue 应用实例；use 注册插件；mount 把组件树挂到 index.html 的 #app 节点。
createApp(App).use(pinia).use(router).mount('#app')
