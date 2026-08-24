<script setup lang="ts">
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
    </nav>
  </header>
  <main class="app-main">
    <RouterView />
  </main>
</template>
