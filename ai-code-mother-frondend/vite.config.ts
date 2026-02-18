import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,
    proxy: {
      // 静态资源预览路径：必须单独配置并使用 bypass，
      // 否则 Vite SPA fallback 会拦截 HTML 请求，返回 ai-code-mother 自己的 index.html
      '/api/static': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          // 返回 null 表示不走 SPA fallback，强制代理到后端
          return null
        }
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
