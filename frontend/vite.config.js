import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-oxc'; // Optimized for Vite 8

export default defineConfig({
  plugins: [react()],
  
  server: {
    port: 5173,
  },

  esbuild: {
    jsx: 'automatic', 
  },
});