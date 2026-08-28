// E2E 冒烟脚本(F2):双端各自的登录/守卫/核心页渲染链路,基于 Playwright。
// 前提:后端 8080、apps/admin dev 5173、apps/customer dev 5174 均已启动。
// 运行:仓库根目录 `npx pnpm@11.19.0 test:e2e`(详见 package.json scripts)。
import { chromium } from 'playwright'

const ADMIN_URL = process.env.ADMIN_URL ?? 'http://localhost:5173'
const CUSTOMER_URL = process.env.CUSTOMER_URL ?? 'http://localhost:5174'
const ADMIN = { phone: process.env.EMPLOYEE_PHONE ?? '18800000000', password: process.env.EMPLOYEE_PASSWORD ?? '123456' }
const CUSTOMER = { phone: process.env.CUSTOMER_PHONE ?? '13900001234', password: process.env.CUSTOMER_PASSWORD ?? '123456' }

let failures = 0
function assert(condition, message) {
  if (condition) {
    console.log(`  ✔ ${message}`)
  } else {
    failures++
    console.error(`  ✘ ${message}`)
  }
}

async function testAdmin(browser) {
  console.log('[商户端 5173]')
  const page = await browser.newPage()
  // 守卫:未登录访问工作台应被重定向到 /login
  await page.goto(`${ADMIN_URL}/orders`)
  await page.waitForURL('**/login**', { timeout: 5000 }).catch(() => {})
  assert(page.url().includes('/login'), '未登录访问 /orders 被守卫重定向到登录页')
  // 登录成功应进入订单工作台
  await page.locator('input[autocomplete="username"]').fill(ADMIN.phone)
  await page.locator('input[autocomplete="current-password"]').fill(ADMIN.password)
  await page.getByRole('button', { name: '登录管理端' }).click()
  await page.waitForURL('**/orders', { timeout: 5000 }).catch(() => {})
  assert(page.url().endsWith('/orders'), '员工登录后进入订单工作台')
  await page.waitForTimeout(800)
  const tableVisible = await page.locator('.el-table').first().isVisible().catch(() => false)
  assert(tableVisible, '订单工作台表格渲染')
  // 旧地址重定向
  await page.goto(`${ADMIN_URL}/admin/resources/employees`)
  await page.waitForTimeout(1000)
  assert(page.url().includes('/resources/employees') && !page.url().includes('/admin/'), '旧地址 /admin/** 去前缀重定向')
  // 登录页无顾客端链接(互通断绝)
  await page.getByRole('button', { name: '退出登录' }).click()
  await page.waitForTimeout(800)
  const hasCustomerLink = await page.locator('a[href="/"], a[href="/customer"]').count()
  assert(hasCustomerLink === 0, '商户端登录页无顾客端互通链接')
  await page.close()
}

async function testCustomer(browser) {
  console.log('[顾客端 5174]')
  const page = await browser.newPage()
  // 守卫:未登录访问点餐页应到登录页
  await page.goto(`${CUSTOMER_URL}/menu`)
  await page.waitForURL('**/login**', { timeout: 5000 }).catch(() => {})
  assert(page.url().includes('/login'), '未登录访问 /menu 被守卫重定向到登录页')
  // 登录(默认注册面板需切换)
  await page.locator('input[autocomplete="tel"]').fill(CUSTOMER.phone)
  await page.locator('input[autocomplete="current-password"]').fill(CUSTOMER.password)
  // 默认就是登录面板(isRegistering=false);若之前停在被切状态则先归位。
  const toRegister = page.getByRole('button', { name: '已有账号，去登录' })
  if (await toRegister.isVisible().catch(() => false)) {
    await page.getByRole('button', { name: '注册新账号' }).click()
  }
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await page.waitForURL('**/menu', { timeout: 5000 }).catch(() => {})
  assert(page.url().endsWith('/menu'), '顾客登录后进入点餐页')
  const dishVisible = await page
    .locator('.menu-dish-card')
    .first()
    .waitFor({ state: 'visible', timeout: 8000 })
    .then(() => true)
    .catch(() => false)
  assert(dishVisible, '菜品列表渲染')
  // 旧地址重定向
  await page.goto(`${CUSTOMER_URL}/customer/orders`)
  await page.waitForTimeout(1000)
  assert(page.url().includes('/orders') && !page.url().includes('/customer/'), '旧地址 /customer/** 去前缀重定向')
  // 404 兜底
  await page.goto(`${CUSTOMER_URL}/no-such-page`)
  await page.waitForTimeout(800)
  const notFound = await page.getByText('页面不存在').isVisible().catch(() => false)
  assert(notFound, '未命中路由渲染 404 兜底页')
  await page.close()
}

const browser = await chromium.launch()
try {
  await testAdmin(browser)
  await testCustomer(browser)
} finally {
  await browser.close()
}

if (failures > 0) {
  console.error(`\nE2E 冒烟失败:${failures} 项断言未通过`)
  process.exit(1)
}
console.log('\nE2E 冒烟全部通过')
