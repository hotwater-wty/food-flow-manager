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
    </nav>
  </header>
  <main class="app-main">
    <RouterView />
  </main>
</template>
