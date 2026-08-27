// 路由集中描述页面入口,并在导航前区分顾客和员工两套认证边界。
// R1 起路由改为"布局父路由 + 页面子路由"的嵌套结构:
// 顾客页共用 CustomerLayout(顶栏 + 底部导航),管理页共用 AdminLayout(侧栏菜单树 + 顶栏),
// 两个登录页和 404 不进布局。所有页面 URL 与首版保持一致,外部书签不受影响。
import { createRouter, createWebHistory } from 'vue-router'
import CustomerLayout from '../layouts/CustomerLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
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

// 模块声明合并为 RouteMeta 增加项目自己的字段,
// 这样 to.meta.requiresAuth 在 TypeScript 中会被识别为布尔值,而不是任意属性。
// title 供布局顶栏显示当前页名;requiresManager 标记店长专属页面(员工管理与桌位删除同源)。
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresAdminAuth?: boolean
    requiresManager?: boolean
    title?: string
  }
}

// createRouter 只描述路由和守卫;具体鉴权凭证由 Pinia Store 保存。
export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 登录页独立于两端布局:登录前不该看到业务导航,登录后再进入对应布局。
    { path: '/customer/login', name: 'customer-login', component: CustomerLoginView },
    { path: '/admin/login', name: 'admin-login', component: () => import('../views/AdminLoginView.vue') },

    // 顾客端:子路由 path 不以"/"开头,表示拼接在父路径"/"之后。
    {
      path: '/',
      component: CustomerLayout,
      children: [
        { path: '', name: 'home', component: HomeView },
        {
          path: 'customer/account',
          name: 'customer-account',
          component: CustomerAccountView,
          meta: { requiresAuth: true, title: '我的账户' },
        },
        {
          path: 'customer/reservations/create',
          name: 'customer-reservation-create',
          component: CustomerReservationCreateView,
          meta: { requiresAuth: true, title: '发起预约' },
        },
        {
          path: 'customer/reservations',
          name: 'customer-reservations',
          component: CustomerReservationsView,
          meta: { requiresAuth: true, title: '我的预约' },
        },
        {
          path: 'customer/session',
          name: 'customer-session',
          component: CustomerSessionView,
          meta: { requiresAuth: true, title: '扫码开台' },
        },
        {
          path: 'customer/menu',
          name: 'customer-menu',
          component: CustomerMenuView,
          meta: { requiresAuth: true, title: '菜单点餐' },
        },
        {
          path: 'customer/orders',
          name: 'customer-orders',
          component: CustomerOrdersView,
          meta: { requiresAuth: true, title: '我的订单' },
        },
      ],
    },

    // 管理端:子路由拼接在"/admin"之后,布局由 AdminLayout 提供。
    // R3 起资料维护拆为五个子路由页;requiresManager 的页面店员不可见且直接访问会被拦截。
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/AdminOrderWorkbenchView.vue'),
          meta: { requiresAdminAuth: true, title: '订单处理' },
        },
        {
          path: 'sessions',
          name: 'admin-sessions',
          component: () => import('../views/AdminSessionWorkbenchView.vue'),
          meta: { requiresAdminAuth: true, title: '会话与桌台' },
        },
        // 旧"资源维护"单页地址重定向到桌位页,保住一期存下的书签和记忆链接。
        { path: 'resources', redirect: { name: 'admin-resources-tables' } },
        {
          path: 'resources/tables',
          name: 'admin-resources-tables',
          component: () => import('../views/admin/TablesManageView.vue'),
          meta: { requiresAdminAuth: true, requiresManager: true, title: '桌位维护' },
        },
        {
          path: 'resources/categories',
          name: 'admin-resources-categories',
          component: () => import('../views/admin/CategoriesManageView.vue'),
          meta: { requiresAdminAuth: true, title: '菜品分类' },
        },
        {
          path: 'resources/dishes',
          name: 'admin-resources-dishes',
          component: () => import('../views/admin/DishesManageView.vue'),
          meta: { requiresAdminAuth: true, title: '菜品维护' },
        },
        {
          path: 'resources/reservations',
          name: 'admin-resources-reservations',
          component: () => import('../views/admin/ReservationsManageView.vue'),
          meta: { requiresAdminAuth: true, title: '预约管理' },
        },
        {
          path: 'resources/employees',
          name: 'admin-resources-employees',
          component: () => import('../views/admin/EmployeesManageView.vue'),
          meta: { requiresAdminAuth: true, requiresManager: true, title: '员工管理' },
        },
      ],
    },

    // 兜底 404::pathMatch(.*)* 匹配所有未命中的路径,之前这类地址会渲染空白页。
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

// beforeEach 在每次导航确认前运行;返回路由对象表示重定向,true 表示放行。
router.beforeEach((to) => {
  // 顾客路由与员工路由分别检查,避免一类 Token 被误当成另一类身份。
  if (to.meta.requiresAuth && !useAuthStore().isAuthenticated) {
    return { name: 'customer-login', query: { redirect: to.fullPath } }
  }
  const adminStore = useAdminAuthStore()
  if (to.meta.requiresAdminAuth && !adminStore.isAuthenticated) {
    return { name: 'admin-login', query: { redirect: to.fullPath } }
  }
  // 店长专属页面:店员访问时回到订单工作台;后端拦截器仍是最终防线,这里拦的是界面层。
  if (to.meta.requiresManager && adminStore.user?.role !== 2) {
    return { name: 'admin-orders' }
  }
  return true
})
