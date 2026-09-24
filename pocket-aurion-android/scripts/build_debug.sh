#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ ! -x ./gradlew ]; then
  echo "gradlew missing. Open the project once in Android Studio or run: gradle wrapper --gradle-version 8.13"
  exit 2
fi
./gradlew :app:assembleDebug -PwithLlama=true
