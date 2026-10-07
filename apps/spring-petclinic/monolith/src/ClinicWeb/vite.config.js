import { defineConfig } from "vite";

export default defineConfig({
  build: {
    target: "esnext",
  },
  optimizeDeps: {
    esbuildOptions: {
      target: "esnext",
    },
  },
  server: {
    port: 5173,
  },
  test: {
    environment: "node",
    testTimeout: 120_000,
    hookTimeout: 120_000,
  },
});
