@echo off
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Requesting administrator privileges...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

set "BIN_DIR=%~dp0"
set APP_ROOT=%BIN_DIR%..
sc query "WedKnots-Springboot" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Service exists - updating nssm configuration...
    C:\tools\nssm-2.24\win64\nssm.exe set "WedKnots-Springboot" Application "%BIN_DIR%start.bat"
    C:\tools\nssm-2.24\win64\nssm.exe set "WedKnots-Springboot" AppDirectory "%APP_ROOT%"
    C:\tools\nssm-2.24\win64\nssm.exe set "WedKnots-Springboot" Start SERVICE_AUTO_START
    C:\tools\nssm-2.24\win64\nssm.exe set "WedKnots-Springboot" ObjectName LocalSystem
) else (
    echo Service not found - installing...
    C:\tools\nssm-2.24\win64\nssm.exe install "WedKnots-Springboot" "%BIN_DIR%start.bat" AppDirectory "%APP_ROOT%" Start SERVICE_AUTO_START ObjectName LocalSystem
)
sc stop "WedKnots-Springboot" >nul 2>&1
sc start "WedKnots-Springboot"
pause