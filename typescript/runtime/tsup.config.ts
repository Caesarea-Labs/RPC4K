import {defineConfig} from "tsup";
export default defineConfig({
    entry: ['src/runtime/index.ts'],
    sourcemap: true,
    clean: true,
})
