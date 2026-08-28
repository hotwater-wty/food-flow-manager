<script setup lang="ts">
// 商户端仪表盘(三期 R5):今日经营概览,数据来自 GET /api/admin/statistics/overview。
// 呈现默认零依赖:统计卡片 + 状态分布条 + 热销榜单,不引入图表库。
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getStatisticsOverview } from '../../services/statistics'
import type { StatisticsOverview } from '@foodflow/shared/types/api'
import { getOrderStatusLabel, getOrderTagKind } from '@foodflow/shared/utils/order-status'
import { formatPrice } from '@foodflow/shared/utils/format'
import { useAutoRefresh } from '../../composables/use-autoRefresh'

const overview = ref<StatisticsOverview | null>(null)
const loading = ref(true)
const errorMessage = ref('')

// 静默刷新与工作台一致:不点亮遮罩,失败保留旧数据,轮询/聚焦均复用本函数。
async function load(options?: { silent?: boolean }) {
  if (!options?.silent) {
    loading.value = true
    errorMessage.value = ''
  }
  try {
    overview.value = await getStatisticsOverview()
  } catch (error) {
    if (options?.silent) {
      console.warn('[仪表盘] 静默刷新失败', error)
    } else {
      errorMessage.value = error instanceof Error ? error.message : '经营统计查询失败'
    }
  } finally {
    if (!options?.silent) {
      loading.value = false
    }
  }
}

// 仪表盘数据频次低、变化感知价值高,默认开启 20 秒轮询(与工作台同款开关)。
const { autoRefresh } = useAutoRefresh(load)

// 状态分布条:过滤掉没有数据的状态,比例按剩余条目计算,避免零宽色块。
const distribution = computed(() => {
  const items = (overview.value?.statusDistribution ?? []).filter((item) => item.count > 0)
  const total = items.reduce((sum, item) => sum + item.count, 0)
  return items.map((item) => ({
    ...item,
    percent: total === 0 ? 0 : Math.round((item.count / total) * 100),
  }))
})

// 状态色与工作台标签一致,复用同一映射源。
function statusColor(status: number) {
  const kind = getOrderTagKind(status)
  if (kind === 'success') return 'var(--el-color-success)'
  if (kind === 'warning') return 'var(--el-color-warning)'
  if (kind === 'info') return 'var(--el-color-info)'
  return 'var(--el-color-primary)'
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>经营概览</h1>
      <p>今日经营数据一览;统计口径为当日有效订单(不含已取消),营收为整数分展示转元。</p>
    </div>

    <div class="admin-toolbar">
      <el-button :icon="Refresh" :loading="loading" @click="load()">刷新</el-button>
      <label class="auto-refresh-toggle">
        <el-switch v-model="autoRefresh" size="small" />
        每 20 秒自动刷新
      </label>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <div v-loading="loading" class="dashboard-body">
      <template v-if="overview">
        <div class="dashboard-cards">
          <div class="dashboard-card">
            <p class="dashboard-card-label">今日订单数</p>
            <p class="dashboard-card-value">{{ overview.todayOrderCount }}</p>
          </div>
          <div class="dashboard-card">
            <p class="dashboard-card-label">今日营收</p>
            <p class="dashboard-card-value">{{ formatPrice(overview.todayRevenue) }}</p>
          </div>
        </div>

        <div class="dashboard-section">
          <h2>订单状态分布</h2>
          <p v-if="distribution.length === 0" class="dashboard-empty-hint">今日暂无有效订单</p>
          <div v-else class="dashboard-dist-bar" role="img" aria-label="今日订单状态分布">
            <div
              v-for="item in distribution"
              :key="item.status"
              class="dashboard-dist-seg"
              :style="{ width: item.percent + '%', background: statusColor(item.status) }"
              :title="`${getOrderStatusLabel(item.status)} ${item.count} 单(${item.percent}%)`"
            />
          </div>
          <ul v-if="distribution.length > 0" class="dashboard-dist-legend">
            <li v-for="item in distribution" :key="item.status">
              <span class="dashboard-legend-dot" :style="{ background: statusColor(item.status) }" />
              {{ getOrderStatusLabel(item.status) }} · {{ item.count }} 单
            </li>
          </ul>
        </div>

        <div class="dashboard-section">
          <h2>今日热销 TOP {{ overview.topDishes.length > 0 ? overview.topDishes.length : 5 }}</h2>
          <p v-if="overview.topDishes.length === 0" class="dashboard-empty-hint">今日暂无菜品销量</p>
          <ol v-else class="dashboard-top-list">
            <li v-for="(dish, index) in overview.topDishes" :key="dish.dishId">
              <span class="dashboard-top-rank">{{ index + 1 }}</span>
              <span class="dashboard-top-name">{{ dish.dishName }}</span>
              <span class="dashboard-top-meta">售出 {{ dish.quantity }} 份 · {{ formatPrice(dish.amount) }}</span>
            </li>
          </ol>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.dashboard-body {
  display: grid;
  gap: var(--space-6);
  min-height: 160px;
}
.dashboard-cards {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}
.dashboard-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}
.dashboard-card-label {
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  margin: 0 0 var(--space-2);
}
.dashboard-card-value {
  color: var(--color-text);
  font-size: 1.6rem;
  font-weight: 700;
  margin: 0;
}
.dashboard-section h2 {
  font-size: 1rem;
  margin: 0 0 var(--space-3);
}
.dashboard-empty-hint {
  color: var(--color-text-secondary);
  margin: 0;
}
.dashboard-dist-bar {
  background: var(--color-bg-admin);
  border-radius: var(--radius-sm);
  display: flex;
  height: 18px;
  overflow: hidden;
}
.dashboard-dist-seg {
  min-width: 6px;
}
.dashboard-dist-legend {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  list-style: none;
  margin: var(--space-3) 0 0;
  padding: 0;
}
.dashboard-dist-legend li {
  align-items: center;
  color: var(--color-text-secondary);
  display: inline-flex;
  font-size: 0.85rem;
  gap: 6px;
}
.dashboard-legend-dot {
  border-radius: 50%;
  display: inline-block;
  height: 10px;
  width: 10px;
}
.dashboard-top-list {
  display: grid;
  gap: var(--space-2);
  list-style: none;
  margin: 0;
  padding: 0;
}
.dashboard-top-list li {
  align-items: center;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}
.dashboard-top-rank {
  color: var(--color-brand);
  font-weight: 700;
  width: 20px;
}
.dashboard-top-name {
  flex: 1;
  font-weight: 600;
}
.dashboard-top-meta {
  color: var(--color-text-secondary);
  font-size: 0.88rem;
}
</style>
