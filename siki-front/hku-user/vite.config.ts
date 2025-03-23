import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 开启代理
  server: {
    host: '0.0.0.0',
    port: 5174, // 用户端使用5174端口，与管理端5173区分
    open: true,
    proxy: {
      '/api': {
        // 前缀替换成代理地址： 5174 -> 8080 后端tomcat服务器端口号
        target: 'http://localhost:8080/user', // 用户端API前缀
        ws: false,
        secure: false,
        changeOrigin: true,
        // /api去掉，变成空串，因为它只是一个标识而已，并不是路径
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})