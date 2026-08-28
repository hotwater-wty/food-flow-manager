const apiBase = process.env.API_BASE_URL ?? 'http://localhost:8080/api'
const customer = { phone: process.env.CUSTOMER_PHONE ?? '13900001234', password: process.env.CUSTOMER_PASSWORD ?? '123456' }
const employee = { phone: process.env.EMPLOYEE_PHONE ?? '18800000000', password: process.env.EMPLOYEE_PASSWORD ?? '123456' }

async function request(path, options = {}) {
  const response = await fetch(`${apiBase}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(options.headers ?? {}) } })
  const body = await response.json().catch(() => null)
  if (!response.ok) throw new Error(`${path}: HTTP ${response.status}`)
  return body
}

function assert(condition, message) {
  if (!condition) throw new Error(message)
}

const customerLogin = await request('/user/auth/login', { method: 'POST', body: JSON.stringify(customer) })
assert(customerLogin.code === 1 && customerLogin.data?.token, 'customer login failed')
const customerHeaders = { Authorization: `Bearer ${customerLogin.data.token}` }
for (const path of ['/user/tables', '/user/dish-categories', '/user/dishes', '/user/orders']) {
  const result = await request(path, { headers: customerHeaders })
  assert(result.code === 1, `${path} returned business failure`)
}

const employeeLogin = await request('/admin/auth/login', { method: 'POST', body: JSON.stringify(employee) })
assert(employeeLogin.code === 1 && employeeLogin.data?.token, 'employee login failed')
const adminHeaders = { Authorization: `Bearer ${employeeLogin.data.token}` }
for (const path of ['/admin/tables?pageNo=1&pageSize=10', '/admin/dish-categories?pageNo=1&pageSize=10', '/admin/dishes?pageNo=1&pageSize=10', '/admin/reservations?pageNo=1&pageSize=10', '/admin/employees?pageNo=1&pageSize=10', '/admin/orders?pageNo=1&pageSize=10', '/admin/sessions?pageNo=1&pageSize=10']) {
  const result = await request(path, { headers: adminHeaders })
  assert(result.code === 1 && result.data && Array.isArray(result.data.records), `${path} returned invalid page result`)
}

console.log('API smoke verification passed')
