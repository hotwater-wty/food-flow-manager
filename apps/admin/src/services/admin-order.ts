// 管理订单服务：封装分页查询、详情和状态推进请求。
import type { AdminOrderData, OrderDetailData, PageResult, Result } from '@foodflow/shared/types/api'
import { http } from './http'
import { unwrapMutation, unwrapQuery } from '../utils/operation-error'

export async function getAdminOrders(pageNo = 1, status?: number): Promise<PageResult<AdminOrderData>> {
  // 可选参数用对象展开拼接；status 未传时不会发送筛选条件。
  return unwrapQuery(
    http.get<Result<PageResult<AdminOrderData>>>('/admin/orders', {
      params: { pageNo, pageSize: 10, ...(status ? { status } : {}) },
    }),
    '管理订单查询失败',
  )
}

export async function getAdminOrderDetail(orderId: number): Promise<OrderDetailData> {
  // 详情请求使用路径参数，返回明细数组供工作台展开显示。
  return unwrapQuery(http.get<Result<OrderDetailData>>(`/admin/orders/${orderId}`), '管理订单详情查询失败')
}

export async function updateOrderStatus(orderId: number, status: number) {
  // 状态推进由后端状态机最终校验，前端只提交用户选择的目标状态。
  return unwrapMutation(
    http.put<Result<{ orderId: number; orderNo: string; status: number }>>(`/admin/orders/${orderId}/status`, {
      status,
    }),
    '订单状态更新失败',
  )
}
