export interface Result<T> {
  code: 0 | 1
  msg: string
  data: T | null
}
