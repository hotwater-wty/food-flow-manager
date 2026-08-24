<script setup lang="ts">
// 顾客菜单页：后端菜品是事实，购物车是当前页面内等待提交的临时状态。
import { computed, onMounted, ref } from 'vue'
import { getDishCategories, getDishDetail, getDishes } from '../services/dish'
import { getCurrentSession } from '../services/session'
import { createOrder } from '../services/order'
import type { DishCategoryData, DishData, DiningSessionData, OrderCreateData } from '../types/api'

// categories/dishes 保存后端目录；cart 只保存 dishId -> quantity 的页面草稿。
const categories = ref<DishCategoryData[]>([])
const dishes = ref<DishData[]>([])
const dishCatalog = ref<Record<number, DishData>>({})
const selectedCategoryId = ref<number | null>(null)
const cart = ref<Record<number, number>>({})
const isLoading = ref(true)
const isFiltering = ref(false)
const errorMessage = ref('')
const currentSession = ref<DiningSessionData | null>(null)
const isSubmitting = ref(false)
const orderResult = ref<OrderCreateData | null>(null)
const selectedDish = ref<DishData | null>(null)

// computed 会追踪 cart 和 dishCatalog；任一变化时自动重新生成提交/展示列表。
// Object.keys 返回字符串数组，map(Number) 将对象键恢复成后端需要的数字 ID。
const cartItems = computed(() => Object.keys(cart.value).map(Number).map((dishId) => dishCatalog.value[dishId]).filter((dish): dish is DishData => dish !== undefined))
// reduce 从 0 开始累加数量；这两个 computed 会在 cart.value 改变时自动重新计算。
const cartCount = computed(() => Object.values(cart.value).reduce((total, quantity) => total + quantity, 0))
const cartTotal = computed(() => cartItems.value.reduce((total, dish) => total + dish.price * (cart.value[dish.id] ?? 0), 0))

function formatPrice(cents: number) {
  // 金额只在 UI 层从“分”转换为“元”，内部仍使用整数计算。
  return `¥${(cents / 100).toFixed(2)}`
}

function addToCart(dish: DishData) {
  // ?? 0 处理首次加入；对象 key 使用稳定 dish.id。
  // 先取旧数量，空值用 0；再写回对象属性触发 Vue 对对象的响应式更新。
  cart.value[dish.id] = (cart.value[dish.id] ?? 0) + 1
}

function decreaseFromCart(dish: DishData) {
  // 数量降到 0 时删除 key，使 cartItems 不再包含该商品。
  const nextQuantity = (cart.value[dish.id] ?? 0) - 1
  if (nextQuantity <= 0) {
    delete cart.value[dish.id]
  } else {
    cart.value[dish.id] = nextQuantity
  }
}

async function selectCategory(categoryId: number | null) {
  // 分类切换是一次新的查询；加载期间用 isFiltering 禁用/提示，而不清空旧数据。
  selectedCategoryId.value = categoryId
  isFiltering.value = true
  errorMessage.value = ''
  try {
    const result = await getDishes(categoryId === null ? undefined : categoryId)
    dishes.value = result
    // forEach 逐项把新列表合并进目录，购物车中已有商品仍能找到价格和名称。
    result.forEach((dish) => { dishCatalog.value[dish.id] = dish })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品查询失败，请稍后重试'
  } finally {
    isFiltering.value = false
  }
}

async function loadMenu() {
  // Promise.all 并行加载分类、菜品和当前会话，三者都成功后统一更新页面。
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [categoryData, dishData, session] = await Promise.all([getDishCategories(), getDishes(), getCurrentSession()])
    categories.value = categoryData
    dishes.value = dishData
    currentSession.value = session
    dishData.forEach((dish) => { dishCatalog.value[dish.id] = dish })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜单查询失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

async function handleCreateOrder() {
  // 三个前置条件分别保证有会话、有商品、没有重复提交。
  if (currentSession.value === null || cartItems.value.length === 0 || isSubmitting.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  orderResult.value = null
  try {
    // cartItems 被映射成后端 OrderItemDTO，只发送 ID 和数量，不信任前端金额。
    // map 只构造后端允许的字段；价格由服务端根据 dishId 重新读取。
    orderResult.value = await createOrder(currentSession.value.sessionId, {
      items: cartItems.value.map((dish) => ({ dishId: dish.id, quantity: cart.value[dish.id] ?? 0 })),
    })
    cart.value = {}
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '创建订单失败，请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}

async function showDishDetail(dish: DishData) {
  // 详情请求不替换菜单列表，只更新 selectedDish 展示补充信息。
  try {
    // 详情是独立的 ref，不替换当前菜单数组，关闭/切换分类不会丢购物车。
    selectedDish.value = await getDishDetail(dish.id)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品详情查询失败'
  }
}

onMounted(loadMenu)
</script>

<template>
  <section class="menu-view">
    <div class="reservation-heading">
      <p class="eyebrow">顾客点餐</p>
      <h1>选择菜品</h1>
      <p>浏览当前可售菜品并准备购物车，订单将在下一步提交。</p>
    </div>

    <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>
    <p v-if="!isLoading && currentSession === null" class="feedback" role="status">当前没有用餐会话，请先完成开台；现在可以先浏览菜单。</p>
    <p v-if="isLoading" class="feedback" role="status">正在加载菜单...</p>
    <div v-if="orderResult" class="order-result">
      <p class="feedback feedback-success" role="status">订单创建成功，编号：{{ orderResult.orderNo }}</p>
      <dl class="status-list">
        <div><dt>桌位</dt><dd>{{ orderResult.tableNo }}</dd></div>
        <div><dt>订单金额</dt><dd>{{ formatPrice(orderResult.totalAmount) }}</dd></div>
      </dl>
    </div>

    <div v-else class="menu-layout">
      <div class="menu-content">
        <div class="category-list" role="tablist" aria-label="菜品分类">
          <button type="button" class="category-button" :class="{ 'category-button-selected': selectedCategoryId === null }" @click="selectCategory(null)">全部</button>
          <button v-for="category in categories" :key="category.id" type="button" class="category-button" :class="{ 'category-button-selected': selectedCategoryId === category.id }" @click="selectCategory(category.id)">
            {{ category.name }}
          </button>
        </div>
        <p v-if="isFiltering" class="feedback" role="status">正在切换分类...</p>
        <p v-else-if="dishes.length === 0" class="feedback" role="status">当前分类没有可售菜品。</p>
        <div v-else class="dish-list" role="list" aria-label="可售菜品">
          <article v-for="dish in dishes" :key="dish.id" class="dish-card">
            <div class="dish-card-content">
              <strong>{{ dish.name }}</strong>
              <p>{{ dish.description || '暂无菜品描述' }}</p>
              <span class="dish-price">{{ formatPrice(dish.price) }}</span>
              <button class="secondary-button" type="button" @click="showDishDetail(dish)">查看详情</button>
            </div>
            <div class="quantity-control">
              <button type="button" :disabled="!cart[dish.id]" :aria-label="`减少 ${dish.name}`" @click="decreaseFromCart(dish)">−</button>
              <span>{{ cart[dish.id] ?? 0 }}</span>
              <button type="button" :aria-label="`增加 ${dish.name}`" @click="addToCart(dish)">+</button>
            </div>
          </article>
        </div>
      </div>

      <aside class="cart-panel" aria-label="购物车">
        <p v-if="selectedDish" class="feedback feedback-success" role="status">{{ selectedDish.name }}：{{ selectedDish.description || '暂无描述' }}</p>
        <h2>购物车</h2>
        <p v-if="cartItems.length === 0" class="feedback" role="status">还没有选择菜品。</p>
        <ul v-else class="cart-list">
          <li v-for="dish in cartItems" :key="dish.id">
            <span>{{ dish.name }} × {{ cart[dish.id] }}</span>
            <strong>{{ formatPrice(dish.price * (cart[dish.id] ?? 0)) }}</strong>
          </li>
        </ul>
        <div class="cart-summary"><span>{{ cartCount }} 件</span><strong>{{ formatPrice(cartTotal) }}</strong></div>
        <button class="open-session-button" type="button" :disabled="currentSession === null || cartItems.length === 0 || isSubmitting" @click="handleCreateOrder">
          {{ isSubmitting ? '提交中...' : '确认下单' }}
        </button>
        <p class="cart-note">订单提交后以服务端返回金额和编号为准。</p>
      </aside>
    </div>
  </section>
</template>
