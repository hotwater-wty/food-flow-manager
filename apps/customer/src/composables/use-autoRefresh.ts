// 数据自动刷新组合式函数(三期 R3):定时轮询 + 标签页不可见暂停 + 窗口聚焦刷新。
// 三种触发统一走调用方的 load 并标记 silent,由调用方决定静默刷新的展示方式;
// 管理端工作台开启轮询,顾客端页面只用聚焦刷新(polling: false)。
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

// 轮询间隔集中定义;后端 V1/V2 冻结扩张,前端用轻量轮询代替推送。
export const AUTO_REFRESH_INTERVAL_MS = 20000

export type LoadFn = (options?: { silent?: boolean }) => Promise<unknown> | void

export function useAutoRefresh(load: LoadFn, options: { polling?: boolean; intervalMs?: number } = {}) {
  const intervalMs = options.intervalMs ?? AUTO_REFRESH_INTERVAL_MS
  const pollingEnabled = options.polling ?? true

  // 开关绑定到工具栏的 el-switch;watch 里同步启停定时器。
  const autoRefresh = ref(pollingEnabled)

  // 节流:切回标签页会几乎同时触发 focus 与 visibilitychange,2 秒内只刷新一次。
  let lastTriggeredAt = 0
  let timer: number | null = null

  function refreshSilently() {
    const now = Date.now()
    if (now - lastTriggeredAt < 2000) return
    lastTriggeredAt = now
    void load({ silent: true })
  }

  // 定时器照常运转,后台标签页到点直接跳过,恢复可见后由可见性监听立即刷新。
  function tick() {
    if (!document.hidden && autoRefresh.value) refreshSilently()
  }

  function startPolling() {
    if (pollingEnabled && timer === null) timer = window.setInterval(tick, intervalMs)
  }

  function stopPolling() {
    if (timer !== null) {
      window.clearInterval(timer)
      timer = null
    }
  }

  function handleVisibilityChange() {
    if (!document.hidden) refreshSilently()
  }

  function handleWindowFocus() {
    refreshSilently()
  }

  watch(autoRefresh, (enabled) => (enabled ? startPolling() : stopPolling()))

  onMounted(() => {
    startPolling()
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('focus', handleWindowFocus)
  })

  // 卸载时必须清理定时器和事件监听,否则组件销毁后仍会发起请求。
  onBeforeUnmount(() => {
    stopPolling()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    window.removeEventListener('focus', handleWindowFocus)
  })

  return { autoRefresh }
}
