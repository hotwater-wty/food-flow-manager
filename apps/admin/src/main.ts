// 商户端应用入口:创建 Pinia、恢复员工认证状态,再挂载 Vue 根组件。
// F1 起组件按需引入:模板中的 El 组件由 unplugin-vue-components 自动注册并按需带样式,
// 这里不再 app.use(ElementPlus) 全量注册。函数式 API(ElMessage/ElMessageBox/ElLoading)
// 不经过模板编译,其样式必须在此显式引入;指令式 v-loading 依赖 ElLoading 样式。
import { createApp } from 'vue'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/loading/style/css'
// 按需引入后组件样式不再统一先于令牌加载;tokens.css 的 --el-* 变量是 CSS 自定义属性,
// 只依赖 :root 存在,与样式的加载顺序无关,主题覆盖依然成立。
import './styles/tokens.css'
import './style.css'
import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import { useAdminAuthStore } from './stores/admin-auth'

// createPinia 返回一个应用级状态容器;把同一个实例传给 Store 和 app,
// 才能让路由守卫、请求拦截器和组件访问到同一份响应式状态。
const pinia = createPinia()

// restore 是同步读取 localStorage 的初始化动作,必须发生在挂载前,
// 否则首个路由判断时可能暂时看不到已经保存的登录状态。
useAdminAuthStore(pinia).restore()

// createApp 创建 Vue 应用实例;use 注册插件;mount 把组件树挂到 index.html 的 #app 节点。
// locale 通过 ConfigProvider 组件注入(见 App.vue),不再是 app.use 的全局选项。
createApp(App).use(pinia).use(router).mount('#app')
