@echo off
REM ========================================
REM   OPENROUTER SETUP
REM ========================================

echo.
echo =========================================
echo   OPENROUTER SETUP
echo =========================================
echo.

REM Create backend directory
if not exist "backend" mkdir backend

echo OpenRouter Configuration
echo.
echo Please enter your OpenRouter API key
echo (Get it from: https://openrouter.ai/settings/keys)
echo.
set /p API_KEY="API Key (sk-or-v1-...): "

if "%API_KEY%"=="" (
    echo [!] No API key provided. Exiting.
    pause
    exit /b 1
)

echo.
echo Choose a model:
echo.
echo FREE Models (recommended to start):
echo   1) meta-llama/llama-3.1-8b-instruct:free (Good, fast)
echo   2) google/gemma-2-9b-it:free (Creative)
echo   3) mistralai/mistral-7b-instruct:free (Fast)
echo.
echo PAID Models (better quality):
echo   4) anthropic/claude-3.5-sonnet (Best quality, ~$3-15/M tokens)
echo   5) anthropic/claude-3-haiku (Good ^& cheap, ~$0.25-1.25/M tokens)
echo   6) meta-llama/llama-3.1-70b-instruct (Great quality, ~$0.35/M tokens)
echo   7) openai/gpt-4-turbo (Excellent, ~$10-30/M tokens)
echo.
set /p MODEL_CHOICE="Choice (1-7, default=1): "

if "%MODEL_CHOICE%"=="2" (
    set MODEL=google/gemma-2-9b-it:free
) else if "%MODEL_CHOICE%"=="3" (
    set MODEL=mistralai/mistral-7b-instruct:free
) else if "%MODEL_CHOICE%"=="4" (
    set MODEL=anthropic/claude-3.5-sonnet
) else if "%MODEL_CHOICE%"=="5" (
    set MODEL=anthropic/claude-3-haiku
) else if "%MODEL_CHOICE%"=="6" (
    set MODEL=meta-llama/llama-3.1-70b-instruct
) else if "%MODEL_CHOICE%"=="7" (
    set MODEL=openai/gpt-4-turbo
) else (
    set MODEL=meta-llama/llama-3.1-8b-instruct:free
)

REM Backup existing .env if it exists
if exist "backend\.env" (
    copy "backend\.env" "backend\.env.backup" >nul
    echo [*] Backed up existing .env to .env.backup
)

REM Create .env file
(
echo.
echo # ===== OpenRouter Configuration =====
echo LLM_PROVIDER=openrouter
echo OPENROUTER_API_KEY=%API_KEY%
echo OPENROUTER_MODEL=%MODEL%
echo # ====================================
) >> backend\.env

echo.
echo [OK] Configuration saved to backend\.env
echo.
echo Selected model: %MODEL%
echo.

REM Check if openai is installed
python -c "import openai" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [*] Installing OpenAI package (required for OpenRouter)...
    pip install openai
) else (
    echo [OK] OpenAI package already installed
)

echo.
echo =========================================
echo   SETUP COMPLETE!
echo =========================================
echo.
echo Next steps:
echo.
echo 1. Start your FastAPI server:
echo    cd backend
echo    uvicorn main:app --reload
echo.
echo 2. Visit your app and check:
echo    [✓] LLM: openrouter - Configured
echo.
echo 3. Monitor usage at:
echo    https://openrouter.ai/activity
echo.

echo %MODEL% | findstr ":free" >nul
if %ERRORLEVEL% EQU 0 (
    echo [FREE] You're using a FREE model - no costs!
) else (
    echo [$$$] You're using a PAID model.
    echo       Monitor costs at: https://openrouter.ai/activity
    echo       Set limits at: https://openrouter.ai/settings/limits
)

echo.
echo Full documentation in OPENROUTER_SETUP.md
echo.
echo Happy writing!
echo.
pause
