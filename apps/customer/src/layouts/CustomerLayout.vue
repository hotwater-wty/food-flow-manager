<script setup lang="ts">
// 顾客端布局:真实场景是顾客用手机扫桌码打开 H5,因此按移动优先设计——
// 顶栏只放品牌和右上角账户入口,业务主导航用 Vant Tabbar 固定在底部。
// 菜单点餐页自带的底部购物车栏(VanSubmitBar)会盖在 Tabbar 之上,由该页面自己管理。
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
// 登录态驱动顶栏右上角:未登录显示"登录 / 注册",已登录显示昵称入口。
const { isAuthenticated, user } = storeToRefs(authStore)

// exactOnly 标记"首页":"/"是所有顾客页的父路径,必须精确匹配;
// 其余项用前缀匹配,覆盖如 /reservations/create 这类子路径。
const tabItems = [
  { label: '首页', to: '/', icon: 'wap-home-o', exactOnly: true },
  { label: '点餐', to: '/menu', icon: 'goods-collect-o' },
  { label: '预约', to: '/reservations', icon: 'calendar-o' },
  { label: '订单', to: '/orders', icon: 'orders-o' },
]

const route = useRoute()
// 高亮完全由当前路由推导,再单向绑定给 van-tabbar 的 model-value:
// 不让组件自己维护选中态,刷新或地址直达时高亮也不会错位;
// 账户中心等不在导航里的页面返回 -1,四个 Tab 全部不点亮。
const activeTabIndex = computed(() => {
  return tabItems.findIndex((item) => (item.exactOnly ? route.path === item.to : route.path.startsWith(item.to)))
})
</script>

<template>
  <div class="customer-shell">
    <header class="customer-topbar">
      <RouterLink class="customer-brand" to="/">膳畅管家</RouterLink>
      <nav class="customer-account" aria-label="账户入口">
        <RouterLink v-if="isAuthenticated && user" to="/account">
          <van-icon name="user-o" />
          {{ user.nickname }}
        </RouterLink>
        <RouterLink v-else to="/login">登录 / 注册</RouterLink>
      </nav>
    </header>

    <main class="customer-main">
      <!-- 路由切换过渡:顾客端只做轻量淡入淡出,避免移动端页面大幅位移。 -->
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <!-- placeholder 固定占位:Tabbar 是 fixed 定位,用它把内容区底部撑出等高空间,避免最后一段被遮住。 -->
    <van-tabbar placeholder :model-value="activeTabIndex">
      <!-- 高亮由 activeTabIndex(路由推导)控制;点击时 to 负责跳转,路由变化后再回推高亮。 -->
      <van-tabbar-item v-for="item in tabItems" :key="item.to" :to="item.to" :icon="item.icon">
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
/* Tabbar 主色已由 tokens.css 的 --van-primary-color 接管,无需额外高亮修正。 */
</style>
