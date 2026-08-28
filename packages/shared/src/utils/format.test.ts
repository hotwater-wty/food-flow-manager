import { describe, expect, it } from 'vitest'

import { formatDateTime, formatPrice } from './format'

describe('formatDateTime', () => {
  it('把 ISO 的 T 分隔符换成空格', () => {
    expect(formatDateTime('2026-08-28T10:54:29')).toBe('2026-08-28 10:54:29')
  })

  it('空值返回空字符串,不产生 undefined 字样', () => {
    expect(formatDateTime(null)).toBe('')
    expect(formatDateTime(undefined)).toBe('')
    expect(formatDateTime('')).toBe('')
  })
})

describe('formatPrice', () => {
  it('整数分转两位小数的元', () => {
    expect(formatPrice(1234)).toBe('¥12.34')
    expect(formatPrice(5)).toBe('¥0.05')
    expect(formatPrice(0)).toBe('¥0.00')
  })

  it('不受浮点误差影响(0.1+0.2 类场景)', () => {
    // 145 分 = 1.45 元;若用浮点累加可能出现 1.4500000000000002。
    expect(formatPrice(145)).toBe('¥1.45')
    expect(formatPrice(3702)).toBe('¥37.02')
  })
})
