@echo off
REM StoryTeller - Quick Start Script (Windows)
REM Run both backend and frontend in development mode

setlocal enabledelayedexpansion

echo.
echo 🚀 Starting StoryTeller...
echo.

REM Check prerequisites
echo 📋 Checking prerequisites...

python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python not found. Please install Python 3.9+
    pause
    exit /b 1
)
echo ✓ Python found

npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js/npm not found. Please install Node.js 18+
    pause
    exit /b 1
)
echo ✓ Node.js/npm found
echo.

REM Setup backend
echo 🔧 Setting up backend...

if not exist "backend\venv\" (
    echo Creating virtual environment...
    cd backend
    python -m venv venv
    cd ..
)

if not exist "backend\requirements.txt" (
    echo ❌ requirements.txt not found
    pause
    exit /b 1
)

REM Install dependencies if needed
call backend\venv\Scripts\activate.bat
python -c "import fastapi" >nul 2>&1
if errorlevel 1 (
    echo Installing Python dependencies...
    pip install -q -r backend\requirements.txt
)

echo ✓ Backend ready
echo.

REM Setup frontend
echo 🎨 Setting up frontend...

if not exist "frontend\node_modules\" (
    echo Installing Node dependencies...
    cd frontend
    call npm install -q
    cd ..
)

echo ✓ Frontend ready
echo.

REM Start services
echo 🎬 Starting services...
echo.

REM Start backend in new window
echo 🔌 Starting Backend (port 8000)...
cd backend
start "StoryTeller Backend" python run.py
cd ..

REM Wait a moment for backend to start
timeout /t 3 /nobreak

REM Start frontend in new window
echo 🖥️  Starting Frontend (port 5173)...
cd frontend
start "StoryTeller Frontend" npm run dev
cd ..

echo.
echo ════════════════════════════════════════════
echo ✓ StoryTeller is running!
echo ════════════════════════════════════════════
echo.
echo 📱 Frontend: http://localhost:5173
echo 🔌 Backend API: http://localhost:8000/docs
echo.
echo Close this window to stop the services.
echo.

pause

