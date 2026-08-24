// 顾客会话服务：区分当前会话查询、非预约开台和预约到店开台。
import type { DiningSessionData, Result } from '../types/api'
import { http } from './http'

export async function getCurrentSession(): Promise<DiningSessionData | null> {
  // “没有会话”是页面正常空状态，服务层把特定业务响应转换成 null。
  const response = await http.get<Result<DiningSessionData>>('/user/sessions/current')
  const result = response.data
  if (result.code === 0 && result.data === null && result.msg === '当前用户没有用餐会话') return null
  if (result.code !== 1) throw new Error(result.msg || '当前会话查询失败')
  return result.data
}

export async function openSession(tableId: number): Promise<DiningSessionData> {
  // 模拟扫码只提供 tableId；会话编号和状态完全由后端生成。
  const response = await http.post<Result<DiningSessionData>>(`/user/tables/${tableId}/sessions`)
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '开台失败')
  }
  return result.data
}

export async function checkInReservation(reservationId: number, tableId: number): Promise<DiningSessionData> {
  // 预约到店必须同时携带预约 ID 和预约绑定的桌位 ID。
  const response = await http.post<Result<DiningSessionData>>(`/user/reservations/${reservationId}/check-in/${tableId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '预约到店开台失败')
  }
  return result.data
}
