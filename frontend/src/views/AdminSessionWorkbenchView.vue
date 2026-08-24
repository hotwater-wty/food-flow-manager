<script setup lang="ts">
// 管理会话工作台：展示会话状态，并提供详情、取消等待和清台动作。
import { onMounted, ref } from 'vue'
import { cancelAdminSession, closeAdminSession, getAdminSessionDetail, getAdminSessions } from '../services/admin-session'
import type { DiningSessionData } from '../types/api'

const sessions = ref<DiningSessionData[]>([])
const pageNo = ref(1)
const total = ref(0)
const loading = ref(true)
const errorMessage = ref('')
const feedback = ref('')
const actionId = ref<number | null>(null)
const sessionLabel = (status: number) => ({ 0: '等待中', 1: '用餐中', 2: '已完成', 3: '已取消' } as Record<number, string>)[status] ?? '未知状态'

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getAdminSessions(pageNo.value)
    sessions.value = result.records
    total.value = result.total
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '会话查询失败'
  } finally { loading.value = false }
}

async function showDetail(sessionId: number) {
  try {
    const detail = await getAdminSessionDetail(sessionId)
    feedback.value = `会话 ${detail.sessionNo}，桌位 ${detail.tableNo}，状态 ${sessionLabel(detail.sessionStatus)}`
  } catch (error) { errorMessage.value = error instanceof Error ? error.message : '会话详情查询失败' }
}

async function action(session: DiningSessionData, type: 'cancel' | 'close') {
  if (actionId.value !== null) return
  actionId.value = session.sessionId
  errorMessage.value = ''
  try {
    if (type === 'cancel') await cancelAdminSession(session.sessionId)
    else await closeAdminSession(session.sessionId)
    await load()
  } catch (error) { errorMessage.value = error instanceof Error ? error.message : '会话操作失败' }
  finally { actionId.value = null }
}

onMounted(load)
</script>

<template>
  <section class="reservation-view">
    <div class="reservation-heading"><p class="eyebrow">管理端会话</p><h1>会话工作台</h1><p>查看堂食会话，取消等待中的会话或完成清台。</p></div>
    <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>
    <p v-if="feedback" class="feedback feedback-success" role="status">{{ feedback }}</p>
    <p v-if="loading" class="feedback" role="status">正在查询会话...</p>
    <p v-else-if="sessions.length === 0" class="feedback" role="status">当前没有堂食会话。</p>
    <div v-else class="reservation-list">
      <article v-for="session in sessions" :key="session.sessionId" class="reservation-card">
        <div class="reservation-card-heading"><div><strong>{{ session.sessionNo }}</strong><span>{{ session.tableNo }}</span></div><span class="reservation-status">{{ sessionLabel(session.sessionStatus) }}</span></div>
        <div class="reservation-actions"><button class="secondary-button" type="button" @click="showDetail(session.sessionId)">详情</button><button v-if="session.sessionStatus === 0" class="danger-button" type="button" :disabled="actionId === session.sessionId" @click="action(session, 'cancel')">取消等待</button><button v-if="session.sessionStatus === 1" class="primary-outline-button" type="button" :disabled="actionId === session.sessionId" @click="action(session, 'close')">清台</button></div>
      </article>
    </div>
    <p class="feedback">第 {{ pageNo }} 页，共 {{ total }} 条</p>
    <div class="pagination-actions"><button class="secondary-button" type="button" :disabled="pageNo <= 1 || loading" @click="pageNo--; load()">上一页</button><button class="secondary-button" type="button" :disabled="pageNo * 10 >= total || loading" @click="pageNo++; load()">下一页</button></div>
  </section>
</template>
