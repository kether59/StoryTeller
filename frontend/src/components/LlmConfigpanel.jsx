import React, { useState, useEffect } from 'react'
import API from '../api/api'
import { notifyError, notifySuccess } from '../utils/notify'

const DEFAULT_URLS = {
  llama: 'http://127.0.0.1:8080',
  lmstudio: 'http://127.0.0.1:1234',
  ollama: 'http://127.0.0.1:11434',
  anthropic: '',
  openai: '',
  gemini: '',
}

export default function LlmConfigpanel() {
  const [config, setConfig] = useState({
    provider: 'llama',
    url: DEFAULT_URLS.llama,
    model: '',
    api_key: '',
    gemini_api_key: '',
    temperature: 0.7,
    max_tokens: 2000,
  })

  const [availableModels, setAvailableModels] = useState([])
  const [loadingModels, setLoadingModels] = useState(false)
  const [loadingConfig, setLoadingConfig] = useState(true)
  const [testStatus, setTestStatus] = useState(null)
  const [testMessage, setTestMessage] = useState('')

  const providers = [
    { id: 'llama', label: 'llama.cpp', color: '#0ea5e9' },
    { id: 'lmstudio', label: 'LM Studio', color: '#8b5cf6' },
    { id: 'anthropic', label: 'Anthropic Claude', color: '#c96a2e' },
    { id: 'openai', label: 'OpenAI', color: '#10a37f' },
    { id: 'gemini', label: 'Google Gemini', color: '#4285f4' },
  ]

  useEffect(() => {
    let cancelled = false
    async function loadConfig() {
      try {
        setLoadingConfig(true)
        const res = await API.get('/api/llm/config')
        if (cancelled || !res.data) return
        const d = res.data

        // Détecte le provider et met l'URL au bon endroit
        const provider = d.provider ?? 'llama'
        const url = d.llmUrl ?? DEFAULT_URLS[provider] ?? ''

        setConfig((c) => ({
          ...c,
          provider,
          model: d.model ?? c.model,
          api_key: d.apiKey ?? c.api_key,
          url,
          temperature: d.temperature ?? c.temperature,
          max_tokens: d.maxTokens ?? d.max_tokens ?? c.max_tokens,
        }))
      } catch (err) {
        console.warn('Impossible de charger la config LLM:', err.message)
      } finally {
        if (!cancelled) setLoadingConfig(false)
      }
    }
    loadConfig()
    return () => { cancelled = true }
  }, [])

  // Quand on change de provider, on propose l'URL par défaut
  const handleProviderChange = (providerId) => {
    setConfig((c) => ({
      ...c,
      provider: providerId,
      url: DEFAULT_URLS[providerId] ?? c.url,
      model: '',
    }))
    setAvailableModels([])
  }

  const loadModels = async () => {
    if (!['llama', 'lmstudio', 'ollama'].includes(config.provider)) {
      setAvailableModels([])
      return
    }

    setLoadingModels(true)
    try {
      const res = await API.get(
          `/api/llm/local/models?url=${encodeURIComponent(config.url)}&provider=${config.provider}`
      )
      setAvailableModels(res.data)
      if (res.data.length > 0 && !String(res.data[0]).startsWith('Erreur')) {
        setConfig((c) => ({ ...c, model: res.data[0] }))
      }
    } catch (err) {
      setAvailableModels(['Erreur de connexion au serveur'])
    } finally {
      setLoadingModels(false)
    }
  }

  const handleTest = async () => {
    setTestStatus('testing')
    try {
      const payload = {
        provider: config.provider,
        model: config.model,
        apiKey: config.provider === 'gemini' ? config.gemini_api_key : config.api_key,
        llmUrl: config.url,   // ✅ UN SEUL CHAMP
      }
      const res = await API.post('/api/llm/test', payload)
      setTestStatus(res.data.ok ? 'success' : 'error')
      setTestMessage(res.data.message || 'Test terminé')
    } catch (err) {
      setTestStatus('error')
      setTestMessage(err.friendlyMessage || err.message)
    }
  }

  const handleSave = async () => {
    try {
      const payload = {
        provider: config.provider,
        model: config.model,
        apiKey: config.provider === 'gemini' ? config.gemini_api_key : config.api_key,
        llmUrl: config.url,   // ✅ UN SEUL CHAMP
        temperature: config.temperature,
        maxTokens: config.max_tokens,
      }
      await API.post('/api/llm/config', payload)
      notifySuccess('Configuration sauvegardée')
    } catch (err) {
      notifyError(err.friendlyMessage || 'Erreur lors de la sauvegarde')
    }
  }

  const isLocal = ['llama', 'lmstudio', 'ollama'].includes(config.provider)

  if (loadingConfig) {
    return (
        <div className="panel">
          <h2>⚙️ Configuration LLM</h2>
          <p>Chargement de la configuration…</p>
        </div>
    )
  }

  return (
      <div className="panel">
        <h2>⚙️ Configuration LLM</h2>

        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
          {providers.map((p) => (
              <div
                  key={p.id}
                  onClick={() => handleProviderChange(p.id)}
                  style={{
                    padding: '12px 20px',
                    border: config.provider === p.id ? `3px solid ${p.color}` : '1px solid #ccc',
                    borderRadius: 8,
                    cursor: 'pointer',
                    background: config.provider === p.id ? '#f0f9ff' : 'white',
                  }}
              >
                {p.label}
              </div>
          ))}
        </div>

        {isLocal && (
            <>
              <div className="field">
                <label>URL du serveur</label>
                <input
                    value={config.url}
                    onChange={(e) => setConfig((c) => ({ ...c, url: e.target.value }))}
                    className="input"
                    placeholder="http://127.0.0.1:8080"
                />
              </div>

              <button onClick={loadModels} disabled={loadingModels} style={{ marginBottom: 16 }}>
                {loadingModels ? 'Chargement...' : '🔄 Charger les modèles'}
              </button>
            </>
        )}

        {availableModels.length > 0 && (
            <div className="field">
              <label>Modèle</label>
              <select
                  value={config.model}
                  onChange={(e) => setConfig((c) => ({ ...c, model: e.target.value }))}
                  className="input"
              >
                {availableModels.map((m) => (
                    <option key={m} value={m}>{m}</option>
                ))}
              </select>
            </div>
        )}

        {['anthropic', 'openai', 'openrouter'].includes(config.provider) && (
            <div className="field">
              <label>Clé API</label>
              <input
                  type="password"
                  value={config.api_key}
                  onChange={(e) => setConfig((c) => ({ ...c, api_key: e.target.value }))}
                  className="input"
              />
            </div>
        )}

        {config.provider === 'gemini' && (
            <div className="field">
              <label>Clé API Gemini</label>
              <input
                  type="password"
                  value={config.gemini_api_key}
                  onChange={(e) => setConfig((c) => ({ ...c, gemini_api_key: e.target.value }))}
                  className="input"
              />
            </div>
        )}

        <div className="field">
          <label>Température ({config.temperature})</label>
          <input
              type="range"
              min="0"
              max="2"
              step="0.1"
              value={config.temperature}
              onChange={(e) => setConfig((c) => ({ ...c, temperature: parseFloat(e.target.value) }))}
          />
        </div>

        <div className="field">
          <label>Max tokens</label>
          <input
              type="number"
              value={config.max_tokens}
              onChange={(e) => setConfig((c) => ({ ...c, max_tokens: parseInt(e.target.value) }))}
              className="input"
          />
        </div>

        <div style={{ marginTop: 20, display: 'flex', gap: 12 }}>
          <button className="primary" onClick={handleTest}>
            🔄 Tester connexion
          </button>
          <button onClick={handleSave}>💾 Sauvegarder</button>
        </div>

        {testStatus && (
            <div
                style={{
                  marginTop: 16,
                  padding: 14,
                  borderRadius: 6,
                  background: testStatus === 'success' ? '#e8f5e9' : '#fee',
                }}
            >
              {testMessage}
            </div>
        )}
      </div>
  )
}