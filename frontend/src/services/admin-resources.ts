import type {
  DishCategoryData, DishCategoryRequest, DishData, DishCreateRequest, DishUpdateRequest,
  EmployeeCreateRequest, EmployeeData, PageResult, ReservationAdminData, Result, TableRequest, TableVO,
} from '../types/api'
import { http } from './http'

async function unwrap<T>(request: Promise<{ data: Result<T> }>, message: string): Promise<T> {
  const result = (await request).data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || message)
  return result.data
}

export const getAdminTables = (pageNo = 1) => unwrap(http.get<Result<PageResult<TableVO>>>('/admin/tables', { params: { pageNo, pageSize: 10 } }), '桌位查询失败')
export const createTable = (data: TableRequest) => unwrap(http.post<Result<null>>('/admin/tables', data), '新增桌位失败')
export const updateTable = (id: number, data: TableRequest) => unwrap(http.put<Result<null>>(`/admin/tables/${id}`, data), '修改桌位失败')
export const deleteTable = (id: number) => unwrap(http.delete<Result<null>>(`/admin/tables/${id}`), '删除桌位失败')
export const setTableEnabled = (id: number, enabled: boolean) => unwrap(http.post<Result<null>>(`/admin/tables/${id}/${enabled ? 'enable' : 'disable'}`), '桌位状态更新失败')

export const getAdminCategories = (pageNo = 1) => unwrap(http.get<Result<PageResult<DishCategoryData>>>('/admin/dish-categories', { params: { pageNo, pageSize: 10 } }), '分类查询失败')
export const createCategory = (data: DishCategoryRequest) => unwrap(http.post<Result<DishCategoryData>>('/admin/dish-categories', data), '新增分类失败')
export const updateCategory = (id: number, data: DishCategoryRequest) => unwrap(http.put<Result<DishCategoryData>>(`/admin/dish-categories/${id}`, data), '修改分类失败')
export const deleteCategory = (id: number) => unwrap(http.delete<Result<null>>(`/admin/dish-categories/${id}`), '删除分类失败')
export const setCategoryEnabled = (id: number, enabled: boolean) => unwrap(http.post<Result<null>>(`/admin/dish-categories/${id}/${enabled ? 'enable' : 'disable'}`), '分类状态更新失败')

export const getAdminDishes = (pageNo = 1) => unwrap(http.get<Result<PageResult<DishData>>>('/admin/dishes', { params: { pageNo, pageSize: 10 } }), '菜品查询失败')
export const createDish = (data: DishCreateRequest) => unwrap(http.post<Result<DishData>>('/admin/dishes', data), '新增菜品失败')
export const updateDish = (id: number, data: DishUpdateRequest) => unwrap(http.put<Result<DishData>>(`/admin/dishes/${id}`, data), '修改菜品失败')
export const deleteDish = (id: number) => unwrap(http.delete<Result<null>>(`/admin/dishes/${id}`), '删除菜品失败')
export const setDishStatus = (id: number, status: number) => unwrap(http.post<Result<null>>(`/admin/dishes/${id}/status`, undefined, { params: { status } }), '菜品状态更新失败')

export const getAdminReservations = (pageNo = 1) => unwrap(http.get<Result<PageResult<ReservationAdminData>>>('/admin/reservations', { params: { pageNo, pageSize: 10 } }), '预约查询失败')
export const getAdminReservationDetail = (id: number) => unwrap(http.get<Result<ReservationAdminData>>(`/admin/reservations/${id}/detail`), '预约详情查询失败')
export const cancelAdminReservation = (id: number) => unwrap(http.post<Result<null>>(`/admin/reservations/${id}/cancel`), '取消预约失败')

export const getEmployees = (pageNo = 1) => unwrap(http.get<Result<PageResult<EmployeeData>>>('/admin/employees', { params: { pageNo, pageSize: 10 } }), '员工查询失败')
export const createEmployee = (data: EmployeeCreateRequest) => unwrap(http.post<Result<EmployeeData>>('/admin/employees', data), '新增员工失败')
export const setEmployeeEnabled = (id: number, enabled: boolean) => unwrap(http.post<Result<null>>(`/admin/employees/${id}/${enabled ? 'enable' : 'disable'}`), '员工状态更新失败')
