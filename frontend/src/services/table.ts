// 顾客桌位服务：只读取后端判定为空闲、可预约或可开台的桌位。
import type { Result, TableVO } from '../types/api'
import { http } from './http'

export async function getAvailableTables(): Promise<TableVO[]> {
  const response = await http.get<Result<TableVO[]>>('/user/tables')
  const result = response.data

  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '桌位查询失败')
  }

  return result.data
}
