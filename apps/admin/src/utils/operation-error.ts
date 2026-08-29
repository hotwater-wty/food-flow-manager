import axios from 'axios'
import type { Result } from '@foodflow/shared/types/api'

export type OperationErrorKind = 'business' | 'http' | 'network'

export interface OperationErrorDetail {
  kind: OperationErrorKind
  errorCode: string
  message: string
  occurredAt: string
  httpStatus?: number
  request?: { method?: string; url?: string }
}

export class OperationError extends Error {
  readonly detail: OperationErrorDetail

  constructor(detail: Omit<OperationErrorDetail, 'occurredAt'>) {
    super(detail.message)
    this.detail = { ...detail, occurredAt: new Date().toISOString() }
    this.name = 'OperationError'
  }
}

type ApiResponse<T> = {
  data: Result<T>
  config?: { method?: string; url?: string }
}

function responseRequest(response: ApiResponse<unknown>) {
  return { method: response.config?.method?.toUpperCase(), url: response.config?.url }
}

export function toOperationError(error: unknown): OperationError {
  if (error instanceof OperationError) return error
  if (axios.isAxiosError(error)) {
    const httpStatus = error.response?.status
    const request = {
      method: error.config?.method?.toUpperCase(),
      url: error.config?.url,
    }
    if (httpStatus) {
      const response = error.response?.data as Partial<Result<unknown>> | undefined
      return new OperationError({
        kind: 'http',
        errorCode: response?.errorCode ?? `HTTP_${httpStatus}`,
        httpStatus,
        message: response?.msg || `请求失败（HTTP ${httpStatus}）`,
        request,
      })
    }
    return new OperationError({
      kind: 'network',
      errorCode: error.code === 'ECONNABORTED' ? 'NETWORK_TIMEOUT' : 'NETWORK_UNAVAILABLE',
      message: error.code === 'ECONNABORTED' ? '请求超时，请检查网络后重试' : '网络异常，请检查连接后重试',
      request,
    })
  }
  return new OperationError({
    kind: 'network',
    errorCode: 'UNKNOWN_ERROR',
    message: error instanceof Error ? error.message : '操作异常，请稍后重试',
  })
}

export async function unwrapQuery<T>(request: Promise<ApiResponse<T>>, fallback: string): Promise<T> {
  const response = await request
  const result = response.data
  if (result.code !== 1) {
    throw new OperationError({
      kind: 'business',
      errorCode: result.errorCode ?? 'BUSINESS_ERROR',
      message: result.msg || fallback,
      request: responseRequest(response),
    })
  }
  if (result.data === null) {
    throw new OperationError({
      kind: 'business',
      errorCode: 'EMPTY_RESPONSE',
      message: fallback,
      request: responseRequest(response),
    })
  }
  return result.data
}

export async function unwrapMutation<T>(request: Promise<ApiResponse<T>>, fallback: string): Promise<T | null> {
  const response = await request
  const result = response.data
  if (result.code !== 1) {
    throw new OperationError({
      kind: 'business',
      errorCode: result.errorCode ?? 'BUSINESS_ERROR',
      message: result.msg || fallback,
      request: responseRequest(response),
    })
  }
  return result.data
}
