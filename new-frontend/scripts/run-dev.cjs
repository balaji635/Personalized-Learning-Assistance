const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");

const cacheDir = path.join(process.cwd(), ".next");

try {
  fs.rmSync(cacheDir, { recursive: true, force: true });
} catch (error) {
  console.warn("Could not clear .next cache:", error);
}

const nextBin = path.join(
  process.cwd(),
  "node_modules",
  ".bin",
  process.platform === "win32" ? "next.cmd" : "next"
);

const child = spawn(nextBin, ["dev"], {
  stdio: "inherit",
  shell: false,
  env: process.env,
});

child.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }

  process.exit(code ?? 0);
});
