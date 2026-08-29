// 管理会话服务：查询会话，并执行取消等待或清台写操作。
import type { DiningSessionCloseData, DiningSessionData, PageResult, Result } from '@foodflow/shared/types/api'
import { http } from './http'
import { unwrapMutation, unwrapQuery } from '../utils/operation-error'

export async function getAdminSessions(pageNo = 1, status?: number): Promise<PageResult<DiningSessionData>> {
  // 管理端会话列表使用 PageResult，而顾客端当前会话是单个对象或 null。
  // status 支持后端的会话状态筛选(0-3);未传时不携带该参数,后端返回全部状态。
  return unwrapQuery(
    http.get<Result<PageResult<DiningSessionData>>>('/admin/sessions', {
      params: { pageNo, pageSize: 10, ...(status !== undefined ? { status } : {}) },
    }),
    '会话列表查询失败',
  )
}

export async function getAdminSessionDetail(sessionId: number): Promise<DiningSessionData> {
  // 详情用于确认某个会话的桌位和状态，不改变服务端数据。
  return unwrapQuery(http.get<Result<DiningSessionData>>(`/admin/sessions/${sessionId}`), '会话详情查询失败')
}

export async function cancelAdminSession(sessionId: number): Promise<DiningSessionCloseData | null> {
  // 取消等待是写操作，成功响应包含关闭后的会话和桌位状态。
  return unwrapMutation(
    http.post<Result<DiningSessionCloseData>>(`/admin/sessions/${sessionId}/cancel`),
    '取消会话失败',
  )
}

export async function closeAdminSession(sessionId: number): Promise<DiningSessionCloseData | null> {
  // 清台会结束会话并释放桌位，页面成功后需要重新加载列表。
  return unwrapMutation(http.post<Result<DiningSessionCloseData>>(`/admin/sessions/${sessionId}/close`), '清台失败')
}
