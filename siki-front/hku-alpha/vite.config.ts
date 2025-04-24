import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// 定义服务器列表
const servers = ['http://localhost:8080/admin', 'http://localhost:8082/admin'];
// 当前服务器索引
let currentServer = 0;

// 获取下一个服务器地址
function getNextServer() {
    const server = servers[currentServer];
    currentServer = (currentServer + 1) % servers.length;
    return server;
}

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
        // public: '0.0.0.0:5173', // 本地的ip:端口号
        port: 5173,
        open: true,
        proxy: {
            '/api': {
                // 在每次请求时动态设置 target
                configure: (proxy, options) => {
                    options.target = getNextServer();
                    proxy.on('proxyReq', () => {
                        options.target = getNextServer();
                    });
                },
                ws: false,
                secure: false,
                changeOrigin: true,
                // /api 去掉，变成空串，因为它只是一个标识而已，并不是路径
                rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    }
});