# Pocket Aurion Android — CHANGELOG

## 0.1.1-dev — Milestone A build bring-up (2026-09-24)

Status: **SOURCE_READY_DEVICE_UNVERIFIED** (unchanged). Nothing below has run on the S25 Ultra yet.

### Environment finding (blocks local APK build in the cloud session)
- The cloud session's network policy denies `dl.google.com` (HTTP 403 at the egress proxy). That host serves the
  Android SDK/NDK/CMake **and** Google Maven (AGP, AndroidX, Material). `./gradlew :app:assembleDebug` therefore fails with
  `Plugin [id: 'com.android.application', version: '8.13.2'] was not found`.
- Reachable: JDK 17 (Ubuntu archive), llama.cpp via git, Gradle distributions, Maven Central (rate-limited; `repo1.maven.org` works).
- Workaround: `.github/workflows/pocket-aurion-android.yml` runs the full Milestone A build on GitHub-hosted runners (see below).

### Milestone A build evidence (GitHub Actions run 35985099307, commit 32612df, JDK 17.0.20, Gradle 8.13)
- `static_verify.py`: pass. `:app:testDebugUnitTest`: pass.
- Shell APK (`:app:assembleDebug`) and llama APK (`-PwithLlama=true`, NDK 29.0.13113456, CMake 3.31.6): both build.
- `aapt2 dump permissions`, both APKs: `RECORD_AUDIO`, `VIBRATE`, and AndroidX's app-private
  `com.horusgrey.aurion.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. **No `android.permission.INTERNET`.**
- llama APK `lib/arm64-v8a/`: `libai-chat.so`, `libllama.so`, `libllama-common.so`, `libggml*.so`, `libomp.so`, and CPU variants
  armv8.0, armv8.2 (×2), armv8.6, armv9.0, armv9.2 (×2). CPU-only: no OpenCL/Vulkan/QNN backends.
- SHA-256: llama `018d9b89b74ae1e4d2c1ea0d61f9bc456590cd328d7060426ddf6ce586f659fb`,
  shell `0725664560afd3d873bd32284bfa8a97d82a53c717154128a2b6dca68cafb256`.
- Artifact `pocket-aurion-debug-apks` (expires 2026-12-23).

### Code changes

| # | Change | Why | Evidence |
|---|--------|-----|----------|
| 1 | `scripts/bootstrap_llama.sh` / `.ps1`: stop copying upstream `libs.versions.toml` over Pocket Aurion's; shallow-fetch the pin instead of a full clone. | First prompt: bootstrap must not replace Pocket Aurion's version catalog. A full clone downloads all llama.cpp history for no benefit. | Bootstrap run: `llama.cpp pinned at f46bc30cb6a7f68a67e34a00061e20a4ad1eff43`; catalog byte-identical before/after (`cmp`). |
| 2 | `gradle/libs.versions.toml`: add `androidx-datastore-preferences` 1.2.0, `junit` 4.13.2, `androidx-junit` 1.3.0 (same versions as upstream), plus `robolectric` 4.17. | The pinned `llama.android/lib/build.gradle.kts` references those aliases; without them `-PwithLlama=true` cannot configure. This is why the old bootstrap overwrote the catalog. | `static_verify.py` alias check fails on the original tree (`missing=['androidx.datastore.preferences','androidx.junit','junit']`), passes now. |
| 3 | `actions/LocalActionBroker.kt`: remove `import android.app.AlarmClockInfo`. | Compile error: no such class (the real one is `AlarmManager.AlarmClockInfo`); the import was unused. | Kotlin 2.3.0 against android-all SDK 36: `Unresolved reference 'AlarmClockInfo'` before, clean after. |
| 4 | `runtime/LlamaCppRuntime.kt`: wait (≤30 s) for the engine's async native init before `loadModel`; `cleanUp()` a previous model / error state first. | Pinned `InferenceEngineImpl` loads `libai-chat` asynchronously and rejects `loadModel` unless state is `Initialized`. Cold-start restore raced it (`Cannot load model in Initializing`); re-importing a model failed (`Cannot load model in ModelReady`). A failed native load never leaves `Initializing`, hence the timeout. | Compiles against the pinned `com.arm.aichat` sources. Runtime behaviour **unverified on device**. |
| 5 | `core/AurionRepository.kt`: reset `systemPromptApplied` on every load; serialize loads with a `Mutex`; add `restoreSavedModel()`; compile the prompt **before** storing the user turn; optional `runtime` constructor param for tests. | Checklist B: "system prompt applied after every fresh model load" — the flag was never reset, so a second load ran without the kernel. Storing first duplicated the current request inside RECENT LOCAL CONVERSATION. | `AurionRepositoryTest` (compiles; see test status). |
| 6 | `assistant/AurionVoiceInteractionSession.kt`: `onShow` restores the saved model when none is loaded. | Milestone A path: after a reboot/process kill, a Side-button press reported "model not loaded" until the user opened MainActivity. | Review only (needs AndroidX/R to compile). |
| 7 | `MainActivity.kt`: use `restoreSavedModel()`; import via `.part` temp file + rename; compute/display/store SHA-256; catch import errors. | Picking a non-`.gguf` file threw inside an unguarded coroutine → app crash. Re-importing the loaded model overwrote a file llama.cpp has mmapped. Checklist B / handoff require the model hash. | Review only (needs AndroidX/R to compile). |
| 8 | `AndroidManifest.xml`: `AurionVoiceInteractionService` → `android:process=":interactor"`. `PocketAurionApp.aurion` → `by lazy`. | Checklist F: "Resident `:interactor` process remains lightweight and does not load the model/database by itself." Previously the always-bound service shared the main process, whose `Application.onCreate` opened the DB and loaded the native runtime at boot. | `static_verify.py` checks both processes. Device check pending (`adb shell ps -A | grep aurion`). |
| 9 | `res/xml/voice_interaction_service.xml`: `supportsLaunchVoiceAssistFromKeyguard="false"`. | Checklist A: "Lock-screen invocation remains disabled." | `static_verify.py`. |
| 10 | New `scripts/static_verify.py` (stdlib only). | Required by the first prompt but absent from the v0.1.0 archive. Checks manifest permissions/backup/cleartext, assistant services + processes, keyguard flag, pin consistency, catalog aliases, no network/cloud/Accessibility code, on-device ASR only, local TTS filter, untrusted screen label, assets, release flags, wrapper version. | 44/44 pass now; on the untouched v0.1.0 tree it flags 5 failures (items 2, 8, 9, and both bootstrap scripts). |
| 11 | Gradle 8.13 wrapper (`gradlew`, `gradle/wrapper/*`). | Milestone A step 4. | `./gradlew --version` → Gradle 8.13, JVM 17.0.20.1. |
| 12 | Unit tests: `AurionDbTest`, `ContextCompilerTest`, `AurionRepositoryTest` (Robolectric, pinned to SDK 34 via `app/src/test/resources/robolectric.properties`). | Handoff: add tests for ContextCompiler and SQLite storage. SDK 34 because Robolectric's SDK 35+ jars need a Java 21 runtime and the project uses JDK 17. | Could not run in the cloud session (Robolectric needs `androidx.test:monitor` from Google Maven). **Pass in CI** run 35985099307 (`:app:testDebugUnitTest`, 13 tests). |
| 13 | `.github/workflows/pocket-aurion-android.yml`. | Produces the APKs where Google's servers are reachable. Order follows the prompt: static verify → bootstrap → JVM tests → shell APK → llama APK → `aapt2 dump permissions` INTERNET gate → `libai-chat.so` present → upload APKs + SHA-256. | Run 35985099307 on `32612df`: every step green in 12.5 min. |
| 14 | `.gitignore`, `SHA256SUMS.txt`/`FILE_INDEX.txt` regenerated, `docs/*` added (checklist, roadmap, first prompt). | Keep vendor/build outputs/GGUFs out of git; checksums matched the old files. | — |

### Compile evidence (scratch harness, not committed)
Kotlin 2.3.0 / JDK 17 JVM project that compiles, in place, `core/**`, `data/**`, `runtime/**`, `actions/**`,
`voice/LocalTts.kt`, `voice/AurionRecognitionService.kt`, `withLlama/.../LlamaCppRuntime.kt` and the pinned
`com.arm.aichat` Kotlin API against `org.robolectric:android-all:16-robolectric-13921718` (SDK 36): **clean**.
Test sources compile. Not compiled here: `MainActivity`, `assistant/*Session*`, `OnDeviceSpeechInput`, `PocketAurionApp` (need AndroidX / generated `R`).

### Known issues (not changed — outside Milestone A's concrete-defect scope)
- The pinned engine keeps its own chat history + KV cache across turns, and `ContextCompiler` also re-sends RECENT LOCAL
  CONVERSATION each turn, so history is duplicated in the context window. Decide during Stage 2 tuning (e.g. reset per turn or drop the compiled history).
- Checklist C: `OnDeviceSpeechInput` reports one message for both "ASR unavailable" and "mic permission missing".
- Checklist C: `LocalTts` exists but is not wired to the session, so "voice output stops when hidden" is untestable.
- Checklist D/E: Project Capsules, action-envelope parsing, and the confirmation UI are not implemented in v0.1 source.
- `allowBackup=false` blocks cloud backup; Android 12+ device-to-device transfer needs `dataExtractionRules` to exclude too.
- `AurionRecognitionService` uses `android:permission="android.permission.BIND_SPEECH_RECOGNITION_SERVICE"`, which is not a
  platform permission; harmless (it only makes the stub unbindable) but should be confirmed during device testing.
- The session's `CoroutineScope` is not cancelled in `onDestroy`.
