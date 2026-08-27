<script setup lang="ts">
// 顾客端布局:真实场景是顾客用手机扫桌码打开 H5,因此按移动优先设计——
// 顶栏只放品牌和右上角账户入口,业务主导航放底部 Tab 栏(R4 将替换为 Vant Tabbar)。
// 菜单点餐页自带的底部购物车栏会在 Tab 栏之上叠加,由该页面自己管理。
import { RouterLink, RouterView } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
// 登录态驱动顶栏右上角:未登录显示"登录 / 注册",已登录显示昵称入口。
const { isAuthenticated, user } = storeToRefs(authStore)

// exactOnly 标记"首页":vue-router 中"/"是所有顾客页的父路由记录,
// 普通的 active 规则会让"首页"在任何顾客页面都高亮,所以它只按精确匹配高亮。
const tabItems = [
  { label: '首页', to: '/', exactOnly: true },
  { label: '点餐', to: '/customer/menu' },
  { label: '预约', to: '/customer/reservations' },
  { label: '订单', to: '/customer/orders' },
]
</script>

<template>
  <div class="customer-shell">
    <header class="customer-topbar">
      <RouterLink class="customer-brand" to="/">膳畅管家</RouterLink>
      <nav class="customer-account" aria-label="账户入口">
        <RouterLink v-if="isAuthenticated && user" to="/customer/account">
          {{ user.nickname }}
        </RouterLink>
        <RouterLink v-else to="/customer/login">登录 / 注册</RouterLink>
      </nav>
    </header>

    <main class="customer-main">
      <RouterView />
    </main>

    <nav class="customer-tabbar" aria-label="顾客端主导航">
      <RouterLink
        v-for="item in tabItems"
        :key="item.to"
        :to="item.to"
        :active-class="item.exactOnly ? 'customer-tab-idle' : 'customer-tab-active'"
        exact-active-class="customer-tab-active"
      >
        {{ item.label }}
      </RouterLink>
    </nav>
  </div>
</template>

<style scoped>
.customer-shell {
  background: var(--color-bg-customer);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.customer-topbar {
  align-items: center;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  height: var(--topbar-height);
  justify-content: space-between;
  padding: 0 var(--space-4);
  position: sticky;
  top: 0;
  z-index: 10;
}
.customer-brand {
  color: var(--color-brand);
  font-weight: 700;
}
.customer-account a {
  color: var(--color-text-secondary);
  font-size: 0.92rem;
}
.customer-account a:hover {
  color: var(--color-brand);
}
.customer-main {
  flex: 1;
  margin: 0 auto;
  max-width: 720px;
  padding: var(--space-6) var(--space-4);
  width: 100%;
}
/* sticky + bottom:0 让 Tab 栏在内容超长时也始终贴住视口底部。 */
.customer-tabbar {
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  bottom: 0;
  display: flex;
  height: var(--tabbar-height);
  justify-content: space-around;
  position: sticky;
}
.customer-tabbar a {
  align-items: center;
  color: var(--color-text-secondary);
  display: flex;
  font-size: 0.88rem;
  padding: 0 var(--space-3);
}
.customer-tabbar a.customer-tab-active {
  color: var(--color-brand);
  font-weight: 600;
}
</style>
