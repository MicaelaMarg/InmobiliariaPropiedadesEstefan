import { spawn } from "node:child_process";
import { resolve } from "node:path";

const port = process.env.PORT || "3000";
const viteBin = resolve("node_modules", "vite", "bin", "vite.js");

const child = spawn(
  process.execPath,
  [viteBin, "preview", "--host", "0.0.0.0", "--port", port],
  {
    stdio: "inherit",
    env: process.env,
  }
);

child.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }

  process.exit(code ?? 0);
});
