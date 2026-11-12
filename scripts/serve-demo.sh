#!/usr/bin/env bash
# 把原型当 app 跑起来：http://localhost:5173/ 与后端的 CORS 允许源一致
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PORT="${PORT:-5173}"
echo "[demo] http://localhost:$PORT/  (目录 $ROOT/product/demo)"
exec python3 -m http.server "$PORT" --directory "$ROOT/product/demo"
