// 前端 API 类型边界：字段与后端 DTO/VO 对齐，避免页面自行猜测响应结构。
export interface Result<T> {
  // 泛型 T 表示同一响应外壳可以包裹不同业务数据。
  code: 0 | 1
  // 字面量联合类型 0 | 1 限制成功码和失败码，避免传入任意数字。
  msg: string
  // 失败响应允许 data 为 null，所以调用方必须先判空再使用数据。
  data: T | null
  // 成功时为空；失败时用于前端稳定展示和定位，不依赖中文文案匹配。
  errorCode?: string | null
}

export interface UserLoginRequest {
  // Request 类型描述发送给后端的 JSON 字段，而不是响应字段。
  phone: string
  password: string
}

export interface UserLoginData {
  // 登录成功返回 Token 和展示用身份资料；Token 不会进入 AuthUser 类型。
  userId: number
  phone: string
  nickname: string
  status: number
  token: string
}

export interface UserRegisterRequest {
  phone: string
  password: string
  nickname: string
}
export interface UserRegisterData {
  userId: number
  phone: string
  nickname: string
  status: number
}

export interface EmployeeLoginData {
  employeeId: number
  phone: string
  name: string
  role: number
  status: number
  token: string
}

export interface TableVO {
  // VO 是 View Object，字段形状对应后端返回给页面的桌位对象。
  tableId: number
  tableNo: string
  capacity: number
  locationDesc: string
  status: number
  currentSessionId: number | null
}

export interface ReservationRequest {
  // 预约请求只包含用户提交的最小输入，编号和状态由后端生成。
  tableId: number
  peopleCount: number
  reserveTime: string
}

export interface SubmitTokenRequest {
  scene: string
}

export interface SubmitTokenData {
  // 一次性令牌的有效期由服务端决定，前端不能自行延长。
  token: string
  expiresInSeconds: number
}

export interface ReservationCreateData {
  reservationId: number
  reservationNo: string
  tableId: number
  tableNo: string | null
  peopleCount: number
  reserveTime: string
  status: number
}

export type ReservationData = ReservationCreateData

export interface DiningSessionData {
  // 会话对象同时携带会话状态和桌位状态，页面展示时不能混用两套状态码。
  sessionId: number
  sessionNo: string
  tableId: number
  tableNo: string
  sessionStatus: number
  tableStatus: number
}

export interface DishCategoryData {
  id: number
  name: string
  sort: number
  status: number
}

export interface DishData {
  // price 保持后端整数“分”，金额格式化只在页面展示边界进行。
  id: number
  categoryId: number
  name: string
  price: number
  image: string | null
  description: string | null
  status: number
}

export interface OrderItemRequest {
  // 订单明细是提交给后端的商品 ID 与数量，金额由后端重新计算。
  dishId: number
  quantity: number
  remark?: string
}

export interface OrderCreateRequest {
  items: OrderItemRequest[]
}

export interface OrderItemCreateData {
  dishId: number
  dishName: string
  dishPrice: number
  quantity: number
  amount: number
}

export interface OrderCreateData {
  orderId: number
  orderNo: string
  sessionId: number
  tableId: number
  tableNo: string
  totalAmount: number
  status: number
  items: OrderItemCreateData[]
}

export interface OrderData {
  orderId: number
  orderNo: string
  tableId: number
  tableNo: string
  totalAmount: number
  status: number
  createTime: string
}

export interface OrderItemData {
  dishId: number
  dishName: string
  dishImage: string | null
  dishPrice: number
  quantity: number
  amount: number
  remark: string | null
}

export interface OrderDetailData extends OrderData {
  items: OrderItemData[]
}

export interface PageResult<T> {
  // 分页结构的 records 是当前页数据，total 是所有页总数。
  total: number
  pageNo: number
  pageSize: number
  records: T[]
}

export interface AdminOrderData extends OrderData {
  sessionId: number
}

export interface DiningSessionCloseData extends DiningSessionData {
  closeTime: string | null
  closeEmployeeId: number | null
}

// 管理端写请求使用独立类型，避免把后端返回对象直接当成表单数据。
export interface TableRequest {
  tableNo: string
  capacity: number
  locationDesc?: string
}
export interface DishCategoryRequest {
  name: string
  sort: number
}
export interface DishCreateRequest {
  categoryId: number
  name: string
  description: string
  price: number
  image: string
  status: number
}
export interface DishUpdateRequest {
  categoryId: number
  name: string
  description: string
  price: number
  image: string
}
export interface EmployeeCreateRequest {
  phone: string
  password: string
  name: string
}
// 当前与管理端预约结构相同,未来需要独立字段时改为 interface 扩展。
export type ReservationAdminData = ReservationData
export interface EmployeeData {
  employeeId: number
  phone: string
  name: string
  role: number
  status: number
}

// 经营统计(三期 R5):口径为服务器时区当日、不含已取消订单,金额均为整数分。
export interface StatusCount {
  status: number
  count: number
}
export interface TopDish {
  dishId: number
  dishName: string
  quantity: number
  amount: number
}
export interface StatisticsOverview {
  todayOrderCount: number
  todayRevenue: number
  statusDistribution: StatusCount[]
  topDishes: TopDish[]
}
