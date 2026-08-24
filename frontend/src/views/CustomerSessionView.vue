<script setup lang="ts">
// 顾客会话页：用桌位选择模拟扫码输入，真实会话仍由服务端创建和恢复。
import { onMounted, ref } from 'vue'
import { getAvailableTables } from '../services/table'
import { getCurrentSession, openSession } from '../services/session'
import type { DiningSessionData, TableVO } from '../types/api'

// 页面状态分为服务端当前会话、可选桌位和请求反馈三组，互不混用。
const tables = ref<TableVO[]>([])
const currentSession = ref<DiningSessionData | null>(null)
const selectedTableId = ref<number | null>(null)
const isLoading = ref(true)
const isOpening = ref(false)
const errorMessage = ref('')

function sessionStatusLabel(status: number) {
  // 会话状态和桌位状态是两套状态机，这里只转换 sessionStatus。
  // as Record<number, string> 告诉 TypeScript 这是数字索引的字典；?? 提供未知状态兜底。
  return ({ 0: '等待中', 1: '用餐中', 2: '已完成', 3: '已取消' } as Record<number, string>)[status] ?? '未知状态'
}

async function loadPage() {
  // 当前会话和可用桌位可并行读取；页面以服务端结果恢复，而不是依赖本地缓存。
  isLoading.value = true
  errorMessage.value = ''
  try {
    // Promise.all 并行等待两个独立 GET；任一请求失败都会进入 catch。
    const [session, availableTables] = await Promise.all([getCurrentSession(), getAvailableTables()])
    currentSession.value = session
    tables.value = availableTables
    // 仅在没有选择时设置默认桌位，避免刷新数据覆盖用户当前选择。
    if (selectedTableId.value === null && availableTables.length > 0) selectedTableId.value = availableTables[0].tableId
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '开台信息查询失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

async function handleOpenSession() {
  // selectedTableId 是模拟二维码解析结果；isOpening 防止重复开台。
  if (selectedTableId.value === null || isOpening.value) return
  isOpening.value = true
  errorMessage.value = ''
  try {
    currentSession.value = await openSession(selectedTableId.value)
    tables.value = await getAvailableTables()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '开台失败，请稍后重试'
  } finally {
    isOpening.value = false
  }
}

onMounted(loadPage)
</script>

<template>
  <section class="reservation-view">
    <div class="reservation-heading">
      <p class="eyebrow">顾客用餐</p>
      <h1>模拟扫码开台</h1>
      <p>选择一张空闲桌位，模拟扫描桌位二维码并开始用餐。</p>
    </div>

    <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>
    <p v-if="isLoading" class="feedback" role="status">正在查询当前会话和空闲桌位...</p>

    <div v-if="currentSession" class="session-result">
      <p class="feedback feedback-success" role="status">当前会话已恢复</p>
      <dl class="status-list">
        <div><dt>会话编号</dt><dd>{{ currentSession.sessionNo }}</dd></div>
        <div><dt>桌位</dt><dd>{{ currentSession.tableNo }}</dd></div>
        <div><dt>状态</dt><dd>{{ sessionStatusLabel(currentSession.sessionStatus) }}</dd></div>
      </dl>
    </div>

    <div v-else-if="!isLoading" class="session-open-panel">
      <h2>选择空闲桌位</h2>
      <p v-if="tables.length === 0" class="feedback" role="status">当前没有可开台的空闲桌位。</p>
      <div v-else class="table-list" role="list" aria-label="可开台桌位列表">
        <button v-for="table in tables" :key="table.tableId" type="button" class="table-option" :class="{ 'table-option-selected': table.tableId === selectedTableId }" @click="selectedTableId = table.tableId">
          <strong>{{ table.tableNo }}</strong>
          <span>{{ table.capacity }} 人桌</span>
          <small>{{ table.locationDesc || '暂无位置描述' }}</small>
        </button>
      </div>
      <button class="open-session-button" type="button" :disabled="selectedTableId === null || isOpening || tables.length === 0" @click="handleOpenSession">
        {{ isOpening ? '开台中...' : '确认模拟扫码开台' }}
      </button>
    </div>
  </section>
</template>
