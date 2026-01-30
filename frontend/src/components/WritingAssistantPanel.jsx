import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function WritingAssistantPanel({ story }) {
  const [mode, setMode] = useState('generate') // generate, continue, rewrite, suggest
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [llmHealth, setLlmHealth] = useState(null)

  // États pour génération de chapitre
  const [characters, setCharacters] = useState([])
  const [locations, setLocations] = useState([])
  const [manuscripts, setManuscripts] = useState([])
  const [generateForm, setGenerateForm] = useState({
    chapter_number: 1,
    chapter_title: '',
    summary: '',
    style: 'narratif',
    length: 'moyen',
    tone: 'neutre',
    pov: 'troisième personne',
    include_characters: [],
    include_locations: []
  })

  // États pour continuation
  const [continueForm, setContinueForm] = useState({
    manuscript_id: null,
    direction: '',
    length: 500
  })

  // États pour réécriture
  const [rewriteForm, setRewriteForm] = useState({
    text: '',
    instruction: ''
  })

  // États pour suggestions
  const [suggestForm, setSuggestForm] = useState({
    current_situation: ''
  })

  useEffect(() => {
    if (story?.id) {
      fetchData()
      checkLlmHealth()
    }
  }, [story])

  async function fetchData() {
    try {
      const [chars, locs, mss] = await Promise.all([
        API.get(`/api/characters?story_id=${story.id}`),
        API.get(`/api/locations?story_id=${story.id}`),
        API.get(`/api/manuscript?story_id=${story.id}`)
      ])
      setCharacters(chars.data)
      setLocations(locs.data)
      setManuscripts(mss.data)
      
      // Pré-sélectionner le premier manuscrit
      if (mss.data.length > 0 && !continueForm.manuscript_id) {
        setContinueForm({ ...continueForm, manuscript_id: mss.data[0].id })
      }
    } catch (err) {
      console.error('Erreur chargement données:', err)
    }
  }

  async function checkLlmHealth() {
    try {
      const r = await API.get('/api/llm/health')
      setLlmHealth(r.data)
    } catch (err) {
      console.error('Erreur vérification LLM:', err)
    }
  }

  async function handleGenerate() {
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const payload = {
        ...generateForm,
        story_id: story.id
      }
      const r = await API.post('/api/llm/generate-chapter', payload)
      setResult(r.data)
    } catch (err) {
      setError(err.response?.data?.detail || 'Erreur lors de la génération')
    } finally {
      setLoading(false)
    }
  }

  async function handleContinue() {
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const r = await API.post('/api/llm/continue-writing', continueForm)
      setResult(r.data)
    } catch (err) {
      setError(err.response?.data?.detail || 'Erreur lors de la continuation')
    } finally {
      setLoading(false)
    }
  }

  async function handleRewrite() {
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const r = await API.post('/api/llm/rewrite', rewriteForm)
      setResult(r.data)
    } catch (err) {
      setError(err.response?.data?.detail || 'Erreur lors de la réécriture')
    } finally {
      setLoading(false)
    }
  }

  async function handleSuggest() {
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const payload = {
        ...suggestForm,
        story_id: story.id
      }
      const r = await API.post('/api/llm/suggest-next-scene', payload)
      setResult(r.data)
    } catch (err) {
      setError(err.response?.data?.detail || 'Erreur lors de la suggestion')
    } finally {
      setLoading(false)
    }
  }

  function toggleCharacter(id) {
    const current = generateForm.include_characters || []
    if (current.includes(id)) {
      setGenerateForm({ ...generateForm, include_characters: current.filter(x => x !== id) })
    } else {
      setGenerateForm({ ...generateForm, include_characters: [...current, id] })
    }
  }

  function toggleLocation(id) {
    const current = generateForm.include_locations || []
    if (current.includes(id)) {
      setGenerateForm({ ...generateForm, include_locations: current.filter(x => x !== id) })
    } else {
      setGenerateForm({ ...generateForm, include_locations: [...current, id] })
    }
  }

  async function saveAsChapter() {
    if (!result?.text) return
    
    try {
      await API.post('/api/manuscript', {
        story_id: story.id,
        title: generateForm.chapter_title || 'Chapitre généré par IA',
        chapter: generateForm.chapter_number || manuscripts.length + 1,
        text: result.text,
        status: 'Généré par IA'
      })
      alert('✅ Chapitre sauvegardé avec succès!')
      fetchData()
    } catch (err) {
      alert('❌ Erreur lors de la sauvegarde')
    }
  }

  if (!story) {
    return (
      <div className="panel">
        <h2>✍️ Assistant d'écriture IA</h2>
        <p style={{ color: '#999' }}>Sélectionnez un roman pour utiliser l'assistant.</p>
      </div>
    )
  }

  return (
    <div className="panel">
      <h2>✍️ Assistant d'écriture IA — <em>{story.title}</em></h2>

      {/* Status LLM */}
      {llmHealth && (
        <div style={{ 
          padding: 12, 
          background: llmHealth.configured ? '#e8f5e9' : '#fff3cd',
          borderRadius: 6,
          marginBottom: 20
        }}>
          <strong>🤖 LLM:</strong> {llmHealth.provider}
          {llmHealth.configured ? (
            <span style={{ color: '#2e7d32', marginLeft: 8 }}>✅ Configuré</span>
          ) : (
            <span style={{ color: '#856404', marginLeft: 8 }}>
              ⚠️ Non configuré - Ajoutez la clé API dans les variables d'environnement
            </span>
          )}
        </div>
      )}

      {/* Sélection du mode */}
      <div style={{ marginBottom: 20 }}>
        <h3>Mode d'assistance</h3>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <button
            onClick={() => setMode('generate')}
            className={mode === 'generate' ? 'primary' : ''}
          >
            📝 Générer un chapitre
          </button>
          <button
            onClick={() => setMode('continue')}
            className={mode === 'continue' ? 'primary' : ''}
          >
            ➡️ Continuer l'écriture
          </button>
          <button
            onClick={() => setMode('rewrite')}
            className={mode === 'rewrite' ? 'primary' : ''}
          >
            🔄 Réécrire un texte
          </button>
          <button
            onClick={() => setMode('suggest')}
            className={mode === 'suggest' ? 'primary' : ''}
          >
            💡 Suggérer une scène
          </button>
        </div>
      </div>

      {/* GÉNÉRATION DE CHAPITRE */}
      {mode === 'generate' && (
        <div>
          <h3>📝 Générer un nouveau chapitre</h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div className="field">
              <label>Numéro du chapitre</label>
              <input
                type="number"
                value={generateForm.chapter_number}
                onChange={e => setGenerateForm({ ...generateForm, chapter_number: parseInt(e.target.value) })}
                className="input"
              />
            </div>
            
            <div className="field">
              <label>Titre du chapitre (optionnel)</label>
              <input
                value={generateForm.chapter_title}
                onChange={e => setGenerateForm({ ...generateForm, chapter_title: e.target.value })}
                placeholder="L'IA peut en générer un"
                className="input"
              />
            </div>
          </div>

          <div className="field">
            <label><strong>Résumé de ce qui doit se passer *</strong></label>
            <textarea
              value={generateForm.summary}
              onChange={e => setGenerateForm({ ...generateForm, summary: e.target.value })}
              placeholder="Ex: Elara découvre le sceptre caché dans les ruines. Elle est poursuivie par Kellan."
              rows={4}
              className="input"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
            <div className="field">
              <label>Style</label>
              <select
                value={generateForm.style}
                onChange={e => setGenerateForm({ ...generateForm, style: e.target.value })}
                className="input"
              >
                <option value="narratif">Narratif</option>
                <option value="dialogue">Dialogue</option>
                <option value="descriptif">Descriptif</option>
                <option value="action">Action</option>
              </select>
            </div>

            <div className="field">
              <label>Longueur</label>
              <select
                value={generateForm.length}
                onChange={e => setGenerateForm({ ...generateForm, length: e.target.value })}
                className="input"
              >
                <option value="court">Court (500-800 mots)</option>
                <option value="moyen">Moyen (1000-1500 mots)</option>
                <option value="long">Long (2000-3000 mots)</option>
              </select>
            </div>

            <div className="field">
              <label>Ton</label>
              <select
                value={generateForm.tone}
                onChange={e => setGenerateForm({ ...generateForm, tone: e.target.value })}
                className="input"
              >
                <option value="neutre">Neutre</option>
                <option value="dramatique">Dramatique</option>
                <option value="humoristique">Humoristique</option>
                <option value="sombre">Sombre</option>
                <option value="léger">Léger</option>
              </select>
            </div>
          </div>

          <div className="field">
            <label>Point de vue</label>
            <select
              value={generateForm.pov}
              onChange={e => setGenerateForm({ ...generateForm, pov: e.target.value })}
              className="input"
              style={{ maxWidth: 300 }}
            >
              <option value="première personne">Première personne (je)</option>
              <option value="troisième personne">Troisième personne (il/elle)</option>
              <option value="deuxième personne">Deuxième personne (tu)</option>
            </select>
          </div>

          {/* Sélection personnages */}
          {characters.length > 0 && (
            <div className="field">
              <label><strong>Personnages à inclure</strong></label>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {characters.map(char => (
                  <label key={char.id} style={{ 
                    padding: '6px 12px', 
                    background: generateForm.include_characters.includes(char.id) ? '#e3f2fd' : '#f5f5f5',
                    borderRadius: 4,
                    cursor: 'pointer'
                  }}>
                    <input
                      type="checkbox"
                      checked={generateForm.include_characters.includes(char.id)}
                      onChange={() => toggleCharacter(char.id)}
                      style={{ marginRight: 6 }}
                    />
                    {char.name}
                  </label>
                ))}
              </div>
            </div>
          )}

          {/* Sélection lieux */}
          {locations.length > 0 && (
            <div className="field">
              <label><strong>Lieux à utiliser</strong></label>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {locations.map(loc => (
                  <label key={loc.id} style={{ 
                    padding: '6px 12px', 
                    background: generateForm.include_locations.includes(loc.id) ? '#e3f2fd' : '#f5f5f5',
                    borderRadius: 4,
                    cursor: 'pointer'
                  }}>
                    <input
                      type="checkbox"
                      checked={generateForm.include_locations.includes(loc.id)}
                      onChange={() => toggleLocation(loc.id)}
                      style={{ marginRight: 6 }}
                    />
                    {loc.name}
                  </label>
                ))}
              </div>
            </div>
          )}

          <button
            className="primary"
            onClick={handleGenerate}
            disabled={loading || !generateForm.summary}
            style={{ fontSize: '1.1em', padding: '12px 24px' }}
          >
            {loading ? '✨ Génération en cours...' : '✨ Générer le chapitre'}
          </button>
        </div>
      )}

      {/* CONTINUATION */}
      {mode === 'continue' && (
        <div>
          <h3>➡️ Continuer l'écriture d'un manuscrit</h3>

          {manuscripts.length === 0 ? (
            <p style={{ color: '#999' }}>Aucun manuscrit disponible. Créez d'abord un chapitre.</p>
          ) : (
            <>
              <div className="field">
                <label>Manuscrit à continuer</label>
                <select
                  value={continueForm.manuscript_id || ''}
                  onChange={e => setContinueForm({ ...continueForm, manuscript_id: parseInt(e.target.value) })}
                  className="input"
                >
                  {manuscripts.map(ms => (
                    <option key={ms.id} value={ms.id}>
                      Chapitre {ms.chapter}: {ms.title}
                    </option>
                  ))}
                </select>
              </div>

              <div className="field">
                <label><strong>Direction pour la suite *</strong></label>
                <textarea
                  value={continueForm.direction}
                  onChange={e => setContinueForm({ ...continueForm, direction: e.target.value })}
                  placeholder="Ex: Le héros découvre un passage secret qui mène à une salle cachée"
                  rows={3}
                  className="input"
                />
              </div>

              <div className="field">
                <label>Nombre de mots approximatif</label>
                <input
                  type="number"
                  value={continueForm.length}
                  onChange={e => setContinueForm({ ...continueForm, length: parseInt(e.target.value) })}
                  className="input"
                  style={{ maxWidth: 200 }}
                />
              </div>

              <button
                className="primary"
                onClick={handleContinue}
                disabled={loading || !continueForm.direction}
                style={{ fontSize: '1.1em', padding: '12px 24px' }}
              >
                {loading ? '✨ Génération...' : '➡️ Continuer l\'écriture'}
              </button>
            </>
          )}
        </div>
      )}

      {/* RÉÉCRITURE */}
      {mode === 'rewrite' && (
        <div>
          <h3>🔄 Réécrire un texte</h3>

          <div className="field">
            <label><strong>Texte à réécrire *</strong></label>
            <textarea
              value={rewriteForm.text}
              onChange={e => setRewriteForm({ ...rewriteForm, text: e.target.value })}
              placeholder="Collez ici le texte que vous souhaitez améliorer..."
              rows={6}
              className="input"
            />
          </div>

          <div className="field">
            <label><strong>Instructions de réécriture *</strong></label>
            <textarea
              value={rewriteForm.instruction}
              onChange={e => setRewriteForm({ ...rewriteForm, instruction: e.target.value })}
              placeholder="Ex: Rendre plus descriptif, Ajouter des dialogues, Changer le ton pour plus dramatique, Simplifier le vocabulaire..."
              rows={3}
              className="input"
            />
          </div>

          <button
            className="primary"
            onClick={handleRewrite}
            disabled={loading || !rewriteForm.text || !rewriteForm.instruction}
            style={{ fontSize: '1.1em', padding: '12px 24px' }}
          >
            {loading ? '✨ Réécriture...' : '🔄 Réécrire'}
          </button>
        </div>
      )}

      {/* SUGGESTIONS */}
      {mode === 'suggest' && (
        <div>
          <h3>💡 Suggérer la prochaine scène</h3>

          <div className="field">
            <label><strong>Situation actuelle dans l'histoire *</strong></label>
            <textarea
              value={suggestForm.current_situation}
              onChange={e => setSuggestForm({ ...suggestForm, current_situation: e.target.value })}
              placeholder="Ex: Elara a trouvé le sceptre mais Kellan est sur ses traces. Elle doit quitter la ville avant l'aube..."
              rows={4}
              className="input"
            />
          </div>

          <button
            className="primary"
            onClick={handleSuggest}
            disabled={loading || !suggestForm.current_situation}
            style={{ fontSize: '1.1em', padding: '12px 24px' }}
          >
            {loading ? '💡 Réflexion...' : '💡 Obtenir des suggestions'}
          </button>
        </div>
      )}

      {/* ERREUR */}
      {error && (
        <div style={{ 
          marginTop: 20,
          padding: 16, 
          background: '#fee', 
          border: '1px solid #fcc',
          borderRadius: 6,
          color: '#c00'
        }}>
          <strong>❌ Erreur:</strong> {error}
        </div>
      )}

      {/* RÉSULTATS */}
      {result && (
        <div style={{ marginTop: 20 }}>
          <h3>✨ Résultat</h3>

          {/* Génération de chapitre */}
          {mode === 'generate' && result.text && (
            <div>
              <div style={{ 
                padding: 16, 
                background: '#f9f9f9', 
                borderRadius: 6,
                border: '1px solid #ddd',
                maxHeight: '500px',
                overflowY: 'auto'
              }}>
                <div style={{ marginBottom: 12 }}>
                  <strong>📊 Statistiques:</strong> {result.word_count} mots
                </div>
                <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>
                  {result.text}
                </div>
              </div>
              <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
                <button className="primary" onClick={saveAsChapter}>
                  💾 Sauvegarder comme chapitre
                </button>
                <button onClick={() => navigator.clipboard.writeText(result.text)}>
                  📋 Copier dans le presse-papier
                </button>
              </div>
            </div>
          )}

          {/* Continuation */}
          {mode === 'continue' && result.continuation && (
            <div>
              <div style={{ 
                padding: 16, 
                background: '#f9f9f9', 
                borderRadius: 6,
                border: '1px solid #ddd',
                maxHeight: '500px',
                overflowY: 'auto'
              }}>
                <div style={{ marginBottom: 12 }}>
                  <strong>📊</strong> {result.word_count} mots ajoutés
                </div>
                <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>
                  {result.continuation}
                </div>
              </div>
              <div style={{ marginTop: 12 }}>
                <button onClick={() => navigator.clipboard.writeText(result.continuation)}>
                  📋 Copier la continuation
                </button>
              </div>
            </div>
          )}

          {/* Réécriture */}
          {mode === 'rewrite' && result.rewritten && (
            <div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                <div>
                  <h4>📄 Texte original</h4>
                  <div style={{ 
                    padding: 12, 
                    background: '#f9f9f9', 
                    borderRadius: 6,
                    maxHeight: '400px',
                    overflowY: 'auto',
                    whiteSpace: 'pre-wrap'
                  }}>
                    {result.original}
                  </div>
                </div>
                <div>
                  <h4>✨ Texte réécrit</h4>
                  <div style={{ 
                    padding: 12, 
                    background: '#e8f5e9', 
                    borderRadius: 6,
                    maxHeight: '400px',
                    overflowY: 'auto',
                    whiteSpace: 'pre-wrap'
                  }}>
                    {result.rewritten}
                  </div>
                </div>
              </div>
              <div style={{ marginTop: 12 }}>
                <button onClick={() => navigator.clipboard.writeText(result.rewritten)}>
                  📋 Copier le texte réécrit
                </button>
              </div>
            </div>
          )}

          {/* Suggestions */}
          {mode === 'suggest' && result.suggestions && (
            <div>
              {result.suggestions.map((sugg, idx) => (
                <div key={idx} style={{ 
                  marginBottom: 16,
                  padding: 16, 
                  background: '#f0f9ff',
                  borderLeft: '4px solid #2196f3',
                  borderRadius: 4
                }}>
                  <h4 style={{ marginTop: 0 }}>💡 {sugg.title}</h4>
                  <p>{sugg.description}</p>
                  {sugg.characters && sugg.characters.length > 0 && (
                    <div style={{ fontSize: '0.9em', color: '#666' }}>
                      <strong>Personnages:</strong> {sugg.characters.join(', ')}
                    </div>
                  )}
                  {sugg.impact && (
                    <div style={{ fontSize: '0.9em', color: '#666', marginTop: 4 }}>
                      <strong>Impact:</strong> {sugg.impact}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {/* Réponse brute si JSON invalide */}
          {result.raw_response && (
            <div style={{ 
              padding: 16, 
              background: '#f9f9f9', 
              borderRadius: 6,
              whiteSpace: 'pre-wrap'
            }}>
              {result.raw_response}
            </div>
          )}
        </div>
      )}
    </div>
  )
}