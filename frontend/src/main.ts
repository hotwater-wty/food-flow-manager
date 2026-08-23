import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'

const pinia = createPinia()

useAuthStore(pinia).restore()
createApp(App).use(pinia).use(router).mount('#app')
