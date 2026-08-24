// 全局界面 Store 预留位置；当前业务认证状态分别维护在专用 Store 中。
import { defineStore } from 'pinia'

// defineStore 返回一个可在组件中调用的 useAppStore 函数。
export const useAppStore = defineStore('app', {
  state: () => ({
    // state 用函数返回对象，Pinia 可据此为每个 Store 实例创建独立初始状态。
    appName: '膳畅管家',
  }),
})
