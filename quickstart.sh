#!/bin/bash
# StoryTeller - Quick Start Script
# Run both backend and frontend in development mode

set -e

echo "🚀 Starting StoryTeller..."
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
check_prerequisites() {
    echo "📋 Checking prerequisites..."

    # Check Python
    if ! command -v python3 &> /dev/null; then
        echo -e "${RED}❌ Python 3 not found. Please install Python 3.9+${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Python 3 found${NC}"

    # Check Node.js
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}❌ Node.js/npm not found. Please install Node.js 18+${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Node.js/npm found${NC}"
    echo ""
}

# Setup backend
setup_backend() {
    echo "🔧 Setting up backend..."

    if [ ! -d "backend/venv" ]; then
        echo "Creating virtual environment..."
        cd backend
        python3 -m venv venv
        cd ..
    fi

    # Activate venv and install dependencies
    source backend/venv/bin/activate
    if [ ! -f "backend/requirements.txt" ]; then
        echo -e "${RED}❌ requirements.txt not found${NC}"
        exit 1
    fi

    # Check if dependencies are installed
    if ! python3 -c "import fastapi" 2>/dev/null; then
        echo "Installing Python dependencies..."
        pip install -q -r backend/requirements.txt
    fi

    echo -e "${GREEN}✓ Backend ready${NC}"
    echo ""
}

# Setup frontend
setup_frontend() {
    echo "🎨 Setting up frontend..."

    if [ ! -d "frontend/node_modules" ]; then
        echo "Installing Node dependencies..."
        cd frontend
        npm install -q
        cd ..
    fi

    echo -e "${GREEN}✓ Frontend ready${NC}"
    echo ""
}

# Start services
start_services() {
    echo "🎬 Starting services..."
    echo ""

    # Start backend in background
    source backend/venv/bin/activate
    echo "🔌 Starting Backend (port 8000)..."
    cd backend
    python run.py > /tmp/storyteller-backend.log 2>&1 &
    BACKEND_PID=$!
    cd ..

    # Give backend time to start
    sleep 3

    # Start frontend in background
    echo "🖥️  Starting Frontend (port 5173)..."
    cd frontend
    npm run dev > /tmp/storyteller-frontend.log 2>&1 &
    FRONTEND_PID=$!
    cd ..

    echo ""
    echo -e "${GREEN}════════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓ StoryTeller is running!${NC}"
    echo -e "${GREEN}════════════════════════════════════════════${NC}"
    echo ""
    echo "📱 Frontend: http://localhost:5173"
    echo "🔌 Backend API: http://localhost:8000/docs"
    echo ""
    echo "Press Ctrl+C to stop..."
    echo ""

    # Handle stop
    trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo ''; echo 'Stopped.'; exit 0" SIGINT

    # Wait for processes
    wait
}

# Main execution
main() {
    check_prerequisites
    setup_backend
    setup_frontend
    start_services
}

main

