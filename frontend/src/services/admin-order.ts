// 管理订单服务：封装分页查询、详情和状态推进请求。
import type { AdminOrderData, OrderDetailData, PageResult, Result } from '../types/api'
import { http } from './http'

export async function getAdminOrders(pageNo = 1, status?: number): Promise<PageResult<AdminOrderData>> {
  const response = await http.get<Result<PageResult<AdminOrderData>>>('/admin/orders', { params: { pageNo, pageSize: 10, ...(status ? { status } : {}) } })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '管理订单查询失败')
  return result.data
}

export async function getAdminOrderDetail(orderId: number): Promise<OrderDetailData> {
  const response = await http.get<Result<OrderDetailData>>(`/admin/orders/${orderId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '管理订单详情查询失败')
  return result.data
}

export async function updateOrderStatus(orderId: number, status: number) {
  const response = await http.put<Result<{ orderId: number; orderNo: string; status: number }>>(`/admin/orders/${orderId}/status`, { status })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '订单状态更新失败')
  return result.data
}
