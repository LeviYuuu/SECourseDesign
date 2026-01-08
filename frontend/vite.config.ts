import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 👇 引入自动导入插件
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 👇 配置 Vant UI 的自动按需引入
    AutoImport({
      resolvers: [VantResolver()],
    }),
    Components({
      resolvers: [VantResolver()],
    }),
  ],

  // 路径别名配置
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },

  server: {
    port: 5173, // 前端运行端口
    proxy: {
      // 这里的配置意思是：
      // 当前端发起 /user/login 请求时，
      // Vite 会把它转发给 http://localhost:8080/user/login
      '^/(user|scenario|session|speech|dialogue|evaluation|profile)': {
        target: 'http://localhost:8080', // 👈 这里填后端运行的地址
        changeOrigin: true,
      }
    }
  }
})
