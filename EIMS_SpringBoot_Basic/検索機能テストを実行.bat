@echo off
setlocal
start "" powershell.exe -NoLogo -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File "%~dp0playwright\launch-dashboard.ps1"
exit /b 0
