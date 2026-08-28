// 顾客菜单查询服务：分类和可售菜品来自后端，不在前端伪造目录。
import type { DishCategoryData, DishData, Result } from '@foodflow/shared/types/api'
import { http } from './http'

export async function getDishCategories(): Promise<DishCategoryData[]> {
  // 分类接口返回普通数组；服务层把失败的 Result 转成异常交给页面处理。
  const response = await http.get<Result<DishCategoryData[]>>('/user/dish-categories')
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '菜品分类查询失败')
  return result.data
}

export async function getDishes(categoryId?: number): Promise<DishData[]> {
  // undefined 表示查询全部；有 categoryId 时 Axios 生成查询字符串。
  const response = await http.get<Result<DishData[]>>('/user/dishes', {
    params: categoryId === undefined ? undefined : { categoryId },
  })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '菜品查询失败')
  return result.data
}

export async function getDishDetail(dishId: number): Promise<DishData> {
  // 详情接口补充单个菜品信息，页面可在不改变列表的情况下展开说明。
  const response = await http.get<Result<DishData>>(`/user/dishes/${dishId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '菜品详情查询失败')
  return result.data
}
