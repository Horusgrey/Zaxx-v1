# Pocket Aurion — Galaxy S25 Ultra Lane

The phone is an intended local host, but it is not treated as equivalent to a desktop workstation.

## Goals

- fully local chat when disconnected;
- project/memory lookup from a curated local snapshot;
- small-model reasoning;
- voice/image input where the selected model/runtime supports it;
- offline capture and handoff generation;
- physical/manual sync back to the primary machine when desired.

## Runtime

llama.cpp supports Android builds and Termux workflows.

This package includes:
- `scripts/install_termux.sh`
- `scripts/start_termux.sh`
- mobile profile in runtime configuration.

## Recommended architecture

**Pocket Aurion**
- small model;
- curated memory;
- current-project canon only;
- local SQLite;
- no full Drive mirror;
- no background WAN dependency.

**Primary Aurion**
- larger reasoning/code/multimodal models;
- full curated corpus;
- eval runner;
- build tools.

## Why separate lanes

Trying to make the phone load every project and every model would recreate the exact context-bloat problem the harness is designed to avoid, now with more heat and less battery.
