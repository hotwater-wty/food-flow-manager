import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // F1 按需引入:模板中的 Van 组件由 unplugin-vue-components 自动注册并按需带样式。
    // 函数式 API(showToast/showFailToast/showConfirmDialog)不经过模板编译,
    // 其样式已在 main.ts 显式引入 Vant 完整样式前不再必要——顾客端保留全量样式,
    // 原因:Vant 样式按需收益有限且函数式组件多,拆分后主包本已只有单一组件库。
    Components({
      resolvers: [VantResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      // workspace 包是 TS 源码直引(无构建产物),dev 与 build 都靠这个别名解析。
      '@foodflow/shared': fileURLToPath(new URL('../../packages/shared/src', import.meta.url)),
    },
  },
  build: {
    rollupOptions: {
      output: {
        // manualChunks 用函数形式(Rolldown-vite 不支持对象形式)。
        chunkFileNames: 'assets/[name]-[hash].js',
        manualChunks(id: string) {
          if (id.includes('node_modules')) {
            if (id.includes('vant') || id.includes('@vant')) return 'vendor-vant'
            if (id.includes('/vue/') || id.includes('vue-router') || id.includes('@vue') || id.includes('pinia')) return 'vendor-vue'
            return 'vendor-misc'
          }
          return undefined
        },
      },
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
