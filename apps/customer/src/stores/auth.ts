// 顾客认证 Store：负责内存状态、localStorage 恢复和退出时清理。
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { UserLoginData } from '@foodflow/shared/types/api'

const AUTH_STORAGE_KEY = 'food-flow-manager.customer-auth'

export type AuthUser = Omit<UserLoginData, 'token'>

interface PersistedAuth {
  // 持久化结构只保存认证恢复所需字段；它不是后端完整响应类型。
  token: string
  user: AuthUser
}

function isAuthUser(value: unknown): value is AuthUser {
  // 类型谓词 value is AuthUser 让 TypeScript 在 return true 后缩小类型。
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const user = value as Record<string, unknown>
  return (
    typeof user.userId === 'number' &&
    typeof user.phone === 'string' &&
    typeof user.nickname === 'string' &&
    typeof user.status === 'number'
  )
}

function isPersistedAuth(value: unknown): value is PersistedAuth {
  // localStorage 内容可被用户手动修改，所以恢复前不能直接断言为可信对象。
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const auth = value as Record<string, unknown>
  return typeof auth.token === 'string' && auth.token.length > 0 && isAuthUser(auth.user)
}

export const useAuthStore = defineStore('auth', () => {
  // 顾客认证状态使用 setup store，ref 是状态，computed 是派生状态。
  const token = ref<string | null>(null)
  const user = ref<AuthUser | null>(null)
  // computed 返回只读派生值；只有 Token 和用户资料同时存在才算登录完成。
  const isAuthenticated = computed(() => token.value !== null && user.value !== null)

  function login(loginData: UserLoginData) {
    // 从服务端响应中拆出 Token，只把非敏感用户资料放入 user。
    const { token: nextToken, ...nextUser } = loginData

    token.value = nextToken
    user.value = nextUser
    // JSON.stringify 把对象序列化成字符串，因为 localStorage 只接受字符串。
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({ token: nextToken, user: nextUser } satisfies PersistedAuth))
  }

  function logout() {
    // 退出同时清空响应式状态和持久化快照。
    token.value = null
    user.value = null
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  function restore() {
    // restore 只在 main.ts 启动阶段调用一次，恢复后路由守卫才能判断权限。
    const storedAuth = localStorage.getItem(AUTH_STORAGE_KEY)
    if (storedAuth === null) {
      return
    }

    try {
      // JSON.parse 的返回值故意标注为 unknown，迫使这里显式验证外部数据。
      const parsedAuth: unknown = JSON.parse(storedAuth)
      if (!isPersistedAuth(parsedAuth)) {
        logout()
        return
      }

      token.value = parsedAuth.token
      user.value = parsedAuth.user
    } catch {
      logout()
    }
  }

  return { token, user, isAuthenticated, login, logout, restore }
})
