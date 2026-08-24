<script setup lang="ts">
// 顾客预约页：展示预约状态，并把待到店预约连接到预约开台动作。
import { onMounted, ref } from 'vue'
import { cancelReservation, getReservationDetail, getReservations } from '../services/reservation'
import { checkInReservation } from '../services/session'
import type { DiningSessionData, ReservationData } from '../types/api'
import { canCancelReservation, getReservationStatusLabel } from '../utils/status'

const reservations = ref<ReservationData[]>([])
const selectedReservation = ref<ReservationData | null>(null)
const isLoading = ref(true)
const detailLoadingId = ref<number | null>(null)
const cancelLoadingId = ref<number | null>(null)
const checkInLoadingId = ref<number | null>(null)
const sessionResult = ref<DiningSessionData | null>(null)
const errorMessage = ref('')

async function loadReservations() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    reservations.value = await getReservations()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '预约列表查询失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

async function showDetail(reservation: ReservationData) {
  if (selectedReservation.value?.reservationId === reservation.reservationId) {
    selectedReservation.value = null
    return
  }

  selectedReservation.value = null
  detailLoadingId.value = reservation.reservationId
  errorMessage.value = ''
  try {
    selectedReservation.value = await getReservationDetail(reservation.reservationId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '预约详情查询失败，请稍后重试'
  } finally {
    detailLoadingId.value = null
  }
}

async function handleCancel(reservation: ReservationData) {
  if (!canCancelReservation(reservation.status) || !window.confirm(`确认取消预约 ${reservation.reservationNo} 吗？`)) return

  cancelLoadingId.value = reservation.reservationId
  errorMessage.value = ''
  try {
    await cancelReservation(reservation.reservationId)
    selectedReservation.value = null
    await loadReservations()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '取消预约失败，请稍后重试'
  } finally {
    cancelLoadingId.value = null
  }
}

async function handleCheckIn(reservation: ReservationData) {
  if (reservation.status !== 0 || checkInLoadingId.value !== null) return
  checkInLoadingId.value = reservation.reservationId
  errorMessage.value = ''
  try {
    sessionResult.value = await checkInReservation(reservation.reservationId, reservation.tableId)
    selectedReservation.value = null
    await loadReservations()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '预约到店开台失败，请稍后重试'
  } finally {
    checkInLoadingId.value = null
  }
}

onMounted(loadReservations)
</script>

<template>
  <section class="reservation-view">
    <div class="reservation-heading">
      <p class="eyebrow">顾客预约</p>
      <h1>我的预约</h1>
      <p>查看预约状态，或取消尚未到店的预约。</p>
    </div>

    <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p>
    <p v-if="isLoading" class="feedback" role="status">正在查询预约...</p>
    <p v-else-if="reservations.length === 0" class="feedback" role="status">当前没有预约记录。</p>

    <div v-if="sessionResult" class="session-result">
      <p class="feedback feedback-success" role="status">预约到店开台成功，会话编号：{{ sessionResult.sessionNo }}</p>
      <dl class="status-list">
        <div><dt>桌位</dt><dd>{{ sessionResult.tableNo }}</dd></div>
        <div><dt>会话状态</dt><dd>{{ sessionResult.sessionStatus === 0 ? '等待中' : '用餐中' }}</dd></div>
      </dl>
    </div>

    <div v-else class="reservation-list" role="list" aria-label="我的预约列表">
      <article v-for="reservation in reservations" :key="reservation.reservationId" class="reservation-card">
        <div class="reservation-card-heading">
          <div>
            <strong>{{ reservation.reservationNo }}</strong>
            <span>{{ reservation.tableNo || `桌位 ${reservation.tableId}` }} · {{ reservation.peopleCount }} 人</span>
          </div>
          <span class="reservation-status">{{ getReservationStatusLabel(reservation.status) }}</span>
        </div>
        <p class="reservation-time">预约时间：{{ reservation.reserveTime }}</p>
        <div class="reservation-actions">
          <button type="button" class="secondary-button" :disabled="detailLoadingId === reservation.reservationId" @click="showDetail(reservation)">
            {{ detailLoadingId === reservation.reservationId ? '加载中...' : selectedReservation?.reservationId === reservation.reservationId ? '收起详情' : '查看详情' }}
          </button>
          <button v-if="canCancelReservation(reservation.status)" type="button" class="danger-button" :disabled="cancelLoadingId === reservation.reservationId" @click="handleCancel(reservation)">
            {{ cancelLoadingId === reservation.reservationId ? '取消中...' : '取消预约' }}
          </button>
          <button v-if="reservation.status === 0" type="button" class="primary-outline-button" :disabled="checkInLoadingId === reservation.reservationId" @click="handleCheckIn(reservation)">
            {{ checkInLoadingId === reservation.reservationId ? '开台中...' : '到店开台' }}
          </button>
        </div>
        <dl v-if="selectedReservation?.reservationId === reservation.reservationId" class="reservation-detail">
          <div><dt>预约编号</dt><dd>{{ selectedReservation.reservationNo }}</dd></div>
          <div><dt>桌位</dt><dd>{{ selectedReservation.tableNo }}</dd></div>
          <div><dt>人数</dt><dd>{{ selectedReservation.peopleCount }} 人</dd></div>
          <div><dt>状态</dt><dd>{{ getReservationStatusLabel(selectedReservation.status) }}</dd></div>
        </dl>
      </article>
    </div>
  </section>
</template>
