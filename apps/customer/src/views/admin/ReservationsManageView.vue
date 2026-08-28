<script setup lang="ts">
// 预约管理页:二期 R3 从单页五 Tab 拆分而来,路由 /admin/resources/reservations。
// 预约是只读为主的管理视角:详情用抽屉渲染,异常预约可取消。
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { cancelAdminReservation, getAdminReservationDetail, getAdminReservations } from '../../services/admin-resources'
import type { ReservationAdminData } from '@foodflow/shared/types/api'
import { canCancelReservation, getReservationStatusLabel, getReservationTagKind } from '@foodflow/shared/utils/status'
import { formatDateTime } from '@foodflow/shared/utils/format'
import { usePagedList } from '../../composables/use-pagedList'

const PAGE_SIZE = 10

const { records, pageNo, total, loading, errorMessage, load, handlePageChange } =
  usePagedList<ReservationAdminData>((page) => getAdminReservations(page))

const actionId = ref<number | null>(null)

const drawerVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ReservationAdminData | null>(null)


async function openDetail(item: ReservationAdminData) {
  drawerVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await getAdminReservationDetail(item.reservationId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '预约详情查询失败'
    drawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function cancel(item: ReservationAdminData) {
  try {
    await ElMessageBox.confirm(
      `确认取消预约 ${item.reservationNo}?该预约当前为「${getReservationStatusLabel(item.status)}」。`,
      '取消预约',
      { confirmButtonText: '确认取消', cancelButtonText: '再想想', type: 'warning' },
    )
  } catch {
    return
  }
  actionId.value = item.reservationId
  try {
    await cancelAdminReservation(item.reservationId)
    ElMessage.success(`预约 ${item.reservationNo} 已取消`)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '预约取消失败'
  } finally {
    actionId.value = null
  }
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>预约管理</h1>
      <p>查看顾客预约,读取详情或取消异常预约。</p>
    </div>

    <div class="admin-toolbar">
      <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="reservationNo" label="预约编号" min-width="180" show-overflow-tooltip />
      <el-table-column label="桌位" width="100">
        <template #default="{ row }">{{ row.tableNo || `桌位 ${row.tableId}` }}</template>
      </el-table-column>
      <el-table-column label="人数" width="70" align="center">
        <template #default="{ row }">{{ row.peopleCount }}</template>
      </el-table-column>
      <el-table-column label="预约时间" min-width="160">
        <template #default="{ row }">{{ formatDateTime(row.reserveTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="getReservationTagKind(row.status)" disable-transitions>{{ getReservationStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="canCancelReservation(row.status)"
            link
            type="danger"
            :disabled="actionId !== null"
            :loading="actionId === row.reservationId"
            @click="cancel(row)"
          >
            取消预约
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有预约" :image-size="72" />
      </template>
    </el-table>

    <div class="admin-pagination">
      <el-pagination
        :current-page="pageNo"
        :page-size="PAGE_SIZE"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>

    <el-drawer v-model="drawerVisible" size="380px">
      <template #header>
        <strong>{{ detailData?.reservationNo ?? '预约详情' }}</strong>
      </template>
      <div v-loading="detailLoading" class="reservation-drawer-body">
        <el-descriptions v-if="detailData" :column="1" border size="small">
          <el-descriptions-item label="桌位">{{ detailData.tableNo || `桌位 ${detailData.tableId}` }}</el-descriptions-item>
          <el-descriptions-item label="人数">{{ detailData.peopleCount }} 人</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDateTime(detailData.reserveTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getReservationStatusLabel(detailData.status) }}</el-descriptions-item>
        </el-descriptions>
        <p v-else class="reservation-drawer-hint">正在读取预约数据...</p>
      </div>
    </el-drawer>
  </section>
</template>

<style scoped>
.reservation-drawer-body {
  min-height: 140px;
}
.reservation-drawer-hint {
  color: var(--color-text-secondary);
  margin: 0;
}
</style>
