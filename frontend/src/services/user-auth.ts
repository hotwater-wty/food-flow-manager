import axios from 'axios'
import type { Result, UserLoginData, UserLoginRequest } from '../types/api'
import { http } from './http'

export async function loginUser(request: UserLoginRequest): Promise<UserLoginData> {
  try {
    const response = await http.post<Result<UserLoginData>>('/user/auth/login', request)
    const result = response.data

    if (result.code !== 1 || result.data === null) {
      throw new Error(result.msg || '登录失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error && !axios.isAxiosError(error)) {
      throw error
    }

    throw new Error('后端服务暂不可用，请检查后端是否启动')
  }
}
