@echo off
chcp 65001 > nul
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0playwright\run-search-tests.ps1"
if errorlevel 1 (
  echo.
  echo テストの実行中に問題が発生しました。表示された結果画面を確認してください。
)
pause
