<script setup lang="ts">
// 顾客预约页:展示预约状态,并把待到店预约连接到预约开台动作。
// R4 改用 Vant:列表用卡片+状态 Tag,详情用弹层,取消改用 Dialog 确认(替代原生 confirm)。
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showConfirmDialog } from 'vant'
import { cancelReservation, getReservationDetail, getReservations } from '../services/reservation'
import { checkInReservation } from '../services/session'
import type { DiningSessionData, ReservationData } from '@foodflow/shared/types/api'
import { canCancelReservation, getReservationStatusLabel, getReservationTagKind } from '@foodflow/shared/utils/status'
import { formatDateTime } from '@foodflow/shared/utils/format'
import { useAutoRefresh } from '../composables/use-autoRefresh'

// 列表使用数组泛型,明确每个元素都满足后端 ReservationData 结构。
const reservations = ref<ReservationData[]>([])
const selectedReservation = ref<ReservationData | null>(null)
const isLoading = ref(true)
const detailLoadingId = ref<number | null>(null)
const cancelLoadingId = ref<number | null>(null)
const checkInLoadingId = ref<number | null>(null)
const sessionResult = ref<DiningSessionData | null>(null)
const errorMessage = ref('')

// Vant 的 Tag 没有 info 色:把共享映射里的 info 适配为 Vant 的 default,
// 颜色规则本身集中在 utils/status.ts,与管理端预约页共用。
function reservationTagType(status: number): 'primary' | 'warning' | 'default' | 'danger' {
  const kind = getReservationTagKind(status)
  return kind === 'info' ? 'default' : kind
}

// silent 表示聚焦刷新触发的静默加载;列表刷新仍是取消和到店开台成功后的共同收敛点。
async function loadReservations(options?: { silent?: boolean }) {
  if (!options?.silent) {
    isLoading.value = true
    errorMessage.value = ''
  }
  try {
    reservations.value = await getReservations()
  } catch (error) {
    if (options?.silent) {
      console.warn('[顾客预约] 静默刷新失败', error)
    } else {
      errorMessage.value = error instanceof Error ? error.message : '预约列表查询失败,请稍后重试'
    }
  } finally {
    if (!options?.silent) {
      isLoading.value = false
    }
  }
}

// 到店开台成功弹窗的"去点餐"确认:关结果面板并跳点餐页(拆分后本应用内路径为 /menu)。
const router = useRouter()
function confirmSessionResult() {
  sessionResult.value = null
  void router.push('/menu')
}

// 详情改为弹层开关;重复点击同一预约只收起。
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
    errorMessage.value = error instanceof Error ? error.message : '预约详情查询失败,请稍后重试'
  } finally {
    detailLoadingId.value = null
  }
}

async function handleCancel(reservation: ReservationData) {
  // showConfirmDialog 是 Promise 风格:确认 resolve、取消 reject,链式表达"确认后才发请求"。
  if (!canCancelReservation(reservation.status)) return
  try {
    await showConfirmDialog({
      title: '取消预约',
      message: `确认取消预约 ${reservation.reservationNo} 吗?`,
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
    })
  } catch {
    return
  }
  cancelLoadingId.value = reservation.reservationId
  errorMessage.value = ''
  try {
    await cancelReservation(reservation.reservationId)
    selectedReservation.value = null
    showSuccessToast('预约已取消')
    await loadReservations()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取消预约失败,请稍后重试'
    errorMessage.value = message
    showFailToast(message)
  } finally {
    cancelLoadingId.value = null
  }
}

async function handleCheckIn(reservation: ReservationData) {
  // tableId 必须来自预约记录,不能让用户选择另一张桌位破坏后端匹配约束。
  if (reservation.status !== 0 || checkInLoadingId.value !== null) return
  checkInLoadingId.value = reservation.reservationId
  errorMessage.value = ''
  try {
    sessionResult.value = await checkInReservation(reservation.reservationId, reservation.tableId)
    selectedReservation.value = null
    showSuccessToast('到店开台成功')
    await loadReservations()
  } catch (error) {
    const message = error instanceof Error ? error.message : '预约到店开台失败,请稍后重试'
    errorMessage.value = message
    showFailToast(message)
  } finally {
    checkInLoadingId.value = null
  }
}

// 聚焦刷新(三期 R3):顾客端不轮询,窗口重新可见/聚焦时静默拉最新预约。
useAutoRefresh(loadReservations, { polling: false })

onMounted(loadReservations)
</script>

<template>
  <section class="my-reservations">
    <p v-if="errorMessage" class="my-reservations-error" role="alert">{{ errorMessage }}</p>
    <van-loading v-if="isLoading" class="my-reservations-loading" size="24px" vertical>正在查询预约...</van-loading>
    <van-empty v-else-if="reservations.length === 0" description="当前没有预约记录">
      <van-button round type="primary" size="small" to="/reservations/create">去预约</van-button>
    </van-empty>

    <div v-else class="my-reservations-list" role="list" aria-label="我的预约列表">
      <article v-for="reservation in reservations" :key="reservation.reservationId" class="my-reservations-card">
        <div class="my-reservations-card-head">
          <div>
            <strong>{{ reservation.reservationNo }}</strong>
            <span>{{ reservation.tableNo || `桌位 ${reservation.tableId}` }} · {{ reservation.peopleCount }} 人</span>
          </div>
          <van-tag :type="reservationTagType(reservation.status)" round>
            {{ getReservationStatusLabel(reservation.status) }}
          </van-tag>
        </div>
        <p class="my-reservations-time">预约时间:{{ formatDateTime(reservation.reserveTime) }}</p>
        <div class="my-reservations-actions">
          <van-button
            size="small"
            plain
            :loading="detailLoadingId === reservation.reservationId"
            @click="showDetail(reservation)"
          >
            {{ selectedReservation?.reservationId === reservation.reservationId ? '收起详情' : '查看详情' }}
          </van-button>
          <van-button
            v-if="canCancelReservation(reservation.status)"
            size="small"
            plain
            type="danger"
            :loading="cancelLoadingId === reservation.reservationId"
            @click="handleCancel(reservation)"
          >
            取消预约
          </van-button>
          <van-button
            v-if="reservation.status === 0"
            size="small"
            type="primary"
            :loading="checkInLoadingId === reservation.reservationId"
            loading-text="开台中..."
            @click="handleCheckIn(reservation)"
          >
            到店开台
          </van-button>
        </div>
        <van-cell-group
          v-if="selectedReservation?.reservationId === reservation.reservationId"
          inset
          class="my-reservations-detail"
        >
          <van-cell title="预约编号" :value="selectedReservation.reservationNo" />
          <van-cell title="桌位" :value="selectedReservation.tableNo || `桌位 ${selectedReservation.tableId}`" />
          <van-cell title="人数" :value="`${selectedReservation.peopleCount} 人`" />
          <van-cell title="状态" :value="getReservationStatusLabel(selectedReservation.status)" />
        </van-cell-group>
      </article>
    </div>

    <!-- 到店开台成功的结果面板:置顶显示会话信息,关闭后回到列表。 -->
    <van-dialog
      :show="sessionResult !== null"
      title="到店开台成功"
      show-cancel-button
      confirm-button-text="去点餐"
      cancel-button-text="关闭"
      @confirm="confirmSessionResult"
      @update:show="
        (value: boolean) => {
          if (!value) sessionResult = null
        }
      "
    >
      <div v-if="sessionResult" class="my-reservations-session">
        <van-cell title="会话编号" :value="sessionResult.sessionNo" />
        <van-cell title="桌位" :value="sessionResult.tableNo" />
        <van-cell title="会话状态" :value="sessionResult.sessionStatus === 0 ? '等待中' : '用餐中'" />
      </div>
    </van-dialog>
  </section>
</template>

<style scoped>
.my-reservations {
  display: grid;
  gap: var(--space-3);
}
.my-reservations-error {
  background: var(--color-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  margin: 0;
  padding: var(--space-2) var(--space-3);
}
.my-reservations-loading {
  display: flex;
  justify-content: center;
  margin: var(--space-8) 0;
}
.my-reservations-list {
  display: grid;
  gap: var(--space-3);
}
.my-reservations-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(37, 37, 37, 0.04);
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4);
}
.my-reservations-card-head {
  align-items: flex-start;
  display: flex;
  gap: var(--space-2);
  justify-content: space-between;
}
.my-reservations-card-head strong {
  display: block;
  font-size: 0.95rem;
}
.my-reservations-card-head span {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}
.my-reservations-time {
  color: var(--color-text-secondary);
  font-size: 0.88rem;
  margin: 0;
}
.my-reservations-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.my-reservations-detail {
  margin: var(--space-1) 0 0;
}
.my-reservations-session {
  padding: var(--space-3) 0;
}
</style>
