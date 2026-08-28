// 时间展示工具:后端契约返回带 T 分隔的 ISO 字符串(如 2026-08-27T20:06:09),
// 界面统一换成空格,保证各页面时间口径一致。
export const formatDateTime = (value: string | null | undefined): string =>
  value ? value.replace('T', ' ') : ''

// 金额展示工具:后端契约以整数分记账(避免浮点误差),界面统一转换为两位小数的"¥"字符串。
// 转换只发生在展示边界,业务数据全程保持分。
export const formatPrice = (cents: number): string => `¥${(cents / 100).toFixed(2)}`
