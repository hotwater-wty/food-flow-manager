import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // workspace 包是 TS 源码直引(无构建产物),dev 与 build 都靠这个别名解析。
      '@foodflow/shared': fileURLToPath(new URL('../../packages/shared/src', import.meta.url)),
    },
  },
  server: {
    // 顾客端固定 5174(商户端占用 5173);两端口同属 localhost origin,
    // 生产环境将以两个独立域名部署,开发端口仅是过渡形态。
    port: 5174,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
