# Build + install on Galaxy S25 Ultra

## Development machine prerequisites
- Android Studio with Android SDK 36
- JDK 17
- Android NDK 29.0.13113456 (matches the pinned llama.cpp Android binding)
- CMake 3.31.6
- Git
- USB debugging enabled on the S25 Ultra

## 0. No-install option: GitHub Actions
Every push that touches `pocket-aurion-android/` runs `.github/workflows/pocket-aurion-android.yml`, which performs steps 1–3 below,
runs the unit tests, rejects any APK that requests `android.permission.INTERNET`, and uploads
`pocket-aurion-shell-debug.apk` and `pocket-aurion-llama-debug.apk` as the `pocket-aurion-debug-apks` artifact.

## 1. Bootstrap llama.cpp
Run `scripts/bootstrap_llama.sh` (macOS/Linux) or `scripts/bootstrap_llama.ps1` (Windows).

Pinned upstream commit:
`f46bc30cb6a7f68a67e34a00061e20a4ad1eff43`

This was the current llama.cpp master commit observed on 2026-09-22. Pinning prevents an upstream update from silently changing the runtime between tests.

The script never touches `gradle/libs.versions.toml`; Pocket Aurion's catalog already carries every alias the llama `:lib` module needs.
Then run `python3 scripts/static_verify.py` — every check must pass.

## 2. Gradle wrapper
The Gradle 8.13 wrapper is committed (`./gradlew`). Use JDK 17 (`JAVA_HOME`).

## 3. Test, then build shell first, then native runtime
`./gradlew :app:testDebugUnitTest`

`./gradlew :app:assembleDebug` (StubRuntime shell; inspect with `aapt2 dump permissions` — no INTERNET)

`./gradlew :app:assembleDebug -PwithLlama=true`

## 4. Install
`adb install -r app/build/outputs/apk/debug/app-debug.apk`

## 5. Set Aurion as default assistant
On Samsung:
1. Settings
2. Apps
3. Choose default apps
4. Digital assistant app
5. Digital assistant app
6. Aurion

Then:
1. Settings
2. Advanced features
3. Side button
4. Long press
5. Digital assistant
6. Aurion

## 6. First launch
Open Aurion once before using the Side button.
- Grant microphone permission if you want local on-device dictation.
- Tap **Import GGUF** and choose the model selected by the Pocket Aurion bake-off.
- The app copies the GGUF into app-private storage because the pinned llama.cpp Android binding loads by filesystem path.
- Tap **Benchmark** and save the result.

Do not select a permanent default model based only on whether it loads. Measure prompt processing speed, token generation, RAM pressure, thermal throttling, battery draw, and response quality on Aurion's real regression prompts.
