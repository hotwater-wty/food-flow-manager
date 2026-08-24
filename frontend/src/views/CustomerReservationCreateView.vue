<script setup lang="ts">
// 顾客预约创建页：表单校验只负责体验，最终桌位和预约合法性由后端确认。
import { computed, onMounted, ref } from 'vue'
import { getAvailableTables } from '../services/table'
import { createReservation } from '../services/reservation'
import type { ReservationCreateData, TableVO } from '../types/api'

const tables = ref<TableVO[]>([])
const selectedTableId = ref<number | null>(null)
const peopleCount = ref(1)
const reserveTime = ref('')
const isLoading = ref(true)
const errorMessage = ref('')
const isSubmitting = ref(false)
const reservationResult = ref<ReservationCreateData | null>(null)

const selectedTable = computed(() => tables.value.find((table) => table.tableId === selectedTableId.value) ?? null)
const timeError = computed(() => {
  if (!reserveTime.value) return '请选择预约时间'
  return new Date(reserveTime.value).getTime() > Date.now() ? '' : '预约时间必须晚于当前时间'
})
const peopleError = computed(() => {
  if (peopleCount.value < 1) return '预约人数至少为 1 人'
  if (selectedTable.value && peopleCount.value > selectedTable.value.capacity) {
    return `该桌最多容纳 ${selectedTable.value.capacity} 人`
  }
  return ''
})
const formError = computed(() => peopleError.value || timeError.value || (!selectedTable.value ? '请选择桌位' : ''))
const canPrepare = computed(() => !isLoading.value && tables.value.length > 0 && formError.value === '')

function formatReserveTime(value: string) {
  const normalized = value.replace('T', ' ')
  return normalized.length === 16 ? `${normalized}:00` : normalized
}

async function handleSubmit() {
  if (!canPrepare.value || selectedTable.value === null) return

  errorMessage.value = ''
  reservationResult.value = null
  isSubmitting.value = true
  try {
    reservationResult.value = await createReservation({
      tableId: selectedTable.value.tableId,
      peopleCount: peopleCount.value,
      reserveTime: formatReserveTime(reserveTime.value),
    })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '创建预约失败，请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}

async function loadTables() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    tables.value = await getAvailableTables()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '桌位查询失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadTables)
</script>

<template>
  <section class="reservation-view">
    <div class="reservation-heading">
      <p class="eyebrow">顾客预约</p>
      <h1>选择桌位与预约时间</h1>
      <p>选择空闲桌位并提交真实预约，也可以查看已有预约。</p>
    </div>

    <div v-if="reservationResult" class="reservation-result">
      <p class="feedback feedback-success" role="status">预约创建成功，编号：{{ reservationResult.reservationNo }}</p>
      <dl class="status-list">
        <div><dt>桌位</dt><dd>{{ reservationResult.tableNo || `桌位 ${reservationResult.tableId}` }}</dd></div>
        <div><dt>人数</dt><dd>{{ reservationResult.peopleCount }} 人</dd></div>
        <div><dt>预约时间</dt><dd>{{ reservationResult.reserveTime }}</dd></div>
      </dl>
    </div>

    <p v-if="isLoading" class="feedback" role="status">正在查询空闲桌位...</p>
    <p v-else-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>
    <p v-else-if="tables.length === 0" class="feedback" role="status">当前没有可预约的空闲桌位。</p>

    <div v-else class="reservation-layout">
      <div>
        <h2>空闲桌位</h2>
        <div class="table-list" role="list" aria-label="空闲桌位列表">
          <button
            v-for="table in tables"
            :key="table.tableId"
            class="table-option"
            :class="{ 'table-option-selected': table.tableId === selectedTableId }"
            type="button"
            @click="selectedTableId = table.tableId"
          >
            <strong>{{ table.tableNo }}</strong>
            <span>{{ table.capacity }} 人桌</span>
            <small>{{ table.locationDesc || '暂无位置描述' }}</small>
          </button>
        </div>
      </div>

      <form class="reservation-form" @submit.prevent="handleSubmit">
        <label>
          预约人数
          <input v-model.number="peopleCount" type="number" min="1" :max="selectedTable?.capacity" required />
        </label>
        <label>
          预约时间
          <input v-model="reserveTime" type="datetime-local" required />
        </label>
        <p v-if="formError" class="feedback feedback-error" role="alert">{{ formError }}</p>
        <p v-else class="feedback feedback-success" role="status">
          信息有效，可以进入下一步提交。预约时间：{{ formatReserveTime(reserveTime) }}
        </p>
        <button type="submit" :disabled="!canPrepare || isSubmitting">
          {{ isSubmitting ? '提交中...' : '创建预约' }}
        </button>
      </form>
    </div>
  </section>
</template>
