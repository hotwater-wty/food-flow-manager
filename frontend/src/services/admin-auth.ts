import axios from 'axios'
import type { EmployeeLoginData, Result } from '../types/api'
import { http } from './http'

export async function loginEmployee(request: { phone: string; password: string }): Promise<EmployeeLoginData> {
  try {
    const response = await http.post<Result<EmployeeLoginData>>('/admin/auth/login', request)
    const result = response.data
    if (result.code !== 1 || result.data === null) throw new Error(result.msg || '员工登录失败')
    return result.data
  } catch (error) {
    if (error instanceof Error && !axios.isAxiosError(error)) throw error
    throw new Error('管理端服务暂不可用，请检查后端是否启动')
  }
}
