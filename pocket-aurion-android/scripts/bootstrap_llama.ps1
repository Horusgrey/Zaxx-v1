$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Vendor = Join-Path $Root "vendor\llama.cpp"
$Ref = "f46bc30cb6a7f68a67e34a00061e20a4ad1eff43"
New-Item -ItemType Directory -Force -Path $Vendor | Out-Null
if (-not (Test-Path (Join-Path $Vendor ".git"))) {
  git -C $Vendor init -q
  git -C $Vendor remote add origin https://github.com/ggml-org/llama.cpp.git
}
git -C $Vendor fetch --depth 1 origin $Ref
git -C $Vendor checkout --detach $Ref
# Pocket Aurion keeps its own gradle/libs.versions.toml. It already carries every alias the
# llama.android :lib module needs; scripts/static_verify.py checks that instead of overwriting it.
Write-Host "llama.cpp pinned at $(git -C $Vendor rev-parse HEAD)"
