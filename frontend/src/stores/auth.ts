import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { UserLoginData } from '../types/api'

const AUTH_STORAGE_KEY = 'food-flow-manager.customer-auth'

export type AuthUser = Omit<UserLoginData, 'token'>

interface PersistedAuth {
  token: string
  user: AuthUser
}

function isAuthUser(value: unknown): value is AuthUser {
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
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const auth = value as Record<string, unknown>
  return typeof auth.token === 'string' && auth.token.length > 0 && isAuthUser(auth.user)
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const user = ref<AuthUser | null>(null)
  const isAuthenticated = computed(() => token.value !== null && user.value !== null)

  function login(loginData: UserLoginData) {
    const { token: nextToken, ...nextUser } = loginData

    token.value = nextToken
    user.value = nextUser
    localStorage.setItem(
      AUTH_STORAGE_KEY,
      JSON.stringify({ token: nextToken, user: nextUser } satisfies PersistedAuth),
    )
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  function restore() {
    const storedAuth = localStorage.getItem(AUTH_STORAGE_KEY)
    if (storedAuth === null) {
      return
    }

    try {
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
