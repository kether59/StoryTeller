# 🌐 OpenRouter Setup Guide - Access to 100+ AI Models!

## What is OpenRouter?

OpenRouter is a unified API that gives you access to **100+ AI models** from different providers through a single interface. It's perfect because:

✅ **One API key** for all models  
✅ **FREE models available** (Llama, Gemma, Mistral)  
✅ **Pay-as-you-go** for premium models  
✅ **No rate limits** on most models  
✅ **Easy switching** between models  
✅ **Cost tracking** in dashboard  

---

## Quick Start

### 1. Get Your API Key

1. Go to https://openrouter.ai/
2. Sign up (GitHub or Google login)
3. Go to https://openrouter.ai/settings/keys
4. Click "Create Key"
5. Copy your key (starts with `sk-or-v1-...`)

### 2. Configure Your App

Create or edit `backend/.env`:

```env
# OpenRouter Configuration
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-your-key-here

# Optional: Choose your model (default is free)
OPENROUTER_MODEL=meta-llama/llama-3.1-8b-instruct:free
```

### 3. Install Dependencies

```bash
pip install openai
```

That's it! OpenRouter uses the OpenAI Python library.

---

## Available Models

### 🆓 **FREE Models** (No cost)

| Model | Provider | Description | Best For |
|-------|----------|-------------|----------|
| `meta-llama/llama-3.1-8b-instruct:free` | Meta | Very good, 8B params | **General use** ⭐ |
| `google/gemma-2-9b-it:free` | Google | Efficient, 9B params | Creative writing |
| `mistralai/mistral-7b-instruct:free` | Mistral | Fast, 7B params | Quick responses |
| `nousresearch/hermes-3-llama-3.1-405b:free` | Nous Research | HUGE 405B params | Complex tasks |

### 💰 **Paid Models** (Pay per use)

| Model | Provider | Cost (per 1M tokens) | Quality |
|-------|----------|---------------------|---------|
| `anthropic/claude-3.5-sonnet` | Anthropic | ~$3 input / $15 output | ⭐⭐⭐⭐⭐ |
| `openai/gpt-4-turbo` | OpenAI | ~$10 input / $30 output | ⭐⭐⭐⭐⭐ |
| `google/gemini-pro-1.5` | Google | ~$1.25 input / $5 output | ⭐⭐⭐⭐ |
| `anthropic/claude-3-haiku` | Anthropic | ~$0.25 input / $1.25 output | ⭐⭐⭐ (cheap!) |
| `meta-llama/llama-3.1-70b-instruct` | Meta | ~$0.35 input / $0.40 output | ⭐⭐⭐⭐ |

**Recommendation:** Start with the **free Llama 3.1 8B** model!

---

## Configuration Examples

### Option 1: Use Free Model (Recommended to start)

```env
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_MODEL=meta-llama/llama-3.1-8b-instruct:free
```

### Option 2: Use Claude 3.5 Sonnet (Best quality)

```env
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_MODEL=anthropic/claude-3.5-sonnet
```

### Option 3: Use GPT-4 Turbo

```env
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_MODEL=openai/gpt-4-turbo
```

### Option 4: Use Cheap but Good Model

```env
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_MODEL=anthropic/claude-3-haiku
```

---

## Model Selection Guide

### For Creative Writing (Novels, Stories)

**Best Choice:**
```env
OPENROUTER_MODEL=anthropic/claude-3.5-sonnet
```
- Most creative and natural writing
- Best at maintaining character consistency
- Cost: ~$3-15 per 1M tokens

**Budget Option:**
```env
OPENROUTER_MODEL=meta-llama/llama-3.1-70b-instruct
```
- Very good quality
- Much cheaper (~$0.35-0.40 per 1M tokens)

**FREE Option:**
```env
OPENROUTER_MODEL=meta-llama/llama-3.1-8b-instruct:free
```
- Decent quality
- Completely free!

### For Text Extraction/Analysis

**Best Choice:**
```env
OPENROUTER_MODEL=openai/gpt-4-turbo
```
- Most accurate for structured data extraction
- Best at following JSON format

**Budget Option:**
```env
OPENROUTER_MODEL=google/gemini-pro-1.5
```
- Good at structured tasks
- Cheaper than GPT-4

**FREE Option:**
```env
OPENROUTER_MODEL=google/gemma-2-9b-it:free
```

---

## Cost Management

### 1. Check Usage

Visit your OpenRouter dashboard:
https://openrouter.ai/activity

You can see:
- Total spend
- Cost per request
- Model usage breakdown

### 2. Set Limits

In OpenRouter settings:
https://openrouter.ai/settings/limits

You can set:
- Daily spending limits
- Monthly budgets
- Per-request limits

### 3. Estimate Costs

**Example for a Novel:**

Generating 10 chapters (15,000 words each) with context:

| Model | Estimated Cost |
|-------|---------------|
| Free Llama 3.1 8B | **$0** |
| Llama 3.1 70B | ~$5-10 |
| Claude 3 Haiku | ~$2-5 |
| Claude 3.5 Sonnet | ~$10-20 |
| GPT-4 Turbo | ~$15-30 |

### 4. Tips to Reduce Costs

1. **Start with free models** for testing
2. **Limit context size** - send only necessary info
3. **Use cheaper models** for simple tasks
4. **Set max_tokens** appropriately
5. **Monitor usage** regularly

---

## Testing Your Setup

### 1. Check Health Endpoint

```bash
curl http://localhost:8000/api/llm/health
```

Expected response:
```json
{
  "provider": "openrouter",
  "configured": true,
  "model": "meta-llama/llama-3.1-8b-instruct:free"
}
```

### 2. Test in Python

```python
import os
os.environ['LLM_PROVIDER'] = 'openrouter'
os.environ['OPENROUTER_API_KEY'] = 'sk-or-v1-...'

from routes.llm import call_openrouter
import asyncio

async def test():
    response = await call_openrouter(
        "Tu es un assistant d'écriture",
        "Écris une phrase sur un dragon"
    )
    print(response)

asyncio.run(test())
```

### 3. Test in UI

1. Start your app
2. Go to "✍️ Assistant d'écriture"
3. You should see: "🤖 LLM: openrouter ✅ Configured"
4. Try generating a short chapter

---

## Switching Models On-The-Fly

You can change models without restarting:

### Method 1: Environment Variable

```bash
export OPENROUTER_MODEL=anthropic/claude-3.5-sonnet
# Restart your app
```

### Method 2: Update .env

Edit `backend/.env`:
```env
OPENROUTER_MODEL=google/gemini-pro-1.5
```

Restart your server.

---

## Troubleshooting

### "OPENROUTER_API_KEY non configurée"

**Solution:** Add your API key to `.env`:
```env
OPENROUTER_API_KEY=sk-or-v1-your-key-here
```

### "Model not found" or 404 Error

**Solution:** Check the model name is correct. Visit:
https://openrouter.ai/models

Copy the exact model ID (e.g., `meta-llama/llama-3.1-8b-instruct:free`)

### "Insufficient credits"

**Solution:** 
1. For free models: Check if the model name ends with `:free`
2. For paid models: Add credits at https://openrouter.ai/settings/credits

### Slow Responses

**Solutions:**
1. Use a smaller model (8B instead of 70B)
2. Free models may have queues during peak times
3. Consider using paid models for faster responses

### "Rate limit exceeded"

**Solution:** 
- Free models have usage limits per IP
- Wait a few minutes or use a paid model
- Check your limits at https://openrouter.ai/settings/limits

---

## Advanced Features

### 1. Model Routing

OpenRouter can automatically choose the best available model:

```env
OPENROUTER_MODEL=openrouter/auto
```

### 2. Fallbacks

If a model is unavailable, OpenRouter can try alternatives.

### 3. Custom Parameters

You can modify the code to add parameters:

```python
response = client.chat.completions.create(
    model=model,
    messages=[...],
    max_tokens=max_tokens,
    temperature=0.7,  # Creativity (0.0-2.0)
    top_p=0.9,        # Diversity
    frequency_penalty=0.0,
    presence_penalty=0.0,
)
```

---

## Comparison: OpenRouter vs Others

| Feature | OpenRouter | Anthropic Direct | OpenAI Direct | Ollama |
|---------|-----------|------------------|---------------|---------|
| **Cost** | Free + Paid | Paid only | Paid only | Free |
| **Model Choice** | 100+ models | Claude only | GPT only | Limited |
| **Setup** | Easy | Easy | Easy | Medium |
| **Speed** | Fast | Fastest | Fastest | Slow (CPU) |
| **Privacy** | Cloud | Cloud | Cloud | Local |
| **Best For** | Flexibility | Claude fans | GPT fans | Privacy |

**Recommendation:** 
- **Start with:** OpenRouter free models
- **Upgrade to:** OpenRouter paid models when you need quality
- **Use Ollama for:** Complete privacy (offline)

---

## Complete Configuration Example

Here's a complete `.env` file:

```env
# ===== OpenRouter Configuration =====
LLM_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-or-v1-1234567890abcdef1234567890abcdef1234567890

# Choose one model:

# FREE Models (recommended to start)
OPENROUTER_MODEL=meta-llama/llama-3.1-8b-instruct:free

# Or best quality (paid)
# OPENROUTER_MODEL=anthropic/claude-3.5-sonnet

# Or good balance (paid but cheap)
# OPENROUTER_MODEL=meta-llama/llama-3.1-70b-instruct

# Or budget option (paid but very cheap)
# OPENROUTER_MODEL=anthropic/claude-3-haiku

# ===================================
```

---

## Getting Help

- **OpenRouter Docs:** https://openrouter.ai/docs
- **Model List:** https://openrouter.ai/models
- **Discord Community:** https://discord.gg/openrouter
- **Status Page:** https://status.openrouter.ai/

---

## Next Steps

1. ✅ Get your API key from https://openrouter.ai/settings/keys
2. ✅ Add it to your `.env` file
3. ✅ Start with a free model
4. ✅ Test it in your app
5. ✅ Monitor usage in the dashboard
6. ✅ Upgrade to paid models when needed

**Happy writing with OpenRouter! 🚀✍️**
