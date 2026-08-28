// Axios 基础实例(顾客端独立版):只服务顾客 Token,统一处理 401 与网络级错误。
// 拆分前这里按 URL 前缀嗅探双端 Token,独立成应用后嗅探逻辑不复存在。
import axios from 'axios'
import { showToast } from 'vant'
import { router } from '../router'
import { useAuthStore } from '../stores/auth'

// 网络级错误提示的节流窗口:聚焦刷新在弱网下会密集失败,2 秒只提示一次。
let lastNetworkToastAt = 0
function notifyNetworkError(error: unknown) {
  if (axios.isAxiosError(error) && error.response) return // 有响应的错误(401/403/5xx)交给调用方
  const now = Date.now()
  if (now - lastNetworkToastAt < 2000) return
  lastNetworkToastAt = now
  showToast(axios.isAxiosError(error) && error.code === 'ECONNABORTED' ? '请求超时,请检查网络后重试' : '网络异常,请检查连接后重试')
}

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
    // 网络级失败(断网/超时/后端未启动)没有 response,统一轻提示,业务页面无需重复处理。
    notifyNetworkError(error)
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
