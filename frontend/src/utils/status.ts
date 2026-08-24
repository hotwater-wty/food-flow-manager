const reservationStatusLabels: Record<number, string> = {
  0: '待到店',
  1: '已到店',
  2: '已取消',
  3: '已超时',
}

export function getReservationStatusLabel(status: number) {
  return reservationStatusLabels[status] ?? '未知状态'
}

export function canCancelReservation(status: number) {
  return status === 0
}
