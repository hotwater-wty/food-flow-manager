// 顾客菜单查询服务：分类和可售菜品来自后端，不在前端伪造目录。
import type { DishCategoryData, DishData, Result } from '../types/api'
import { http } from './http'

export async function getDishCategories(): Promise<DishCategoryData[]> {
  const response = await http.get<Result<DishCategoryData[]>>('/user/dish-categories')
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '菜品分类查询失败')
  return result.data
}

export async function getDishes(categoryId?: number): Promise<DishData[]> {
  const response = await http.get<Result<DishData[]>>('/user/dishes', {
    params: categoryId === undefined ? undefined : { categoryId },
  })
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '菜品查询失败')
  return result.data
}

export async function getDishDetail(dishId: number): Promise<DishData> {
  const response = await http.get<Result<DishData>>(`/user/dishes/${dishId}`)
  const result = response.data
  if (result.code !== 1 || result.data === null) throw new Error(result.msg || '菜品详情查询失败')
  return result.data
}
