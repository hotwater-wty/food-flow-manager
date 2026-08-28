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

// 订单状态对应的标签颜色语义(三期 R4 收敛):进行中黄色系、完成绿色、取消灰。
// Element Plus 标签直接可用;Vant 标签没有 info 色,由视图把 info 适配为 default。
export type OrderTagKind = 'primary' | 'warning' | 'success' | 'info'

export function getOrderTagKind(status: number): OrderTagKind {
  if (status === 4) return 'success'
  if (status === 5) return 'info'
  if (status === 2 || status === 3) return 'warning'
  return 'primary'
}
