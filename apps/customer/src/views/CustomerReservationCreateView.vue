<script setup lang="ts">
// 顾客预约创建页:表单校验只负责体验,最终桌位和预约合法性由后端确认。
// R4 改用 Vant:桌位选择保持卡片网格,人数用 Stepper,时间用原生 datetime-local
// (Vant 无对应组件,Field 只读展示+点击唤起原生控件是移动端常用折中),
// 校验与提交交给 VanForm/VanField 的 rules 体系。
import { computed, onMounted, ref } from 'vue'
import { showSuccessToast } from 'vant'
import { getAvailableTables } from '../services/table'
import { createReservation } from '../services/reservation'
import type { ReservationCreateData, TableVO } from '@foodflow/shared/types/api'
import { formatDateTime } from '@foodflow/shared/utils/format'

const tables = ref<TableVO[]>([])
const selectedTableId = ref<number | null>(null)
const peopleCount = ref(1)
const reserveTime = ref('')
const isLoading = ref(true)
const errorMessage = ref('')
const isSubmitting = ref(false)
const reservationResult = ref<ReservationCreateData | null>(null)

const selectedTable = computed(() => tables.value.find((table) => table.tableId === selectedTableId.value) ?? null)

// VanField 的 rules 校验器:返回 true 通过,返回错误文案则不通过并展示在字段下方。
const peopleValidator = (value: number) => {
  if (value < 1) return '预约人数至少为 1 人'
  if (selectedTable.value && value > selectedTable.value.capacity) return `该桌最多容纳 ${selectedTable.value.capacity} 人`
  return true
}
const timeValidator = (value: string) => {
  if (!value) return '请选择预约时间'
  return new Date(value).getTime() > Date.now() ? true : '预约时间必须晚于当前时间'
}

function formatReserveTime(value: string) {
  // datetime-local 使用 T 分隔,后端契约要求空格和秒数。
  const normalized = value.replace('T', ' ')
  return normalized.length === 16 ? `${normalized}:00` : normalized
}

// VanForm 的 submit 只在全部 rules 通过后触发;失败时组件自动标红并提示首个错误字段。
async function onSubmit() {
  if (selectedTable.value === null) return
  errorMessage.value = ''
  reservationResult.value = null
  isSubmitting.value = true
  try {
    reservationResult.value = await createReservation({
      tableId: selectedTable.value.tableId,
      peopleCount: peopleCount.value,
      reserveTime: formatReserveTime(reserveTime.value),
    })
    showSuccessToast(`预约成功:${reservationResult.value.reservationNo}`)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '创建预约失败,请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}

async function loadTables() {
  // 页面挂载时读取真实空闲桌位,并将异常转换为可展示文案。
  isLoading.value = true
  errorMessage.value = ''
  try {
    tables.value = await getAvailableTables()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '桌位查询失败,请稍后重试'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadTables)
</script>

<template>
  <section class="reserve-page">
    <div v-if="reservationResult" class="reserve-result">
      <van-icon name="checked" class="reserve-result-icon" />
      <p class="reserve-result-title">预约创建成功</p>
      <van-cell-group inset>
        <van-cell title="预约编号" :value="reservationResult.reservationNo" />
        <van-cell title="桌位" :value="reservationResult.tableNo || `桌位 ${reservationResult.tableId}`" />
        <van-cell title="人数" :value="`${reservationResult.peopleCount} 人`" />
        <van-cell title="预约时间" :value="formatDateTime(reservationResult.reserveTime)" />
      </van-cell-group>
      <div class="reserve-result-actions">
        <van-button block type="primary" plain @click="reservationResult = null">再约一桌</van-button>
        <van-button block type="primary" plain to="/customer/reservations">查看我的预约</van-button>
      </div>
    </div>

    <van-loading v-else-if="isLoading" class="reserve-loading" size="24px" vertical>正在查询空闲桌位...</van-loading>
    <van-empty v-else-if="errorMessage" image="network" :description="errorMessage">
      <van-button round type="primary" size="small" @click="loadTables">重新加载</van-button>
    </van-empty>
    <van-empty v-else-if="tables.length === 0" description="当前没有可预约的空闲桌位" />

    <template v-else>
      <p class="reserve-section-title">选择空闲桌位</p>
      <div class="reserve-table-grid" role="list" aria-label="空闲桌位列表">
        <button
          v-for="table in tables"
          :key="table.tableId"
          type="button"
          class="reserve-table-card"
          :class="{ 'reserve-table-card--active': table.tableId === selectedTableId }"
          @click="selectedTableId = table.tableId"
        >
          <strong>{{ table.tableNo }}</strong>
          <span>{{ table.capacity }} 人桌</span>
          <small>{{ table.locationDesc || '暂无位置描述' }}</small>
        </button>
      </div>

      <van-form class="reserve-form" @submit="onSubmit">
        <van-cell-group inset>
          <van-field name="stepper" label="预约人数" :rules="[{ validator: peopleValidator }]">
            <template #input>
              <van-stepper v-model="peopleCount" min="1" :max="selectedTable?.capacity ?? 99" theme="round" button-size="26" />
            </template>
          </van-field>
          <!-- v-model 绑定 reserveTime 后,rules 校验器才能拿到表单值;自定义 input 插槽只负责录入控件形态。 -->
          <van-field v-model="reserveTime" name="预约时间" label="预约时间" :rules="[{ validator: timeValidator }]">
            <template #input>
              <!-- Field 内嵌原生 datetime-local:视觉归 Vant,录入归系统控件。 -->
              <input
                v-model="reserveTime"
                type="datetime-local"
                class="reserve-time-input"
                aria-label="预约时间"
              />
            </template>
          </van-field>
        </van-cell-group>
        <p v-if="errorMessage" class="reserve-error" role="alert">{{ errorMessage }}</p>
        <div class="reserve-submit">
          <van-button round block type="primary" native-type="submit" :disabled="selectedTableId === null" :loading="isSubmitting">
            {{ selectedTableId === null ? '请先选择桌位' : '创建预约' }}
          </van-button>
        </div>
      </van-form>
    </template>
  </section>
</template>

<style scoped>
.reserve-page {
  display: grid;
  gap: var(--space-4);
}
.reserve-loading {
  display: flex;
  justify-content: center;
  margin: var(--space-8) 0;
}
.reserve-section-title {
  color: var(--color-text);
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 var(--space-1);
}
.reserve-table-grid {
  display: grid;
  gap: var(--space-2);
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
}
.reserve-table-card {
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: grid;
  gap: 2px;
  padding: var(--space-3);
  text-align: left;
}
.reserve-table-card span,
.reserve-table-card small {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}
.reserve-table-card small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.reserve-table-card--active {
  background: var(--color-surface);
  border-color: var(--color-brand);
  box-shadow: 0 0 0 2px var(--color-brand-ring);
}
.reserve-form {
  display: grid;
  gap: var(--space-2);
  margin-top: var(--space-1);
}
/* 原生 datetime输入框去掉自带边框,宽度撑满 Field 输入区。 */
.reserve-time-input {
  background: transparent;
  border: 0;
  color: var(--color-text);
  font: inherit;
  min-height: 24px;
  width: 100%;
}
.reserve-time-input:focus-visible {
  outline: none;
}
.reserve-error {
  background: var(--color-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  margin: 0;
  padding: var(--space-2) var(--space-3);
}
.reserve-submit {
  padding: 0 var(--space-4);
}
.reserve-result {
  display: grid;
  gap: var(--space-4);
  justify-items: center;
  padding-top: var(--space-6);
}
.reserve-result-icon {
  color: var(--color-success);
  font-size: 48px;
}
.reserve-result-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}
.reserve-result-actions {
  display: grid;
  gap: var(--space-2);
  width: 100%;
}
</style>
