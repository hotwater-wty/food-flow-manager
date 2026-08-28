<script setup lang="ts">
// 顾客认证页：在登录和注册两种模式间切换，写操作完成后更新认证上下文。
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { loginUser, registerUser } from '../services/user-auth'
import { useAuthStore } from '../stores/auth'

// ref 返回带 .value 的响应式引用；模板中会自动解包，脚本中必须显式读写 .value。
const phone = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const nickname = ref('')
// 一个布尔状态控制同一表单的两种提交分支，避免复制两套页面。
const isRegistering = ref(false)

// 登录/注册面板切换:切换时清掉上一次表单操作的错误提示。
function toggleRegistering() {
  isRegistering.value = !isRegistering.value
  errorMessage.value = ''
}
const authStore = useAuthStore()
// storeToRefs 保留 Store 字段的响应式连接，登录成功后模板会自动更新。
const { isAuthenticated, user } = storeToRefs(authStore)
// 路由守卫拦截未登录访问时会带上 redirect 查询参数，登录成功后按它回跳。
const router = useRouter()
const route = useRoute()

async function handleSubmit() {
  // 表单事件使用 preventDefault，避免浏览器刷新；isRegistering 决定调用哪个接口。
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    if (isRegistering.value) {
      // 注册分支只发送表单资料；服务层会把后端 Result 转成异常或数据。
      // 注册成功不自动登录，先切回登录模式，明确两个后端动作的边界。
      await registerUser({ phone: phone.value, password: password.value, nickname: nickname.value })
      isRegistering.value = false
      errorMessage.value = '注册成功，请使用新账号登录'
    } else {
      // 登录分支等待服务层返回 Token，再由 Store 统一持久化。
      // 登录成功才把 Token 交给 Store，Store 负责持久化和后续请求鉴权。
      const loginData = await loginUser({ phone: phone.value, password: password.value })
      authStore.login(loginData)
      // 登录页已脱离顾客端布局，登录成功后必须主动跳转：
      // 优先回跳守卫带来的 redirect，没有则进入默认的点餐页。
      await router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/menu')
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
      <p class="feedback feedback-success" role="status">已登录，欢迎 {{ user.nickname }}。</p>
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
      <div class="auth-session-actions">
        <RouterLink class="secondary-button" to="/menu">进入点餐</RouterLink>
        <button class="secondary-button" type="button" @click="authStore.logout">退出登录</button>
      </div>
    </div>

    <form v-else class="auth-form" @submit.prevent="handleSubmit">
      <label>
        手机号
        <input v-model="phone" type="tel" inputmode="numeric" autocomplete="tel" placeholder="请输入手机号" required />
      </label>

      <label v-if="isRegistering">
        昵称
        <input v-model="nickname" maxlength="16" required placeholder="请输入昵称" />
      </label>

      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" placeholder="请输入密码" required />
      </label>

      <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '处理中...' : isRegistering ? '注册' : '登录' }}
      </button>
      <button class="secondary-button" type="button" @click="toggleRegistering">
        {{ isRegistering ? '已有账号，去登录' : '注册新账号' }}
      </button>
    </form>
  </section>
</template>
