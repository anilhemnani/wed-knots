@echo off
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Requesting administrator privileges...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

set "BIN_DIR=%~dp0"
set APP_ROOT=%BIN_DIR%..
C:\tools\nssm-2.24\win64\nssm.exe remove WedKnots-Springboot confirm
C:\tools\nssm-2.24\win64\nssm.exe install "WedKnots-Springboot" "%APP_ROOT%bin\start.bat" AppDirectory "%APP_ROOT%" Start SERVICE_AUTO_START
sc start WedKnots-Springboot
nssm.exe set WedKnots-Springboot ObjectName LocalSystem
pause