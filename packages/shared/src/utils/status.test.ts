import { describe, expect, it } from 'vitest'

import { getOrderStatusLabel, getOrderTagKind } from './order-status'
import {
  canCancelReservation,
  getEmployeeRoleLabel,
  getEmployeeStatusLabel,
  getReservationStatusLabel,
  getReservationTagKind,
  getSessionStatusLabel,
  getTableStatusLabel,
  getDishStatusLabel,
} from './status'

describe('order-status 映射', () => {
  it('五个状态文案与契约一致', () => {
    expect(getOrderStatusLabel(1)).toBe('已下单')
    expect(getOrderStatusLabel(2)).toBe('制作中')
    expect(getOrderStatusLabel(3)).toBe('已上齐')
    expect(getOrderStatusLabel(4)).toBe('已完成')
    expect(getOrderStatusLabel(5)).toBe('已取消')
  })

  it('未知状态回退到"未知状态"', () => {
    expect(getOrderStatusLabel(99)).toBe('未知状态')
  })

  it('标签色语义:终态绿、取消灰、进行中黄、初始主色', () => {
    expect(getOrderTagKind(1)).toBe('primary')
    expect(getOrderTagKind(2)).toBe('warning')
    expect(getOrderTagKind(3)).toBe('warning')
    expect(getOrderTagKind(4)).toBe('success')
    expect(getOrderTagKind(5)).toBe('info')
  })
})

describe('status 映射', () => {
  it('预约状态与可取消规则', () => {
    expect(getReservationStatusLabel(0)).toBe('待到店')
    expect(getReservationStatusLabel(3)).toBe('已超时')
    expect(canCancelReservation(0)).toBe(true)
    expect(canCancelReservation(1)).toBe(false)
    // 标签色:待到店橙、已到店主色、取消灰、超时红
    expect(getReservationTagKind(0)).toBe('warning')
    expect(getReservationTagKind(1)).toBe('primary')
    expect(getReservationTagKind(2)).toBe('info')
    expect(getReservationTagKind(3)).toBe('danger')
  })

  it('会话/桌位/菜品/员工文案抽样', () => {
    expect(getSessionStatusLabel(0)).toBe('等待中')
    expect(getSessionStatusLabel(1)).toBe('用餐中')
    expect(getTableStatusLabel(0)).toBe('空闲')
    expect(getTableStatusLabel(4)).toBe('禁用')
    expect(getDishStatusLabel(1)).toBe('启售')
    expect(getDishStatusLabel(2)).toBe('售罄')
    expect(getEmployeeRoleLabel(2)).toBe('店长')
    expect(getEmployeeRoleLabel(1)).toBe('店员')
    expect(getEmployeeStatusLabel(2)).toBe('禁用')
  })
})
