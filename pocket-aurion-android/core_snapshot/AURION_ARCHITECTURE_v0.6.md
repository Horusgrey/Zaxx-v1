# Architecture

## Durable layers

```text
LOCAL UI / CLI
      │
      ▼
AURION ORCHESTRATOR
      │
      ├── Authority + project router
      ├── Memory compiler
      ├── Hybrid retrieval
      ├── Model profile router
      ├── Tool broker + approvals
      ├── Audit / eval / handoff
      │
      ├──────────────► SQLite state
      │
      ├──────────────► local source snapshots
      │
      └──────────────► OpenAI-compatible localhost inference
                         ├── reasoning model
                         ├── code/creative model
                         └── multimodal/mobile model
```

## Why the agent loop is outside llama.cpp

The inference runtime is responsible for model execution. Aurion owns:
- memory;
- authority;
- project state;
- tools;
- approvals;
- retrieval;
- security;
- evaluation.

That keeps model/runtime upgrades from silently changing the user's operating system.

## Model routing

`profile=auto` uses deterministic intent cues:
- code → code profile
- evidence/research → research
- image/audio → vision
- creative/story → creative
- otherwise → general

The router may fall back only to another **local** model listed by `/v1/models`. It never falls back to a cloud API.

## Context packing

System:
- stable Aurion Kernel only.

User context envelope:
- current task;
- active project state;
- durable memories relevant to project;
- canonical context;
- active retrieval;
- source IDs.

Retrieved material is wrapped as untrusted data.

## Retrieval

Foundation retrieval uses:
- FTS5 BM25;
- optional local embeddings;
- reciprocal rank fusion;
- source diversity cap;
- project/status filtering.

This avoids committing to a single vector database before corpus size proves one necessary.

## Agent/tool loop

Tool calls are accepted only after:
- schema validation;
- path validation;
- risk classification;
- approval policy.

Read-only calls within the approved workspace can auto-execute.
Mutating/destructive/execution calls enter the approval queue.

## Storage

SQLite is the initial system of record because it is:
- local;
- portable;
- inspectable;
- backup-friendly;
- sufficient for one user's control plane.

Model weights and raw source snapshots remain files, not SQLite blobs.
