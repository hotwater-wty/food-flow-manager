import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import CustomerLoginView from '../views/CustomerLoginView.vue'
import CustomerAccountView from '../views/CustomerAccountView.vue'
import CustomerReservationCreateView from '../views/CustomerReservationCreateView.vue'
import { useAuthStore } from '../stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
  }
}

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
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !useAuthStore().isAuthenticated) {
    return { name: 'customer-login', query: { redirect: to.fullPath } }
  }
  return true
})
