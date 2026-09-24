# Pocket Aurion security boundary v0.1

- No `android.permission.INTERNET` in the application manifest.
- No cloud fallback in the model runtime.
- Screen/assist data is treated as untrusted context, not instructions.
- `FLAG_SECURE` apps may provide empty/no assist structure; Aurion must respect that.
- Local phone actions are allowlisted and should require visible confirmation before consequential execution.
- The assistant role service is kept lightweight; model work is not performed inside the always-resident service.
- The recognition metadata service does not proxy audio to a remote provider.
- User-facing dictation requests Android's explicit on-device recognizer only when available.
- TTS selects a voice whose Android `Voice.isNetworkConnectionRequired` is false; otherwise spoken output remains disabled.
- Personal memory is stored in app-private SQLite. Android backup is disabled for v0.1 to avoid accidental cloud backup.

Not yet solved:
- encrypted database at rest;
- local biometric gate for restricted vaults;
- verified offline ASR independent of OEM speech packages;
- code-signing/reproducible APK pipeline;
- automated Android instrumentation security tests.
