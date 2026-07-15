@echo off
setlocal
set "EIMS_FEATURE=search"
start "" powershell.exe -NoLogo -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File "%~dp0playwright\launch-dashboard.ps1"
exit /b 0
