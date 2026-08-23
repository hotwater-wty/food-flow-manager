<script setup lang="ts">
import { ref } from 'vue'
import { loginUser } from '../services/user-auth'
import type { UserLoginData } from '../types/api'

const phone = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const loginResult = ref<UserLoginData | null>(null)

async function handleSubmit() {
  errorMessage.value = ''
  loginResult.value = null
  isSubmitting.value = true

  try {
    loginResult.value = await loginUser({
      phone: phone.value,
      password: password.value,
    })
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
      <p>使用手机号登录，验证真实后端认证链路。</p>
    </div>

    <form class="auth-form" @submit.prevent="handleSubmit">
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
      <p v-if="loginResult" class="feedback feedback-success" role="status">
        登录成功，欢迎 {{ loginResult.nickname }}。本轮暂不保存 Token。
      </p>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '登录中...' : '登录' }}
      </button>
    </form>
  </section>
</template>
