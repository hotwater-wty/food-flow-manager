<script setup lang="ts">
// 管理订单工作台:二期 R2 试点,用 Element Plus 的表格/分页/标签/抽屉重构首版卡片列表。
// 业务逻辑与首版一致:分页读取订单、按后端状态机推进状态、点击详情查看菜品明细;
// 变化只在展示层——表格替代卡片列表,详情从行内展开改为右侧抽屉。
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getAdminOrderDetail, getAdminOrders, updateOrderStatus } from '../services/admin-order'
import type { AdminOrderData, OrderDetailData } from '@foodflow/shared/types/api'
import { getOrderStatusLabel, getOrderTagKind } from '@foodflow/shared/utils/order-status'
import { formatDateTime, formatPrice } from '@foodflow/shared/utils/format'
import { usePagedList } from '../composables/use-pagedList'
import { useAutoRefresh } from '../composables/use-autoRefresh'

// 页大小与后端约定一致;服务层固定传 pageSize=10。
const PAGE_SIZE = 10

// 状态筛选用空字符串表示"全部":el-option 的 value 不接受 undefined,
// 服务层调用前再把 '' 翻译回 undefined(不携带筛选参数)。
const statusFilter = ref<number | ''>('')
const statusOptions: Array<{ label: string; value: number | '' }> = [
  { label: '全部状态', value: '' },
  { label: '已下单', value: 1 },
  { label: '制作中', value: 2 },
  { label: '已上齐', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
]

// 三期 R4 改用 usePagedList:分页状态机与静默加载都交给组合式函数,
// 闭包捕获 statusFilter,筛选值变化后重新 load 即携带新的查询参数。
const { records: orders, pageNo, total, loading, errorMessage, load, handlePageChange, reloadFromFirstPage } =
  usePagedList<AdminOrderData>((page) =>
    getAdminOrders(page, statusFilter.value === '' ? undefined : statusFilter.value),
  )

// 详情抽屉:drawerVisible 控制开合,detailLoading 区分"加载中"与"已加载"。
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<OrderDetailData | null>(null)

// 写操作锁:记录正在推进的订单 ID,并让整页其他推进按钮暂时禁用,避免并发写请求。
const actionId = ref<number | null>(null)

// 推进按钮文案由当前状态推导;只允许相邻状态,跳级会被后端拒绝。
function advanceLabel(status: number) {
  if (status === 1) return '开始制作'
  if (status === 2) return '标记已上齐'
  return '完成订单'
}

// 打开详情抽屉;每次都重新请求,保证看到最新金额与明细。
async function openDetail(order: AdminOrderData) {
  drawerVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await getAdminOrderDetail(order.orderId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单详情查询失败'
    drawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// 状态推进采用当前状态 + 1,后端仍会拒绝跳级或非法状态。
async function advance(order: AdminOrderData) {
  // actionId 非空表示已有写请求;早返回避免并发推进多个订单。
  if (order.status >= 4 || actionId.value !== null) return
  // "完成订单"是终态操作,用确认弹窗拦截误触;用户取消时 confirm 会 reject。
  if (order.status === 3) {
    try {
      await ElMessageBox.confirm(`确认完成订单 ${order.orderNo}?完成后订单进入终态,不能再推进。`, '完成订单', {
        confirmButtonText: '确认完成',
        cancelButtonText: '取消',
        type: 'warning',
      })
    } catch {
      return
    }
  }
  actionId.value = order.orderId
  errorMessage.value = ''
  try {
    // 前端只计算相邻目标状态,后端状态机仍是最终裁判。
    await updateOrderStatus(order.orderId, order.status + 1)
    ElMessage.success(`订单 ${order.orderNo} 已推进至「${getOrderStatusLabel(order.status + 1)}」`)
    // 抽屉正展示该订单时,用已确认的新状态同步本地详情,避免展示过期状态。
    if (drawerVisible.value && detailData.value?.orderId === order.orderId) {
      detailData.value = { ...detailData.value, status: order.status + 1 }
    }
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '订单状态更新失败'
  } finally {
    actionId.value = null
  }
}

// 自动刷新(三期 R3):工作台默认每 20 秒静默轮询,标签页不可见时暂停;
// 切回标签页/窗口聚焦时也会立即静默刷新,三种触发共用上面的 load。
const { autoRefresh } = useAutoRefresh(load)

// onMounted 接收函数引用;组件插入 DOM 后由 Vue 自动调用一次。
onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>订单工作台</h1>
      <p>查看订单并按后端状态机推进制作流程。</p>
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

    <el-table v-loading="loading" :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单编号" min-width="190" show-overflow-tooltip />
      <el-table-column prop="tableNo" label="桌位" width="80" />
      <el-table-column label="下单时间" min-width="165">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="getOrderTagKind(row.status)" disable-transitions>{{ getOrderStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="100" align="right">
        <template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row as AdminOrderData)">详情</el-button>
          <el-button
            v-if="row.status >= 1 && row.status <= 3"
            link
            type="primary"
            :disabled="actionId !== null"
            :loading="actionId === row.orderId"
            @click="advance(row as AdminOrderData)"
          >
            {{ advanceLabel(row.status) }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有订单" :image-size="72" />
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

    <el-drawer v-model="drawerVisible" size="400px">
      <template #header>
        <div class="order-drawer-header">
          <strong>{{ detailData?.orderNo ?? '订单详情' }}</strong>
          <el-tag v-if="detailData" :type="getOrderTagKind(detailData.status)" disable-transitions>
            {{ getOrderStatusLabel(detailData.status) }}
          </el-tag>
        </div>
      </template>

      <div v-loading="detailLoading" class="order-drawer-body">
        <el-descriptions v-if="detailData" :column="1" border size="small">
          <el-descriptions-item label="桌位">{{ detailData.tableNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">{{ formatPrice(detailData.totalAmount) }}</el-descriptions-item>
        </el-descriptions>

        <el-table v-if="detailData" :data="detailData.items" size="small">
          <el-table-column prop="dishName" label="菜品" min-width="110" show-overflow-tooltip />
          <el-table-column prop="quantity" label="数量" width="60" align="center" />
          <el-table-column label="小计" width="90" align="right">
            <template #default="{ row }">{{ formatPrice(row.amount) }}</template>
          </el-table-column>
        </el-table>

        <ul v-if="detailData?.items.some((item) => item.remark)" class="order-drawer-remarks">
          <li v-for="item in detailData.items.filter((i) => i.remark)" :key="item.dishId">
            {{ item.dishName }}:{{ item.remark }}
          </li>
        </ul>
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
/* 抽屉是本页面私有结构,样式用 scoped 与其他管理页隔离。 */
.order-drawer-header {
  align-items: center;
  display: flex;
  gap: var(--space-2);
}
.order-drawer-body {
  display: grid;
  gap: var(--space-4);
  min-height: 120px;
}
.order-drawer-remarks {
  color: var(--color-text-secondary);
  display: grid;
  font-size: 0.85rem;
  gap: var(--space-1);
  list-style: none;
  margin: 0;
  padding: 0;
}
</style>
