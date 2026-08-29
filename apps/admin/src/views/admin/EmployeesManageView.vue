<script setup lang="ts">
// 员工管理页:二期 R3 从单页五 Tab 拆分而来,路由 /admin/resources/employees。
// 后端整个 /api/admin/employees 前缀都限制店长;本页路由同样标记 requiresManager,
// 店员登录时侧栏不显示入口,直接访问 URL 也会被前端守卫拦下(后端拦截器仍是最终防线)。
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { createEmployee, getEmployees, setEmployeeEnabled } from '../../services/admin-resources'
import type { EmployeeCreateRequest, EmployeeData } from '@foodflow/shared/types/api'
import type { FormInstance, FormRules } from 'element-plus'
import { getEmployeeRoleLabel, getEmployeeStatusLabel } from '@foodflow/shared/utils/status'
import { usePagedList } from '../../composables/use-pagedList'
import { useAdminOperation } from '../../composables/use-admin-operation'

const PAGE_SIZE = 10

const { records, pageNo, total, loading, errorMessage, load, handlePageChange } = usePagedList<EmployeeData>((page) =>
  getEmployees(page),
)

const actionId = ref<number | null>(null)
const { run } = useAdminOperation()

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<EmployeeCreateRequest>({ phone: '', password: '', name: '' })
const submitting = ref(false)

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

function openCreate() {
  form.phone = ''
  form.password = ''
  form.name = ''
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  submitting.value = true
  const succeeded = await run({
    execute: () => createEmployee(form),
    refresh: () => load({ rethrow: true }),
    successMessage: `员工 ${form.name} 已创建`,
  })
  if (succeeded) dialogVisible.value = false
  submitting.value = false
}

async function toggleEnabled(item: EmployeeData) {
  // 禁用员工影响对方登录,弹出确认;启用同样提示,保持操作一致。
  try {
    await ElMessageBox.confirm(
      `确认${item.status === 1 ? '禁用' : '启用'}员工 ${item.name}(${item.phone})?`,
      item.status === 1 ? '禁用员工' : '启用员工',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }
  actionId.value = item.employeeId
  await run({
    execute: () => setEmployeeEnabled(item.employeeId, item.status !== 1),
    refresh: () => load({ rethrow: true }),
    successMessage: `员工 ${item.name} 已${item.status === 1 ? '禁用' : '启用'}`,
  })
  actionId.value = null
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>员工管理</h1>
      <p>创建员工账号并管理启停;禁用的员工无法登录管理端。店长账号不可禁用。本页仅店长可用。</p>
    </div>

    <div class="admin-toolbar">
      <el-button type="primary" :icon="Plus" :disabled="actionId !== null" @click="openCreate">新增员工</el-button>
      <el-button :icon="Refresh" :loading="loading" @click="load()">刷新</el-button>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="employeeId" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" min-width="110" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 2 ? 'primary' : 'info'" disable-transitions>{{
            getEmployeeRoleLabel(row.role)
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" disable-transitions>{{
            getEmployeeStatusLabel(row.status)
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <!-- 店长行不可禁用：按钮标灰并说明原因，后端 disableEmployee 也有角色守卫兜底。 -->
          <el-tooltip v-if="row.role === 2 && row.status === 1" content="店长账号不可禁用" placement="top">
            <span>
              <el-button link type="warning" disabled>禁用</el-button>
            </span>
          </el-tooltip>
          <el-button
            v-else
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            :disabled="actionId !== null"
            :loading="actionId === row.employeeId"
            @click="toggleEnabled(row as EmployeeData)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有员工" :image-size="72" />
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

    <el-dialog v-model="dialogVisible" title="新增员工" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="用于员工登录" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="员工姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">创建</el-button>
      </template>
    </el-dialog>
  </section>
</template>
