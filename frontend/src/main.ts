// 应用入口：创建 Pinia、恢复两类认证状态，再挂载 Vue 根组件。
import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'
import { useAdminAuthStore } from './stores/admin-auth'

const pinia = createPinia()

useAuthStore(pinia).restore()
useAdminAuthStore(pinia).restore()
createApp(App).use(pinia).use(router).mount('#app')
