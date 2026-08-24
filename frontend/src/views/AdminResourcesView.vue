<script setup lang="ts">
// 管理资料工作台：通过 Tab 切换五类资源，表单负责写入，服务层负责契约。
import { onMounted, ref } from 'vue'
import {
  cancelAdminReservation, createCategory, createDish, createEmployee, createTable, deleteCategory, deleteDish, deleteTable,
  getAdminCategories, getAdminCategory, getAdminDish, getAdminDishes, getAdminReservationDetail, getAdminReservations, getAdminTable, getAdminTables, getEmployee, getEmployees,
  setCategoryEnabled, setDishStatus, setEmployeeEnabled, setTableEnabled, updateCategory, updateDish, updateTable,
} from '../services/admin-resources'
import type { DishCategoryData, DishData, EmployeeData, ReservationAdminData, TableVO } from '../types/api'

// 联合字面量类型限制 Tab 只能是这五个字符串，切换时不会传入任意文本。
type Tab = 'tables' | 'categories' | 'dishes' | 'reservations' | 'employees'
const tab = ref<Tab>('tables')
// 每个 ref 只负责一个可变值；列表状态使用数组泛型约束元素结构。
const pageNo = ref(1)
const total = ref(0)
const isLoading = ref(false)
const errorMessage = ref('')
const feedback = ref('')
const tables = ref<TableVO[]>([])
const categories = ref<DishCategoryData[]>([])
const dishes = ref<DishData[]>([])
const reservations = ref<ReservationAdminData[]>([])
const employees = ref<EmployeeData[]>([])
const editingId = ref<number | null>(null)
const tableForm = ref({ tableNo: '', capacity: 4, locationDesc: '' })
const categoryForm = ref({ name: '', sort: 0 })
const dishForm = ref({ categoryId: 1, name: '', description: '', price: 0, image: 'https://example.com/dish.jpg', status: 0 })
const employeeForm = ref({ phone: '', password: '', name: '' })

// 这些纯函数只负责把后端数字状态转换成可读文案，不修改状态。
const statusText = (status: number) => ({ 0: '空闲', 1: '已预约', 2: '等待中', 3: '用餐中', 4: '禁用' } as Record<number, string>)[status] ?? '未知'
const reservationText = (status: number) => ({ 0: '待到店', 1: '已到店', 2: '已取消', 3: '已超时' } as Record<number, string>)[status] ?? '未知'
const dishText = (status: number) => ({ 0: '停售', 1: '启售', 2: '售罄' } as Record<number, string>)[status] ?? '未知'
const employeeText = (status: number) => ({ 1: '正常', 2: '禁用', 3: '离职' } as Record<number, string>)[status] ?? '未知'

async function load() {
  // Tab 决定请求哪个资源；每次只更新当前资源的 records，避免混淆不同数组。
  isLoading.value = true
  errorMessage.value = ''
  try {
    // if 分支根据当前 Tab 选择一个真实接口；每个接口返回相同的分页外壳。
    if (tab.value === 'tables') {
      const result = await getAdminTables(pageNo.value)
      tables.value = result.records
      total.value = result.total
    }
    if (tab.value === 'categories') {
      const result = await getAdminCategories(pageNo.value)
      categories.value = result.records
      total.value = result.total
    }
    if (tab.value === 'dishes') {
      const result = await getAdminDishes(pageNo.value)
      dishes.value = result.records
      total.value = result.total
    }
    if (tab.value === 'reservations') {
      const result = await getAdminReservations(pageNo.value)
      reservations.value = result.records
      total.value = result.total
    }
    if (tab.value === 'employees') {
      const result = await getEmployees(pageNo.value)
      employees.value = result.records
      total.value = result.total
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '资料查询失败'
  } finally {
    isLoading.value = false
  }
}
// 切换资源时重置页码和编辑态，再启动异步加载；不阻塞点击事件。
function switchTab(next: Tab) {
  tab.value = next
  pageNo.value = 1
  editingId.value = null
  // 不 await 是有意的：点击事件立即结束，加载状态由 load 内部管理。
  void load()
}
// 清空表单相当于把“编辑模式”恢复为“新增模式”。
function resetForms() {
  editingId.value = null
  tableForm.value = { tableNo: '', capacity: 4, locationDesc: '' }
  categoryForm.value = { name: '', sort: 0 }
  dishForm.value = { categoryId: 1, name: '', description: '', price: 0, image: 'https://example.com/dish.jpg', status: 0 }
  employeeForm.value = { phone: '', password: '', name: '' }
}
// editingId 有值时走 PUT，没有值时走 POST；三元表达式表达两种提交路径。
async function saveTable() {
  try {
    if (editingId.value !== null) await updateTable(editingId.value, tableForm.value)
    else await createTable(tableForm.value)
    feedback.value = '桌位已保存'
    resetForms()
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '桌位保存失败'
  }
}

async function saveCategory() {
  try {
    if (editingId.value !== null) await updateCategory(editingId.value, categoryForm.value)
    else await createCategory(categoryForm.value)
    feedback.value = '分类已保存'
    resetForms()
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '分类保存失败'
  }
}

async function saveDish() {
  try {
    // 解构只是读取表单快照；更新接口不接受创建时的 status 字段。
    const { categoryId, name, description, price, image, status } = dishForm.value
    if (editingId.value !== null) await updateDish(editingId.value, { categoryId, name, description, price, image })
    else await createDish({ categoryId, name, description, price, image, status })
    feedback.value = '菜品已保存'
    resetForms()
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜品保存失败'
  }
}

async function saveEmployee() {
  try {
    await createEmployee(employeeForm.value)
    feedback.value = '员工已创建'
    resetForms()
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '员工创建失败'
  }
}
// 所有资料操作共用这个包装器：执行、成功提示、重新加载和异常展示保持一致。
async function run(action: () => Promise<unknown>, message: string) {
  try {
    // () => Promise<unknown> 是回调类型：调用方决定具体 HTTP 动作，run 统一处理结果。
    await action()
    feedback.value = message
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '操作失败'
  }
}
// 编辑函数把列表对象复制到表单 ref，避免表单输入直接修改列表数据。
function editTable(item: TableVO) {
  editingId.value = item.tableId
  tableForm.value = { tableNo: item.tableNo, capacity: item.capacity, locationDesc: item.locationDesc }
}

function editCategory(item: DishCategoryData) {
  editingId.value = item.id
  categoryForm.value = { name: item.name, sort: item.sort }
}

function editDish(item: DishData) {
  editingId.value = item.id
  // ?? 把后端允许为空的描述/图片转换为表单需要的字符串。
  dishForm.value = { categoryId: item.categoryId, name: item.name, description: item.description ?? '', price: item.price, image: item.image ?? '', status: item.status }
}

// 组件首次挂载时读取默认的桌位 Tab。
onMounted(load)
</script>
<template>
  <section class="reservation-view"><div class="reservation-heading"><p class="eyebrow">管理端资料</p><h1>资料维护工作台</h1><p>按资源查看和维护当前后端已提供的管理数据。</p></div>
    <div class="resource-tabs"><button v-for="item in ([['tables','桌位'],['categories','分类'],['dishes','菜品'],['reservations','预约'],['employees','员工']] as [Tab,string][])" :key="item[0]" type="button" :class="{ 'category-button-selected': tab === item[0] }" class="category-button" @click="switchTab(item[0])">{{ item[1] }}</button></div>
    <p v-if="errorMessage" class="feedback feedback-error" role="alert">{{ errorMessage }}</p><p v-if="feedback" class="feedback feedback-success" role="status">{{ feedback }}</p><p v-if="isLoading" class="feedback" role="status">正在查询...</p>
    <form v-if="tab === 'tables'" class="resource-form" @submit.prevent="saveTable"><h2>{{ editingId ? '修改桌位' : '新增桌位' }}</h2><input v-model="tableForm.tableNo" placeholder="桌号，例如 A01" required pattern="[A-Za-z0-9]+" /><input v-model.number="tableForm.capacity" type="number" min="1" placeholder="容量" required /><input v-model="tableForm.locationDesc" placeholder="位置描述" /><button class="open-session-button" type="submit">保存桌位</button></form>
    <form v-if="tab === 'categories'" class="resource-form" @submit.prevent="saveCategory"><h2>{{ editingId ? '修改分类' : '新增分类' }}</h2><input v-model="categoryForm.name" placeholder="分类名称" required /><input v-model.number="categoryForm.sort" type="number" min="0" placeholder="排序" /><button class="open-session-button" type="submit">保存分类</button></form>
    <form v-if="tab === 'dishes'" class="resource-form" @submit.prevent="saveDish"><h2>{{ editingId ? '修改菜品' : '新增菜品' }}</h2><input v-model.number="dishForm.categoryId" type="number" min="1" placeholder="分类 ID" required /><input v-model="dishForm.name" placeholder="菜品名称" required /><input v-model="dishForm.description" placeholder="菜品描述" required /><input v-model.number="dishForm.price" type="number" min="0" placeholder="价格（分）" required /><input v-model="dishForm.image" placeholder="图片地址" required /><select v-model.number="dishForm.status"><option :value="0">停售</option><option :value="1">启售</option></select><button class="open-session-button" type="submit">保存菜品</button></form>
    <form v-if="tab === 'employees'" class="resource-form" @submit.prevent="saveEmployee"><h2>新增员工</h2><input v-model="employeeForm.phone" placeholder="手机号" pattern="1[3-9][0-9]{9}" required /><input v-model="employeeForm.password" type="password" minlength="6" placeholder="初始密码" required /><input v-model="employeeForm.name" placeholder="姓名" required /><button class="open-session-button" type="submit">创建员工</button></form>
    <p v-if="!isLoading && ((tab === 'tables' && tables.length === 0) || (tab === 'categories' && categories.length === 0) || (tab === 'dishes' && dishes.length === 0) || (tab === 'reservations' && reservations.length === 0) || (tab === 'employees' && employees.length === 0))" class="feedback" role="status">当前没有数据。</p>
    <div v-if="tab === 'tables' && tables.length" class="resource-list"><article v-for="item in tables" :key="item.tableId" class="reservation-card"><strong>{{ item.tableNo }}</strong><span>{{ item.capacity }} 人 · {{ item.locationDesc || '无位置描述' }} · {{ statusText(item.status) }}</span><div class="reservation-actions"><button class="secondary-button" type="button" @click="editTable(item)">编辑</button><button class="secondary-button" type="button" @click="run(() => getAdminTable(item.tableId), '桌位详情已读取')">详情</button><button class="primary-outline-button" type="button" @click="run(() => setTableEnabled(item.tableId, item.status === 4), '桌位状态已更新')">{{ item.status === 4 ? '启用' : '禁用' }}</button><button class="danger-button" type="button" @click="run(() => deleteTable(item.tableId), '桌位已删除')">删除</button></div></article></div>
    <div v-if="tab === 'categories' && categories.length" class="resource-list"><article v-for="item in categories" :key="item.id" class="reservation-card"><strong>{{ item.name }}</strong><span>排序 {{ item.sort }} · {{ item.status === 1 ? '启用' : '禁用' }}</span><div class="reservation-actions"><button class="secondary-button" type="button" @click="editCategory(item)">编辑</button><button class="secondary-button" type="button" @click="run(() => getAdminCategory(item.id), '分类详情已读取')">详情</button><button class="primary-outline-button" type="button" @click="run(() => setCategoryEnabled(item.id, item.status !== 1), '分类状态已更新')">{{ item.status === 1 ? '禁用' : '启用' }}</button><button class="danger-button" type="button" @click="run(() => deleteCategory(item.id), '分类已删除')">删除</button></div></article></div>
    <div v-if="tab === 'dishes' && dishes.length" class="resource-list"><article v-for="item in dishes" :key="item.id" class="reservation-card"><strong>{{ item.name }}</strong><span>{{ (item.price / 100).toFixed(2) }} 元 · 分类 {{ item.categoryId }} · {{ dishText(item.status) }}</span><div class="reservation-actions"><button class="secondary-button" type="button" @click="editDish(item)">编辑</button><button class="secondary-button" type="button" @click="run(() => getAdminDish(item.id), '菜品详情已读取')">详情</button><button class="primary-outline-button" type="button" @click="run(() => setDishStatus(item.id, item.status === 1 ? 0 : 1), '菜品状态已更新')">{{ item.status === 1 ? '停售' : '启售' }}</button><button class="danger-button" type="button" @click="run(() => deleteDish(item.id), '菜品已删除')">删除</button></div></article></div>
    <div v-if="tab === 'reservations' && reservations.length" class="resource-list"><article v-for="item in reservations" :key="item.reservationId" class="reservation-card"><strong>{{ item.reservationNo }}</strong><span>{{ item.tableNo || `桌位 ${item.tableId}` }} · {{ item.reserveTime }} · {{ reservationText(item.status) }}</span><div class="reservation-actions"><button v-if="item.status === 0" class="danger-button" type="button" @click="run(() => cancelAdminReservation(item.reservationId), '预约已取消')">取消预约</button><button class="secondary-button" type="button" @click="run(() => getAdminReservationDetail(item.reservationId), '预约详情已读取')">读取详情</button></div></article></div>
    <div v-if="tab === 'employees' && employees.length" class="resource-list"><article v-for="item in employees" :key="item.employeeId" class="reservation-card"><strong>{{ item.name }}</strong><span>{{ item.phone }} · {{ item.role === 2 ? '店长' : '店员' }} · {{ employeeText(item.status) }}</span><div class="reservation-actions"><button class="secondary-button" type="button" @click="run(() => getEmployee(item.employeeId), '员工详情已读取')">详情</button><button class="primary-outline-button" type="button" @click="run(() => setEmployeeEnabled(item.employeeId, item.status !== 1), '员工状态已更新')">{{ item.status === 1 ? '禁用' : '启用' }}</button></div></article></div>
    <div class="pagination-actions"><button class="secondary-button" type="button" :disabled="pageNo <= 1 || isLoading" @click="pageNo--; load()">上一页</button><span>第 {{ pageNo }} 页，共 {{ total }} 条</span><button class="secondary-button" type="button" :disabled="pageNo * 10 >= total || isLoading" @click="pageNo++; load()">下一页</button></div>
  </section>
</template>
