// Axios 基础实例(顾客端独立版):只服务顾客 Token,统一处理 401。
// 拆分前这里按 URL 前缀嗅探双端 Token,独立成应用后嗅探逻辑不复存在。
import axios from 'axios'
import { router } from '../router'
import { useAuthStore } from '../stores/auth'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const token = useAuthStore().token
  if (token) {
    // AxiosHeaders 支持属性写入；后端 JWT 拦截器读取 Bearer 前缀。
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    // 只有 HTTP 401 才触发自动退出；业务 code=0 仍交由具体页面展示。
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      useAuthStore().logout()
      if (router.currentRoute.value.name !== 'customer-login') {
        await router.push({ name: 'customer-login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }
    return Promise.reject(error)
  },
)
