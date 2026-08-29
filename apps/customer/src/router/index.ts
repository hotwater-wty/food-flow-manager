// 顾客端路由(拆分后独立成应用):根路径为顾客首页,全部路由都在本应用内。
// 拆分前旧地址 /customer/** 的书签由 redirect 段兜底。
import { createRouter, createWebHistory } from 'vue-router'
import CustomerLayout from '../layouts/CustomerLayout.vue'
import HomeView from '../views/HomeView.vue'
import CustomerLoginView from '../views/CustomerLoginView.vue'
import CustomerAccountView from '../views/CustomerAccountView.vue'
import CustomerReservationCreateView from '../views/CustomerReservationCreateView.vue'
import CustomerReservationsView from '../views/CustomerReservationsView.vue'
import CustomerSessionView from '../views/CustomerSessionView.vue'
import CustomerMenuView from '../views/CustomerMenuView.vue'
import CustomerOrdersView from '../views/CustomerOrdersView.vue'
import { useAuthStore } from '../stores/auth'

// 模块声明合并为 RouteMeta 增加项目自己的字段,
// 这样 to.meta.requiresAuth 在 TypeScript 中会被识别为布尔值,而不是任意属性。
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    title?: string
  }
}

// createRouter 只描述路由和守卫;具体鉴权凭证由 Pinia Store 保存。
export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 登录页独立于布局:登录前不该看到业务导航。
    { path: '/login', name: 'customer-login', component: CustomerLoginView },

    // 顾客端:子路由 path 不以"/"开头,表示拼接在父路径"/"之后。
    {
      path: '/',
      component: CustomerLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        // 拆分前旧地址 /customer/** 兜底:去掉前缀后重定向到对应页面,保住旧书签。
        // 拆分前旧地址 /customer/** 兜底:去掉前缀后重定向到对应页面,保住旧书签。
        // pathMatch 在带 repeat 的通配下是字符串数组,需自行拼回路径。
        {
          path: 'customer/:pathMatch(.*)*',
          redirect: (to) => {
            const rest = Array.isArray(to.params.pathMatch)
              ? to.params.pathMatch.join('/')
              : (to.params.pathMatch ?? '')
            return { path: `/${rest}`, query: to.query }
          },
        },
        {
          path: 'account',
          name: 'customer-account',
          component: CustomerAccountView,
          meta: { requiresAuth: true, title: '我的账户' },
        },
        {
          path: 'reservations/create',
          name: 'customer-reservation-create',
          component: CustomerReservationCreateView,
          meta: { requiresAuth: true, title: '发起预约' },
        },
        {
          path: 'reservations',
          name: 'customer-reservations',
          component: CustomerReservationsView,
          meta: { requiresAuth: true, title: '我的预约' },
        },
        {
          path: 'session',
          name: 'customer-session',
          component: CustomerSessionView,
          meta: { requiresAuth: true, title: '选择座位' },
        },
        {
          path: 'menu',
          name: 'customer-menu',
          component: CustomerMenuView,
          meta: { requiresAuth: true, title: '菜单点餐' },
        },
        {
          path: 'orders',
          name: 'customer-orders',
          component: CustomerOrdersView,
          meta: { requiresAuth: true, title: '我的订单' },
        },
      ],
    },

    // 兜底 404:pathMatch(.*)* 匹配所有未命中的路径,之前这类地址会渲染空白页。
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

// beforeEach 在每次导航确认前运行;返回路由对象表示重定向,true 表示放行。
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !useAuthStore().isAuthenticated) {
    return { name: 'customer-login', query: { redirect: to.fullPath } }
  }
  return true
})
