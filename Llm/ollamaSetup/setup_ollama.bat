@echo off
REM ========================================
REM   FREE LOCAL LLM SETUP - OLLAMA
REM ========================================

echo.
echo =========================================
echo   FREE LOCAL LLM SETUP - OLLAMA
echo =========================================
echo.

REM Check if Ollama is installed
where ollama >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Ollama is already installed!
) else (
    echo [!] Ollama not found!
    echo.
    echo Please download and install Ollama from:
    echo https://ollama.com/download
    echo.
    echo After installation:
    echo 1. Restart this script
    echo 2. Or continue manually with the steps below
    echo.
    pause
    exit /b 1
)

echo.
echo =========================================
echo   DOWNLOADING MODEL
echo =========================================
echo.

REM Check if mistral model exists
ollama list | findstr "mistral" >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Mistral model already downloaded!
) else (
    echo [*] Downloading Mistral model (4GB)...
    echo     This may take a few minutes...
    ollama pull mistral
)

echo.
echo =========================================
echo   TESTING OLLAMA
echo =========================================
echo.

echo [*] Testing Ollama with a simple prompt...
ollama run mistral "Dis bonjour en une phrase courte"

echo.
echo [OK] Ollama is working!

echo.
echo =========================================
echo   CONFIGURING YOUR APP
echo =========================================
echo.

REM Create backend directory if it doesn't exist
if not exist "backend" mkdir backend

REM Create .env file
echo [*] Creating .env configuration...
(
echo.
echo # ===== FREE LOCAL LLM CONFIGURATION =====
echo LLM_PROVIDER=ollama
echo OLLAMA_URL=http://localhost:11434
echo # ========================================
) >> backend\.env

echo [OK] Configuration saved to backend\.env

echo.
echo =========================================
echo   INSTALLATION COMPLETE!
echo =========================================
echo.
echo Next steps:
echo.
echo 1. Install Python dependencies:
echo    pip install httpx
echo.
echo 2. Start your FastAPI server:
echo    cd backend
echo    uvicorn main:app --reload
echo.
echo 3. Test in your UI - you should see:
echo    [✓] LLM: ollama - Configured
echo.
echo =========================================
echo.
echo Available models:
echo   - mistral (current, 4GB)
echo   - mixtral (better but larger, 26GB)  
echo   - llama3 (alternative, 4.7GB)
echo.
echo To switch models:
echo    ollama pull ^<model-name^>
echo.
echo =========================================
echo.
echo Tips:
echo  - Ollama runs automatically in the background
echo  - No API keys needed - 100%% FREE!
echo  - All processing happens on your computer
echo  - Works offline after model download
echo.
echo Happy writing!
echo.
pause
