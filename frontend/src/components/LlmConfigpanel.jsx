import React, { useState, useEffect } from 'react'
import API from '../api/api'

export default function LlmConfigpanel() {
    const [config, setConfig] = useState({
        provider: 'llama',
        url: 'http://127.0.0.1:8080',
        model: '',
        api_key: '',
        gemini_api_key: '',
        temperature: 0.7,
        max_tokens: 2000,
        lmstudio_url: 'http://192.168.1.28:1234',
        ollama_url: 'http://127.0.0.1:8080/'
    })

    const [availableModels, setAvailableModels] = useState([])
    const [loadingModels, setLoadingModels] = useState(false)
    const [testStatus, setTestStatus] = useState(null)
    const [testMessage, setTestMessage] = useState('')

    const providers = [
        { id: 'llama', label: 'llama.cpp', color: '#0ea5e9' },
        { id: 'lmstudio', label: 'LM Studio', color: '#8b5cf6' },
        { id: 'anthropic', label: 'Anthropic Claude', color: '#c96a2e' },
        { id: 'openai', label: 'OpenAI', color: '#10a37f' },
        { id: 'gemini', label: 'Google Gemini', color: '#4285f4' },
    ]

    const loadModels = async () => {
        if (!['llama', 'lmstudio'].includes(config.provider)) {
            setAvailableModels([])
            return
        }

        setLoadingModels(true)
        try {
            const res = await API.get(
                `/api/llm/local/models?url=${encodeURIComponent(config.url)}&provider=${config.provider}`
            )
            setAvailableModels(res.data)
            if (res.data.length > 0 && !res.data[0].startsWith('Erreur')) {
                setConfig(c => ({ ...c, model: res.data[0] }))
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
                apiKey: config.api_key || config.gemini_api_key,
                ollamaUrl: config.ollama_url,
                lmstudioUrl: config.lmstudio_url || config.lmstudioUrl,
                geminiApiKey: config.gemini_api_key
            }
            const res = await API.post('/api/llm/test', payload)
            setTestStatus('success')
            setTestMessage(res.data.message || '✅ Connexion réussie')
        } catch (err) {
            setTestStatus('error')
            setTestMessage(err.response?.data?.detail || err.message)
        }
    }

    const handleSave = async () => {
        try {
            const payload = {
                provider: config.provider,
                model: config.model,
                apiKey: config.api_key,
                ollamaUrl: config.url,
                temperature: config.temperature,
                maxTokens: config.max_tokens
            }
            await API.post('/api/llm/config', payload)
            alert('✅ Configuration sauvegardée')
        } catch (err) {
            alert('❌ Erreur lors de la sauvegarde')
        }
    }

    return (
        <div className="panel">
            <h2>⚙️ Configuration LLM</h2>

            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {providers.map(p => (
                    <div
                        key={p.id}
                        onClick={() => setConfig(c => ({ ...c, provider: p.id }))}
                        style={{
                            padding: '12px 20px',
                            border: config.provider === p.id ? `3px solid ${p.color}` : '1px solid #ccc',
                            borderRadius: 8,
                            cursor: 'pointer',
                            background: config.provider === p.id ? '#f0f9ff' : 'white'
                        }}
                    >
                        {p.label}
                    </div>
                ))}
            </div>

            <div className="field">
                <label>URL du serveur</label>
                <input
                    value={config.url}
                    onChange={e => setConfig(c => ({ ...c, url: e.target.value }))}
                    className="input"
                    placeholder="http://127.0.0.1:8080"
                />
            </div>

            {['llama', 'lmstudio'].includes(config.provider) && (
                <button onClick={loadModels} disabled={loadingModels}>
                    {loadingModels ? 'Chargement...' : '🔄 Charger les modèles'}
                </button>
            )}

            {availableModels.length > 0 && (
                <div className="field">
                    <label>Modèle</label>
                    <select
                        value={config.model}
                        onChange={e => setConfig(c => ({ ...c, model: e.target.value }))}
                        className="input"
                    >
                        {availableModels.map(m => <option key={m} value={m}>{m}</option>)}
                    </select>
                </div>
            )}

            {(config.provider === 'anthropic' || config.provider === 'openai') && (
                <div className="field">
                    <label>Clé API</label>
                    <input type="password" value={config.api_key} onChange={e => setConfig(c => ({ ...c, api_key: e.target.value }))} className="input" />
                </div>
            )}

            {config.provider === 'gemini' && (
                <div className="field">
                    <label>Clé API Gemini</label>
                    <input type="password" value={config.gemini_api_key} onChange={e => setConfig(c => ({ ...c, gemini_api_key: e.target.value }))} className="input" />
                </div>
            )}

            <div style={{ marginTop: 20, display: 'flex', gap: 12 }}>
                <button className="primary" onClick={handleTest}>🔄 Tester connexion</button>
                <button onClick={handleSave}>💾 Sauvegarder</button>
            </div>

            {testStatus && (
                <div style={{ marginTop: 16, padding: 14, borderRadius: 6, background: testStatus === 'success' ? '#e8f5e9' : '#fee' }}>
                    {testMessage}
                </div>
            )}
        </div>
    )
}