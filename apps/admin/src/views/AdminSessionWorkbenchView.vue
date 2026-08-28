<script setup lang="ts">
// 管理会话工作台:二期 R3 用 Element Plus 重构。
// 与首版的差异:1) 补上后端早已支持的状态筛选;2) "详情"从拼接一行反馈文本
// 改为右侧抽屉真正渲染会话数据;3) 取消等待/清台两个写操作增加确认弹窗。
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { cancelAdminSession, closeAdminSession, getAdminSessionDetail, getAdminSessions } from '../services/admin-session'
import type { DiningSessionData } from '@foodflow/shared/types/api'
import { getSessionStatusLabel, getTableStatusLabel } from '@foodflow/shared/utils/status'
import { usePagedList } from '../composables/use-pagedList'
import { useAutoRefresh } from '../composables/use-autoRefresh'

// 状态筛选:空字符串表示"全部"(el-option 的 value 不接受 undefined)。
const statusFilter = ref<number | ''>('')
const statusOptions: Array<{ label: string; value: number | '' }> = [
  { label: '全部状态', value: '' },
  { label: '等待中', value: 0 },
  { label: '用餐中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
]

// usePagedList 把"页码+总数+加载锁+错误信息"的重复状态机收敛为一个组合式函数;
// 闭包捕获 statusFilter,筛选值变化后重新 load 即携带新的查询参数。
const { records, pageNo, total, loading, errorMessage, load, handlePageChange, reloadFromFirstPage } =
  usePagedList<DiningSessionData>((page) =>
    getAdminSessions(page, statusFilter.value === '' ? undefined : statusFilter.value),
  )

// 详情抽屉:每次打开都重新请求,展示最新会话与桌位状态。
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<DiningSessionData | null>(null)

// 写操作互斥锁:记录正在操作的会话 ID,并禁用其余行的操作按钮。
const actionId = ref<number | null>(null)

// 会话状态映射 Tag 颜色:等待黄、用餐主色、完成绿、取消灰。
function sessionTagType(status: number): 'primary' | 'warning' | 'success' | 'info' {
  if (status === 2) return 'success'
  if (status === 3) return 'info'
  if (status === 0) return 'warning'
  return 'primary'
}

async function openDetail(session: DiningSessionData) {
  drawerVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await getAdminSessionDetail(session.sessionId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '会话详情查询失败'
    drawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function action(session: DiningSessionData, type: 'cancel' | 'close') {
  // actionId 非空表示已有写请求;早返回避免并发操作多个会话。
  if (actionId.value !== null) return
  // 两个动作都会改变会话走向,统一用确认弹窗拦截误触;用户取消时 confirm 会 reject。
  const tip =
    type === 'cancel'
      ? `确认取消等待中的会话 ${session.sessionNo}?取消后顾客需要重新开台。`
      : `确认为桌位 ${session.tableNo} 清台?清台要求会话处于用餐中且不存在已下单/制作中的订单。`
  try {
    await ElMessageBox.confirm(tip, type === 'cancel' ? '取消等待' : '清台', {
      confirmButtonText: type === 'cancel' ? '确认取消' : '确认清台',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return
  }
  actionId.value = session.sessionId
  errorMessage.value = ''
  try {
    if (type === 'cancel') await cancelAdminSession(session.sessionId)
    else await closeAdminSession(session.sessionId)
    ElMessage.success(type === 'cancel' ? `会话 ${session.sessionNo} 已取消` : `桌位 ${session.tableNo} 已清台`)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '会话操作失败'
  } finally {
    actionId.value = null
  }
}

// 自动刷新(三期 R3):与订单工作台一致,每 20 秒静默轮询,不可见暂停、聚焦即刷。
const { autoRefresh } = useAutoRefresh(load)

// 首次进入工作台时自动拉取第一页。
onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>会话工作台</h1>
      <p>查看堂食会话,取消等待中的会话或完成清台。</p>
    </div>

    <div class="admin-toolbar">
      <el-select v-model="statusFilter" class="status-select" placeholder="全部状态" @change="reloadFromFirstPage">
        <el-option v-for="option in statusOptions" :key="option.label" :label="option.label" :value="option.value" />
      </el-select>
      <el-button :icon="Refresh" :loading="loading" @click="load()">刷新</el-button>
      <label class="auto-refresh-toggle">
        <el-switch v-model="autoRefresh" size="small" />
        每 20 秒自动刷新
      </label>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="sessionNo" label="会话编号" min-width="190" show-overflow-tooltip />
      <el-table-column prop="tableNo" label="桌位" width="80" />
      <el-table-column label="会话状态" width="100">
        <template #default="{ row }">
          <el-tag :type="sessionTagType(row.sessionStatus)" disable-transitions>{{ getSessionStatusLabel(row.sessionStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="桌位状态" width="100">
        <template #default="{ row }">{{ getTableStatusLabel(row.tableStatus) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row as DiningSessionData)">详情</el-button>
          <el-button
            v-if="row.sessionStatus === 0"
            link
            type="danger"
            :disabled="actionId !== null"
            :loading="actionId === row.sessionId"
            @click="action(row as DiningSessionData, 'cancel')"
          >
            取消等待
          </el-button>
          <el-button
            v-if="row.sessionStatus === 1"
            link
            type="primary"
            :disabled="actionId !== null"
            :loading="actionId === row.sessionId"
            @click="action(row as DiningSessionData, 'close')"
          >
            清台
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有堂食会话" :image-size="72" />
      </template>
    </el-table>

    <div class="admin-pagination">
      <el-pagination
        :current-page="pageNo"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>

    <el-drawer v-model="drawerVisible" size="380px">
      <template #header>
        <strong>{{ detailData?.sessionNo ?? '会话详情' }}</strong>
      </template>
      <div v-loading="detailLoading" class="session-drawer-body">
        <el-descriptions v-if="detailData" :column="1" border size="small">
          <el-descriptions-item label="桌位">{{ detailData.tableNo }}</el-descriptions-item>
          <el-descriptions-item label="会话状态">{{ getSessionStatusLabel(detailData.sessionStatus) }}</el-descriptions-item>
          <el-descriptions-item label="桌位状态">{{ getTableStatusLabel(detailData.tableStatus) }}</el-descriptions-item>
        </el-descriptions>
        <p v-else class="session-drawer-hint">正在读取会话数据...</p>
      </div>
    </el-drawer>
  </section>
</template>

<style scoped>
.auto-refresh-toggle {
  align-items: center;
  color: var(--color-text-secondary);
  display: inline-flex;
  font-size: 0.85rem;
  gap: var(--space-2);
}
.session-drawer-body {
  min-height: 140px;
}
.session-drawer-hint {
  color: var(--color-text-secondary);
  margin: 0;
}
</style>
