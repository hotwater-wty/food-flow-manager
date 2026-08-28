<script setup lang="ts">
// 顾客端首页:首版这里是脚手架状态页("业务接口:尚未调用"已过期),
// 现在改为真实业务入口,按登录态给出不同的欢迎语和行动按钮。
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const { isAuthenticated, user } = storeToRefs(authStore)
</script>

<template>
  <section class="home-view">
    <p class="eyebrow">膳畅管家</p>
    <h1>{{ isAuthenticated && user ? `欢迎回来,${user.nickname}` : '扫码点餐 · 预约座位' }}</h1>
    <p class="summary">
      到店后扫描桌码开台即可点餐,也可以提前预约座位;提交订单后可随时在"订单"里查看进度。
    </p>

    <div class="home-actions">
      <RouterLink class="home-primary-action" to="/menu">去点餐</RouterLink>
      <RouterLink class="secondary-button" to="/reservations/create">预约座位</RouterLink>
      <RouterLink v-if="!isAuthenticated" class="secondary-button" to="/login">
        登录 / 注册
      </RouterLink>
    </div>

    <dl class="status-list">
      <div>
        <dt>第一步</dt>
        <dd>扫码或进入"点餐"页开台,绑定桌位</dd>
      </div>
      <div>
        <dt>第二步</dt>
        <dd>选菜品加入购物车并提交订单</dd>
      </div>
      <div>
        <dt>第三步</dt>
        <dd>在底部"订单"里跟踪制作进度</dd>
      </div>
    </dl>

  </section>
</template>
