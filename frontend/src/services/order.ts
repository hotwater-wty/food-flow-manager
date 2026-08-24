// 创建订单服务：先取一次性令牌，再提交当前会话中的购物车明细。
import type { OrderCreateData, OrderCreateRequest, Result, SubmitTokenData } from '../types/api'
import { http } from './http'

async function getOrderSubmitToken(): Promise<SubmitTokenData> {
  const response = await http.post<Result<SubmitTokenData>>('/user/submit-token', { scene: 'create-order' })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '获取订单提交令牌失败')
  return result.data
}

export async function createOrder(sessionId: number, request: OrderCreateRequest): Promise<OrderCreateData> {
  const submitToken = await getOrderSubmitToken()
  const response = await http.post<Result<OrderCreateData>>(`/user/sessions/${sessionId}/orders`, request, {
    headers: { 'X-Submit-Token': submitToken.token },
  })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '创建订单失败')
  return result.data
}
