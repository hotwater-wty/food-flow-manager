import { useOperationFeedbackStore } from '../stores/operation-feedback'

export function useAdminOperation() {
  const feedback = useOperationFeedbackStore()

  async function run(options: {
    execute: () => Promise<unknown>
    refresh: () => Promise<unknown>
    successMessage: string
  }) {
    try {
      await options.execute()
    } catch (error) {
      feedback.showError(error)
      return false
    }
    feedback.showSuccess(options.successMessage)
    try {
      await options.refresh()
    } catch (error) {
      feedback.showWarning('操作已成功，但数据刷新失败。请点击页面“刷新”重试。')
      console.warn('[管理端操作] 成功后的刷新失败', error)
    }
    return true
  }

  return { run }
}
