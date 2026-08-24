// 顾客预约服务：包含令牌保护的创建、查询详情和取消。
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
  // scene 让后端区分预约提交和订单提交；函数返回 Promise，表示异步请求稍后才会完成。
  const response = await http.post<Result<SubmitTokenData>>('/user/submit-token', { scene })
  // response.data 是后端 Result；这里的 data 还可能为 null，因此不能直接 return。
  const result = response.data

  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '获取提交令牌失败')
  }

  return result.data
}

export async function createReservation(request: ReservationRequest): Promise<ReservationCreateData> {
  try {
    // 两次 await 按顺序执行：先拿令牌，再把令牌放到真正的写请求头中。
    const submitToken = await getSubmitToken('create-reservation')
    const response = await http.post<Result<ReservationCreateData>>('/user/reservations', request, {
      headers: { 'X-Submit-Token': submitToken.token },
    })
    // 解包成功响应后，只向页面返回 ReservationCreateData，不泄漏 Axios 细节。
    const result = response.data

    if (result.code !== 1 || result.data === null) {
      throw new Error(result.msg || '创建预约失败')
    }

    return result.data
  } catch (error) {
    // catch 的 error 类型是 unknown；先保留主动抛出的业务错误，再统一处理网络错误。
    if (error instanceof Error && !axios.isAxiosError(error)) {
      throw error
    }
    throw new Error('预约提交失败，请检查后端服务或桌位状态')
  }
}

export async function getReservations(): Promise<ReservationData[]> {
  // 查询函数返回数组；页面把空数组当作正常的“暂无预约”状态。
  const response = await http.get<Result<ReservationData[]>>('/user/reservations')
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '预约列表查询失败')
  }
  return result.data
}

export async function getReservationDetail(reservationId: number): Promise<ReservationData> {
  // 路径插值把数字 ID 放入 URL，服务层负责保证路径与后端契约一致。
  const response = await http.get<Result<ReservationData>>(`/user/reservations/${reservationId}/detail`)
  const result = response.data
  if (result.code !== 1 || result.data === null) {
    throw new Error(result.msg || '预约详情查询失败')
  }
  return result.data
}

export async function cancelReservation(reservationId: number): Promise<void> {
  // Promise<void> 表示成功时不需要业务数据，调用方只关心请求是否抛错。
  const response = await http.post<Result<null>>(`/user/reservations/${reservationId}/cancel`)
  const result = response.data
  if (result.code !== 1) {
    throw new Error(result.msg || '取消预约失败')
  }
}
