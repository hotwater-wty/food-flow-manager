// 顾客预约状态映射和可执行操作规则。
const reservationStatusLabels: Record<number, string> = {
  0: '待到店',
  1: '已到店',
  2: '已取消',
  3: '已超时',
}

export function getReservationStatusLabel(status: number) {
  // Record<number, string> 明确表示状态码到展示文案的查表关系。
  return reservationStatusLabels[status] ?? '未知状态'
}

export function canCancelReservation(status: number) {
  // 操作规则集中在工具函数，模板只表达 v-if，不直接散落状态数字。
  return status === 0
}
