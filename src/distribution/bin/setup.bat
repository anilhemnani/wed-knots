@echo off
setlocal enabledelayedexpansion

REM ==========================================
REM WedKnots - Interactive Setup (Windows)
REM ==========================================
REM Reads ..\config.env as defaults, lets user edit, writes overrides to config\config.env

set "BIN_DIR=%~dp0"
for %%I in ("%BIN_DIR%..") do set "APP_ROOT=%%~fI"
cd /d "%APP_ROOT%"

set "PARENT_ENV=%APP_ROOT%\..\config.env"
set "CHILD_ENV_DIR=%APP_ROOT%\config"
set "CHILD_ENV=%CHILD_ENV_DIR%\config.env"

if not exist "%CHILD_ENV_DIR%" mkdir "%CHILD_ENV_DIR%"

REM Initialize default values
set "SPRING_PROFILES_ACTIVE=prod"
set "JASYPT_ENCRYPTOR_PASSWORD="
set "DATABASE_URL=jdbc:postgresql://localhost:5432/wedknots"
set "DATABASE_USERNAME=wedknots_user"
set "DATABASE_PASSWORD="
set "PORT=8080"
set "LOG_LEVEL=INFO"
set "LOG_FILE=logs/wedknots.log"
set "WHATSAPP_VERIFY_TOKEN="
set "WHATSAPP_APP_SECRET="

REM Seed defaults from parent config.env if present
if exist "%PARENT_ENV%" (
  echo Loading defaults from parent: "%PARENT_ENV%"
  for /f "usebackq delims=" %%A in ("%PARENT_ENV%") do (
    if not "%%A"=="" if /i not "%%A:~0,1%%"=="#" set "%%A"
  )
) else (
  echo No parent config.env found.
)

REM Prompt interactive (show defaults)
call :promptValue SPRING_PROFILES_ACTIVE
call :promptValue JASYPT_ENCRYPTOR_PASSWORD
call :promptValue DATABASE_URL
call :promptValue DATABASE_USERNAME
call :promptValue DATABASE_PASSWORD
call :promptValue PORT
call :promptValue LOG_LEVEL
call :promptValue LOG_FILE
call :promptValue WHATSAPP_VERIFY_TOKEN
call :promptValue WHATSAPP_APP_SECRET

REM Write overrides to child config.env
(
  echo # WedKnots Application - Overrides
  echo # Generated: %date% %time%
  echo SPRING_PROFILES_ACTIVE=!SPRING_PROFILES_ACTIVE!
  echo JASYPT_ENCRYPTOR_PASSWORD=!JASYPT_ENCRYPTOR_PASSWORD!
  echo DATABASE_URL=!DATABASE_URL!
  echo DATABASE_USERNAME=!DATABASE_USERNAME!
  echo DATABASE_PASSWORD=!DATABASE_PASSWORD!
  echo PORT=!PORT!
  echo LOG_LEVEL=!LOG_LEVEL!
  echo LOG_FILE=!LOG_FILE!
  echo WHATSAPP_VERIFY_TOKEN=!WHATSAPP_VERIFY_TOKEN!
  echo WHATSAPP_APP_SECRET=!WHATSAPP_APP_SECRET!
) > "%CHILD_ENV%"

echo.
echo Saved overrides to: "%CHILD_ENV%"
echo.
echo Setup complete.
exit /b 0


:promptValue
set "NAME=%~1"
REM Get the current value of the variable using call set
call set "CURRENT=!%NAME%!"
set /p INPUT="!NAME! [!CURRENT!]: "
if not "!INPUT!"=="" (
  set "%NAME%=!INPUT!"
)
exit /b 0

