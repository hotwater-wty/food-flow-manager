import axios from 'axios'
import type {
  ReservationCreateData,
  ReservationRequest,
  Result,
  SubmitTokenData,
} from '../types/api'
import { http } from './http'

async function getSubmitToken(scene: string): Promise<SubmitTokenData> {
  const response = await http.post<Result<SubmitTokenData>>('/user/submit-token', { scene })
  const result = response.data

  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '获取提交令牌失败')
  }

  return result.data
}

export async function createReservation(request: ReservationRequest): Promise<ReservationCreateData> {
  try {
    const submitToken = await getSubmitToken('create-reservation')
    const response = await http.post<Result<ReservationCreateData>>('/user/reservations', request, {
      headers: { 'X-Submit-Token': submitToken.token },
    })
    const result = response.data

    if (result.code !== 1 || result.data === null) {
      throw new Error(result.msg || '创建预约失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error && !axios.isAxiosError(error)) {
      throw error
    }
    throw new Error('预约提交失败，请检查后端服务或桌位状态')
  }
}
