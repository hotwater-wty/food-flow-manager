// Axios 基础实例：根据请求路径选择顾客或员工 Token，并统一处理 401。
import axios from 'axios'
import { router } from '../router'
import { useAuthStore } from '../stores/auth'
import { useAdminAuthStore } from '../stores/admin-auth'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  // URL 是相对于 baseURL 的路径，因此 /admin 可以可靠地区分认证域。
  const isAdminRequest = config.url?.startsWith('/admin') === true
  const token = isAdminRequest ? useAdminAuthStore().token : useAuthStore().token
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
      const isAdminRoute = router.currentRoute.value.path.startsWith('/admin')
      if (isAdminRoute) {
        useAdminAuthStore().logout()
        if (router.currentRoute.value.name !== 'admin-login') await router.push({ name: 'admin-login' })
      } else {
        useAuthStore().logout()
        if (router.currentRoute.value.name !== 'customer-login') await router.push({ name: 'customer-login' })
      }
    }
    return Promise.reject(error)
  },
)
