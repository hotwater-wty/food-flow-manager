import type { OrderData, OrderDetailData, Result } from '../types/api'
import { http } from './http'

export async function getOrders(): Promise<OrderData[]> {
  const response = await http.get<Result<OrderData[]>>('/user/orders')
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '订单列表查询失败')
  return result.data
}

export async function getOrderDetail(orderId: number): Promise<OrderDetailData> {
  const response = await http.get<Result<OrderDetailData>>(`/user/orders/${orderId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '订单详情查询失败')
  return result.data
}
