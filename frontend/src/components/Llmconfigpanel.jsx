import React, { useEffect, useState, useCallback } from 'react'
import API from '../api/api'

const Icon = ({ name }) => {
    const icons = {
        anthropic: (
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
                <path d="M13.827 3.52h3.603L24 20h-3.603l-6.57-16.48zm-7.258 0h3.767L16.906 20h-3.674l-1.343-3.461H5.017L3.674 20H0L6.569 3.52zm4.132 9.959L8.453 7.687 6.205 13.48h4.496z"/>
            </svg>
        ),
        openai: (
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
                <path d="M22.282 9.821a5.985 5.985 0 0 0-.516-4.91 6.046 6.046 0 0 0-6.51-2.9A6.065 6.065 0 0 0 4.981 4.18a5.985 5.985 0 0 0-3.998 2.9 6.046 6.046 0 0 0 .743 7.097 5.98 5.98 0 0 0 .51 4.911 6.051 6.051 0 0 0 6.515 2.9A5.985 5.985 0 0 0 13.26 24a6.056 6.056 0 0 0 5.772-4.206 5.99 5.99 0 0 0 3.997-2.9 6.056 6.056 0 0 0-.747-7.073zM13.26 22.43a4.476 4.476 0 0 1-2.876-1.04l.141-.081 4.779-2.758a.795.795 0 0 0 .392-.681v-6.737l2.02 1.168a.071.071 0 0 1 .038.052v5.583a4.504 4.504 0 0 1-4.494 4.494zM3.6 18.304a4.47 4.47 0 0 1-.535-3.014l.142.085 4.783 2.759a.771.771 0 0 0 .78 0l5.843-3.369v2.332a.08.08 0 0 1-.033.062L9.74 19.95a4.5 4.5 0 0 1-6.14-1.646zM2.34 7.896a4.485 4.485 0 0 1 2.366-1.973V11.6a.766.766 0 0 0 .388.676l5.815 3.355-2.02 1.168a.076.076 0 0 1-.071 0l-4.83-2.786A4.504 4.504 0 0 1 2.34 7.872zm16.597 3.855l-5.843-3.372L15.115 7.2a.076.076 0 0 1 .071 0l4.83 2.791a4.494 4.494 0 0 1-.676 8.105v-5.678a.79.79 0 0 0-.403-.667zm2.01-3.023l-.141-.085-4.774-2.782a.776.776 0 0 0-.785 0L9.409 9.23V6.897a.066.066 0 0 1 .028-.061l4.83-2.787a4.5 4.5 0 0 1 6.68 4.66zm-12.64 4.135l-2.02-1.164a.08.08 0 0 1-.038-.057V6.075a4.5 4.5 0 0 1 7.375-3.453l-.142.08-4.778 2.758a.795.795 0 0 0-.393.681zm1.097-2.365l2.602-1.5 2.607 1.5v2.999l-2.597 1.5-2.607-1.5z"/>
            </svg>
        ),
        openrouter: (
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/>
            </svg>
        ),
        ollama: (
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
                <path d="M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 0-16 8 8 0 0 1 0 16zm-1-5h2v2h-2zm0-8h2v6h-2z"/>
            </svg>
        ),
        check: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" width="16" height="16">
                <polyline points="20 6 9 17 4 12"/>
            </svg>
        ),
        warning: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
        ),
        spinner: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16" style={{animation:'spin 1s linear infinite'}}>
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
        ),
        eye: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
            </svg>
        ),
        eyeOff: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
        ),
        save: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
            </svg>
        ),
        refresh: (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
        ),
    }
    return icons[name] || null
}

let PROVIDERS;
PROVIDERS = [
    {
        id: 'anthropic',
        label: 'Anthropic Claude',
        description: 'Modèles Claude — excellents pour la créativité et la nuance narrative.',
        models: [
            {id: 'claude-opus-4-5', label: 'Claude Opus 4.5', badge: 'Puissant'},
            {id: 'claude-sonnet-4-5', label: 'Claude Sonnet 4.5', badge: 'Équilibré'},
            {id: 'claude-haiku-4-5', label: 'Claude Haiku 4.5', badge: 'Rapide'},
        ],
        fields: [{key: 'api_key', label: 'Clé API Anthropic', placeholder: 'sk-ant-...', secret: true}],
        color: '#c96a2e',
        docsUrl: 'https://console.anthropic.com',
    },
    {
        id: 'openai',
        label: 'OpenAI GPT',
        description: 'Modèles GPT-4 — polyvalents et bien documentés.',
        models: [
            {id: 'gpt-4o', label: 'GPT-4o', badge: 'Recommandé'},
            {id: 'gpt-4-turbo-preview', label: 'GPT-4 Turbo', badge: 'Puissant'},
            {id: 'gpt-3.5-turbo', label: 'GPT-3.5 Turbo', badge: 'Économique'},
        ],
        fields: [{key: 'api_key', label: 'Clé API OpenAI', placeholder: 'sk-...', secret: true}],
        color: '#10a37f',
        docsUrl: 'https://platform.openai.com/api-keys',
    },
    {
        id: 'openrouter',
        label: 'OpenRouter',
        description: 'Accès unifié à des dizaines de modèles : Llama, Mistral, Gemini…',
        models: [
            {id: 'meta-llama/llama-3.1-8b-instruct:free', label: 'Llama 3.1 8B (gratuit)', badge: 'Gratuit'},
            {id: 'meta-llama/llama-3.3-70b-instruct', label: 'Llama 3.3 70B', badge: ''},
            {id: 'mistralai/mistral-7b-instruct:free', label: 'Mistral 7B (gratuit)', badge: 'Gratuit'},
            {id: 'google/gemini-flash-1.5', label: 'Gemini Flash 1.5', badge: 'Rapide'},
            {id: 'anthropic/claude-3.5-sonnet', label: 'Claude 3.5 Sonnet', badge: ''},
        ],
        fields: [
            {key: 'api_key', label: 'Clé API OpenRouter', placeholder: 'sk-or-...', secret: true},
            {key: 'model', label: 'Modèle personnalisé (optionnel)', placeholder: 'org/model:variant', secret: false},
        ],
        color: '#6366f1',
        docsUrl: 'https://openrouter.ai/keys',
    },
    {
        id: 'ollama',
        label: 'Ollama (local)',
        description: 'Exécutez des modèles open-source en local, sans envoyer de données à l extérieur.',
        models: [
            {id: 'mistral', label: 'Mistral 7B', badge: 'Populaire'},
            {id: 'llama3.2', label: 'Llama 3.2', badge: ''},
            {id: 'gemma3', label: 'Gemma 3', badge: ''},
            {id: 'deepseek-r1', label: 'DeepSeek R1', badge: 'Raisonnement'},
        ],
        fields: [
            {key: 'ollama_url', label: 'URL Ollama', placeholder: 'http://localhost:11434', secret: false},
        ],
        color: '#0ea5e9',
        docsUrl: 'https://ollama.com',
    },
];

const DEFAULT_CONFIG = {
    provider: 'anthropic',
    model: 'claude-sonnet-4-5',
    api_key: '',
    ollama_url: 'http://localhost:11434',
    temperature: 0.7,
    max_tokens: 4000,
}


export default function LLMConfigPanel() {
    const [config, setConfig] = useState(DEFAULT_CONFIG)
    const [savedConfig, setSavedConfig] = useState(null)
    const [status, setStatus] = useState(null)
    const [testStatus, setTestStatus] = useState(null)
    const [testMessage, setTestMessage] = useState('')
    const [showSecret, setShowSecret] = useState({})
    const [loaded, setLoaded] = useState(false)

    const provider = PROVIDERS.find(p => p.id === config.provider) || PROVIDERS[0]


    useEffect(() => {
        API.get('/api/llm/config')
            .then(r => {
                const c = { ...DEFAULT_CONFIG, ...r.data }
                setConfig(c)
                setSavedConfig(c)
                setLoaded(true)
            })
            .catch(() => {
                setLoaded(true)
            })
    }, [])

    const handleProviderChange = (pid) => {
        const p = PROVIDERS.find(x => x.id === pid)
        setConfig(prev => ({
            ...prev,
            provider: pid,
            model: p.models[0]?.id || '',
            api_key: '',
        }))
        setTestStatus(null)
        setTestMessage('')
    }

    const handleField = (key, value) => {
        setConfig(prev => ({ ...prev, [key]: value }))
    }


    const handleSave = async () => {
        setStatus('saving')
        try {
            await API.post('/api/llm/config', config)
            setSavedConfig({ ...config })
            setStatus('saved')
            setTimeout(() => setStatus(null), 2500)
        } catch (err) {
            setStatus('error')
            setTimeout(() => setStatus(null), 3000)
        }
    }


    const handleTest = async () => {
        setTestStatus('testing')
        setTestMessage('')
        try {
            const r = await API.post('/api/llm/test', {
                provider: config.provider,
                model: config.model,
                api_key: config.api_key,
                ollama_url: config.ollama_url,
            })
            setTestStatus('ok')
            setTestMessage(r.data.message || 'Connexion réussie !')
        } catch (err) {
            setTestStatus('fail')
            setTestMessage(err.response?.data?.detail || 'Impossible de contacter le LLM.')
        }
    }

    const isDirty = JSON.stringify(config) !== JSON.stringify(savedConfig)

    if (!loaded) {
        return (
            <div className="llm-config-loading">
                <span className="llm-spinner" />
                <span>Chargement de la configuration…</span>
            </div>
        )
    }

    return (
        <>
            <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Mono:wght@400;500&family=DM+Sans:ital,wght@0,300;0,400;0,500;0,600;1,300&display=swap');

        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes fadeIn { from { opacity:0; transform:translateY(6px); } to { opacity:1; transform:translateY(0); } }
        @keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:.5; } }
        @keyframes slideIn { from { opacity:0; transform:translateX(-8px); } to { opacity:1; transform:translateX(0); } }

        .llm-config-wrap {
          font-family: 'DM Sans', sans-serif;
          background: #0f0f13;
          color: #e8e4df;
          min-height: 100vh;
          padding: 2rem 1.5rem 4rem;
        }

        .llm-config-inner {
          max-width: 820px;
          margin: 0 auto;
        }

        .llm-config-header {
          margin-bottom: 2.5rem;
          animation: fadeIn .4s ease;
        }

        .llm-config-header h2 {
          font-family: 'DM Serif Display', serif;
          font-size: 2.1rem;
          font-weight: 400;
          color: #f2ede8;
          margin: 0 0 .4rem;
          letter-spacing: -.01em;
        }

        .llm-config-header p {
          color: #7a746e;
          font-size: .92rem;
          margin: 0;
          font-weight: 300;
        }

        /* ─── Provider grid ─── */
        .llm-provider-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 1rem;
          margin-bottom: 2rem;
          animation: fadeIn .4s ease .05s both;
        }

        @media (max-width: 580px) {
          .llm-provider-grid { grid-template-columns: 1fr; }
        }

        .llm-provider-card {
          position: relative;
          border: 1.5px solid #252528;
          border-radius: 12px;
          padding: 1.1rem 1.1rem 1rem;
          cursor: pointer;
          background: #16161b;
          transition: border-color .18s, background .18s, transform .15s;
          user-select: none;
        }

        .llm-provider-card:hover {
          border-color: #3a3a40;
          background: #1a1a20;
          transform: translateY(-1px);
        }

        .llm-provider-card.active {
          border-color: var(--pc);
          background: color-mix(in srgb, var(--pc) 6%, #16161b);
        }

        .llm-provider-card.active::before {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: 11px;
          box-shadow: 0 0 0 1px var(--pc) inset;
          pointer-events: none;
        }

        .llm-pc-top {
          display: flex;
          align-items: center;
          gap: .65rem;
          margin-bottom: .5rem;
        }

        .llm-pc-icon {
          width: 34px;
          height: 34px;
          border-radius: 8px;
          background: color-mix(in srgb, var(--pc) 15%, #1a1a1a);
          color: var(--pc);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .llm-pc-name {
          font-weight: 600;
          font-size: .94rem;
          color: #e8e4df;
        }

        .llm-pc-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: var(--pc);
          margin-left: auto;
          opacity: 0;
          transition: opacity .2s;
        }

        .llm-provider-card.active .llm-pc-dot {
          opacity: 1;
        }

        .llm-pc-desc {
          font-size: .8rem;
          color: #5c5a56;
          line-height: 1.45;
          font-weight: 300;
        }

        /* ─── Config section ─── */
        .llm-config-section {
          background: #16161b;
          border: 1.5px solid #252528;
          border-radius: 14px;
          padding: 1.6rem;
          margin-bottom: 1.2rem;
          animation: slideIn .3s ease both;
        }

        .llm-section-title {
          font-family: 'DM Serif Display', serif;
          font-size: 1.05rem;
          font-weight: 400;
          color: #c8c2bb;
          margin: 0 0 1.2rem;
          padding-bottom: .7rem;
          border-bottom: 1px solid #252528;
          display: flex;
          align-items: center;
          gap: .5rem;
        }

        /* ─── Model selector ─── */
        .llm-model-grid {
          display: flex;
          flex-wrap: wrap;
          gap: .6rem;
        }

        .llm-model-btn {
          padding: .45rem .9rem;
          border-radius: 8px;
          border: 1.5px solid #2a2a30;
          background: #1e1e25;
          color: #9a9490;
          cursor: pointer;
          font-size: .83rem;
          font-family: 'DM Mono', monospace;
          transition: all .16s;
          display: flex;
          align-items: center;
          gap: .5rem;
        }

        .llm-model-btn:hover {
          border-color: #3a3a42;
          color: #e0dbd5;
          background: #23232b;
        }

        .llm-model-btn.active {
          border-color: var(--pc);
          background: color-mix(in srgb, var(--pc) 10%, #1e1e25);
          color: var(--pc);
        }

        .llm-model-badge {
          font-size: .68rem;
          padding: .1rem .4rem;
          border-radius: 4px;
          background: color-mix(in srgb, var(--pc) 15%, transparent);
          color: var(--pc);
          font-family: 'DM Sans', sans-serif;
          font-weight: 500;
        }

        /* ─── Fields ─── */
        .llm-field {
          margin-bottom: 1rem;
        }

        .llm-field label {
          display: block;
          font-size: .8rem;
          font-weight: 500;
          color: #7a746e;
          margin-bottom: .35rem;
          text-transform: uppercase;
          letter-spacing: .05em;
        }

        .llm-field-input-wrap {
          position: relative;
        }

        .llm-field input {
          width: 100%;
          box-sizing: border-box;
          background: #111115;
          border: 1.5px solid #272730;
          border-radius: 8px;
          color: #e0dbd5;
          font-family: 'DM Mono', monospace;
          font-size: .85rem;
          padding: .6rem .9rem;
          outline: none;
          transition: border-color .16s;
        }

        .llm-field input:focus {
          border-color: var(--pc);
        }

        .llm-field input::placeholder { color: #3c3a38; }

        .llm-eye-btn {
          position: absolute;
          right: .6rem;
          top: 50%;
          transform: translateY(-50%);
          background: none;
          border: none;
          cursor: pointer;
          color: #5c5a56;
          padding: .2rem;
          display: flex;
          align-items: center;
          transition: color .15s;
        }

        .llm-eye-btn:hover { color: #9a9490; }

        /* ─── Range slider ─── */
        .llm-range-row {
          display: flex;
          align-items: center;
          gap: .8rem;
        }

        .llm-range-row input[type=range] {
          flex: 1;
          accent-color: var(--pc);
          cursor: pointer;
        }

        .llm-range-val {
          font-family: 'DM Mono', monospace;
          font-size: .85rem;
          color: var(--pc);
          min-width: 2.5rem;
          text-align: right;
        }

        /* ─── Actions bar ─── */
        .llm-actions {
          display: flex;
          align-items: center;
          gap: .8rem;
          flex-wrap: wrap;
          animation: fadeIn .3s ease .1s both;
        }

        .llm-btn {
          display: inline-flex;
          align-items: center;
          gap: .45rem;
          padding: .55rem 1.1rem;
          border-radius: 8px;
          border: none;
          cursor: pointer;
          font-size: .88rem;
          font-family: 'DM Sans', sans-serif;
          font-weight: 500;
          transition: all .16s;
        }

        .llm-btn-primary {
          background: var(--pc);
          color: #0f0f13;
        }

        .llm-btn-primary:hover { filter: brightness(1.1); }
        .llm-btn-primary:disabled { opacity: .5; cursor: not-allowed; filter: none; }

        .llm-btn-ghost {
          background: #1e1e25;
          color: #9a9490;
          border: 1.5px solid #2a2a30;
        }

        .llm-btn-ghost:hover { border-color: #3a3a42; color: #e0dbd5; }
        .llm-btn-ghost:disabled { opacity: .4; cursor: not-allowed; }

        .llm-status-chip {
          display: inline-flex;
          align-items: center;
          gap: .4rem;
          font-size: .82rem;
          padding: .35rem .75rem;
          border-radius: 6px;
        }

        .llm-status-chip.saving { background:#252530; color:#7a746e; }
        .llm-status-chip.saved  { background: color-mix(in srgb, #22c55e 12%, #1e1e25); color:#4ade80; }
        .llm-status-chip.error  { background: color-mix(in srgb, #ef4444 10%, #1e1e25); color:#f87171; }

        /* ─── Test result ─── */
        .llm-test-result {
          margin-top: 1rem;
          padding: .8rem 1rem;
          border-radius: 8px;
          font-size: .86rem;
          display: flex;
          align-items: flex-start;
          gap: .6rem;
          animation: fadeIn .25s ease;
        }

        .llm-test-result.ok   { background: color-mix(in srgb, #22c55e 8%, #16161b); border: 1px solid color-mix(in srgb, #22c55e 25%, transparent); color: #4ade80; }
        .llm-test-result.fail { background: color-mix(in srgb, #ef4444 8%, #16161b); border: 1px solid color-mix(in srgb, #ef4444 25%, transparent); color: #f87171; }
        .llm-test-result.testing { background: #1a1a22; border: 1px solid #2a2a35; color: #7a746e; animation: pulse 1.2s ease infinite; }

        /* ─── Docs link ─── */
        .llm-docs-link {
          font-size: .78rem;
          color: #4a4848;
          text-decoration: none;
          display: inline-flex;
          align-items: center;
          gap: .3rem;
          margin-top: .5rem;
          transition: color .15s;
        }
        .llm-docs-link:hover { color: var(--pc); }

        .llm-config-loading {
          display: flex;
          align-items: center;
          gap: .8rem;
          color: #5c5a56;
          padding: 3rem;
          font-family: 'DM Sans', sans-serif;
        }

        .llm-spinner {
          width: 18px; height: 18px;
          border: 2px solid #2a2a35;
          border-top-color: #6366f1;
          border-radius: 50%;
          animation: spin 1s linear infinite;
          display: inline-block;
        }

        .llm-dirty-dot {
          width: 6px; height: 6px;
          border-radius: 50%;
          background: #f59e0b;
          display: inline-block;
          animation: pulse 1.5s ease infinite;
        }
      `}</style>

            <div className="llm-config-wrap">
                <div className="llm-config-inner">

                    {/* ── En-tête ── */}
                    <div className="llm-config-header">
                        <h2>Configuration du LLM</h2>
                        <p>Choisissez le fournisseur d'intelligence artificielle et ajustez les paramètres utilisés par l'assistant d'écriture et l'extraction.</p>
                    </div>

                    {/* ── Sélection du provider ── */}
                    <div className="llm-provider-grid">
                        {PROVIDERS.map(p => (
                            <div
                                key={p.id}
                                className={`llm-provider-card ${config.provider === p.id ? 'active' : ''}`}
                                style={{ '--pc': p.color }}
                                onClick={() => handleProviderChange(p.id)}
                                role="radio"
                                aria-checked={config.provider === p.id}
                                tabIndex={0}
                                onKeyDown={e => e.key === 'Enter' && handleProviderChange(p.id)}
                            >
                                <div className="llm-pc-top">
                                    <div className="llm-pc-icon"><Icon name={p.id} /></div>
                                    <span className="llm-pc-name">{p.label}</span>
                                    <span className="llm-pc-dot" />
                                </div>
                                <p className="llm-pc-desc">{p.description}</p>
                            </div>
                        ))}
                    </div>

                    {/* ── Sélection du modèle ── */}
                    <div
                        className="llm-config-section"
                        key={`model-${provider.id}`}
                        style={{ '--pc': provider.color }}
                    >
                        <div className="llm-section-title">Modèle</div>
                        <div className="llm-model-grid">
                            {provider.models.map(m => (
                                <button
                                    key={m.id}
                                    className={`llm-model-btn ${config.model === m.id ? 'active' : ''}`}
                                    style={{ '--pc': provider.color }}
                                    onClick={() => handleField('model', m.id)}
                                >
                                    {m.label}
                                    {m.badge && <span className="llm-model-badge">{m.badge}</span>}
                                </button>
                            ))}
                        </div>
                        <a
                            href={provider.docsUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="llm-docs-link"
                        >
                            ↗ Obtenir une clé API / en savoir plus sur {provider.label}
                        </a>
                    </div>

                    {/* ── Champs spécifiques au provider ── */}
                    <div
                        className="llm-config-section"
                        key={`fields-${provider.id}`}
                        style={{ '--pc': provider.color }}
                    >
                        <div className="llm-section-title">Authentification & accès</div>
                        {provider.fields.map(field => (
                            <div key={field.key} className="llm-field">
                                <label>{field.label}</label>
                                <div className="llm-field-input-wrap">
                                    <input
                                        type={field.secret && !showSecret[field.key] ? 'password' : 'text'}
                                        value={config[field.key] || ''}
                                        onChange={e => handleField(field.key, e.target.value)}
                                        placeholder={field.placeholder}
                                        autoComplete="off"
                                        spellCheck={false}
                                    />
                                    {field.secret && (
                                        <button
                                            className="llm-eye-btn"
                                            onClick={() => setShowSecret(prev => ({ ...prev, [field.key]: !prev[field.key] }))}
                                            type="button"
                                            title={showSecret[field.key] ? 'Masquer' : 'Afficher'}
                                        >
                                            <Icon name={showSecret[field.key] ? 'eyeOff' : 'eye'} />
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* ── Paramètres avancés ── */}
                    <div
                        className="llm-config-section"
                        style={{ '--pc': provider.color }}
                    >
                        <div className="llm-section-title">Paramètres de génération</div>

                        <div className="llm-field">
                            <label>Température — créativité ({config.temperature})</label>
                            <div className="llm-range-row">
                                <input
                                    type="range" min="0" max="2" step="0.05"
                                    value={config.temperature}
                                    onChange={e => handleField('temperature', parseFloat(e.target.value))}
                                />
                                <span className="llm-range-val">{config.temperature.toFixed(2)}</span>
                            </div>
                        </div>

                        <div className="llm-field">
                            <label>Tokens max ({config.max_tokens.toLocaleString('fr')})</label>
                            <div className="llm-range-row">
                                <input
                                    type="range" min="512" max="8192" step="256"
                                    value={config.max_tokens}
                                    onChange={e => handleField('max_tokens', parseInt(e.target.value))}
                                />
                                <span className="llm-range-val">{config.max_tokens.toLocaleString('fr')}</span>
                            </div>
                        </div>
                    </div>

                    {/* ── Test + résultat ── */}
                    <div style={{ marginBottom: '1.2rem' }}>
                        <button
                            className="llm-btn llm-btn-ghost"
                            style={{ '--pc': provider.color }}
                            onClick={handleTest}
                            disabled={testStatus === 'testing'}
                        >
                            {testStatus === 'testing'
                                ? <><Icon name="spinner" /> Test en cours…</>
                                : <><Icon name="refresh" /> Tester la connexion</>
                            }
                        </button>

                        {testStatus && testStatus !== 'testing' && (
                            <div className={`llm-test-result ${testStatus}`}>
                                <Icon name={testStatus === 'ok' ? 'check' : 'warning'} />
                                <span>{testMessage}</span>
                            </div>
                        )}
                    </div>

                    {/* ── Bouton Sauvegarder ── */}
                    <div className="llm-actions">
                        <button
                            className="llm-btn llm-btn-primary"
                            style={{ '--pc': provider.color }}
                            onClick={handleSave}
                            disabled={status === 'saving' || !isDirty}
                        >
                            {status === 'saving'
                                ? <><Icon name="spinner" /> Sauvegarde…</>
                                : <><Icon name="save" /> Sauvegarder</>
                            }
                        </button>

                        {isDirty && !status && (
                            <span title="Modifications non sauvegardées">
                <span className="llm-dirty-dot" />
              </span>
                        )}

                        {status === 'saved' && (
                            <span className="llm-status-chip saved">
                <Icon name="check" /> Configuration enregistrée
              </span>
                        )}
                        {status === 'error' && (
                            <span className="llm-status-chip error">
                <Icon name="warning" /> Échec de la sauvegarde
              </span>
                        )}
                    </div>

                </div>
            </div>
        </>
    )
}