# Pocket Aurion Android architecture

```text
Samsung Side Button / Android Assistant Role
                  |
                  v
VoiceInteractionService (tiny, resident)
                  |
                  v
VoiceInteractionSession (UI + assist context)
                  |
                  v
AurionRepository / ContextCompiler
      |                 |                |
      v                 v                v
SQLite state       protected assets   Action broker
      |                                  |
      v                                  v
memory/stances/projects               Android intents
      |
      v
LocalModelRuntime interface
      |
      +--> llama.cpp Android binding (first runtime)
      +--> future ExecuTorch/QNN candidate after measured benchmark
```

The app declares **no INTERNET permission**. Networking is not part of the core assistant. Any future network capability must live behind an explicit, separately permissioned edge rather than silently appearing inside the model runtime.

The Android `VoiceInteractionService` stays lightweight because Android keeps the active voice interactor resident. Heavy work lives in the session/app process and model runtime.
