$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Vendor = Join-Path $Root "vendor\llama.cpp"
$Ref = "f46bc30cb6a7f68a67e34a00061e20a4ad1eff43"
New-Item -ItemType Directory -Force -Path (Join-Path $Root "vendor") | Out-Null
if (-not (Test-Path (Join-Path $Vendor ".git"))) {
  git clone https://github.com/ggml-org/llama.cpp.git $Vendor
}
git -C $Vendor fetch --depth 1 origin $Ref
git -C $Vendor checkout --detach $Ref
Copy-Item (Join-Path $Vendor "examples\llama.android\gradle\libs.versions.toml") (Join-Path $Root "gradle\libs.versions.toml") -Force
Write-Host "llama.cpp pinned at $Ref"
