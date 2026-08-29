import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElNotification } from 'element-plus'
import { toOperationError, type OperationError } from '../utils/operation-error'

export const useOperationFeedbackStore = defineStore('operation-feedback', () => {
  const detail = ref<OperationError | null>(null)
  const detailVisible = ref(false)

  function showSuccess(message: string) {
    ElNotification({ title: '操作成功', message, type: 'success', duration: 2600 })
  }

  function showWarning(message: string) {
    ElNotification({ title: '需要刷新', message, type: 'warning', duration: 5000 })
  }

  function showError(error: unknown) {
    const normalized = toOperationError(error)
    ElNotification({
      title: '操作异常',
      message: normalized.message,
      type: 'error',
      duration: 6000,
      onClick: () => {
        detail.value = normalized
        detailVisible.value = true
      },
    })
  }

  return { detail, detailVisible, showSuccess, showWarning, showError }
})
