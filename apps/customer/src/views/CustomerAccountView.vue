<script setup lang="ts">
// 顾客账户中心:R4 从"认证状态验证页"升级为真实账户中心。
// 后端当前没有用户资料编辑/修改密码接口(属候选能力),所以本页只展示
// 登录身份信息 + 常用入口 + 退出登录;资料编辑接口出现后再扩展表单区。
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()
// storeToRefs 保留 ref 响应性;直接解构 store 属性会丢失响应式连接。
const { user } = storeToRefs(authStore)

// 退出登录:确认弹窗拦截误触,清空 Store 后回到登录页。
async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确认退出当前账号吗?',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  authStore.logout()
  showSuccessToast('已退出登录')
  await router.push({ name: 'customer-login' })
}
</script>

<template>
  <section class="account-page">
    <div v-if="user" class="account-profile">
      <div class="account-avatar">{{ user.nickname.slice(0, 1) }}</div>
      <div>
        <strong class="account-nickname">{{ user.nickname }}</strong>
        <span class="account-phone">{{ user.phone }}</span>
      </div>
    </div>

    <van-cell-group inset title="我的服务">
      <van-cell title="我的预约" icon="calendar-o" is-link to="/reservations" />
      <van-cell title="我的订单" icon="orders-o" is-link to="/orders" />
      <van-cell title="当前用餐/开台" icon="shop-o" is-link to="/session" />
    </van-cell-group>

    <van-cell-group inset title="账号">
      <van-cell title="用户 ID" :value="user?.userId" />
      <van-cell title="退出登录" icon="revoke" is-link center @click="handleLogout" />
    </van-cell-group>

    <p class="account-note">当前后端暂未提供资料编辑与修改密码接口,账号信息以注册时为准。</p>
  </section>
</template>

<style scoped>
.account-page {
  display: grid;
  gap: var(--space-4);
}
.account-profile {
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(37, 37, 37, 0.04);
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
}
.account-avatar {
  align-items: center;
  background: var(--color-brand-soft);
  border-radius: 50%;
  color: var(--color-brand);
  display: flex;
  font-size: 1.4rem;
  font-weight: 700;
  height: 56px;
  justify-content: center;
  width: 56px;
}
.account-nickname {
  display: block;
  font-size: 1.05rem;
}
.account-phone {
  color: var(--color-text-secondary);
  font-size: 0.88rem;
}
.account-note {
  color: var(--color-text-muted);
  font-size: 0.8rem;
  line-height: 1.6;
  margin: 0;
  padding: 0 var(--space-4);
}
</style>
