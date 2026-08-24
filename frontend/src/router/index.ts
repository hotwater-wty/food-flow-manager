// 路由集中描述页面入口，并在导航前区分顾客和员工两套认证边界。
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import CustomerLoginView from '../views/CustomerLoginView.vue'
import CustomerAccountView from '../views/CustomerAccountView.vue'
import CustomerReservationCreateView from '../views/CustomerReservationCreateView.vue'
import CustomerReservationsView from '../views/CustomerReservationsView.vue'
import CustomerSessionView from '../views/CustomerSessionView.vue'
import CustomerMenuView from '../views/CustomerMenuView.vue'
import CustomerOrdersView from '../views/CustomerOrdersView.vue'
import { useAuthStore } from '../stores/auth'
import { useAdminAuthStore } from '../stores/admin-auth'

// 模块声明合并为 RouteMeta 增加项目自己的字段，
// 这样 to.meta.requiresAuth 在 TypeScript 中会被识别为布尔值，而不是任意属性。
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresAdminAuth?: boolean
  }
}

// createRouter 只描述路由和守卫；具体鉴权凭证由 Pinia Store 保存。
export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/customer/login',
      name: 'customer-login',
      component: CustomerLoginView,
    },
    {
      path: '/customer/account',
      name: 'customer-account',
      component: CustomerAccountView,
      meta: { requiresAuth: true },
    },
    {
      path: '/customer/reservations/create',
      name: 'customer-reservation-create',
      component: CustomerReservationCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/customer/reservations',
      name: 'customer-reservations',
      component: CustomerReservationsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/customer/session',
      name: 'customer-session',
      component: CustomerSessionView,
      meta: { requiresAuth: true },
    },
    {
      path: '/customer/menu',
      name: 'customer-menu',
      component: CustomerMenuView,
      meta: { requiresAuth: true },
    },
    {
      path: '/customer/orders',
      name: 'customer-orders',
      component: CustomerOrdersView,
      meta: { requiresAuth: true },
    },
    { path: '/admin/login', name: 'admin-login', component: () => import('../views/AdminLoginView.vue') },
    { path: '/admin/orders', name: 'admin-orders', component: () => import('../views/AdminOrderWorkbenchView.vue'), meta: { requiresAdminAuth: true } },
    { path: '/admin/sessions', name: 'admin-sessions', component: () => import('../views/AdminSessionWorkbenchView.vue'), meta: { requiresAdminAuth: true } },
    { path: '/admin/resources', name: 'admin-resources', component: () => import('../views/AdminResourcesView.vue'), meta: { requiresAdminAuth: true } },
  ],
})

// beforeEach 在每次导航确认前运行；返回路由对象表示重定向，true 表示放行。
router.beforeEach((to) => {
  // 顾客路由与员工路由分别检查，避免一类 Token 被误当成另一类身份。
  if (to.meta.requiresAuth && !useAuthStore().isAuthenticated) {
    return { name: 'customer-login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdminAuth && !useAdminAuthStore().isAuthenticated) {
    return { name: 'admin-login', query: { redirect: to.fullPath } }
  }
  return true
})
