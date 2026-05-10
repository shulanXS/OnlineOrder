import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/cart': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/orders': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/restaurant': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/restaurants': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/swagger-ui': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/v3/api-docs': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      '@': '/src',
    },
  },
});
