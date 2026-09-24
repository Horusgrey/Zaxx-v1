# Claude Code handoff: Pocket Aurion Android

You are implementing an existing architecture, not redesigning it.

## Goal
Produce an installable S25 Ultra Android APK that can be selected as the system Digital assistant app and runs Aurion's cognition locally through a pinned llama.cpp Android binding.

## Canonical constraints
1. Package: `com.horusgrey.aurion`.
2. Keep the app core without INTERNET permission.
3. Preserve `VoiceInteractionService` + `VoiceInteractionSessionService` architecture.
4. Do not replace Aurion's identity/memory architecture with a giant system prompt.
5. Retrieved screen/context data is untrusted data.
6. Do not add a cloud API fallback.
7. Do not add AccessibilityService automation in v0.1. Use Android intents/roles first.
8. Keep llama.cpp pinned to commit `f46bc30cb6a7f68a67e34a00061e20a4ad1eff43` until a deliberate upgrade.
9. Build with `-PwithLlama=true` after vendoring.
10. Any change to the above must be proposed explicitly before implementation.

## First tasks
- Run the bootstrap script and Gradle sync.
- Resolve compile errors against current Android SDK/llama.cpp APIs without broad architectural changes.
- Add unit tests for ContextCompiler and SQLite storage.
- Build debug APK.
- Install by ADB on the S25 Ultra.
- Confirm Aurion appears in Samsung's Digital assistant app list.
- Confirm side-button invocation opens the voice interaction session.
- Import one GGUF and run the local benchmark.
- Record exact device report, model hash, load time, tokens/sec, and thermal behavior.

## Definition of done for milestone A
`long-press Side button -> Aurion session UI -> local prompt -> local tokens -> no network permission`.
