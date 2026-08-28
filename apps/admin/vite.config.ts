import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // F1 按需引入:模板里的 El 组件按使用自动注册并带样式;composables 自动导入。
    // 注意:main.ts 里的 ElMessage/ElMessageBox 等函数式调用仍需手动引样式,
    // 由 unplugin 的 ElementPlusResolver({ importStyle }) 只覆盖模板组件,函数式组件样式
    // 在 main.ts 单独引入 element-plus/theme-chalk 下对应文件(见 main.ts 注释)。
    AutoImport({
      imports: ['vue'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
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
        // manualChunks 用函数形式(Rolldown-vite 不支持对象形式):
        // 把不常变的依赖拆出独立 chunk,业务代码更新时用户无需重新下载。
        chunkFileNames: 'assets/[name]-[hash].js',
        manualChunks(id: string) {
          if (id.includes('node_modules')) {
            if (id.includes('element-plus') || id.includes('@element-plus')) return 'vendor-element'
            if (id.includes('/vue/') || id.includes('vue-router') || id.includes('@vue') || id.includes('pinia')) return 'vendor-vue'
            return 'vendor-misc'
          }
          return undefined
        },
      },
    },
  },
  server: {
    // 商户端固定 5173(顾客端使用 5174);两端口同属 localhost origin,
    // 生产环境将以两个独立域名部署,开发端口仅是过渡形态。
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
