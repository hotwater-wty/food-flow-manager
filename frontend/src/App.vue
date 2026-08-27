<script setup lang="ts">
// 根组件只负责渲染路由出口。页面结构由 layouts/ 里的布局组件决定:
// 顾客页渲染在 CustomerLayout 内,管理页渲染在 AdminLayout 内,登录页和 404 独立成页。
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>
