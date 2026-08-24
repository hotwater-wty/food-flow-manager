// 顾客认证服务：注册和登录响应都经过 Result 解包后交给 Store。
import axios from 'axios'
import type { Result, UserLoginData, UserLoginRequest, UserRegisterData, UserRegisterRequest } from '../types/api'
import { http } from './http'

export async function loginUser(request: UserLoginRequest): Promise<UserLoginData> {
  // 登录函数返回业务数据，组件不需要知道 Axios response 的嵌套结构。
  try {
    const response = await http.post<Result<UserLoginData>>('/user/auth/login', request)
    const result = response.data

    if (result.code !== 1 || result.data === null) {
      throw new Error(result.msg || '登录失败')
    }

    return result.data
  } catch (error) {
    // instanceof Error 保留主动抛出的业务提示；Axios 网络异常则统一提示服务不可用。
    if (error instanceof Error && !axios.isAxiosError(error)) {
      throw error
    }

    throw new Error('后端服务暂不可用，请检查后端是否启动')
  }
}

export async function registerUser(request: UserRegisterRequest): Promise<UserRegisterData> {
  // 注册只创建账号，不自动写入登录 Store；用户需要随后登录获得 Token。
  const response = await http.post<Result<UserRegisterData>>('/user/auth/register', request)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '注册失败')
  return result.data
}
