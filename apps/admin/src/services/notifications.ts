import type { Result } from '@foodflow/shared/types/api'
import { http } from './http'

export async function getNotificationTicket(): Promise<string> {
  const response = await http.post<Result<{ ticket: string }>>('/admin/notifications/ticket')
  const result = response.data
  if (result.code !== 1 || !result.data?.ticket) throw new Error(result.msg || '通知连接建立失败')
  return result.data.ticket
}

export function getNotificationStreamUrl(ticket: string): string {
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${base.replace(/\/$/, '')}/admin/notifications/stream?ticket=${encodeURIComponent(ticket)}`
}
