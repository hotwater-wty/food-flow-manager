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
