// 时间展示工具:后端契约返回带 T 分隔的 ISO 字符串(如 2026-08-27T20:06:09),
// 界面统一换成空格,保证各页面时间口径一致。
export const formatDateTime = (value: string | null | undefined): string =>
  value ? value.replace('T', ' ') : ''
