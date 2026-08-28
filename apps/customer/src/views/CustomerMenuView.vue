<script setup lang="ts">
// 顾客菜单页:后端菜品是事实,购物车是跨页面存活的应用状态(见 stores/cart.ts)。
// R4 改用 Vant:分类用 VanTabs,菜品卡片带 VanStepper 加减数量,
// 底部用 VanSubmitBar 购物车栏(盖在 Tabbar 之上),菜品详情用 VanActionSheet 半屏弹出。
// F5 起购物车入 Pinia:切到订单/预约页再回来,已加购内容不丢失。
import { computed, onMounted, ref } from 'vue'
import { showFailToast, showSuccessToast } from 'vant'
import { getDishCategories, getDishDetail, getDishes } from '../services/dish'
import { getCurrentSession } from '../services/session'
import { createOrder } from '../services/order'
import { useCartStore } from '../stores/cart'
import type { DishCategoryData, DishData, DiningSessionData, OrderCreateData } from '@foodflow/shared/types/api'
import { formatPrice } from '@foodflow/shared/utils/format'

const cartStore = useCartStore()

// categories/dishes 保存后端目录;数量映射与派生明细都在 cart store 中。
const categories = ref<DishCategoryData[]>([])
const dishes = ref<DishData[]>([])
// VanTabs 的 name 绑定数字 ID;0 表示"全部"分类(后端不传 categoryId)。
const ALL_CATEGORY = 0
const activeCategory = ref<number>(ALL_CATEGORY)
const isLoading = ref(true)
const isFiltering = ref(false)
const errorMessage = ref('')
const currentSession = ref<DiningSessionData | null>(null)
const isSubmitting = ref(false)
const orderResult = ref<OrderCreateData | null>(null)
// 菜品详情:ActionSheet 的展示状态与数据。
const detailVisible = ref(false)
const selectedDish = ref<DishData | null>(null)

// 派生量全部来自 cart store;computed 保持对 store 的响应式追踪。
const cartItems = computed(() => cartStore.items)
const cartTotal = computed(() => cartStore.totalPrice)
const cartSummary = computed(() => cartStore.summary)
const cartQuantities = computed(() => cartStore.quantities)

// VanStepper 的 change 事件把新数量写入 store;数量归零时 store 内部移除该 key。
function onQuantityChange(dish: DishData, quantity: number) {
  cartStore.setQuantity(dish.id, quantity)
}

// VanTabs 的 click-tab 事件在切换后触发;name 即分类 ID。
async function onCategoryChange() {
  // 分类切换是一次新的查询;加载期间用 isFiltering 提示,而不清空旧数据。
  isFiltering.value = true
  errorMessage.value = ''
  try {
    const result = await getDishes(activeCategory.value === ALL_CATEGORY ? undefined : activeCategory.value)
    dishes.value = result
    // 新列表合并进 store 目录,购物车中已有商品仍能找到价格和名称。
    cartStore.syncCatalog(result)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品查询失败,请稍后重试'
  } finally {
    isFiltering.value = false
  }
}

async function loadMenu() {
  // Promise.all 并行加载分类、菜品和当前会话,三者都成功后统一更新页面。
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [categoryData, dishData, session] = await Promise.all([getDishCategories(), getDishes(), getCurrentSession()])
    categories.value = categoryData
    dishes.value = dishData
    currentSession.value = session
    cartStore.syncCatalog(dishData)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜单查询失败,请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 提交订单成功后显示结果面板;关闭面板即回到菜单(购物车已清空)。
function dismissOrderResult() {
  orderResult.value = null
}

async function handleCreateOrder() {
  // 三个前置条件分别保证有会话、有商品、没有重复提交。
  if (currentSession.value === null || cartItems.value.length === 0 || isSubmitting.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  orderResult.value = null
  try {
    // 只发送 ID 和数量,不信任前端金额;价格由服务端根据 dishId 重新读取。
    orderResult.value = await createOrder(currentSession.value.sessionId, {
      items: cartItems.value.map((dish) => ({ dishId: dish.id, quantity: cartQuantities.value[dish.id] ?? 0 })),
    })
    cartStore.clear()
    showSuccessToast(`下单成功:${orderResult.value.orderNo}`)
  } catch (error) {
    const message = error instanceof Error ? error.message : '创建订单失败,请稍后重试'
    errorMessage.value = message
    showFailToast(message)
  } finally {
    isSubmitting.value = false
  }
}

// 打开菜品详情半屏面板;详情请求不替换菜单列表,也不影响购物车。
async function openDishDetail(dish: DishData) {
  detailVisible.value = true
  selectedDish.value = dish
  try {
    // 详情是独立的 ref,只补充描述;关闭/切换分类不会丢购物车。
    selectedDish.value = await getDishDetail(dish.id)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品详情查询失败'
  }
}

onMounted(loadMenu)
</script>

<template>
  <section class="menu-page">
    <!-- 没有会话时给出醒目提示与跳转入口;不阻塞浏览菜单。 -->
    <van-notice-bar v-if="!isLoading && currentSession === null" left-icon="info-o" mode="link" text="当前没有用餐会话,下单前请先开台" to="/session" />

    <van-loading v-if="isLoading" class="menu-loading" size="24px" vertical>正在加载菜单...</van-loading>
    <template v-else>
      <div v-if="orderResult" class="menu-order-result">
        <van-icon name="checked" class="menu-order-result-icon" />
        <p class="menu-order-result-title">订单创建成功</p>
        <van-cell-group inset>
          <van-cell title="订单编号" :value="orderResult.orderNo" />
          <van-cell title="桌位" :value="orderResult.tableNo" />
          <van-cell title="订单金额" :value="formatPrice(orderResult.totalAmount)" />
        </van-cell-group>
        <van-button block type="primary" plain class="menu-order-result-button" @click="dismissOrderResult">继续点餐</van-button>
      </div>

      <template v-else>
        <van-tabs v-model:active="activeCategory" sticky :offset-top="56" line-width="20px" @click-tab="onCategoryChange">
          <van-tab title="全部" :name="ALL_CATEGORY" />
          <van-tab v-for="category in categories" :key="category.id" :title="category.name" :name="category.id" />
        </van-tabs>

        <p v-if="errorMessage" class="menu-error" role="alert">{{ errorMessage }}</p>
        <van-loading v-if="isFiltering" class="menu-loading" size="20px">正在切换分类...</van-loading>
        <van-empty v-else-if="dishes.length === 0" description="当前分类没有可售菜品" />

        <div v-else class="menu-dish-list" role="list" aria-label="可售菜品">
          <article v-for="dish in dishes" :key="dish.id" class="menu-dish-card" @click="openDishDetail(dish)">
            <van-image
              :src="dish.image ?? undefined"
              fit="cover"
              width="88"
              height="88"
              radius="8"
              class="menu-dish-thumb"
            >
              <template #error>
                <div class="menu-dish-thumb menu-dish-thumb--fallback">
                  <van-icon name="goods-collect-o" />
                  {{ dish.name.slice(0, 1) }}
                </div>
              </template>
            </van-image>
            <div class="menu-dish-content" @click.stop="openDishDetail(dish)">
              <strong class="menu-dish-name">{{ dish.name }}</strong>
              <p class="menu-dish-desc">{{ dish.description || '暂无菜品描述' }}</p>
              <div class="menu-dish-bottom">
                <span class="menu-dish-price">{{ formatPrice(dish.price) }}</span>
                <!-- 点击卡片本身开详情;Stepper 区 stop 阻止冒泡,避免加减时误触详情。 -->
                <div class="menu-dish-stepper" @click.stop>
                  <van-stepper
                    :model-value="cartQuantities[dish.id] ?? 0"
                    min="0"
                    theme="round"
                    button-size="26"
                    @change="(quantity: number) => onQuantityChange(dish, quantity)"
                  />
                </div>
              </div>
            </div>
          </article>
        </div>

        <!-- 购物车栏:fixed 在 Tabbar 之上(bottom 默认贴合底部,设置 bottom=50 避开 Tabbar)。 -->
        <van-submit-bar
          v-if="currentSession !== null"
          :price="cartTotal"
          :button-text="isSubmitting ? '提交中...' : '确认下单'"
          button-type="primary"
          :disabled="cartItems.length === 0"
          :loading="isSubmitting"
          :safe-area-inset-bottom="true"
          bottom="50"
          class="menu-submit-bar"
          @submit="handleCreateOrder"
        >
          <span class="menu-cart-summary">{{ cartSummary }}</span>
        </van-submit-bar>

        <!-- 菜品详情半屏面板:点击遮罩即可关闭。 -->
        <van-action-sheet
          v-model:show="detailVisible"
          :title="selectedDish?.name ?? '菜品详情'"
          close-on-click-action
          :actions="[]"
        >
          <div v-if="selectedDish" class="menu-dish-detail">
            <van-image :src="selectedDish.image ?? undefined" fit="cover" width="100%" height="160" radius="8">
              <template #error><div class="menu-dish-thumb--fallback menu-dish-detail-fallback">暂无图片</div></template>
            </van-image>
            <p class="menu-dish-desc">{{ selectedDish.description || '暂无菜品描述' }}</p>
            <p class="menu-dish-price menu-dish-detail-price">{{ formatPrice(selectedDish.price) }}</p>
          </div>
        </van-action-sheet>
      </template>
    </template>
  </section>
</template>

<style scoped>
.menu-page {
  /* 底部为 SubmitBar(50)+Tabbar(50)留出滚动空间。 */
  padding-bottom: 120px;
}
.menu-loading {
  display: flex;
  justify-content: center;
  margin: var(--space-8) 0;
}
.menu-error {
  background: var(--color-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  margin: var(--space-3) 0;
  padding: var(--space-2) var(--space-3);
}
.menu-dish-list {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-3);
}
.menu-dish-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(37, 37, 37, 0.04);
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
}
.menu-dish-thumb {
  /* 顾客端不再使用 --color-bg-admin(拆分后令牌已删),改用暖白的加深近似。 */
  background: var(--color-border-soft);
  flex: 0 0 auto;
}
.menu-dish-thumb--fallback {
  align-items: center;
  color: var(--color-text-muted);
  display: flex;
  font-size: 0.8rem;
  gap: 4px;
  height: 100%;
  justify-content: center;
  width: 100%;
}
.menu-dish-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}
.menu-dish-name {
  font-size: 1rem;
}
.menu-dish-desc {
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  color: var(--color-text-secondary);
  display: -webkit-box;
  font-size: 0.85rem;
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
}
.menu-dish-bottom {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-top: auto;
}
.menu-dish-price {
  color: var(--color-brand);
  font-size: 1.05rem;
  font-weight: 700;
}
.menu-cart-summary {
  color: var(--color-text-secondary);
  font-size: 0.8rem;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.menu-submit-bar {
  /* SubmitBar 的 z-index 低于 Tabbar 时会被遮挡,提到其上;bottom=50 让它停在 Tabbar 之上。 */
  z-index: 20;
  /* F8 修复 6px 视觉缝隙:Tabbar(fixed,bottom:0)与 SubmitBar(bottom:50)之间
     露出页面背景形成一条缝;把 SubmitBar 背景向下延伸 6px 盖住缝隙,
     不用改 bottom 值,避免与 safe-area 逻辑互相干扰。 */
  padding-bottom: 6px;
}
.menu-dish-detail {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
}
.menu-dish-detail-fallback {
  align-items: center;
  display: flex;
  height: 160px;
  justify-content: center;
}
.menu-dish-detail-price {
  font-size: 1.2rem;
}
.menu-order-result {
  display: grid;
  gap: var(--space-4);
  justify-items: center;
  padding-top: var(--space-6);
}
.menu-order-result-icon {
  color: var(--color-success);
  font-size: 48px;
}
.menu-order-result-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}
.menu-order-result-button {
  margin-top: var(--space-2);
}
</style>
