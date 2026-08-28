<script setup lang="ts">
// 管理端登录页：提交员工凭证，成功后把服务端 Token 写入员工 Store。
// 登录页独立于 AdminLayout，登录前不显示任何业务导航。
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { loginEmployee } from '../services/admin-auth'
import { useAdminAuthStore } from '../stores/admin-auth'

const phone = ref('')
const password = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)
const router = useRouter()
const route = useRoute()

async function submit() {
  // 提交前锁定按钮并清掉旧错误，防止用户重复发送登录请求。
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    // await 等待登录完成后再写入 Store，随后根据 redirect 或默认工作台导航。
    useAdminAuthStore().login(await loginEmployee({ phone: phone.value, password: password.value }))
    await router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/orders')
  } catch (error) {
    // unknown 错误先用 instanceof 判断，再给模板一个稳定字符串。
    errorMessage.value = error instanceof Error ? error.message : '员工登录失败'
  }
  finally { isSubmitting.value = false }
}
</script>
<template>
  <section class="auth-view"><div class="auth-heading"><p class="eyebrow">管理端</p><h1>员工登录</h1><p>登录后进入订单和会话工作台。</p></div>
    <form class="auth-form" @submit.prevent="submit"><label>手机号<input v-model="phone" required autocomplete="username" /></label><label>密码<input v-model="password" required type="password" autocomplete="current-password" /></label><p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p><button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '登录中...' : '登录管理端' }}</button></form>
    <p class="auth-alt-link"><RouterLink to="/">返回顾客端首页</RouterLink></p>
  </section>
</template>
