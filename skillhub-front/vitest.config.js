import { defineConfig } from "vitest/config";

export default defineConfig({
    test: {
        environment: "jsdom",
        globals: true,
        setupFiles: ["./src/tests/setup.js"],
        include: ["src/services/**/*.test.js"],
        coverage: {
            provider: "v8",
            reporter: ["text", "html", "json-summary"],
            include: ["src/services/**/*.js"],
            thresholds: {
                lines: 96,
                functions: 96,
                statements: 96,
            },
        },
    },
});

