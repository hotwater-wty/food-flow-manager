const orderStatusLabels: Record<number, string> = {
  1: '已下单',
  2: '制作中',
  3: '已上齐',
  4: '已完成',
  5: '已取消',
}

export function getOrderStatusLabel(status: number) {
  return orderStatusLabels[status] ?? '未知状态'
}
