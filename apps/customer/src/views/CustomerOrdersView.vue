<script setup lang="ts">
// 顾客订单页:先加载摘要列表,用户展开某项时再请求详情明细。
// R4 改用 Vant:订单卡片+状态 Tag,详情展开为菜品明细单元格组。
import { onMounted, ref } from 'vue'
import { getOrderDetail, getOrders } from '../services/order-query'
import type { OrderData, OrderDetailData } from '@foodflow/shared/types/api'
import { getOrderStatusLabel, getOrderTagKind } from '@foodflow/shared/utils/order-status'
import { formatDateTime, formatPrice } from '@foodflow/shared/utils/format'
import { useAutoRefresh } from '../composables/use-autoRefresh'

// 列表和当前展开详情分开存储,避免详情响应覆盖摘要列表。
const orders = ref<OrderData[]>([])
const selectedOrder = ref<OrderDetailData | null>(null)
const isLoading = ref(true)
const detailLoadingId = ref<number | null>(null)
const errorMessage = ref('')

// Vant 的 Tag 没有 info 色:把共享映射里的 info 适配为 Vant 的 default,
// 颜色规则本身(进行中暖色、完成绿、取消灰)集中在 utils/order-status.ts。
function orderTagType(status: number): 'primary' | 'warning' | 'success' | 'default' {
  const kind = getOrderTagKind(status)
  return kind === 'info' ? 'default' : kind
}

// silent 表示聚焦刷新触发的静默加载:不点亮加载动画,失败保留当前列表不惊扰用户。
async function loadOrders(options?: { silent?: boolean }) {
  if (!options?.silent) {
    isLoading.value = true
    errorMessage.value = ''
  }
  try {
    orders.value = await getOrders()
  } catch (error) {
    if (options?.silent) {
      console.warn('[顾客订单] 静默刷新失败', error)
    } else {
      errorMessage.value = error instanceof Error ? error.message : '订单列表查询失败,请稍后重试'
    }
  } finally {
    if (!options?.silent) {
      isLoading.value = false
    }
  }
}

async function showDetail(order: OrderData) {
  // selectedOrder 是独立 ref:列表保留摘要,详情按需加载;再次点击收起。
  if (selectedOrder.value?.orderId === order.orderId) {
    selectedOrder.value = null
    return
  }
  detailLoadingId.value = order.orderId
  errorMessage.value = ''
  try {
    // 只有点击具体订单才查询明细,降低列表首次加载的响应体大小。
    selectedOrder.value = await getOrderDetail(order.orderId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单详情查询失败,请稍后重试'
  } finally {
    detailLoadingId.value = null
  }
}

// 聚焦刷新(三期 R3):顾客端不轮询,窗口重新可见/聚焦时静默拉最新订单。
useAutoRefresh(loadOrders, { polling: false })

onMounted(loadOrders)
</script>

<template>
  <section class="my-orders">
    <p v-if="errorMessage" class="my-orders-error" role="alert">{{ errorMessage }}</p>
    <van-loading v-if="isLoading" class="my-orders-loading" size="24px" vertical>正在查询订单...</van-loading>
    <van-empty v-else-if="orders.length === 0" description="当前没有订单记录">
      <van-button round type="primary" size="small" to="/menu">去点餐</van-button>
    </van-empty>

    <div v-else class="my-orders-list" role="list" aria-label="我的订单列表">
      <article v-for="order in orders" :key="order.orderId" class="my-orders-card">
        <div class="my-orders-card-head">
          <div>
            <strong>{{ order.orderNo }}</strong>
            <span>{{ order.tableNo }} · {{ formatDateTime(order.createTime) }}</span>
          </div>
          <van-tag :type="orderTagType(order.status)" round>{{ getOrderStatusLabel(order.status) }}</van-tag>
        </div>
        <p class="my-orders-amount">
          订单金额:<strong>{{ formatPrice(order.totalAmount) }}</strong>
        </p>
        <van-button size="small" plain :loading="detailLoadingId === order.orderId" @click="showDetail(order)">
          {{ selectedOrder?.orderId === order.orderId ? '收起明细' : '查看明细' }}
        </van-button>

        <van-cell-group v-if="selectedOrder?.orderId === order.orderId" inset class="my-orders-detail">
          <van-cell
            v-for="item in selectedOrder.items"
            :key="item.dishId"
            :title="`${item.dishName} × ${item.quantity}`"
            :value="formatPrice(item.amount)"
          />
          <van-cell title="订单总额" :value="formatPrice(selectedOrder.totalAmount)" class="my-orders-total" />
        </van-cell-group>
      </article>
    </div>
  </section>
</template>

<style scoped>
.my-orders {
  display: grid;
  gap: var(--space-3);
}
.my-orders-error {
  background: var(--color-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  margin: 0;
  padding: var(--space-2) var(--space-3);
}
.my-orders-loading {
  display: flex;
  justify-content: center;
  margin: var(--space-8) 0;
}
.my-orders-list {
  display: grid;
  gap: var(--space-3);
}
.my-orders-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(37, 37, 37, 0.04);
  display: grid;
  gap: var(--space-2);
  justify-items: start;
  padding: var(--space-4);
}
.my-orders-card-head {
  align-items: flex-start;
  display: flex;
  gap: var(--space-2);
  justify-content: space-between;
  width: 100%;
}
.my-orders-card-head strong {
  display: block;
  font-size: 0.95rem;
}
.my-orders-card-head span {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}
.my-orders-amount {
  color: var(--color-text-secondary);
  font-size: 0.88rem;
  margin: 0;
}
.my-orders-amount strong {
  color: var(--color-brand);
  font-size: 1rem;
}
.my-orders-detail {
  margin: var(--space-1) 0 0;
  width: 100%;
}
.my-orders-total :deep(.van-cell__value) {
  color: var(--color-brand);
  font-weight: 700;
}
</style>
