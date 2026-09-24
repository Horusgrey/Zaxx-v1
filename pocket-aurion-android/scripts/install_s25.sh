#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
[ -f "$APK" ] || { echo "Build APK first"; exit 2; }
adb devices
adb install -r "$APK"
echo "Installed. On the S25 Ultra: Settings > Apps > Choose default apps > Digital assistant app > Aurion."
