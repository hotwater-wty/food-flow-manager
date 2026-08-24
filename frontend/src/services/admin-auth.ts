// 管理端认证服务：只调用员工登录接口，不与顾客认证复用数据类型。
import axios from 'axios'
import type { EmployeeLoginData, Result } from '../types/api'
import { http } from './http'

/** 发送员工手机号和密码；Promise 的泛型表示成功时最终拿到的响应数据。 */
export async function loginEmployee(request: { phone: string; password: string }): Promise<EmployeeLoginData> {
  try {
    // http 实例会根据 /admin 前缀自动附加员工 Token；登录时通常还没有 Token。
    const response = await http.post<Result<EmployeeLoginData>>('/admin/auth/login', request)
    // Axios 的 response.data 是后端统一 Result 外壳，而不是直接的员工对象。
    const result = response.data
    // code 表示业务成功与否；data 为 null 时即使 code 为 1 也不能继续使用。
    if (result.code !== 1 || result.data === null) throw new Error(result.msg || '员工登录失败')
    return result.data
  } catch (error) {
    // 已知业务错误直接保留；网络错误统一转换成页面可理解的提示。
    if (error instanceof Error && !axios.isAxiosError(error)) throw error
    throw new Error('管理端服务暂不可用，请检查后端是否启动')
  }
}
