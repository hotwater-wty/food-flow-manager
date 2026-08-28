<script setup lang="ts">
// 顾客会话页:用桌位选择模拟扫码输入,真实会话仍由服务端创建和恢复。
// R4 改用 Vant:会话恢复态用结果卡片展示,开台用桌位卡片+主按钮。
import { onMounted, ref } from 'vue'
import { showFailToast, showSuccessToast } from 'vant'
import { getAvailableTables } from '../services/table'
import { getCurrentSession, openSession } from '../services/session'
import type { DiningSessionData, TableVO } from '@foodflow/shared/types/api'
import { getSessionStatusLabel } from '@foodflow/shared/utils/status'

// 页面状态分为服务端当前会话、可选桌位和请求反馈三组,互不混用。
const tables = ref<TableVO[]>([])
const currentSession = ref<DiningSessionData | null>(null)
const selectedTableId = ref<number | null>(null)
const isLoading = ref(true)
const isOpening = ref(false)
const errorMessage = ref('')

async function loadPage() {
  // 当前会话和可用桌位可并行读取;页面以服务端结果恢复,而不是依赖本地缓存。
  isLoading.value = true
  errorMessage.value = ''
  try {
    // Promise.all 并行等待两个独立 GET;任一请求失败都会进入 catch。
    const [session, availableTables] = await Promise.all([getCurrentSession(), getAvailableTables()])
    currentSession.value = session
    tables.value = availableTables
    // 仅在没有选择时设置默认桌位,避免刷新数据覆盖用户当前选择。
    if (selectedTableId.value === null && availableTables.length > 0) selectedTableId.value = availableTables[0].tableId
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '开台信息查询失败,请稍后重试'
  } finally {
    isLoading.value = false
  }
}

async function handleOpenSession() {
  // selectedTableId 是模拟二维码解析结果;isOpening 防止重复开台。
  if (selectedTableId.value === null || isOpening.value) return
  isOpening.value = true
  errorMessage.value = ''
  try {
    currentSession.value = await openSession(selectedTableId.value)
    tables.value = await getAvailableTables()
    showSuccessToast('开台成功,开始点餐吧')
  } catch (error) {
    const message = error instanceof Error ? error.message : '开台失败,请稍后重试'
    errorMessage.value = message
    showFailToast(message)
  } finally {
    isOpening.value = false
  }
}

onMounted(loadPage)
</script>

<template>
  <section class="session-page">
    <van-loading v-if="isLoading" class="session-loading" size="24px" vertical>正在查询当前会话和空闲桌位...</van-loading>

    <div v-else-if="currentSession" class="session-restored">
      <van-icon name="checked" class="session-restored-icon" />
      <p class="session-restored-title">当前会话已恢复</p>
      <van-cell-group inset>
        <van-cell title="会话编号" :value="currentSession.sessionNo" />
        <van-cell title="桌位" :value="currentSession.tableNo" />
        <van-cell title="状态" :value="getSessionStatusLabel(currentSession.sessionStatus)" />
      </van-cell-group>
      <div class="session-restored-actions">
        <van-button block round type="primary" to="/customer/menu">去点餐</van-button>
        <van-button v-if="currentSession.sessionStatus !== 1" block round type="primary" plain @click="currentSession = null; loadPage()">
          换桌重开
        </van-button>
      </div>
    </div>

    <template v-else>
      <p v-if="errorMessage" class="session-error" role="alert">{{ errorMessage }}</p>
      <p class="session-section-title">选择空闲桌位(模拟扫描桌位二维码)</p>
      <van-empty v-if="tables.length === 0" description="当前没有可开台的空闲桌位" />
      <div v-else class="session-table-grid" role="list" aria-label="可开台桌位列表">
        <button
          v-for="table in tables"
          :key="table.tableId"
          type="button"
          class="session-table-card"
          :class="{ 'session-table-card--active': table.tableId === selectedTableId }"
          @click="selectedTableId = table.tableId"
        >
          <strong>{{ table.tableNo }}</strong>
          <span>{{ table.capacity }} 人桌</span>
          <small>{{ table.locationDesc || '暂无位置描述' }}</small>
        </button>
      </div>
      <div class="session-submit">
        <van-button
          block
          round
          type="primary"
          :disabled="selectedTableId === null || tables.length === 0"
          :loading="isOpening"
          loading-text="开台中..."
          @click="handleOpenSession"
        >
          确认模拟扫码开台
        </van-button>
      </div>
    </template>
  </section>
</template>

<style scoped>
.session-page {
  display: grid;
  gap: var(--space-4);
}
.session-loading {
  display: flex;
  justify-content: center;
  margin: var(--space-8) 0;
}
.session-error {
  background: var(--color-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  margin: 0;
  padding: var(--space-2) var(--space-3);
}
.session-section-title {
  color: var(--color-text);
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 var(--space-1);
}
.session-table-grid {
  display: grid;
  gap: var(--space-2);
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
}
.session-table-card {
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: grid;
  gap: 2px;
  padding: var(--space-3);
  text-align: left;
}
.session-table-card span,
.session-table-card small {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}
.session-table-card--active {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 2px var(--color-brand-ring);
}
.session-submit {
  padding: 0 var(--space-4);
}
.session-restored {
  display: grid;
  gap: var(--space-4);
  justify-items: center;
  padding-top: var(--space-6);
}
.session-restored-icon {
  color: var(--color-success);
  font-size: 48px;
}
.session-restored-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}
.session-restored-actions {
  display: grid;
  gap: var(--space-2);
  width: 100%;
}
</style>
