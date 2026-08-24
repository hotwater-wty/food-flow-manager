// 将订单状态码集中转换为页面文案，避免模板散落魔法数字。
const orderStatusLabels: Record<number, string> = {
  1: '已下单',
  2: '制作中',
  3: '已上齐',
  4: '已完成',
  5: '已取消',
}

export function getOrderStatusLabel(status: number) {
  // ?? 只在映射结果为 null/undefined 时使用兜底文案，0 等有效数字不会误判。
  // 函数不修改状态表，只把后端数字转换为模板可读文本。
  return orderStatusLabels[status] ?? '未知状态'
}
