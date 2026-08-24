<script setup lang="ts">
// RouterLink 负责声明式导航；RouterView 根据当前 URL 渲染匹配的页面组件。
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header class="app-header">
    <RouterLink class="brand" to="/">膳畅管家</RouterLink>
    <nav aria-label="主导航">
      <RouterLink to="/">首页</RouterLink>
      <RouterLink to="/customer/login">顾客登录</RouterLink>
      <RouterLink to="/customer/account">顾客账户</RouterLink>
      <RouterLink to="/customer/reservations/create">预约</RouterLink>
      <RouterLink to="/customer/reservations">我的预约</RouterLink>
      <RouterLink to="/customer/session">模拟开台</RouterLink>
      <RouterLink to="/customer/menu">菜单</RouterLink>
      <RouterLink to="/customer/orders">我的订单</RouterLink>
      <RouterLink to="/admin/orders">管理订单</RouterLink>
      <RouterLink to="/admin/sessions">管理会话</RouterLink>
      <RouterLink to="/admin/resources">资料维护</RouterLink>
    </nav>
  </header>
  <main class="app-main">
    <RouterView />
  </main>
</template>
