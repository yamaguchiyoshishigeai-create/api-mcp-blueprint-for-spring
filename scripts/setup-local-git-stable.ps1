Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..")
Set-Location $RepoRoot

git remote set-head origin -a
git config --local gc.auto 0
git config --local maintenance.auto false
git config --local gc.autoDetach false

Write-Host "[OK] LOCAL_GIT_STABILITY_SETUP_DONE"
