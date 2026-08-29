<script setup lang="ts">
// 菜品分类维护页:二期 R3 从单页五 Tab 拆分而来,路由 /admin/resources/categories。
// 分类结构最简单(名称+排序+启停),作为五个资源页中的样板页。
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createCategory,
  deleteCategory,
  getAdminCategories,
  setCategoryEnabled,
  updateCategory,
} from '../../services/admin-resources'
import type { DishCategoryData, DishCategoryRequest } from '@foodflow/shared/types/api'
import type { FormInstance, FormRules } from 'element-plus'
import { usePagedList } from '../../composables/use-pagedList'
import { useAdminOperation } from '../../composables/use-admin-operation'

const PAGE_SIZE = 10

const { records, pageNo, total, loading, errorMessage, load, handlePageChange } = usePagedList<DishCategoryData>(
  (page) => getAdminCategories(page),
)

const actionId = ref<number | null>(null)
const actionType = ref<'delete' | 'toggle' | null>(null)
const { run } = useAdminOperation()

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<DishCategoryRequest>({ name: '', sort: 0 })
const submitting = ref(false)

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.sort = 0
  dialogVisible.value = true
}

function openEdit(item: DishCategoryData) {
  editingId.value = item.id
  form.name = item.name
  form.sort = item.sort
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  submitting.value = true
  const isEditing = editingId.value !== null
  const succeeded = await run({
    execute: () => (isEditing ? updateCategory(editingId.value!, form) : createCategory(form)),
    refresh: () => load({ rethrow: true }),
    successMessage: isEditing ? '分类已更新' : '分类已创建',
  })
  if (succeeded) {
    dialogVisible.value = false
  }
  submitting.value = false
}

async function remove(item: DishCategoryData) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${item.name}」?该操作不可恢复。`, '删除分类', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  actionId.value = item.id
  actionType.value = 'delete'
  await run({
    execute: () => deleteCategory(item.id),
    refresh: () => load({ rethrow: true }),
    successMessage: `分类「${item.name}」已删除`,
  })
  actionId.value = null
  actionType.value = null
}

async function toggleEnabled(item: DishCategoryData) {
  actionId.value = item.id
  actionType.value = 'toggle'
  await run({
    execute: () => setCategoryEnabled(item.id, item.status !== 1),
    refresh: () => load({ rethrow: true }),
    successMessage: `分类「${item.name}」已${item.status === 1 ? '禁用' : '启用'}`,
  })
  actionId.value = null
  actionType.value = null
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>菜品分类</h1>
      <p>维护菜单分类的名称、排序与启停;禁用的分类不再出现在顾客菜单筛选中。</p>
    </div>

    <div class="admin-toolbar">
      <el-button type="primary" :icon="Plus" :disabled="actionId !== null" @click="openCreate">新增分类</el-button>
      <el-button :icon="Refresh" :loading="loading" @click="load()">刷新</el-button>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="分类名称" min-width="140" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" disable-transitions>
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="actionId !== null" @click="openEdit(row as DishCategoryData)"
            >编辑</el-button
          >
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            :disabled="actionId !== null"
            :loading="actionId === row.id && actionType === 'toggle'"
            @click="toggleEnabled(row as DishCategoryData)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            link
            type="danger"
            :disabled="actionId !== null"
            :loading="actionId === row.id && actionType === 'delete'"
            @click="remove(row as DishCategoryData)"
            >删除</el-button
          >
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有分类" :image-size="72" />
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

    <el-dialog v-model="dialogVisible" :title="editingId !== null ? '编辑分类' : '新增分类'" width="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如 热菜" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
