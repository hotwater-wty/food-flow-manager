// 全局界面 Store 预留位置；当前业务认证状态分别维护在专用 Store 中。
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    appName: '膳畅管家',
  }),
})
