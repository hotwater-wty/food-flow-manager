<script setup lang="ts">
// 桌位维护页:二期 R3 从单页五 Tab 拆分而来,路由 /admin/resources/tables。
// 新增/编辑共用一个 ElDialog 表单;删除是店长专属操作,由路由守卫保证只有店长能进本页。
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createTable, deleteTable, getAdminTables, setTableEnabled, updateTable,
} from '../../services/admin-resources'
import type { TableRequest, TableVO } from '../../services/admin-resources'
import type { FormInstance, FormRules } from 'element-plus'
import { getTableStatusLabel } from '@foodflow/shared/utils/status'
import { usePagedList } from '../../composables/use-pagedList'

const PAGE_SIZE = 10

const { records, pageNo, total, loading, errorMessage, load, handlePageChange } =
  usePagedList<TableVO>((page) => getAdminTables(page))

// 写操作锁:任一写请求进行中时禁用其余行按钮。
const actionId = ref<number | null>(null)

// 对话框表单:editingId 为 null 表示新增,有值表示编辑对应桌位。
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<TableRequest>({ tableNo: '', capacity: 4, locationDesc: '' })
const submitting = ref(false)

// rules 是 Element Plus 的声明式校验配置,提交前由 validate() 统一执行。
const rules: FormRules = {
  tableNo: [
    { required: true, message: '请输入桌号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '桌号只能包含字母和数字', trigger: 'blur' },
  ],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
}

// 打开新增:重置表单为默认值。
function openCreate() {
  editingId.value = null
  form.tableNo = ''
  form.capacity = 4
  form.locationDesc = ''
  dialogVisible.value = true
}

// 打开编辑:把列表行数据复制进表单,避免输入直接修改列表对象。
function openEdit(item: TableVO) {
  editingId.value = item.tableId
  form.tableNo = item.tableNo
  form.capacity = item.capacity
  form.locationDesc = item.locationDesc
  dialogVisible.value = true
}

async function submit() {
  // validate 返回 Promise,校验失败时 reject;catch 后直接返回,保持弹窗打开。
  await formRef.value?.validate().catch(() => Promise.reject())
  submitting.value = true
  try {
    if (editingId.value !== null) await updateTable(editingId.value, form)
    else await createTable(form)
    ElMessage.success(editingId.value !== null ? '桌位已更新' : '桌位已创建')
    dialogVisible.value = false
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '桌位保存失败'
  } finally {
    submitting.value = false
  }
}

// 删除不可恢复,统一走确认弹窗。
async function remove(item: TableVO) {
  try {
    await ElMessageBox.confirm(`确认删除桌位 ${item.tableNo}?该操作不可恢复。`, '删除桌位', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  actionId.value = item.tableId
  try {
    await deleteTable(item.tableId)
    ElMessage.success(`桌位 ${item.tableNo} 已删除`)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '桌位删除失败'
  } finally {
    actionId.value = null
  }
}

async function toggleEnabled(item: TableVO) {
  actionId.value = item.tableId
  try {
    await setTableEnabled(item.tableId, item.status === 4)
    ElMessage.success(`桌位 ${item.tableNo} 已${item.status === 4 ? '启用' : '禁用'}`)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '桌位状态更新失败'
  } finally {
    actionId.value = null
  }
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>桌位维护</h1>
      <p>维护堂食桌位的编号、容量与启用状态。</p>
    </div>

    <div class="admin-toolbar">
      <el-button type="primary" :icon="Plus" @click="openCreate">新增桌位</el-button>
      <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="tableNo" label="桌号" width="110" />
      <el-table-column label="容量" width="90">
        <template #default="{ row }">{{ row.capacity }} 人</template>
      </el-table-column>
      <el-table-column prop="locationDesc" label="位置描述" min-width="140">
        <template #default="{ row }">{{ row.locationDesc || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="row.status === 4 ? 'info' : row.status === 0 ? 'success' : 'warning'" disable-transitions>
            {{ getTableStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 4 ? 'success' : 'warning'" :disabled="actionId !== null" @click="toggleEnabled(row)">
            {{ row.status === 4 ? '启用' : '禁用' }}
          </el-button>
          <el-button link type="danger" :disabled="actionId !== null" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有桌位" :image-size="72" />
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

    <el-dialog v-model="dialogVisible" :title="editingId !== null ? '编辑桌位' : '新增桌位'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="桌号" prop="tableNo">
          <el-input v-model="form.tableNo" placeholder="例如 A01" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input v-model="form.locationDesc" placeholder="例如 二楼靠窗" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
