# Pocket Aurion Android v0.1.0

Native Android assistant shell for Galaxy S25 Ultra.

## Milestone A
Long-press the Samsung Side button and get **Aurion**, using an on-device GGUF model through llama.cpp, with local memory and no app-level network permission.

## What is already implemented
- Android Assistant role request.
- `VoiceInteractionService` + `VoiceInteractionSessionService` declarations.
- assistant-session UI for side-button invocation.
- focused-app AssistStructure capture as untrusted local context.
- local SQLite for conversation, memory, stances, projects, lineage, and settings.
- protected Aurion kernel/Soul/EGO/operator-model assets.
- local model runtime interface.
- pinned llama.cpp Android integration lane.
- GGUF import to app-private storage.
- on-device SpeechRecognizer path only.
- local-only TTS voice selection helper.
- hardware/device report.
- local benchmark hook.
- allowlisted Android-intent action broker skeleton.
- no `INTERNET` permission.

## Important
This source package is a **buildable engineering handoff**, not a claim that an APK has already been compiled or tested on the S25 Ultra. The current environment does not contain the Android SDK/NDK or your phone. The next proof is an actual build/install/device test.

Read `docs/BUILD_AND_INSTALL.md` and `docs/CLAUDE_CODE_HANDOFF.md`.
