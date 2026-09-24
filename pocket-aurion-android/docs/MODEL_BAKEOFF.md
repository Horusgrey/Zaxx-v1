# Pocket Aurion model bake-off

No permanent model is crowned before the S25 Ultra measures it.

First practical lane:
1. Gemma 4 E4B IT GGUF quantized for mobile: first multimodal-capable candidate family.
2. A strong current 4B–9B text model in GGUF: compare responsiveness/quality against Gemma.
3. Optional larger candidate only if measured memory headroom and sustained thermals justify it.

Score each model on:
- cold load time;
- prompt processing tokens/sec;
- generation tokens/sec;
- peak RAM / Android memory pressure;
- 10-minute sustained thermal behavior;
- battery cost;
- instruction following;
- anti-sycophancy regression;
- project continuity regression;
- correction retention;
- concise assistant latency;
- long-form reasoning quality.

A phone assistant has a stricter latency target than a desktop reasoning agent. A smarter model that takes 18 seconds to begin speaking may lose to a slightly smaller model that begins in 1–3 seconds.
