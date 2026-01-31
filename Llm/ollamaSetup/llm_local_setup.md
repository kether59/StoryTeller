# 🆓 FREE Local LLM Setup Guide - No API Keys Required!

## Why Use Local LLM?

✅ **Completely FREE** - No API costs ever  
✅ **Private** - Your data never leaves your computer  
✅ **Unlimited** - No rate limits or usage caps  
✅ **Offline** - Works without internet (after model download)  

❌ **Cons:**  
- Slower without a good GPU  
- Models are slightly less capable than Claude/GPT-4  
- Uses disk space (4-7GB per model)  

---

## Option 1: Ollama (Recommended) ⭐

### Installation

#### Windows
1. Download Ollama from: https://ollama.com/download
2. Run the installer
3. Ollama will start automatically in the background

#### Linux/Mac
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

### Download Models

Open a terminal and run:

```bash
# Best for French creative writing (Recommended)
ollama pull mistral

# More powerful but slower
ollama pull mixtral

# Good alternative
ollama pull llama3
```

**Model Comparison:**
- `mistral` (7B) - Fast, good quality, 4GB
- `mixtral` (8x7B) - Better quality, slower, 26GB  
- `llama3` (8B) - Good balance, 4.7GB

### Configuration

#### Method 1: Using .env file

Create or edit `backend/.env`:

```env
# Use Ollama (FREE!)
LLM_PROVIDER=ollama
OLLAMA_URL=http://localhost:11434
```

#### Method 2: Environment variables

```bash
export LLM_PROVIDER=ollama
export OLLAMA_URL=http://localhost:11434
```

### Verify Installation

```bash
# Check if Ollama is running
curl http://localhost:11434

# You should see: "Ollama is running"
```

### Test the Model

```bash
ollama run mistral "Écris une courte histoire sur un dragon"
```

---

## Option 2: LM Studio (GUI Alternative)

If you prefer a graphical interface:

1. Download from: https://lmstudio.ai/
2. Install and open LM Studio
3. Go to "Discover" tab
4. Download a model (e.g., "Mistral 7B Instruct")
5. Go to "Local Server" tab
6. Click "Start Server"
7. Use port shown (usually 1234)

Configuration in `.env`:
```env
LLM_PROVIDER=openai  # LM Studio uses OpenAI-compatible API
OPENAI_API_KEY=lm-studio  # Any value works
OPENAI_BASE_URL=http://localhost:1234/v1
```

---

## Option 3: GPT4All (Easiest)

1. Download from: https://gpt4all.io/
2. Install and run
3. Download a model from the app
4. Enable "API Server" in settings
5. Note the port (usually 4891)

Configuration:
```env
LLM_PROVIDER=openai
OPENAI_API_KEY=not-needed
OPENAI_BASE_URL=http://localhost:4891/v1
```

---

## Updating Your Code for Ollama

Your `llm.py` already supports Ollama! Just make sure you have:

```python
# In llm.py - already there!
async def call_ollama(system_prompt: str, user_prompt: str, model: str = "mistral") -> str:
    """Calls Ollama local"""
    # ... implementation
```

For the **extraction feature**, let's update it to support Ollama too.

---

## Hardware Requirements

### Minimum (CPU only):
- 8GB RAM
- 10GB disk space
- Works but SLOW (2-5 minutes per response)

### Recommended (GPU):
- 16GB RAM
- NVIDIA GPU with 6GB+ VRAM
- Fast responses (10-30 seconds)

### Check if you have a GPU:

```bash
# NVIDIA
nvidia-smi

# AMD (Linux)
rocm-smi

# Apple Silicon
system_profiler SPDisplaysDataType
```

---

## Performance Tips

### 1. Use Smaller Models
```bash
ollama pull mistral  # 4GB, faster
# Instead of
ollama pull mixtral  # 26GB, slower but better
```

### 2. Reduce Context Size
In your code, limit the context sent to the model:

```python
# In build_system_prompt()
for event in context["timeline"][:5]:  # Only 5 instead of 10
```

### 3. Enable GPU Acceleration

Ollama automatically uses GPU if available. Check with:
```bash
ollama run mistral "test"
# Watch nvidia-smi in another terminal
```

---

## Troubleshooting

### "Connection refused" to localhost:11434

**Solution:** Start Ollama
```bash
# Linux/Mac
ollama serve

# Windows - Ollama should auto-start, or run the app
```

### Very Slow Responses

**Solutions:**
1. Use a smaller model: `mistral` instead of `mixtral`
2. Reduce max_tokens in your code
3. Close other applications to free RAM
4. Consider upgrading hardware or using cloud API

### "Model not found"

**Solution:** Download the model first
```bash
ollama pull mistral
```

### Out of Memory

**Solutions:**
1. Use smaller model
2. Close other applications
3. Reduce context size in code
4. Increase system swap space (Linux)

---

## Comparing Options

| Feature | Ollama | LM Studio | GPT4All |
|---------|--------|-----------|---------|
| Ease of setup | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Performance | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| Model choice | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Command line | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| GUI | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

**Recommendation:** Start with **Ollama** + **Mistral** model

---

## Next Steps

1. Install Ollama
2. Download mistral model
3. Update your `.env` file
4. Restart your FastAPI server
5. Test in the UI - you should see "🤖 LLM: ollama ✅ Configured"

---

## Example Usage

Once set up, test it:

```bash
# In your project directory
cd backend
python

>>> from routes.llm import call_ollama
>>> import asyncio
>>> 
>>> async def test():
...     result = await call_ollama(
...         "Tu es un assistant d'écriture",
...         "Écris une courte description d'un dragon"
...     )
...     print(result)
>>> 
>>> asyncio.run(test())
```

You should see a generated description!

---

## Cost Comparison

| Provider | 10 Chapters | 1 Novel |
|----------|-------------|---------|
| Claude | $5-15 | $50-150 |
| OpenAI GPT-4 | $10-20 | $100-200 |
| **Ollama** | **$0** | **$0** |

**Winner:** Ollama 🏆

---

**You're all set for FREE, unlimited AI writing assistance!** 🎉
