// 创建订单服务：先取一次性令牌，再提交当前会话中的购物车明细。
import type { OrderCreateData, OrderCreateRequest, Result, SubmitTokenData } from '@foodflow/shared/types/api'
import { http } from './http'

async function getOrderSubmitToken(): Promise<SubmitTokenData> {
  // 一次性令牌必须在真正提交前获取，不能跨请求长期缓存。
  const response = await http.post<Result<SubmitTokenData>>('/user/submit-token', { scene: 'create-order' })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '获取订单提交令牌失败')
  return result.data
}

export async function createOrder(sessionId: number, request: OrderCreateRequest): Promise<OrderCreateData> {
  // await 保证令牌请求完成后才发送创建订单请求。
  const submitToken = await getOrderSubmitToken()
  const response = await http.post<Result<OrderCreateData>>(`/user/sessions/${sessionId}/orders`, request, {
    // 令牌放在请求头，不混入业务 JSON；后端消费后不能重复使用。
    headers: { 'X-Submit-Token': submitToken.token },
  })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '创建订单失败')
  return result.data
}
