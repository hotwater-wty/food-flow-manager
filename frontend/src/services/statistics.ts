// 经营统计服务:接入三期新增的只读统计端点,数据口径见 05 契约文档
// (服务器时区当日、不含已取消订单、金额为整数分)。
import type { Result, StatisticsOverview } from '../types/api'
import { http } from './http'

export async function getStatisticsOverview(): Promise<StatisticsOverview> {
  const response = await http.get<Result<StatisticsOverview>>('/admin/statistics/overview')
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '经营统计查询失败')
  return result.data
}
