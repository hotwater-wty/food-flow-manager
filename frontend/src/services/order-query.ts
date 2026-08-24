// 顾客订单只负责读取列表和详情，创建订单由 order.ts 单独负责。
import type { OrderData, OrderDetailData, Result } from '../types/api'
import { http } from './http'

export async function getOrders(): Promise<OrderData[]> {
  // 列表接口是普通数组，不要套用管理端 PageResult 类型。
  const response = await http.get<Result<OrderData[]>>('/user/orders')
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '订单列表查询失败')
  return result.data
}

export async function getOrderDetail(orderId: number): Promise<OrderDetailData> {
  // 详情单独请求明细，避免列表接口携带不必要的数据。
  const response = await http.get<Result<OrderDetailData>>(`/user/orders/${orderId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '订单详情查询失败')
  return result.data
}
