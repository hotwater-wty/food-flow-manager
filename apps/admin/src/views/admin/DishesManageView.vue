<script setup lang="ts">
// 菜品维护页:二期 R3 从单页五 Tab 拆分而来,路由 /admin/resources/dishes。
// 两个重点:1) 首版采集但从未渲染的图片字段,这里用 ElImage 渲染(带加载失败占位);
// 2) 后端金额单位是"分",表单让店长直接输入"元",提交前换算回分,展示时再除回元。
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createDish, deleteDish, getAdminCategories, getAdminDishes, setDishStatus, updateDish,
} from '../../services/admin-resources'
import type { DishCategoryData, DishData } from '@foodflow/shared/types/api'
import type { FormInstance, FormRules } from 'element-plus'
import { getDishStatusLabel } from '@foodflow/shared/utils/status'
import { formatPrice } from '@foodflow/shared/utils/format'
import { usePagedList } from '../../composables/use-pagedList'

const PAGE_SIZE = 10

const { records, pageNo, total, loading, errorMessage, load, handlePageChange } =
  usePagedList<DishData>((page) => getAdminDishes(page))

// 分类下拉选项:新增/编辑菜品时把 categoryId 从"手填数字"升级为下拉选择。
// 弹窗打开时按需加载一次;排序取后端默认顺序。
const categoryOptions = ref<DishCategoryData[]>([])
const categoryOptionsLoaded = ref(false)
async function loadCategoryOptions() {
  if (categoryOptionsLoaded.value) return
  try {
    // 分类是低频维护的小集合:一次取足(200)避免"只取第一页"导致下拉缺项。
    const result = await getAdminCategories(1, 200)
    categoryOptions.value = result.records
    categoryOptionsLoaded.value = true
  } catch {
    // 下拉加载失败不阻塞弹窗,表单仍可手填数字兜底。
  }
}

const actionId = ref<number | null>(null)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
// priceYuan 是表单里的"元";提交前 ×100 取整为分,展示层永远从分换算回元。
const priceYuan = ref('12.00')
const form = reactive({ categoryId: undefined as number | undefined, name: '', description: '', image: '', status: 0 })
const submitting = ref(false)

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  image: [{ required: true, message: '请输入图片地址', trigger: 'blur' }],
}

const categoryLabel = computed(() => (id: number) => categoryOptions.value.find((c) => c.id === id)?.name ?? `分类 ${id}`)


function dishTagType(status: number): 'success' | 'warning' | 'info' {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'info'
}

function openCreate() {
  editingId.value = null
  form.categoryId = categoryOptions.value[0]?.id
  form.name = ''
  form.description = ''
  form.image = ''
  form.status = 0
  priceYuan.value = '12.00'
  void loadCategoryOptions()
  dialogVisible.value = true
}

function openEdit(item: DishData) {
  editingId.value = item.id
  form.categoryId = item.categoryId
  form.name = item.name
  // ?? 把后端允许为空的描述/图片转换为表单需要的字符串。
  form.description = item.description ?? ''
  form.image = item.image ?? ''
  form.status = item.status
  priceYuan.value = (item.price / 100).toFixed(2)
  void loadCategoryOptions()
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  // 元转分:先转数值再取整,避免浮点乘法误差(如 12.34 * 100 = 1233.9999...)。
  const priceCents = Math.round(Number(priceYuan.value) * 100)
  if (!Number.isFinite(priceCents) || priceCents < 0) {
    errorMessage.value = '价格格式不正确'
    return
  }
  submitting.value = true
  try {
    if (editingId.value !== null) {
      // 更新接口不接受创建时的 status 字段,状态变更走独立接口。
      await updateDish(editingId.value, { categoryId: form.categoryId!, name: form.name, description: form.description, price: priceCents, image: form.image })
    } else {
      await createDish({ categoryId: form.categoryId!, name: form.name, description: form.description, price: priceCents, image: form.image, status: form.status })
    }
    ElMessage.success(editingId.value !== null ? '菜品已更新' : '菜品已创建')
    dialogVisible.value = false
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品保存失败'
  } finally {
    submitting.value = false
  }
}

async function remove(item: DishData) {
  try {
    await ElMessageBox.confirm(`确认删除菜品「${item.name}」?该操作不可恢复。`, '删除菜品', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  actionId.value = item.id
  try {
    await deleteDish(item.id)
    ElMessage.success(`菜品「${item.name}」已删除`)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品删除失败'
  } finally {
    actionId.value = null
  }
}

// 在"停售(0)"与"启售(1)"间切换;售罄(2)由后端业务流转产生,不由资料页设置。
async function toggleStatus(item: DishData) {
  actionId.value = item.id
  try {
    await setDishStatus(item.id, item.status === 1 ? 0 : 1)
    ElMessage.success(`菜品「${item.name}」已${item.status === 1 ? '停售' : '启售'}`)
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品状态更新失败'
  } finally {
    actionId.value = null
  }
}

onMounted(() => {
  void load()
  // 分类选项在页面挂载时就预载:表格的分类列靠它把 categoryId 翻译成名称,
  // 不能等打开表单弹窗才加载,否则首屏显示的是"分类 N"兜底文案。
  void loadCategoryOptions()
})
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading">
      <h1>菜品维护</h1>
      <p>维护菜品信息、价格与售卖状态;售罄状态由后端订单流转产生。</p>
    </div>

    <div class="admin-toolbar">
      <el-button type="primary" :icon="Plus" @click="openCreate">新增菜品</el-button>
      <el-button :icon="Refresh" :loading="loading" @click="load()">刷新</el-button>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column label="图片" width="76">
        <template #default="{ row }">
          <el-image
            :src="row.image ?? undefined"
            fit="cover"
            class="dish-thumb"
            preview-teleported
            :preview-src-list="row.image ? [row.image] : undefined"
          >
            <template #error>
              <div class="dish-thumb dish-thumb--fallback">无图</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
      <el-table-column label="分类" width="100">
        <template #default="{ row }">{{ categoryLabel(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column label="价格" width="95" align="right">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="85">
        <template #default="{ row }">
          <el-tag :type="dishTagType(row.status)" disable-transitions>{{ getDishStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.description || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row as DishData)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" :disabled="actionId !== null" @click="toggleStatus(row as DishData)">
            {{ row.status === 1 ? '停售' : '启售' }}
          </el-button>
          <el-button link type="danger" :disabled="actionId !== null" @click="remove(row as DishData)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当前没有菜品" :image-size="72" />
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

    <el-dialog v-model="dialogVisible" :title="editingId !== null ? '编辑菜品' : '新增菜品'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="option in categoryOptions" :key="option.id" :label="`${option.name}(#${option.id})`" :value="option.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如 招牌红烧肉" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="菜品简介" />
        </el-form-item>
        <el-form-item label="价格(元)">
          <el-input v-model="priceYuan" placeholder="例如 12.34" style="width: 160px" />
        </el-form-item>
        <el-form-item label="图片地址" prop="image">
          <el-input v-model="form.image" placeholder="https://..." />
        </el-form-item>
        <el-form-item v-if="editingId === null" label="初始状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">停售</el-radio>
            <el-radio :value="1">启售</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.dish-thumb {
  border-radius: var(--radius-sm);
  display: block;
  height: 44px;
  width: 44px;
}
.dish-thumb--fallback {
  align-items: center;
  background: var(--color-bg-admin);
  color: var(--color-text-muted);
  display: flex;
  font-size: 0.72rem;
  justify-content: center;
}
</style>
