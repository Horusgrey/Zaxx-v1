#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VENDOR="$ROOT/vendor/llama.cpp"
REF="f46bc30cb6a7f68a67e34a00061e20a4ad1eff43"
mkdir -p "$ROOT/vendor"
if [ ! -d "$VENDOR/.git" ]; then
  git clone https://github.com/ggml-org/llama.cpp.git "$VENDOR"
fi
git -C "$VENDOR" fetch --depth 1 origin "$REF"
git -C "$VENDOR" checkout --detach "$REF"
cp "$VENDOR/examples/llama.android/gradle/libs.versions.toml" "$ROOT/gradle/libs.versions.toml"
echo "llama.cpp pinned at $REF"
