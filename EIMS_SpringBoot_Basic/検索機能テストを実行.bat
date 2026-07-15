@echo off
setlocal
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0playwright\run-search-tests.ps1"
set "EIMS_TEST_EXIT=%ERRORLEVEL%"
echo.
pause
exit /b %EIMS_TEST_EXIT%
