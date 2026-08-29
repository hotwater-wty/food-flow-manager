<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant'
import { useRouter } from 'vue-router'
import { getCurrentSession, openSession } from '../services/session'
import { getAvailableTables } from '../services/table'
import type { DiningSessionData, TableVO } from '@foodflow/shared/types/api'
import { getSessionStatusLabel } from '@foodflow/shared/utils/status'

const router = useRouter()
const tables = ref<TableVO[]>([])
const currentSession = ref<DiningSessionData | null>(null)
const selectedTableId = ref<number | null>(null)
const isLoading = ref(true)
const isOpening = ref(false)
const errorMessage = ref('')

async function load() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [session, availableTables] = await Promise.all([getCurrentSession(), getAvailableTables()])
    currentSession.value = session
    tables.value = availableTables
    if (selectedTableId.value === null && availableTables.length > 0) selectedTableId.value = availableTables[0].tableId
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '开台信息查询失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

async function confirmOpenSession() {
  const table = tables.value.find((item) => item.tableId === selectedTableId.value)
  if (!table || isOpening.value) return
  try {
    await showConfirmDialog({
      title: '确认选择桌位',
      message: `确认选择 ${table.tableNo}（${table.capacity} 人桌）开台吗？`,
    })
  } catch {
    return
  }

  isOpening.value = true
  errorMessage.value = ''
  try {
    currentSession.value = await openSession(table.tableId)
    showSuccessToast('开台成功，开始点餐吧')
    await router.push('/menu')
  } catch (error) {
    const message = error instanceof Error ? error.message : '开台失败，请稍后重试'
    errorMessage.value = message
    showFailToast(message)
    await load()
  } finally {
    isOpening.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="table-picker">
    <van-loading v-if="isLoading" class="table-picker-loading" size="24px" vertical>正在查询可用桌位...</van-loading>

    <div v-else-if="currentSession" class="table-picker-session">
      <van-icon name="checked" class="table-picker-session-icon" />
      <p class="table-picker-session-title">当前已绑定桌位</p>
      <van-cell-group inset>
        <van-cell title="会话编号" :value="currentSession.sessionNo" />
        <van-cell title="桌位" :value="currentSession.tableNo" />
        <van-cell title="状态" :value="getSessionStatusLabel(currentSession.sessionStatus)" />
      </van-cell-group>
      <van-button block round type="primary" to="/menu">去点餐</van-button>
    </div>

    <template v-else>
      <p v-if="errorMessage" class="table-picker-error" role="alert">{{ errorMessage }}</p>
      <p class="table-picker-title">选择空闲桌位</p>
      <p class="table-picker-hint">确认后将为你创建本次用餐会话。</p>
      <van-empty v-if="tables.length === 0" description="当前没有可开台的空闲桌位" />
      <div v-else class="table-picker-grid" role="list" aria-label="可开台桌位列表">
        <button
          v-for="table in tables"
          :key="table.tableId"
          type="button"
          class="table-picker-card"
          :class="{ 'table-picker-card--active': table.tableId === selectedTableId }"
          :disabled="isOpening"
          @click="selectedTableId = table.tableId"
        >
          <strong>{{ table.tableNo }}</strong>
          <span>{{ table.capacity }} 人桌</span>
          <small>{{ table.locationDesc || '暂无位置描述' }}</small>
        </button>
      </div>
      <van-button
        block
        round
        type="primary"
        :disabled="selectedTableId === null || tables.length === 0"
        :loading="isOpening"
        loading-text="开台中..."
        @click="confirmOpenSession"
        >确认选择此桌位</van-button
      >
    </template>
  </section>
</template>

<style scoped>
.table-picker {
  display: grid;
  gap: var(--space-3);
}
.table-picker-loading {
  display: flex;
  justify-content: center;
  margin: var(--space-8) 0;
}
.table-picker-error {
  background: var(--color-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  margin: 0;
  padding: var(--space-2) var(--space-3);
}
.table-picker-title {
  color: var(--color-text);
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}
.table-picker-hint {
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  margin: calc(-1 * var(--space-2)) 0 0;
}
.table-picker-grid {
  display: grid;
  gap: var(--space-2);
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
}
.table-picker-card {
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: grid;
  gap: 2px;
  padding: var(--space-3);
  text-align: left;
}
.table-picker-card:disabled {
  cursor: wait;
  opacity: 0.65;
}
.table-picker-card span,
.table-picker-card small {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}
.table-picker-card--active {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 2px var(--color-brand-ring);
}
.table-picker-session {
  display: grid;
  gap: var(--space-4);
  justify-items: center;
  padding-top: var(--space-4);
}
.table-picker-session-icon {
  color: var(--color-success);
  font-size: 48px;
}
.table-picker-session-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}
</style>
