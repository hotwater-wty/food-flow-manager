<script setup lang="ts">
// 管理订单工作台：分页读取订单，并按照后端状态机推进订单状态。
import { onMounted, ref } from 'vue'
import { getAdminOrderDetail, getAdminOrders, updateOrderStatus } from '../services/admin-order'
import type { AdminOrderData, OrderDetailData } from '../types/api'
import { getOrderStatusLabel } from '../utils/order-status'

const orders = ref<AdminOrderData[]>([])
const selectedOrder = ref<OrderDetailData | null>(null)
// 分页、筛选、加载锁和错误信息分别由独立 ref 管理，模板可精确绑定每种状态。
const pageNo = ref(1)
const total = ref(0)
const status = ref<number | undefined>()
const loading = ref(true)
const actionId = ref<number | null>(null)
const errorMessage = ref('')
// 后端金额是整数分；只在展示边界转换为两位小数。
const formatPrice = (cents: number) => `¥${(cents / 100).toFixed(2)}`

// 列表请求更新 records 和 total，模板据此决定空状态和分页按钮。
async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    // await 将 PageResult 解析为普通对象，再分别取当前页 records 与总数 total。
    const result = await getAdminOrders(pageNo.value, status.value)
    orders.value = result.records
    total.value = result.total
  } catch (error) {
    // unknown 错误经过 instanceof Error 类型收窄后才能读取 message。
    errorMessage.value = error instanceof Error ? error.message : '管理订单查询失败'
  } finally {
    // 无论成功失败都解除加载状态，避免按钮永久 disabled。
    loading.value = false
  }
}

// 同一订单再次点击时只收起本地详情，不重复请求网络。
async function detail(order: AdminOrderData) {
  if (selectedOrder.value?.orderId === order.orderId) {
    selectedOrder.value = null
    return
  }
  try {
    selectedOrder.value = await getAdminOrderDetail(order.orderId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单详情查询失败'
  }
}

// 状态推进采用当前状态 + 1，后端仍会拒绝跳级或非法状态。
async function advance(order: AdminOrderData) {
  // actionId 非空表示已有写请求；早返回避免并发推进多个订单。
  if (order.status >= 4 || actionId.value !== null) return
  actionId.value = order.orderId
  errorMessage.value = ''
  try {
    // 前端只计算相邻目标状态，后端状态机仍是最终裁判。
    await updateOrderStatus(order.orderId, order.status + 1)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单状态更新失败'
  } finally {
    actionId.value = null
  }
}

// onMounted 接收函数引用；组件插入 DOM 后由 Vue 自动调用一次。
onMounted(load)
</script>
<template><section class="reservation-view"><div class="reservation-heading"><p class="eyebrow">管理端订单</p><h1>订单工作台</h1><p>查看订单并按后端状态机推进制作流程。</p></div><div class="admin-toolbar"><label>状态<select v-model="status" @change="pageNo = 1; load()"><option :value="undefined">全部</option><option :value="1">已下单</option><option :value="2">制作中</option><option :value="3">已上齐</option><option :value="4">已完成</option><option :value="5">已取消</option></select></label></div><p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p><p v-if="loading" class="feedback" role="status">正在查询订单...</p><p v-else-if="orders.length === 0" class="feedback" role="status">当前没有订单。</p><div v-else class="reservation-list"><article v-for="order in orders" :key="order.orderId" class="reservation-card"><div class="reservation-card-heading"><div><strong>{{ order.orderNo }}</strong><span>{{ order.tableNo }} · {{ order.createTime }}</span></div><span class="reservation-status">{{ getOrderStatusLabel(order.status) }}</span></div><p class="reservation-time">{{ formatPrice(order.totalAmount) }}</p><div class="reservation-actions"><button class="secondary-button" type="button" @click="detail(order)">{{ selectedOrder?.orderId === order.orderId ? '收起详情' : '详情' }}</button><button v-if="order.status >= 1 && order.status <= 3" class="primary-outline-button" type="button" :disabled="actionId === order.orderId" @click="advance(order)">{{ actionId === order.orderId ? '更新中...' : order.status === 1 ? '开始制作' : order.status === 2 ? '标记已上齐' : '完成订单' }}</button></div><dl v-if="selectedOrder?.orderId === order.orderId" class="reservation-detail"><div v-for="item in selectedOrder.items" :key="item.dishId"><dt>{{ item.dishName }} × {{ item.quantity }}</dt><dd>{{ formatPrice(item.amount) }}</dd></div></dl></article></div><p class="feedback">第 {{ pageNo }} 页，共 {{ total }} 条</p><div class="pagination-actions"><button class="secondary-button" type="button" :disabled="pageNo <= 1 || loading" @click="pageNo--; load()">上一页</button><button class="secondary-button" type="button" :disabled="pageNo * 10 >= total || loading" @click="pageNo++; load()">下一页</button></div></section></template>
