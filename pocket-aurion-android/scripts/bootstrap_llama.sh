#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VENDOR="$ROOT/vendor/llama.cpp"
REF="f46bc30cb6a7f68a67e34a00061e20a4ad1eff43"
mkdir -p "$VENDOR"
if [ ! -d "$VENDOR/.git" ]; then
  git -C "$VENDOR" init -q
  git -C "$VENDOR" remote add origin https://github.com/ggml-org/llama.cpp.git
fi
git -C "$VENDOR" fetch --depth 1 origin "$REF"
git -C "$VENDOR" checkout --detach "$REF"
# Pocket Aurion keeps its own gradle/libs.versions.toml. It already carries every alias the
# llama.android :lib module needs; scripts/static_verify.py checks that instead of overwriting it.
echo "llama.cpp pinned at $(git -C "$VENDOR" rev-parse HEAD)"
