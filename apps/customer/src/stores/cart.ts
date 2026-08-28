// 购物车 Store(F5):把菜单页的临时草稿状态提升为跨页面存活的应用状态。
// 拆分前 cart 是菜单页级 ref,切到订单页再回来内容即丢;改为 Pinia Store 后,
// 顾客可在点餐/订单/预约之间来回切换,购物车内容与应用生命周期一致。
// 仅保存 dishId -> quantity 映射:菜品名和价格提交前从后端目录重新解析,不信任前端快照。
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { DishData } from '@foodflow/shared/types/api'

export const useCartStore = defineStore('cart', () => {
  // dishId -> 数量;零数量即移除,避免提交空行。
  const quantities = ref<Record<number, number>>({})
  // 提交成功后的记忆快照,只用于"已下单"结果面板的展示,不参与业务计算。
  const lastOrderSummary = ref('')

  function setQuantity(dishId: number, quantity: number) {
    if (quantity <= 0) {
      delete quantities.value[dishId]
    } else {
      quantities.value[dishId] = quantity
    }
  }

  function clear() {
    quantities.value = {}
  }

  // 已知目录(由菜单页把后端菜品表注入)派生明细;catalog 为空时明细为空,不伪造数据。
  const catalog = ref<Record<number, DishData>>({})

  function syncCatalog(dishes: DishData[]) {
    const next: Record<number, DishData> = {}
    for (const dish of dishes) next[dish.id] = dish
    catalog.value = next
  }

  const items = computed(() =>
    Object.keys(quantities.value)
      .map(Number)
      .map((dishId) => catalog.value[dishId])
      .filter((dish): dish is DishData => dish !== undefined),
  )

  const totalCount = computed(() => Object.values(quantities.value).reduce((sum, quantity) => sum + quantity, 0))

  const totalPrice = computed(() =>
    items.value.reduce((total, dish) => total + dish.price * (quantities.value[dish.id] ?? 0), 0),
  )

  const summary = computed(() =>
    items.value.length === 0
      ? '先选一些菜品吧'
      : items.value.map((dish) => `${dish.name} × ${quantities.value[dish.id]}`).join('、'),
  )

  return {
    quantities,
    catalog,
    items,
    totalCount,
    totalPrice,
    summary,
    lastOrderSummary,
    setQuantity,
    clear,
    syncCatalog,
  }
})
