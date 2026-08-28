// 员工认证 Store：持久化管理端 Token 和员工身份，独立于顾客 Store。
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { EmployeeLoginData } from '@foodflow/shared/types/api'

const STORAGE_KEY = 'food-flow-manager.admin-auth'
// token 不放进 user，避免页面把凭证和可展示员工信息混为一谈。
export type AdminUser = Omit<EmployeeLoginData, 'token'>

export const useAdminAuthStore = defineStore('admin-auth', () => {
  // ref 创建响应式容器；修改 .value 后，依赖它的路由和模板会更新。
  const token = ref<string | null>(null)
  const user = ref<AdminUser | null>(null)
  // computed 会追踪两个 ref；任一清空都会让受保护路由失去访问资格。
  const isAuthenticated = computed(() => token.value !== null && user.value !== null)

  function login(data: EmployeeLoginData) {
    // 解构同时取出 Token 和其余身份字段，...nextUser 保留未来新增字段。
    const { token: nextToken, ...nextUser } = data
    token.value = nextToken
    user.value = nextUser
    // 持久化前把对象转成 JSON 字符串，刷新页面后 restore 才能还原。
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: nextToken, user: nextUser }))
  }

  function logout() {
    // 内存和 localStorage 必须一起清理，否则刷新后会重新恢复旧身份。
    token.value = null
    user.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  function restore() {
    // 应用启动时读取字符串；JSON.parse 的结果必须先经过最小结构判断。
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return
    try {
      // 外部字符串先 parse，再检查 token 和 user；类型断言本身不会运行时校验。
      const parsed = JSON.parse(raw) as { token?: unknown; user?: AdminUser }
      if (typeof parsed.token !== 'string' || !parsed.user) return logout()
      token.value = parsed.token
      user.value = parsed.user
    } catch {
      logout()
    }
  }

  return { token, user, isAuthenticated, login, logout, restore }
})
