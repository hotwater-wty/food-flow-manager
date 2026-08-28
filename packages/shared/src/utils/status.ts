// 状态码到展示文案的集中映射;枚举值以后端接口规范文档为准。
// 页面只调用这些纯函数,不在模板里散落魔法数字。

// ---- 顾客预约(0-3) ----
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
  // 操作规则集中在工具函数,模板只表达 v-if,不直接散落状态数字。
  return status === 0
}

// 预约状态对应的标签颜色语义(三期 R4 收敛):待到店橙、已到店主色、取消灰、超时红。
// Element Plus 标签直接可用;Vant 标签把 info 适配为 default(见顾客预约页)。
export type ReservationTagKind = 'primary' | 'warning' | 'info' | 'danger'

export function getReservationTagKind(status: number): ReservationTagKind {
  if (status === 1) return 'primary'
  if (status === 2) return 'info'
  if (status === 3) return 'danger'
  return 'warning'
}

// ---- 堂食会话(0-3) ----
const sessionStatusLabels: Record<number, string> = {
  0: '等待中',
  1: '用餐中',
  2: '已完成',
  3: '已取消',
}

export function getSessionStatusLabel(status: number) {
  return sessionStatusLabels[status] ?? '未知状态'
}

// ---- 桌位(0-4) ----
const tableStatusLabels: Record<number, string> = {
  0: '空闲',
  1: '已预约',
  2: '等待中',
  3: '用餐中',
  4: '禁用',
}

export function getTableStatusLabel(status: number) {
  return tableStatusLabels[status] ?? '未知状态'
}

// ---- 菜品(0-2) ----
const dishStatusLabels: Record<number, string> = {
  0: '停售',
  1: '启售',
  2: '售罄',
}

export function getDishStatusLabel(status: number) {
  return dishStatusLabels[status] ?? '未知状态'
}

// ---- 员工(状态 1-3,角色 1-2) ----
const employeeStatusLabels: Record<number, string> = {
  1: '正常',
  2: '禁用',
  3: '离职',
}

export function getEmployeeStatusLabel(status: number) {
  return employeeStatusLabels[status] ?? '未知状态'
}

export function getEmployeeRoleLabel(role: number) {
  return role === 2 ? '店长' : '店员'
}
