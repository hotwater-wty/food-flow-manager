<script setup lang="ts">
// 404 兜底页:捕获所有未匹配的路径。之前这类地址渲染成空白,
// 现在给出明确提示和两个出口(顾客端首页 / 员工登录),避免用户卡死在无效地址。
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="auth-view">
    <div class="auth-heading">
      <p class="eyebrow">404</p>
      <h1>页面不存在</h1>
      <p>当前地址没有对应的页面,可能链接已过期或输入有误。</p>
    </div>
    <div class="not-found-actions">
      <RouterLink class="secondary-button" to="/">返回顾客端首页</RouterLink>
      <RouterLink class="secondary-button" to="/admin/login">员工登录</RouterLink>
    </div>
  </section>
</template>
