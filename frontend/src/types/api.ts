export interface Result<T> {
  code: 0 | 1
  msg: string
  data: T | null
}

export interface UserLoginRequest {
  phone: string
  password: string
}

export interface UserLoginData {
  userId: number
  phone: string
  nickname: string
  status: number
  token: string
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
  tableId: number
  tableNo: string
  capacity: number
  locationDesc: string
  status: number
  currentSessionId: number | null
}

export interface ReservationRequest {
  tableId: number
  peopleCount: number
  reserveTime: string
}

export interface SubmitTokenRequest {
  scene: string
}

export interface SubmitTokenData {
  token: string
  expiresInSeconds: number
}

export interface ReservationCreateData {
  reservationId: number
  reservationNo: string
  tableId: number
  tableNo: string
  peopleCount: number
  reserveTime: string
  status: number
}

export type ReservationData = ReservationCreateData

export interface DiningSessionData {
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
  id: number
  categoryId: number
  name: string
  price: number
  image: string | null
  description: string | null
  status: number
}

export interface OrderItemRequest {
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
