<script setup lang="ts">
// 顾客账户页：读取认证 Store，验证受保护路由和退出登录行为。
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
// storeToRefs 保留 ref 响应性；直接解构 store 属性会丢失响应式连接。
const { user } = storeToRefs(authStore)
</script>

<template>
  <section class="home-view account-view">
    <p class="eyebrow">顾客账户</p>
    <h1>认证状态已验证</h1>
    <p class="summary">这是一个受认证保护的最小页面，后续预约和点餐页面会沿用同一条路由边界。</p>
    <dl v-if="user" class="status-list">
      <div>
        <dt>昵称</dt>
        <dd>{{ user.nickname }}</dd>
      </div>
      <div>
        <dt>手机号</dt>
        <dd>{{ user.phone }}</dd>
      </div>
      <div>
        <dt>用户 ID</dt>
        <dd>{{ user.userId }}</dd>
      </div>
    </dl>
  </section>
</template>
