import type { DiningSessionCloseData, DiningSessionData, PageResult, Result } from '../types/api'
import { http } from './http'

export async function getAdminSessions(pageNo = 1, status?: number): Promise<PageResult<DiningSessionData>> {
  const response = await http.get<Result<PageResult<DiningSessionData>>>('/admin/sessions', { params: { pageNo, pageSize: 10, ...(status ? { status } : {}) } })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '会话列表查询失败')
  return result.data
}

export async function cancelAdminSession(sessionId: number): Promise<DiningSessionCloseData> {
  const response = await http.post<Result<DiningSessionCloseData>>(`/admin/sessions/${sessionId}/cancel`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '取消会话失败')
  return result.data
}

export async function closeAdminSession(sessionId: number): Promise<DiningSessionCloseData> {
  const response = await http.post<Result<DiningSessionCloseData>>(`/admin/sessions/${sessionId}/close`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '清台失败')
  return result.data
}
