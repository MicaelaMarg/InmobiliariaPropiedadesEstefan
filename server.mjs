import { spawn } from "node:child_process";

const port = process.env.PORT || 3000;

const child = spawn(
  "npx",
  ["serve", "dist", "-l", port],
  {
    stdio: "inherit",
    env: process.env,
  }
);

child.on("exit", (code) => {
  process.exit(code ?? 0);
});