<script setup lang="ts">
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { loginUser } from '../services/user-auth'
import { useAuthStore } from '../stores/auth'

const phone = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const authStore = useAuthStore()
const { isAuthenticated, user } = storeToRefs(authStore)

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    const loginData = await loginUser({
      phone: phone.value,
      password: password.value,
    })
    authStore.login(loginData)
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
      <p>使用手机号登录，验证真实后端认证链路。</p>
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
        {{ isSubmitting ? '登录中...' : '登录' }}
      </button>
    </form>
  </section>
</template>
