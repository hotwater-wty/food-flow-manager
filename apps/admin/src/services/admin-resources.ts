// 第二批资料服务：把桌位、分类、菜品、预约和员工的 HTTP 契约集中管理。
import type {
  DishCategoryData,
  DishCategoryRequest,
  DishData,
  DishCreateRequest,
  DishUpdateRequest,
  EmployeeCreateRequest,
  EmployeeData,
  PageResult,
  ReservationAdminData,
  Result,
  TableRequest,
  TableVO,
} from '@foodflow/shared/types/api'
// 桌位维护页把请求/实体类型从服务层一起引入,这里做类型再导出。
export type { TableRequest, TableVO }
import { http } from './http'
import { unwrapMutation, unwrapQuery } from '../utils/operation-error'

// 下面的箭头函数只是把固定路径和参数绑定到通用 unwrap 上，调用方得到的是 Promise<T>。
// 每个箭头函数只绑定一个资源的 URL 和返回类型，真正的 Result 判定复用 unwrap。
// 默认参数 pageNo = 1 让首次加载不必显式传页码。
export const getAdminTables = (pageNo = 1) =>
  unwrapQuery(
    http.get<Result<PageResult<TableVO>>>('/admin/tables', { params: { pageNo, pageSize: 10 } }),
    '桌位查询失败',
  )
export const getAdminTable = (id: number) =>
  unwrapQuery(http.get<Result<TableVO>>(`/admin/tables/${id}`), '桌位详情查询失败')
export const createTable = (data: TableRequest) =>
  unwrapMutation(http.post<Result<null>>('/admin/tables', data), '新增桌位失败')
export const updateTable = (id: number, data: TableRequest) =>
  unwrapMutation(http.put<Result<null>>(`/admin/tables/${id}`, data), '修改桌位失败')
export const deleteTable = (id: number) =>
  unwrapMutation(http.delete<Result<null>>(`/admin/tables/${id}`), '删除桌位失败')
export const setTableEnabled = (id: number, enabled: boolean) =>
  unwrapMutation(http.post<Result<null>>(`/admin/tables/${id}/${enabled ? 'enable' : 'disable'}`), '桌位状态更新失败')

// 分类、菜品、预约和员工沿用相同的分页与 CRUD 形状；URL 不同，服务层类型不同。
export const getAdminCategories = (pageNo = 1, pageSize = 10) =>
  unwrapQuery(
    http.get<Result<PageResult<DishCategoryData>>>('/admin/dish-categories', { params: { pageNo, pageSize } }),
    '分类查询失败',
  )
export const getAdminCategory = (id: number) =>
  unwrapQuery(http.get<Result<DishCategoryData>>(`/admin/dish-categories/${id}`), '分类详情查询失败')
export const createCategory = (data: DishCategoryRequest) =>
  unwrapMutation(http.post<Result<DishCategoryData>>('/admin/dish-categories', data), '新增分类失败')
export const updateCategory = (id: number, data: DishCategoryRequest) =>
  unwrapMutation(http.put<Result<DishCategoryData>>(`/admin/dish-categories/${id}`, data), '修改分类失败')
export const deleteCategory = (id: number) =>
  unwrapMutation(http.delete<Result<null>>(`/admin/dish-categories/${id}`), '删除分类失败')
export const setCategoryEnabled = (id: number, enabled: boolean) =>
  unwrapMutation(
    http.post<Result<null>>(`/admin/dish-categories/${id}/${enabled ? 'enable' : 'disable'}`),
    '分类状态更新失败',
  )

export const getAdminDishes = (pageNo = 1) =>
  unwrapQuery(
    http.get<Result<PageResult<DishData>>>('/admin/dishes', { params: { pageNo, pageSize: 10 } }),
    '菜品查询失败',
  )
export const getAdminDish = (id: number) =>
  unwrapQuery(http.get<Result<DishData>>(`/admin/dishes/${id}`), '菜品详情查询失败')
export const createDish = (data: DishCreateRequest) =>
  unwrapMutation(http.post<Result<DishData>>('/admin/dishes', data), '新增菜品失败')
export const updateDish = (id: number, data: DishUpdateRequest) =>
  unwrapMutation(http.put<Result<DishData>>(`/admin/dishes/${id}`, data), '修改菜品失败')
export const deleteDish = (id: number) =>
  unwrapMutation(http.delete<Result<null>>(`/admin/dishes/${id}`), '删除菜品失败')
export const setDishStatus = (id: number, status: number) =>
  unwrapMutation(
    http.post<Result<null>>(`/admin/dishes/${id}/status`, undefined, { params: { status } }),
    '菜品状态更新失败',
  )

export const getAdminReservations = (pageNo = 1) =>
  unwrapQuery(
    http.get<Result<PageResult<ReservationAdminData>>>('/admin/reservations', { params: { pageNo, pageSize: 10 } }),
    '预约查询失败',
  )
export const getAdminReservationDetail = (id: number) =>
  unwrapQuery(http.get<Result<ReservationAdminData>>(`/admin/reservations/${id}/detail`), '预约详情查询失败')
export const cancelAdminReservation = (id: number) =>
  unwrapMutation(http.post<Result<null>>(`/admin/reservations/${id}/cancel`), '取消预约失败')

export const getEmployees = (pageNo = 1) =>
  unwrapQuery(
    http.get<Result<PageResult<EmployeeData>>>('/admin/employees', { params: { pageNo, pageSize: 10 } }),
    '员工查询失败',
  )
export const getEmployee = (id: number) =>
  unwrapQuery(http.get<Result<EmployeeData>>(`/admin/employees/${id}`), '员工详情查询失败')
export const createEmployee = (data: EmployeeCreateRequest) =>
  unwrapMutation(http.post<Result<EmployeeData>>('/admin/employees', data), '新增员工失败')
export const setEmployeeEnabled = (id: number, enabled: boolean) =>
  unwrapMutation(
    http.post<Result<null>>(`/admin/employees/${id}/${enabled ? 'enable' : 'disable'}`),
    '员工状态更新失败',
  )
