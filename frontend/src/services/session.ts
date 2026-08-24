import type { DiningSessionData, Result } from '../types/api'
import { http } from './http'

export async function getCurrentSession(): Promise<DiningSessionData | null> {
  const response = await http.get<Result<DiningSessionData>>('/user/sessions/current')
  const result = response.data
  if (result.code === 0 && result.data === null && result.msg === '当前用户没有用餐会话') return null
  if (result.code !== 1) throw new Error(result.msg || '当前会话查询失败')
  return result.data
}

export async function openSession(tableId: number): Promise<DiningSessionData> {
  const response = await http.post<Result<DiningSessionData>>(`/user/tables/${tableId}/sessions`)
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '开台失败')
  }
  return result.data
}
