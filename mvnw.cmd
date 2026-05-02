@echo off
setlocal

set SCRIPT_DIR=%~dp0
set WRAPPER_PROPS=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_PROPS%" (
  echo [ERROR] Maven wrapper properties not found: %WRAPPER_PROPS% 1>&2
  exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPS%") do (
  if "%%A"=="distributionUrl" set DISTRIBUTION_URL=%%B
)

if "%DISTRIBUTION_URL%"=="" (
  echo [ERROR] distributionUrl is not defined in %WRAPPER_PROPS% 1>&2
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "^$ErrorActionPreference='Stop'; ^$url='%DISTRIBUTION_URL%'; ^$home=[Environment]::GetFolderPath('UserProfile'); ^$m2=Join-Path ^$home '.m2'; ^$distRoot=Join-Path ^$m2 'wrapper\dists'; ^$fileName=Split-Path ^$url -Leaf; ^$mainName=^$fileName -replace '-bin\.zip$',''; ^$targetRoot=Join-Path ^$distRoot ^$mainName; ^$mavenHome=Join-Path ^$targetRoot ^$mainName; ^$mvn=Join-Path ^$mavenHome 'bin\mvn.cmd'; if (!(Test-Path ^$mvn)) { New-Item -ItemType Directory -Force -Path ^$targetRoot | Out-Null; ^$zip=Join-Path ^$targetRoot ^$fileName; if (!(Test-Path ^$zip)) { Write-Host ('Downloading Maven from ' + ^$url); Invoke-WebRequest -Uri ^$url -OutFile ^$zip }; ^$tmp=Join-Path ^$targetRoot 'extract'; if (Test-Path ^$tmp) { Remove-Item ^$tmp -Recurse -Force }; New-Item -ItemType Directory -Force -Path ^$tmp | Out-Null; Expand-Archive -Path ^$zip -DestinationPath ^$tmp -Force; ^$dir=Get-ChildItem ^$tmp -Directory | Select-Object -First 1; if (!(Test-Path ^$mavenHome)) { Move-Item ^$dir.FullName ^$mavenHome } ; Remove-Item ^$tmp -Recurse -Force }; & ^$mvn @args" -- %*

exit /b %ERRORLEVEL%
