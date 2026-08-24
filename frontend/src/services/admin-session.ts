// 管理会话服务：查询会话，并执行取消等待或清台写操作。
import type { DiningSessionCloseData, DiningSessionData, PageResult, Result } from '../types/api'
import { http } from './http'

export async function getAdminSessions(pageNo = 1, status?: number): Promise<PageResult<DiningSessionData>> {
  // 管理端会话列表使用 PageResult，而顾客端当前会话是单个对象或 null。
  const response = await http.get<Result<PageResult<DiningSessionData>>>('/admin/sessions', { params: { pageNo, pageSize: 10, ...(status ? { status } : {}) } })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '会话列表查询失败')
  return result.data
}

export async function getAdminSessionDetail(sessionId: number): Promise<DiningSessionData> {
  // 详情用于确认某个会话的桌位和状态，不改变服务端数据。
  const response = await http.get<Result<DiningSessionData>>(`/admin/sessions/${sessionId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '会话详情查询失败')
  return result.data
}

export async function cancelAdminSession(sessionId: number): Promise<DiningSessionCloseData> {
  // 取消等待是写操作，成功响应包含关闭后的会话和桌位状态。
  const response = await http.post<Result<DiningSessionCloseData>>(`/admin/sessions/${sessionId}/cancel`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '取消会话失败')
  return result.data
}

export async function closeAdminSession(sessionId: number): Promise<DiningSessionCloseData> {
  // 清台会结束会话并释放桌位，页面成功后需要重新加载列表。
  const response = await http.post<Result<DiningSessionCloseData>>(`/admin/sessions/${sessionId}/close`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '清台失败')
  return result.data
}
