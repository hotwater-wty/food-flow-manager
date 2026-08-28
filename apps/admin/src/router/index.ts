// 商户端路由(拆分后独立成应用):根路径即管理端首页,不再有 /admin 前缀。
// 原路径 /admin/** 的书签由顶部的 redirect 段兜底。
import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminLoginView from '../views/AdminLoginView.vue'
import { useAdminAuthStore } from '../stores/admin-auth'

// 模块声明合并为 RouteMeta 增加项目自己的字段,
// 这样 to.meta.requiresAdminAuth 在 TypeScript 中会被识别为布尔值,而不是任意属性。
// title 供布局顶栏显示当前页名;requiresManager 标记店长专属页面。
declare module 'vue-router' {
  interface RouteMeta {
    requiresAdminAuth?: boolean
    requiresManager?: boolean
    title?: string
  }
}

// createRouter 只描述路由和守卫;具体鉴权凭证由 Pinia Store 保存。
export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 登录页独立于布局:登录前不该看到业务导航。
    { path: '/login', name: 'admin-login', component: AdminLoginView },

    {
      path: '/',
      component: AdminLayout,
      children: [
        // 根路径直接进工作台;保留 /admin/orders 名称,登录回跳逻辑无需感知拆分。
        { path: '', redirect: { name: 'admin-orders' } },
        // 拆分前旧地址 /admin/** 兜底:去掉前缀后重定向到对应页面,保住旧书签。
        { path: 'admin/:pathMatch(.*)*', redirect: (to) => ({ path: to.params.pathMatch as string, query: to.query }) },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/admin/DashboardView.vue'),
          meta: { requiresAdminAuth: true, title: '经营概览' },
        },
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
