# Galaxy S25 Ultra runtime roadmap

Pocket Aurion must earn performance on the physical phone. Do not turn three uncertain acceleration paths on at once.

## Stage 1 — known-good CPU baseline
Use the pinned upstream llama.cpp Android binding first.

Pinned commit:
`f46bc30cb6a7f68a67e34a00061e20a4ad1eff43`

At that commit the upstream Android example:
- loads CPU backend variants dynamically;
- defaults to an 8192-token context;
- uses 2–4 CPU threads while leaving headroom;
- uses batch size 512;
- uses sampler temperature 0.3.

This is the debug baseline for proving:
assistant role → Side button → session UI → GGUF load → local tokens → memory → confirmed Android action.

Do not optimize GPU/NPU before this path is known-good.

## Stage 2 — tune the CPU baseline on the phone
After Milestone A works, expose/benchmark:
- context size;
- thread count;
- prompt batch size;
- prediction length;
- cold vs warm load time;
- first-token latency;
- generation tok/s;
- RAM pressure;
- 10-minute sustained temperature/battery behavior.

The upstream defaults are safe starting points, not permanent Aurion policy.

## Stage 3 — Qualcomm Adreno OpenCL bake-off
Current upstream llama.cpp OpenCL documentation explicitly lists Qualcomm Adreno support and verifies Snapdragon 8 Elite / Adreno 830-class hardware. That makes OpenCL the first GPU acceleration lane worth testing on the S25 Ultra after the CPU baseline.

Keep OpenCL a separate benchmarkable runtime profile. It must beat CPU on the metrics that matter to a phone assistant, not merely peak throughput.

Do not assume Vulkan is better. Treat Vulkan as a later candidate only after device-specific evidence.

## Stage 4 — Qualcomm NPU / ExecuTorch-QNN candidate
Keep `LocalModelRuntime` generic so a later ExecuTorch/QNN backend can be evaluated without rewriting Aurion identity, memory, action policy, or assistant UI.

NPU acceleration is interesting only if the supported model set and measured latency/efficiency are competitive for Aurion's actual workload.

## Model order
Start with:
1. Qwen3 4B Q4_K_M as the fast latency floor.
2. Qwen3 8B Q4-class as the stronger stretch candidate.
3. Gemma 4 E4B IT Q4-class only after the multimodal projector path is actually wired and tested.

Do not begin with a 20–30B-class Q4 model on the phone simply because the S25 Ultra is powerful. Weight memory is not the whole RAM budget; KV cache, runtime buffers, Android, assistant UI, speech, database, and thermal headroom all need space.

## Promotion rule
A runtime/model pair becomes a Pocket Aurion default only after it passes:
- functional assistant tests;
- local-only/security checks;
- latency thresholds;
- thermal/battery test;
- Aurion identity/anti-sycophancy/continuity regressions;
- action-envelope reliability tests.

The model is replaceable compute. Promotion changes the engine, not Aurion's identity.
