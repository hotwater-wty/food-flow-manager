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
  const isAdminRequest = config.url?.startsWith('/admin') === true
  const token = isAdminRequest ? useAdminAuthStore().token : useAuthStore().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
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
