import { fileURLToPath, URL } from 'node:url'
import externalGlobals from "rollup-plugin-external-globals";
import {resolve}  from 'path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),    
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    rollupOptions: {
      // input: {
      //   main: resolve(__dirname, 'public/index.html'),
      // },
      // 不打包依赖
      // external: ['three'],
      plugins: [
        // 不打包依赖映射的对象
        externalGlobals({
          "@tslfe/dt-engine": '@tslfe/dtEngine'
        })
      ]
   }
  },
  server: {
    proxy: {
      "/masterApi": {
        target: "http://183.192.162.17"
      },
      // "/Data/Models_v2": {
      //   target: "http://183.192.162.17",
      //   rewrite: (path)=>'/modelstudio/Output/wenyanan/%E7%A7%91%E5%A4%A7%E8%AE%AF%E9%A3%9E' + path
      // },
      // "/modelstudio/Output/": {
      //   target: "http://183.192.162.17",
      //   changeOrigin: true,
      // },
    }
  }
});
