<script setup lang="ts">
// 管理端登录页：提交员工凭证，成功后把服务端 Token 写入员工 Store。
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginEmployee } from '../services/admin-auth'
import { useAdminAuthStore } from '../stores/admin-auth'

const phone = ref('')
const password = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)
const router = useRouter()
const route = useRoute()

async function submit() {
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    useAdminAuthStore().login(await loginEmployee({ phone: phone.value, password: password.value }))
    await router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/orders')
  } catch (error) { errorMessage.value = error instanceof Error ? error.message : '员工登录失败' }
  finally { isSubmitting.value = false }
}
</script>
<template>
  <section class="auth-view"><div class="auth-heading"><p class="eyebrow">管理端</p><h1>员工登录</h1><p>登录后进入订单和会话工作台。</p></div>
    <form class="auth-form" @submit.prevent="submit"><label>手机号<input v-model="phone" required autocomplete="username" /></label><label>密码<input v-model="password" required type="password" autocomplete="current-password" /></label><p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p><button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '登录中...' : '登录管理端' }}</button></form>
  </section>
</template>
