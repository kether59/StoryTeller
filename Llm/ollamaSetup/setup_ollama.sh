#!/bin/bash

# 🚀 Quick Start Script for FREE Local LLM with Ollama

echo "========================================="
echo "  FREE LOCAL LLM SETUP - OLLAMA"
echo "========================================="
echo ""

# Check if Ollama is installed
if command -v ollama &> /dev/null; then
    echo "✅ Ollama is already installed!"
else
    echo "❌ Ollama not found. Installing..."
    echo ""
    
    # Detect OS
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "📦 Installing Ollama for Linux..."
        curl -fsSL https://ollama.com/install.sh | sh
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "📦 Installing Ollama for macOS..."
        curl -fsSL https://ollama.com/install.sh | sh
    else
        echo "⚠️  Windows detected. Please download Ollama from:"
        echo "   https://ollama.com/download"
        echo ""
        echo "After installation, run this script again."
        exit 1
    fi
fi

echo ""
echo "========================================="
echo "  DOWNLOADING MODEL"
echo "========================================="
echo ""

# Check if mistral model exists
if ollama list | grep -q "mistral"; then
    echo "✅ Mistral model already downloaded!"
else
    echo "📥 Downloading Mistral model (4GB)..."
    echo "   This may take a few minutes..."
    ollama pull mistral
fi

echo ""
echo "========================================="
echo "  TESTING OLLAMA"
echo "========================================="
echo ""

# Test Ollama
echo "🧪 Testing Ollama with a simple prompt..."
echo ""
RESPONSE=$(ollama run mistral "Dis bonjour en une phrase courte" --verbose 2>&1 | tail -n 1)
echo "Response: $RESPONSE"

if [ -n "$RESPONSE" ]; then
    echo ""
    echo "✅ Ollama is working correctly!"
else
    echo ""
    echo "❌ Ollama test failed. Try running: ollama serve"
    exit 1
fi

echo ""
echo "========================================="
echo "  CONFIGURING YOUR APP"
echo "========================================="
echo ""

# Create or update .env file
ENV_FILE="backend/.env"

if [ -f "$ENV_FILE" ]; then
    echo "📝 Updating existing .env file..."
    # Comment out any existing LLM_PROVIDER
    sed -i.bak 's/^LLM_PROVIDER=/#LLM_PROVIDER=/g' "$ENV_FILE"
    sed -i.bak 's/^OLLAMA_URL=/#OLLAMA_URL=/g' "$ENV_FILE"
else
    echo "📝 Creating new .env file..."
    mkdir -p backend
fi

# Add Ollama configuration
cat >> "$ENV_FILE" << 'EOF'

# ===== FREE LOCAL LLM CONFIGURATION =====
LLM_PROVIDER=ollama
OLLAMA_URL=http://localhost:11434
# ========================================

EOF

echo "✅ Configuration saved to $ENV_FILE"

echo ""
echo "========================================="
echo "  INSTALLATION COMPLETE! 🎉"
echo "========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. Make sure Ollama is running:"
echo "   $ ollama serve"
echo ""
echo "2. Install Python dependencies:"
echo "   $ pip install httpx"
echo ""
echo "3. Start your FastAPI server:"
echo "   $ cd backend"
echo "   $ uvicorn main:app --reload"
echo ""
echo "4. Test in your UI - you should see:"
echo "   🤖 LLM: ollama ✅ Configured"
echo ""
echo "========================================="
echo ""
echo "📚 Available models:"
echo "   - mistral (current, 4GB)"
echo "   - mixtral (better but larger, 26GB)"
echo "   - llama3 (alternative, 4.7GB)"
echo ""
echo "To switch models:"
echo "   $ ollama pull <model-name>"
echo ""
echo "========================================="
echo ""
echo "💡 Tips:"
echo "  - Ollama runs in the background automatically"
echo "  - No API keys needed - 100% FREE!"
echo "  - All processing happens on your computer"
echo "  - Works offline after model download"
echo ""
echo "Happy writing! ✍️✨"
echo ""
