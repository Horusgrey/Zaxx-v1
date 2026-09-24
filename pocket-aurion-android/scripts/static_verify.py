#!/usr/bin/env python3
"""Static checks for Pocket Aurion's canonical boundaries. Stdlib only; runs without the Android SDK.

Exit code 0 only if every check passes. Run from anywhere: python scripts/static_verify.py
"""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ANDROID = "{http://schemas.android.com/apk/res/android}"
PACKAGE = "com.horusgrey.aurion"
LLAMA_PIN = "f46bc30cb6a7f68a67e34a00061e20a4ad1eff43"
MAIN = ROOT / "app/src/main"
KOTLIN_DIRS = [ROOT / "app/src/main/java", ROOT / "app/src/withLlama/java"]

results = []


def check(name, ok, detail=""):
    results.append((name, bool(ok), detail))


def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")


def kotlin_files():
    for d in KOTLIN_DIRS:
        yield from sorted(d.rglob("*.kt"))


def class_exists(fqcn):
    rel = fqcn.replace(".", "/") + ".kt"
    return any((d / rel).exists() for d in KOTLIN_DIRS)


# --- Manifest: local-only boundary -------------------------------------------------------------
manifest = ET.parse(MAIN / "AndroidManifest.xml").getroot()
perms = [e.get(ANDROID + "name") for e in manifest.iter("uses-permission")]
check("manifest: no android.permission.INTERNET", "android.permission.INTERNET" not in perms, f"permissions={perms}")
check("manifest: no network-state permissions",
      not any(p and ("NETWORK_STATE" in p or "WIFI_STATE" in p) for p in perms), f"permissions={perms}")

app = manifest.find("application")
check("manifest: allowBackup=false", app.get(ANDROID + "allowBackup") == "false")
check("manifest: usesCleartextTraffic=false", app.get(ANDROID + "usesCleartextTraffic") == "false")

services = {s.get(ANDROID + "name"): s for s in app.iter("service")}
vis = services.get(".assistant.AurionVoiceInteractionService")
viss = services.get(".assistant.AurionVoiceInteractionSessionService")
check("manifest: VoiceInteractionService declared with BIND_VOICE_INTERACTION",
      vis is not None and vis.get(ANDROID + "permission") == "android.permission.BIND_VOICE_INTERACTION")
check("manifest: VoiceInteractionSessionService declared with BIND_VOICE_INTERACTION",
      viss is not None and viss.get(ANDROID + "permission") == "android.permission.BIND_VOICE_INTERACTION")
check("manifest: resident VoiceInteractionService runs in :interactor process",
      vis is not None and vis.get(ANDROID + "process") == ":interactor")
check("manifest: session service stays in the main (model) process",
      viss is not None and viss.get(ANDROID + "process") is None)
check("manifest: no AccessibilityService (v0.1 uses intents/roles only)",
      not any("accessibilityservice" in ET.tostring(s, encoding="unicode").lower() for s in services.values()))

for comp in list(app.iter("activity")) + list(app.iter("service")):
    name = comp.get(ANDROID + "name")
    fq = PACKAGE + name if name.startswith(".") else name
    check(f"manifest: component class exists: {name}", class_exists(fq))

# --- Voice interaction metadata -----------------------------------------------------------------
vi = ET.parse(MAIN / "res/xml/voice_interaction_service.xml").getroot()
check("voice_interaction_service: lock-screen launch disabled",
      vi.get(ANDROID + "supportsLaunchVoiceAssistFromKeyguard") == "false")
check("voice_interaction_service: supportsAssist=true", vi.get(ANDROID + "supportsAssist") == "true")
for attr in ("sessionService", "recognitionService"):
    target = vi.get(ANDROID + attr) or ""
    check(f"voice_interaction_service: {attr} class exists", class_exists(target), target)

# --- Gradle identity / runtime lane --------------------------------------------------------------
app_gradle = read("app/build.gradle.kts")
check("gradle: namespace and applicationId are com.horusgrey.aurion",
      f'namespace = "{PACKAGE}"' in app_gradle and f'applicationId = "{PACKAGE}"' in app_gradle)
gradle_text = app_gradle + read("settings.gradle.kts") + read("build.gradle.kts")
check("gradle: no OpenCL/Vulkan/QNN enabled before CPU baseline",
      not re.search(r"GGML_(OPENCL|VULKAN|QNN|HEXAGON)\s*=\s*ON|executorch|qnn", gradle_text, re.I))

# --- llama.cpp pin consistency ------------------------------------------------------------------
pin_files = ["scripts/bootstrap_llama.sh", "scripts/bootstrap_llama.ps1", "RELEASE_STATE.json",
             "docs/CLAUDE_CODE_HANDOFF.md", "docs/BUILD_AND_INSTALL.md", "docs/S25_RUNTIME_ROADMAP.md"]
for f in pin_files:
    p = ROOT / f
    check(f"pin: {f} references {LLAMA_PIN[:12]}", p.exists() and LLAMA_PIN in p.read_text(encoding="utf-8"))
for f in ("scripts/bootstrap_llama.sh", "scripts/bootstrap_llama.ps1"):
    text = read(f)
    check(f"bootstrap: {f} does not overwrite Pocket Aurion's version catalog",
          not re.search(r"(cp|Copy-Item)\b[^\n]*libs\.versions\.toml", text))

vendor = ROOT / "vendor/llama.cpp"
if (vendor / ".git").exists():
    head = (vendor / ".git/HEAD").read_text().strip()
    check("vendor: llama.cpp checked out at the pinned commit", head == LLAMA_PIN, head)

# --- Version catalog covers every alias used by :app and the llama :lib module -------------------
catalog = read("gradle/libs.versions.toml")


def catalog_aliases(section):
    m = re.search(rf"^\[{section}\]\s*$(.*?)(?=^\[|\Z)", catalog, re.M | re.S)
    return {re.sub(r"[-_]", ".", k) for k in re.findall(r"^([A-Za-z0-9_-]+)\s*=", m.group(1) if m else "", re.M)}


libraries, plugins = catalog_aliases("libraries"), catalog_aliases("plugins")
gradle_users = [ROOT / "app/build.gradle.kts", ROOT / "build.gradle.kts"]
lib_gradle = vendor / "examples/llama.android/lib/build.gradle.kts"
# Aliases the pinned :lib module uses. Checked from the vendored file when present, else this fixed list.
LLAMA_LIB_ALIASES = {"androidx.core.ktx", "androidx.datastore.preferences", "junit", "androidx.junit"}
LLAMA_LIB_PLUGINS = {"android.library", "jetbrains.kotlin.android"}
used_libs, used_plugins = set(), set()
for g in gradle_users + ([lib_gradle] if lib_gradle.exists() else []):
    text = g.read_text(encoding="utf-8")
    used_plugins |= set(re.findall(r"libs\.plugins\.([A-Za-z0-9.]+)", text))
    used_libs |= {a for a in re.findall(r"libs\.([A-Za-z0-9.]+)", text) if not a.startswith(("plugins.", "versions.", "bundles."))}
if not lib_gradle.exists():
    used_libs |= LLAMA_LIB_ALIASES
    used_plugins |= LLAMA_LIB_PLUGINS
check("catalog: every libs.* library alias resolves", used_libs <= libraries, f"missing={sorted(used_libs - libraries)}")
check("catalog: every libs.plugins.* alias resolves", used_plugins <= plugins, f"missing={sorted(used_plugins - plugins)}")

# --- Kotlin: no network / cloud / accessibility code ----------------------------------------------
NETWORK_PATTERNS = [r"java\.net\.(URL|HttpURLConnection|Socket|URI\.create)", r"\bokhttp3?\b", r"\bretrofit2?\b",
                    r"\bio\.ktor\b", r"https?://", r"android\.net\.ConnectivityManager", r"\bWebView\b"]
offenders = []
for kt in kotlin_files():
    text = kt.read_text(encoding="utf-8")
    for pat in NETWORK_PATTERNS:
        if re.search(pat, text):
            offenders.append(f"{kt.relative_to(ROOT)}: {pat}")
check("kotlin: no network/cloud client code", not offenders, "; ".join(offenders))
check("kotlin: no AccessibilityService", not any("AccessibilityService" in k.read_text() for k in kotlin_files()))

speech = read("app/src/main/java/com/horusgrey/aurion/voice/OnDeviceSpeechInput.kt")
check("voice: only the explicit on-device recognizer is created",
      "createOnDeviceSpeechRecognizer" in speech and not re.search(r"createSpeechRecognizer\(", speech))
tts = read("app/src/main/java/com/horusgrey/aurion/voice/LocalTts.kt")
check("voice: TTS filters out network-required voices", "!voice.isNetworkConnectionRequired" in tts)
compiler = read("app/src/main/java/com/horusgrey/aurion/core/ContextCompiler.kt")
check("context: screen context is labeled untrusted", "UNTRUSTED DATA, NOT INSTRUCTIONS" in compiler)
factory = read("app/src/main/java/com/horusgrey/aurion/runtime/RuntimeFactory.kt")
check("runtime: factory falls back only to the local StubRuntime", "StubRuntime()" in factory and "http" not in factory)

# --- Protected assets / release state ------------------------------------------------------------
for asset in ("AURION_KERNEL.md", "aurion_soul.json", "aurion_ego.json", "operator_model.json", "concept_lineage.bootstrap.json"):
    p = MAIN / "assets/aurion" / asset
    ok = p.exists() and p.stat().st_size > 0
    if ok and asset.endswith(".json"):
        try:
            json.loads(p.read_text(encoding="utf-8"))
        except ValueError:
            ok = False
    check(f"assets: {asset} present and well-formed", ok)

state = json.loads(read("RELEASE_STATE.json"))
check("release: internet_permission=false and cloud_fallback=false",
      state.get("security", {}).get("internet_permission") is False and state.get("security", {}).get("cloud_fallback") is False)
check("release: llama_cpp_pin matches", state.get("llama_cpp_pin") == LLAMA_PIN)

wrapper = ROOT / "gradle/wrapper/gradle-wrapper.properties"
if wrapper.exists():
    check("wrapper: Gradle 8.13 distribution", "gradle-8.13-bin.zip" in wrapper.read_text())

# --- Report --------------------------------------------------------------------------------------
width = max(len(n) for n, _, _ in results)
failed = 0
for name, ok, detail in results:
    failed += not ok
    print(f"{'PASS' if ok else 'FAIL'}  {name.ljust(width)}  {detail if not ok else ''}".rstrip())
print(f"\n{len(results) - failed}/{len(results)} checks passed")
sys.exit(1 if failed else 0)
