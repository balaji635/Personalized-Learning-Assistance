const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");

const cacheDir = path.join(process.cwd(), ".next");

try {
  fs.rmSync(cacheDir, { recursive: true, force: true });
} catch (error) {
  console.warn("Could not clear .next cache:", error);
}

let nextCli;
try {
  nextCli = require.resolve("next/dist/bin/next");
} catch (error) {
  console.error(
    "Could not locate Next.js CLI (next/dist/bin/next). Run npm install first."
  );
  process.exit(1);
}

const child = spawn(process.execPath, [nextCli, "dev"], {
  stdio: "inherit",
  shell: false,
  env: process.env,
  cwd: process.cwd(),
});

child.on("error", (error) => {
  console.error("Failed to start Next.js dev server:", error);
  process.exit(1);
});

child.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }

  process.exit(code ?? 0);
});
