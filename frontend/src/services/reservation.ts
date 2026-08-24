import axios from 'axios'
import type {
  ReservationCreateData,
  ReservationData,
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

export async function getReservations(): Promise<ReservationData[]> {
  const response = await http.get<Result<ReservationData[]>>('/user/reservations')
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '预约列表查询失败')
  }
  return result.data
}

export async function getReservationDetail(reservationId: number): Promise<ReservationData> {
  const response = await http.get<Result<ReservationData>>(`/user/reservations/${reservationId}/detail`)
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '预约详情查询失败')
  }
  return result.data
}

export async function cancelReservation(reservationId: number): Promise<void> {
  const response = await http.post<Result<null>>(`/user/reservations/${reservationId}/cancel`)
  const result = response.data
  if (result.code !== 1) {
    throw new Error(result.msg || '取消预约失败')
  }
}
