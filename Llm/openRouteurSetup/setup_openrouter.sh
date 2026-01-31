#!/bin/bash

# 🌐 OpenRouter Quick Setup Script

echo "========================================="
echo "  OPENROUTER SETUP"
echo "========================================="
echo ""

# Check if .env exists
ENV_FILE="backend/.env"
mkdir -p backend

echo "📝 OpenRouter Configuration"
echo ""
echo "Please enter your OpenRouter API key"
echo "(Get it from: https://openrouter.ai/settings/keys)"
echo ""
read -p "API Key (sk-or-v1-...): " API_KEY

if [ -z "$API_KEY" ]; then
    echo "❌ No API key provided. Exiting."
    exit 1
fi

echo ""
echo "Choose a model:"
echo ""
echo "FREE Models (recommended to start):"
echo "  1) meta-llama/llama-3.1-8b-instruct:free (Good, fast)"
echo "  2) google/gemma-2-9b-it:free (Creative)"
echo "  3) mistralai/mistral-7b-instruct:free (Fast)"
echo ""
echo "PAID Models (better quality):"
echo "  4) anthropic/claude-3.5-sonnet (Best quality, ~$3-15/M tokens)"
echo "  5) anthropic/claude-3-haiku (Good & cheap, ~$0.25-1.25/M tokens)"
echo "  6) meta-llama/llama-3.1-70b-instruct (Great quality, ~$0.35/M tokens)"
echo "  7) openai/gpt-4-turbo (Excellent, ~$10-30/M tokens)"
echo ""
read -p "Choice (1-7, default=1): " MODEL_CHOICE

case $MODEL_CHOICE in
    2)
        MODEL="google/gemma-2-9b-it:free"
        ;;
    3)
        MODEL="mistralai/mistral-7b-instruct:free"
        ;;
    4)
        MODEL="anthropic/claude-3.5-sonnet"
        ;;
    5)
        MODEL="anthropic/claude-3-haiku"
        ;;
    6)
        MODEL="meta-llama/llama-3.1-70b-instruct"
        ;;
    7)
        MODEL="openai/gpt-4-turbo"
        ;;
    *)
        MODEL="meta-llama/llama-3.1-8b-instruct:free"
        ;;
esac

# Backup existing .env if it exists
if [ -f "$ENV_FILE" ]; then
    cp "$ENV_FILE" "${ENV_FILE}.backup"
    echo "📦 Backed up existing .env to .env.backup"
    
    # Comment out old LLM settings
    sed -i.tmp 's/^LLM_PROVIDER=/#LLM_PROVIDER=/g' "$ENV_FILE"
    sed -i.tmp 's/^ANTHROPIC_API_KEY=/#ANTHROPIC_API_KEY=/g' "$ENV_FILE"
    sed -i.tmp 's/^OPENAI_API_KEY=/#OPENAI_API_KEY=/g' "$ENV_FILE"
    sed -i.tmp 's/^OPENROUTER_API_KEY=/#OPENROUTER_API_KEY=/g' "$ENV_FILE"
    sed -i.tmp 's/^OPENROUTER_MODEL=/#OPENROUTER_MODEL=/g' "$ENV_FILE"
    sed -i.tmp 's/^OLLAMA_URL=/#OLLAMA_URL=/g' "$ENV_FILE"
    rm -f "${ENV_FILE}.tmp"
fi

# Add OpenRouter configuration
cat >> "$ENV_FILE" << EOF

# ===== OpenRouter Configuration =====
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=$API_KEY
OPENROUTER_MODEL=$MODEL
# ====================================

EOF

echo ""
echo "✅ Configuration saved to $ENV_FILE"
echo ""
echo "Selected model: $MODEL"
echo ""

# Check if openai is installed
if ! python3 -c "import openai" 2>/dev/null; then
    echo "📦 Installing OpenAI package (required for OpenRouter)..."
    pip install openai
else
    echo "✅ OpenAI package already installed"
fi

echo ""
echo "========================================="
echo "  SETUP COMPLETE! 🎉"
echo "========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. Start your FastAPI server:"
echo "   cd backend"
echo "   uvicorn main:app --reload"
echo ""
echo "2. Visit your app and check:"
echo "   🤖 LLM: openrouter ✅ Configured"
echo ""
echo "3. Monitor usage at:"
echo "   https://openrouter.ai/activity"
echo ""

if [[ $MODEL == *":free" ]]; then
    echo "💰 You're using a FREE model - no costs!"
else
    echo "💰 You're using a PAID model."
    echo "   Monitor costs at: https://openrouter.ai/activity"
    echo "   Set limits at: https://openrouter.ai/settings/limits"
fi

echo ""
echo "📚 Full documentation in OPENROUTER_SETUP.md"
echo ""
echo "Happy writing! ✍️✨"
echo ""
