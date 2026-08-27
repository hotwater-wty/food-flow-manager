<script setup lang="ts">
// 管理端布局:业务功能集中在左侧菜单树,员工身份和登出集中在顶栏右上角。
// 布局组件只负责结构和导航,不发请求、不碰业务数据;页面数据仍由各视图自己加载。
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAdminAuthStore } from '../stores/admin-auth'

// useRoute 读当前路由(meta.title 由路由表提供),useRouter 做编程式跳转。
const route = useRoute()
const router = useRouter()
const adminAuthStore = useAdminAuthStore()
// storeToRefs 保持 Store 字段的响应式连接;员工登录/退出后顶栏会自动更新。
const { user } = storeToRefs(adminAuthStore)

// 菜单用数据驱动而不是把链接硬编码进模板,新增页面只需在这里加一项。
// R3 拆分资料维护后,"资源维护"项会被桌位/分类/菜品/预约/员工五个子项替换。
const menuGroups = [
  {
    label: '经营工作台',
    items: [
      { label: '订单处理', to: '/admin/orders' },
      { label: '会话与桌台', to: '/admin/sessions' },
    ],
  },
  {
    label: '资料维护',
    items: [{ label: '资源维护', to: '/admin/resources' }],
  },
]

// role 是后端枚举(1=店员,2=店长),显示层负责翻译成中文标签。
const roleLabel = computed(() => (user.value?.role === 2 ? '店长' : '店员'))

// 登出先清空 Store(内存 + localStorage),再回到员工登录页。
function handleLogout() {
  adminAuthStore.logout()
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <RouterLink class="admin-brand" to="/">
        膳畅管家<span>商户端</span>
      </RouterLink>
      <nav class="admin-menu" aria-label="管理端导航">
        <section v-for="group in menuGroups" :key="group.label" class="admin-menu-group">
          <p class="admin-menu-label">{{ group.label }}</p>
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            class="admin-menu-item"
            :to="item.to"
          >
            {{ item.label }}
          </RouterLink>
        </section>
      </nav>
    </aside>

    <div class="admin-body">
      <header class="admin-topbar">
        <p class="admin-page-title">{{ route.meta.title }}</p>
        <div v-if="user" class="admin-user">
          <span class="admin-user-name">{{ user.name }}</span>
          <span class="admin-role-tag" :class="{ 'admin-role-tag--manager': user.role === 2 }">
            {{ roleLabel }}
          </span>
          <button class="admin-logout-button" type="button" @click="handleLogout">退出登录</button>
        </div>
      </header>
      <main class="admin-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
/* scoped 样式只作用于本组件的 DOM,布局私有结构不再写进全局 style.css。 */
.admin-shell {
  background: var(--color-bg-admin);
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  min-height: 100vh;
}
.admin-sidebar {
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  height: 100vh;
  overflow-y: auto;
  padding: var(--space-6) var(--space-4);
  position: sticky;
  top: 0;
}
.admin-brand {
  color: var(--color-brand);
  font-size: 1.05rem;
  font-weight: 700;
}
.admin-brand span {
  color: var(--color-text-muted);
  font-size: 0.78rem;
  font-weight: 500;
  margin-left: var(--space-2);
}
.admin-menu {
  display: grid;
  gap: var(--space-6);
}
.admin-menu-group {
  display: grid;
  gap: var(--space-2);
}
.admin-menu-label {
  color: var(--color-text-muted);
  font-size: 0.75rem;
  letter-spacing: 0.06em;
  margin: 0 0 var(--space-1);
}
.admin-menu-item {
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  display: block;
  font-size: 0.92rem;
  padding: var(--space-2) var(--space-3);
}
.admin-menu-item:hover {
  background: var(--color-brand-soft);
  color: var(--color-brand);
}
.admin-menu-item.router-link-active {
  background: var(--color-brand);
  color: #fff;
  font-weight: 600;
}
.admin-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.admin-topbar {
  align-items: center;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  height: var(--topbar-height);
  justify-content: space-between;
  padding: 0 var(--space-6);
  position: sticky;
  top: 0;
  z-index: 10;
}
.admin-page-title {
  color: var(--color-text);
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}
.admin-user {
  align-items: center;
  display: flex;
  gap: var(--space-3);
}
.admin-user-name {
  color: var(--color-text);
  font-size: 0.92rem;
  font-weight: 500;
}
.admin-role-tag {
  background: var(--color-bg-admin);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: 0.75rem;
  padding: 2px var(--space-2);
}
.admin-role-tag--manager {
  background: var(--color-brand-soft);
  border-color: transparent;
  color: var(--color-brand);
}
.admin-logout-button {
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  font: inherit;
  font-size: 0.85rem;
  padding: 6px var(--space-3);
}
.admin-logout-button:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
}
.admin-content {
  flex: 1;
  margin: 0 auto;
  max-width: var(--content-max-width);
  padding: var(--space-6) var(--space-6) var(--space-8);
  width: 100%;
}
/* 窄屏管理端:侧栏折叠为顶部横向菜单,保证 390px 下仍可操作。 */
@media (max-width: 760px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }
  .admin-sidebar {
    border-bottom: 1px solid var(--color-border);
    border-right: 0;
    height: auto;
    position: static;
  }
  .admin-menu {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-2);
  }
  /* display: contents 让分组容器在横向布局里"消失",子项直接参与 flex 排列。 */
  .admin-menu-group {
    display: contents;
  }
  .admin-menu-label {
    display: none;
  }
  .admin-topbar {
    padding: 0 var(--space-4);
  }
  .admin-content {
    padding: var(--space-4);
  }
}
</style>
