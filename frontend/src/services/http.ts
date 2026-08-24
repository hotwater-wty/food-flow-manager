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
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      useAuthStore().logout()
      if (router.currentRoute.value.name !== 'customer-login') {
        await router.push({ name: 'customer-login' })
      }
    }
    return Promise.reject(error)
  },
)
