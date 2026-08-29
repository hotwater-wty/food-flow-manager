// 可复用的分页列表状态机:管理端各资源页共用"加载锁+错误信息+页码+总数"的相同逻辑。
// fetchPage 由调用方以闭包提供,组合式函数不关心背后是哪个接口、带什么筛选条件。
import { ref } from 'vue'
import type { Ref } from 'vue'
import type { PageResult } from '@foodflow/shared/types/api'

export function usePagedList<T>(fetchPage: (pageNo: number) => Promise<PageResult<T>>) {
  // T 是列表元素类型;调用方传入 TableVO、DishData 等即得到对应类型的列表。
  const records: Ref<T[]> = ref([])
  const pageNo: Ref<number> = ref(1)
  const total: Ref<number> = ref(0)
  const loading: Ref<boolean> = ref(true)
  const errorMessage: Ref<string> = ref('')
  let pendingLoad: Promise<void> | null = null

  // silent 表示由自动刷新/聚焦刷新触发的静默加载:不点亮加载遮罩,失败也只记录不惊扰用户。
  async function load(options?: { silent?: boolean; rethrow?: boolean }) {
    if (pendingLoad) {
      // SSE/聚焦触发的静默刷新不应把在途写后刷新的异常变成未处理 Promise；
      // 写操作本身携带 rethrow，仍会在自己的调用链中得到失败结果并提示用户。
      if (!options?.rethrow) return pendingLoad.catch(() => undefined)
      await pendingLoad
      return load(options)
    }

    pendingLoad = (async () => {
      if (!options?.silent) {
        loading.value = true
        errorMessage.value = ''
      }
      try {
        const result = await fetchPage(pageNo.value)
        records.value = result.records
        total.value = result.total
      } catch (error) {
        // 静默刷新失败保留旧数据；写操作的刷新需要把失败继续交给统一反馈模块。
        if (options?.silent) {
          console.warn('[usePagedList] 静默刷新失败', error)
        } else {
          errorMessage.value = error instanceof Error ? error.message : '查询失败'
        }
        if (options?.rethrow) throw error
      } finally {
        if (!options?.silent) loading.value = false
      }
    })()

    try {
      await pendingLoad
    } finally {
      pendingLoad = null
    }
  }

  // 分页组件回调:同步页码后复用同一个 load。
  function handlePageChange(page: number) {
    pageNo.value = page
    void load()
  }

  // 筛选条件变化时先回到第一页再查询,避免停留在不存在的高页码。
  function reloadFromFirstPage() {
    pageNo.value = 1
    void load()
  }

  return { records, pageNo, total, loading, errorMessage, load, handlePageChange, reloadFromFirstPage }
}
