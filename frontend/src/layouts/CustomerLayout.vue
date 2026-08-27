<script setup lang="ts">
// 顾客端布局:真实场景是顾客用手机扫桌码打开 H5,因此按移动优先设计——
// 顶栏只放品牌和右上角账户入口,业务主导航用 Vant Tabbar 固定在底部。
// 菜单点餐页自带的底部购物车栏(VanSubmitBar)会盖在 Tabbar 之上,由该页面自己管理。
import { RouterLink, RouterView } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
// 登录态驱动顶栏右上角:未登录显示"登录 / 注册",已登录显示昵称入口。
const { isAuthenticated, user } = storeToRefs(authStore)

// exactOnly 标记"首页":vue-router 中"/"是所有顾客页的父路由记录,
// 普通的 active 规则会让"首页"在任何顾客页面都高亮,所以它只按精确匹配高亮。
// Tabbar 图标用 Vant 内置的字体图标名(字符串);换自定义图标时改 icon 前缀配置即可。
const tabItems = [
  { label: '首页', to: '/', icon: 'wap-home-o', exactOnly: true },
  { label: '点餐', to: '/customer/menu', icon: 'goods-collect-o' },
  { label: '预约', to: '/customer/reservations', icon: 'calendar-o' },
  { label: '订单', to: '/customer/orders', icon: 'orders-o' },
]
</script>

<template>
  <div class="customer-shell">
    <header class="customer-topbar">
      <RouterLink class="customer-brand" to="/">膳畅管家</RouterLink>
      <nav class="customer-account" aria-label="账户入口">
        <RouterLink v-if="isAuthenticated && user" to="/customer/account">
          <van-icon name="user-o" />
          {{ user.nickname }}
        </RouterLink>
        <RouterLink v-else to="/customer/login">登录 / 注册</RouterLink>
      </nav>
    </header>

    <main class="customer-main">
      <RouterView />
    </main>

    <!-- placeholder 固定占位:Tabbar 是 fixed 定位,用它把内容区底部撑出等高空间,避免最后一段被遮住。 -->
    <van-tabbar placeholder>
      <!-- 首页项手动控制高亮:父路由 active 规则会让它常亮,改为仅精确匹配时点亮。 -->
      <van-tabbar-item
        v-for="item in tabItems"
        :key="item.to"
        :to="item.to"
        :icon="item.icon"
        :class="item.exactOnly ? 'customer-tab-exact' : undefined"
      >
        {{ item.label }}
      </van-tabbar-item>
    </van-tabbar>
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
  align-items: center;
  color: var(--color-text-secondary);
  display: inline-flex;
  font-size: 0.92rem;
  gap: 4px;
}
.customer-account a:hover {
  color: var(--color-brand);
}
.customer-main {
  flex: 1;
  margin: 0 auto;
  max-width: 720px;
  padding: var(--space-4) var(--space-4) var(--space-6);
  width: 100%;
}
/* Tabbar 主色已由 tokens.css 的 --van-primary-color 接管,这里只修正首页项的高亮规则:
   默认 RouterLink active 会让"/"在任何顾客页命中,强制非精确匹配时回到未激活配色。 */
.customer-tab-exact:not(.van-tabbar-item--active) {
  color: var(--color-text-secondary);
}
</style>
