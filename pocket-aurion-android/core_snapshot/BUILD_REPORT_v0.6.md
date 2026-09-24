# AURION LOCAL OS 0.6.0 - Build Report

**Project ID:** PRJ-AURION-LLM-81026  
**Release:** 0.6.0 - Cognitive Control Plane  
**Date:** 2026-08-10  
**Status:** Software release candidate; target-hardware/model acceptance pending.

## Verified

- Automated regression/integration/security tests: **25 / 25 pass**
- Python source/test/script files syntax compiled: **52**
- SQLite schema: **v6**
- Clean initialization: **7 projects**, **6 bootstrap memories**, **7 project locks**
- Local control-room API smoke: **PASS**
- UI JavaScript syntax: **PASS**
- End-to-end OpenAI-compatible mock-model orchestration: **PASS**
- Application strict-offline checks: **PASS**
- Runtime DB/private state removed from distribution tree: **PASS**

## 0.6 architecture

- Temporal memory with volatility, validity, verification and expiry.
- Raw episodic work history preserved independently of consolidation.
- Claim / evidence / conflict ledger.
- Correction-driven regression guards.
- Project locks for canon, visuals, architecture, workflow, style and security.
- Open-loop focus engine.
- Task-scoped capability leases.
- Shadow Build staging transactions with base-hash promotion gate.
- Named validator execution against materialized candidate overlays.
- Context budgets and context receipts.
- Local model scorecards and Shadow League comparison.
- Council Mode: Advocate, Red Team, Historian, Operator, then adjudication.
- Project Capsules and portable device mesh.
- Restricted physical vault databases, excluded from ordinary retrieval.
- Exact-project private routing; global private data excluded from unrelated contexts.
- Selective read-only Drive snapshot planner.
- Read-only MCP v2 interoperability edge.
- Cold-start recovery audit.

## Security posture

Normal model tools do not expose:
- web/network tools;
- arbitrary shell;
- arbitrary Python execution;
- direct canonical model writes.

Model-authored edits stage first. Canonical promotion requires a separately approved transaction commit.

Restricted vault data is not visible to normal retrieval. Explicit vault access is a separate code path.

Application-level offline checks are not host-level network isolation. Use host firewall/network isolation for high-assurance deployments.

## Cold-start status

**NOT READY by design.**

Missing physical recovery assets:
- local llama.cpp/runtime bundle;
- at least one accepted GGUF model and checksum;
- host Python 3.11+ recovery/installer path.

Optional wheelhouse is also not yet staged.

## Pending empirical gates

1. Import the real primary-computer hardware report.
2. Install local inference runtime on that host.
3. Benchmark at least two candidate local models.
4. Run model-backed Aurion regression/security/continuity evaluations.
5. Accept profile defaults only from local scorecards.
6. Build the physical Cold-Start Kit.
7. Run Pocket Aurion separately on the intended mobile host.
8. Create the dedicated private GitHub repository from this release.

## Release principle

The model is replaceable compute. Aurion is the durable control plane around it.
