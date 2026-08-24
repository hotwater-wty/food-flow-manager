// 顾客认证服务：注册和登录响应都经过 Result 解包后交给 Store。
import axios from 'axios'
import type { Result, UserLoginData, UserLoginRequest, UserRegisterData, UserRegisterRequest } from '../types/api'
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

export async function registerUser(request: UserRegisterRequest): Promise<UserRegisterData> {
  const response = await http.post<Result<UserRegisterData>>('/user/auth/register', request)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '注册失败')
  return result.data
}
