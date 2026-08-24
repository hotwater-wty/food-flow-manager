// 员工认证 Store：持久化管理端 Token 和员工身份，独立于顾客 Store。
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { EmployeeLoginData } from '../types/api'

const STORAGE_KEY = 'food-flow-manager.admin-auth'
export type AdminUser = Omit<EmployeeLoginData, 'token'>

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const token = ref<string | null>(null)
  const user = ref<AdminUser | null>(null)
  const isAuthenticated = computed(() => token.value !== null && user.value !== null)

  function login(data: EmployeeLoginData) {
    const { token: nextToken, ...nextUser } = data
    token.value = nextToken
    user.value = nextUser
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: nextToken, user: nextUser }))
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  function restore() {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return
    try {
      const parsed = JSON.parse(raw) as { token?: unknown; user?: AdminUser }
      if (typeof parsed.token !== 'string' || !parsed.user) return logout()
      token.value = parsed.token
      user.value = parsed.user
    } catch { logout() }
  }

  return { token, user, isAuthenticated, login, logout, restore }
})
