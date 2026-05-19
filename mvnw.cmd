@echo off
setlocal

set SCRIPT_DIR=%~dp0
set WRAPPER_PS1=%SCRIPT_DIR%.mvn\wrapper\mvnw-wrapper.ps1

if not exist "%WRAPPER_PS1%" (
  echo [ERROR] Maven wrapper PowerShell script not found: %WRAPPER_PS1% 1>&2
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%WRAPPER_PS1%" %*
exit /b %ERRORLEVEL%
