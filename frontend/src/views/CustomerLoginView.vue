<script setup lang="ts">
// 顾客认证页：在登录和注册两种模式间切换，写操作完成后更新认证上下文。
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { loginUser, registerUser } from '../services/user-auth'
import { useAuthStore } from '../stores/auth'

const phone = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const nickname = ref('')
const isRegistering = ref(false)
const authStore = useAuthStore()
const { isAuthenticated, user } = storeToRefs(authStore)

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    if (isRegistering.value) {
      await registerUser({ phone: phone.value, password: password.value, nickname: nickname.value })
      isRegistering.value = false
      errorMessage.value = '注册成功，请使用新账号登录'
    } else {
      const loginData = await loginUser({ phone: phone.value, password: password.value })
      authStore.login(loginData)
    }
    password.value = ''
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录请求失败，请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="auth-view">
    <div class="auth-heading">
      <p class="eyebrow">顾客端</p>
      <h1>登录膳畅管家</h1>
      <p>{{ isRegistering ? '注册顾客账号后即可登录。' : '使用手机号登录，验证真实后端认证链路。' }}</p>
    </div>

    <div v-if="isAuthenticated && user" class="auth-session">
      <p class="feedback feedback-success" role="status">
        已登录，欢迎 {{ user.nickname }}。
      </p>
      <dl class="auth-user-details">
        <div>
          <dt>手机号</dt>
          <dd>{{ user.phone }}</dd>
        </div>
        <div>
          <dt>用户 ID</dt>
          <dd>{{ user.userId }}</dd>
        </div>
      </dl>
      <button class="secondary-button" type="button" @click="authStore.logout">退出登录</button>
    </div>

    <form v-else class="auth-form" @submit.prevent="handleSubmit">
      <label>
        手机号
        <input
          v-model="phone"
          type="tel"
          inputmode="numeric"
          autocomplete="tel"
          placeholder="请输入手机号"
          required
        />
      </label>

      <label v-if="isRegistering">
        昵称
        <input v-model="nickname" maxlength="16" required placeholder="请输入昵称" />
      </label>

      <label>
        密码
        <input
          v-model="password"
          type="password"
          autocomplete="current-password"
          placeholder="请输入密码"
          required
        />
      </label>

      <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '处理中...' : isRegistering ? '注册' : '登录' }}
      </button>
      <button class="secondary-button" type="button" @click="isRegistering = !isRegistering; errorMessage = ''">
        {{ isRegistering ? '已有账号，去登录' : '注册新账号' }}
      </button>
    </form>
  </section>
</template>
