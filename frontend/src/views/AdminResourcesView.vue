<script setup lang="ts">
// 管理资料工作台：通过 Tab 切换五类资源，表单负责写入，服务层负责契约。
import { onMounted, ref } from 'vue'
import {
  cancelAdminReservation, createCategory, createDish, createEmployee, createTable, deleteCategory, deleteDish, deleteTable,
  getAdminCategories, getAdminCategory, getAdminDish, getAdminDishes, getAdminReservationDetail, getAdminReservations, getAdminTable, getAdminTables, getEmployee, getEmployees,
  setCategoryEnabled, setDishStatus, setEmployeeEnabled, setTableEnabled, updateCategory, updateDish, updateTable,
} from '../services/admin-resources'
import type { DishCategoryData, DishData, EmployeeData, ReservationAdminData, TableVO } from '../types/api'

type Tab = 'tables' | 'categories' | 'dishes' | 'reservations' | 'employees'
const tab = ref<Tab>('tables')
const pageNo = ref(1); const total = ref(0); const isLoading = ref(false); const errorMessage = ref(''); const feedback = ref('')
const tables = ref<TableVO[]>([]); const categories = ref<DishCategoryData[]>([]); const dishes = ref<DishData[]>([]); const reservations = ref<ReservationAdminData[]>([]); const employees = ref<EmployeeData[]>([])
const editingId = ref<number | null>(null)
const tableForm = ref({ tableNo: '', capacity: 4, locationDesc: '' })
const categoryForm = ref({ name: '', sort: 0 })
const dishForm = ref({ categoryId: 1, name: '', description: '', price: 0, image: 'https://example.com/dish.jpg', status: 0 })
const employeeForm = ref({ phone: '', password: '', name: '' })

const statusText = (status: number) => ({ 0: '空闲', 1: '已预约', 2: '等待中', 3: '用餐中', 4: '禁用' } as Record<number, string>)[status] ?? '未知'
const reservationText = (status: number) => ({ 0: '待到店', 1: '已到店', 2: '已取消', 3: '已超时' } as Record<number, string>)[status] ?? '未知'
const dishText = (status: number) => ({ 0: '停售', 1: '启售', 2: '售罄' } as Record<number, string>)[status] ?? '未知'
const employeeText = (status: number) => ({ 1: '正常', 2: '禁用', 3: '离职' } as Record<number, string>)[status] ?? '未知'

async function load() {
  isLoading.value = true; errorMessage.value = ''
  try {
    if (tab.value === 'tables') { const r = await getAdminTables(pageNo.value); tables.value = r.records; total.value = r.total }
    if (tab.value === 'categories') { const r = await getAdminCategories(pageNo.value); categories.value = r.records; total.value = r.total }
    if (tab.value === 'dishes') { const r = await getAdminDishes(pageNo.value); dishes.value = r.records; total.value = r.total }
    if (tab.value === 'reservations') { const r = await getAdminReservations(pageNo.value); reservations.value = r.records; total.value = r.total }
    if (tab.value === 'employees') { const r = await getEmployees(pageNo.value); employees.value = r.records; total.value = r.total }
  } catch (e) { errorMessage.value = e instanceof Error ? e.message : '资料查询失败' } finally { isLoading.value = false }
}
function switchTab(next: Tab) { tab.value = next; pageNo.value = 1; editingId.value = null; load() }
function resetForms() { editingId.value = null; tableForm.value = { tableNo: '', capacity: 4, locationDesc: '' }; categoryForm.value = { name: '', sort: 0 }; dishForm.value = { categoryId: 1, name: '', description: '', price: 0, image: 'https://example.com/dish.jpg', status: 0 }; employeeForm.value = { phone: '', password: '', name: '' } }
async function saveTable() { try { editingId.value ? await updateTable(editingId.value, tableForm.value) : await createTable(tableForm.value); feedback.value = '桌位已保存'; resetForms(); await load() } catch (e) { errorMessage.value = e instanceof Error ? e.message : '桌位保存失败' } }
async function saveCategory() { try { editingId.value ? await updateCategory(editingId.value, categoryForm.value) : await createCategory(categoryForm.value); feedback.value = '分类已保存'; resetForms(); await load() } catch (e) { errorMessage.value = e instanceof Error ? e.message : '分类保存失败' } }
async function saveDish() { try { const { categoryId, name, description, price, image, status } = dishForm.value; if (editingId.value) await updateDish(editingId.value, { categoryId, name, description, price, image }); else await createDish({ categoryId, name, description, price, image, status }); feedback.value = '菜品已保存'; resetForms(); await load() } catch (e) { errorMessage.value = e instanceof Error ? e.message : '菜品保存失败' } }
async function saveEmployee() { try { await createEmployee(employeeForm.value); feedback.value = '员工已创建'; resetForms(); await load() } catch (e) { errorMessage.value = e instanceof Error ? e.message : '员工创建失败' } }
async function run(action: () => Promise<unknown>, message: string) { try { await action(); feedback.value = message; await load() } catch (e) { errorMessage.value = e instanceof Error ? e.message : '操作失败' } }
function editTable(item: TableVO) { editingId.value = item.tableId; tableForm.value = { tableNo: item.tableNo, capacity: item.capacity, locationDesc: item.locationDesc } }
function editCategory(item: DishCategoryData) { editingId.value = item.id; categoryForm.value = { name: item.name, sort: item.sort } }
function editDish(item: DishData) { editingId.value = item.id; dishForm.value = { categoryId: item.categoryId, name: item.name, description: item.description ?? '', price: item.price, image: item.image ?? '', status: item.status } }
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
