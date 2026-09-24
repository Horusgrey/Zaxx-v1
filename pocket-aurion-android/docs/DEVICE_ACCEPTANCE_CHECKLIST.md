# Pocket Aurion S25 Ultra acceptance checklist

A build is not promoted merely because an APK installs.

## A. Installation and assistant role
- [ ] APK installs without requesting `android.permission.INTERNET`.
- [ ] Aurion launches normally.
- [ ] Microphone permission can be granted explicitly.
- [ ] Aurion appears in Android/Samsung Digital assistant selection.
- [ ] Aurion successfully becomes the Assistant role holder.
- [ ] Samsung Side-button long press is set to Digital assistant.
- [ ] Long-press Side button opens an Aurion `VoiceInteractionSession`.
- [ ] Lock-screen invocation remains disabled.

## B. Local model
- [ ] A GGUF can be selected through Storage Access Framework.
- [ ] GGUF is copied into app-private storage.
- [ ] SHA-256 is displayed/stored.
- [ ] Model loads without network access.
- [ ] Aurion system prompt is applied after every fresh model load.
- [ ] Tokens stream into both the normal UI and assistant session.
- [ ] App/process survives repeated assistant show/hide cycles.

## C. Voice
- [ ] Status distinguishes local ASR unavailable vs microphone permission missing.
- [ ] On-device speech recognition works with Wi-Fi/mobile data disabled.
- [ ] No cloud recognizer fallback occurs.
- [ ] TTS selects a local voice.
- [ ] Voice output stops when the assistant is hidden.

## D. Context and memory
- [ ] Focused-app AssistStructure can be captured where Android permits it.
- [ ] Secure/no-assist apps do not leak content.
- [ ] Screen context is labeled and treated as untrusted data.
- [ ] Recent conversation survives app/session recreation.
- [ ] Relevant normal memory can be retrieved.
- [ ] Concept-lineage matches are presented as hypotheses, not facts.
- [ ] A normal-only Project Capsule imports successfully.
- [ ] Capsule hash tampering is rejected.
- [ ] A capsule marked `contains_private=true` is rejected.

## E. Phone action boundary
- [ ] Unsupported action types do nothing.
- [ ] `intent:`, `file:`, `content:`, and `javascript:` URI proposals fail closed.
- [ ] Supported action proposals show a visible confirmation button.
- [ ] SMS opens a draft instead of sending automatically.
- [ ] Calendar opens a draft instead of committing automatically.
- [ ] Alarm path preserves visible Clock UI.
- [ ] Action envelope markup is not stored in assistant conversation history.

## F. Offline/security validation
- [ ] Disable Wi-Fi and mobile data and repeat core conversation.
- [ ] `dumpsys package` / APK inspection confirms no INTERNET permission.
- [ ] Android backup remains disabled.
- [ ] No unexpected sockets/endpoints are observed from Aurion core.
- [ ] Resident `:interactor` process remains lightweight and does not load the model/database by itself.

## G. Performance gate
Record for every model/runtime profile:
- [ ] cold model load seconds;
- [ ] first-token latency;
- [ ] prompt-processing tok/s;
- [ ] generation tok/s;
- [ ] memory pressure / crashes;
- [ ] 10-minute sustained thermal behavior;
- [ ] battery delta;
- [ ] assistant responsiveness score;
- [ ] continuity / anti-sycophancy regression score.

Only after A–G pass may a build be labeled `DEVICE_VERIFIED`.
