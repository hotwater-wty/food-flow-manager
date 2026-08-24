<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getOrderDetail, getOrders } from '../services/order-query'
import type { OrderData, OrderDetailData } from '../types/api'
import { getOrderStatusLabel } from '../utils/order-status'

const orders = ref<OrderData[]>([])
const selectedOrder = ref<OrderDetailData | null>(null)
const isLoading = ref(true)
const detailLoadingId = ref<number | null>(null)
const errorMessage = ref('')

function formatPrice(cents: number) {
  return `¥${(cents / 100).toFixed(2)}`
}

async function loadOrders() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    orders.value = await getOrders()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单列表查询失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

async function showDetail(order: OrderData) {
  if (selectedOrder.value?.orderId === order.orderId) {
    selectedOrder.value = null
    return
  }
  detailLoadingId.value = order.orderId
  errorMessage.value = ''
  try {
    selectedOrder.value = await getOrderDetail(order.orderId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单详情查询失败，请稍后重试'
  } finally {
    detailLoadingId.value = null
  }
}

onMounted(loadOrders)
</script>

<template>
  <section class="reservation-view">
    <div class="reservation-heading">
      <p class="eyebrow">顾客点餐</p>
      <h1>我的订单</h1>
      <p>查看堂食订单状态和菜品明细。</p>
    </div>

    <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>
    <p v-if="isLoading" class="feedback" role="status">正在查询订单...</p>
    <p v-else-if="orders.length === 0" class="feedback" role="status">当前没有订单记录。</p>

    <div v-else class="reservation-list" role="list" aria-label="我的订单列表">
      <article v-for="order in orders" :key="order.orderId" class="reservation-card">
        <div class="reservation-card-heading">
          <div>
            <strong>{{ order.orderNo }}</strong>
            <span>{{ order.tableNo }} · {{ order.createTime }}</span>
          </div>
          <span class="reservation-status">{{ getOrderStatusLabel(order.status) }}</span>
        </div>
        <p class="reservation-time">订单金额：{{ formatPrice(order.totalAmount) }}</p>
        <button type="button" class="secondary-button" :disabled="detailLoadingId === order.orderId" @click="showDetail(order)">
          {{ detailLoadingId === order.orderId ? '加载中...' : selectedOrder?.orderId === order.orderId ? '收起详情' : '查看详情' }}
        </button>
        <dl v-if="selectedOrder?.orderId === order.orderId" class="reservation-detail">
          <div v-for="item in selectedOrder.items" :key="item.dishId"><dt>{{ item.dishName }} × {{ item.quantity }}</dt><dd>{{ formatPrice(item.amount) }}</dd></div>
          <div><dt>订单总额</dt><dd>{{ formatPrice(selectedOrder.totalAmount) }}</dd></div>
        </dl>
      </article>
    </div>
  </section>
</template>
