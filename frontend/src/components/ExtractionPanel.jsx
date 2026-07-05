import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function ExtractionPanel({ story }) {
  const [manuscripts, setManuscripts] = useState([])
  const [selectedManuscript, setSelectedManuscript] = useState(null)
  const [extractTypes, setExtractTypes] = useState(['characters', 'locations', 'timeline', 'lore'])
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState(null)
  const [error, setError] = useState(null)


  const [validationState, setValidationState] = useState({
    characters: {},
    locations: {},
    timeline: {},
    lore: {}
  })

  const [currentStep, setCurrentStep] = useState('select')

  useEffect(() => {
    if (story?.id) {
      fetchManuscripts()
    }
  }, [story])

  async function fetchManuscripts() {
    try {
      const r = await API.get(`/api/manuscript?storyId=${story.id}`)
      setManuscripts(r.data)
      if (r.data.length > 0 && !selectedManuscript) {
        setSelectedManuscript(r.data[0].id)
      }
    } catch (err) {
      console.error('Erreur chargement manuscrits:', err)
    }
  }

  async function handleExtract() {
    setLoading(true)
    setError(null)
    setResults(null)

    try {
      const r = await API.post('/api/extraction/analyze', {
        manuscript_id: selectedManuscript,
        extract_types: extractTypes
      })

      setResults(r.data)


      const newState = {
        characters: {},
        locations: {},
        timeline: {},
        lore: {}
      }

      r.data.characters?.forEach((char, idx) => {
        newState.characters[idx] = { approved: char.confidence > 0.7, edited: false }
      })
      r.data.locations?.forEach((loc, idx) => {
        newState.locations[idx] = { approved: loc.confidence > 0.7, edited: false }
      })
      r.data.timeline?.forEach((evt, idx) => {
        newState.timeline[idx] = { approved: evt.confidence > 0.7, edited: false }
      })
      r.data.lore?.forEach((lore, idx) => {
        newState.lore[idx] = { approved: lore.confidence > 0.7, edited: false }
      })

      setValidationState(newState)
      setCurrentStep('validate')
    } catch (err) {
      console.error('Erreur extraction:', err)
      setError(err.response?.data?.detail || 'Erreur lors de l\'extraction')
    } finally {
      setLoading(false)
    }
  }

  function toggleApproval(type, index) {
    setValidationState(prev => ({
      ...prev,
      [type]: {
        ...prev[type],
        [index]: {
          ...prev[type][index],
          approved: !prev[type][index]?.approved
        }
      }
    }))
  }

  function updateItem(type, index, field, value) {
    const items = [...results[type]]
    items[index] = { ...items[index], [field]: value }
    setResults({ ...results, [type]: items })

    // Marquer comme édité
    setValidationState(prev => ({
      ...prev,
      [type]: {
        ...prev[type],
        [index]: { ...prev[type][index], edited: true }
      }
    }))
  }

  async function handleValidateAndCreate() {
    setLoading(true)
    setCurrentStep('create')

    const createdItems = {
      characters: [],
      locations: [],
      timeline: [],
      lore: []
    }

    try {
      // Créer les personnages
      for (let i = 0; i < (results.characters || []).length; i++) {
        if (validationState.characters[i]?.approved) {
          try {
            const r = await API.post('/api/extraction/validate-and-create', {
              storyId: story.id,
              item_type: 'character',
              item_data: results.characters[i],
              approved: true
            })
            createdItems.characters.push(r.data)
          } catch (err) {
            console.error('Erreur création personnage:', err)
          }
        }
      }

      // Créer les lieux
      for (let i = 0; i < (results.locations || []).length; i++) {
        if (validationState.locations[i]?.approved) {
          try {
            const r = await API.post('/api/extraction/validate-and-create', {
              storyId: story.id,
              item_type: 'location',
              item_data: results.locations[i],
              approved: true
            })
            createdItems.locations.push(r.data)
          } catch (err) {
            console.error('Erreur création lieu:', err)
          }
        }
      }

      // Créer les entrées de lore
      for (let i = 0; i < (results.lore || []).length; i++) {
        if (validationState.lore[i]?.approved) {
          try {
            const r = await API.post('/api/extraction/validate-and-create', {
              storyId: story.id,
              item_type: 'lore',
              item_data: results.lore[i],
              approved: true
            })
            createdItems.lore.push(r.data)
          } catch (err) {
            console.error('Erreur création lore:', err)
          }
        }
      }

      // Créer les événements chronologiques (après les personnages et lieux)
      for (let i = 0; i < (results.timeline || []).length; i++) {
        if (validationState.timeline[i]?.approved) {
          try {
            const r = await API.post('/api/extraction/validate-and-create', {
              storyId: story.id,
              item_type: 'timeline',
              item_data: results.timeline[i],
              approved: true
            })
            createdItems.timeline.push(r.data)
          } catch (err) {
            console.error('Erreur création événement:', err)
          }
        }
      }

      // Afficher le résumé
      const total = Object.values(createdItems).reduce((sum, arr) => sum + arr.length, 0)
      alert(`✅ ${total} éléments créés avec succès !\n\n` +
            `Personnages: ${createdItems.characters.length}\n` +
            `Lieux: ${createdItems.locations.length}\n` +
            `Timeline: ${createdItems.timeline.length}\n` +
            `Lore: ${createdItems.lore.length}`)

      // Réinitialiser
      setResults(null)
      setCurrentStep('select')

    } catch (err) {
      console.error('Erreur validation:', err)
      alert('❌ Erreur lors de la création des éléments')
    } finally {
      setLoading(false)
    }
  }

  function getApprovedCount(type) {
    return Object.values(validationState[type] || {}).filter(v => v.approved).length
  }

  function getTotalCount(type) {
    return (results?.[type] || []).length
  }

  if (!story) {
    return (
      <div className="panel">
        <h2>🔍 Extraction automatique</h2>
        <p style={{ color: '#999' }}>Sélectionnez un roman pour utiliser l'extraction.</p>
      </div>
    )
  }

  return (
    <div className="panel">
      <h2>🔍 Extraction automatique : <em>{story.title}</em></h2>

      <div style={{
        padding: 16,
        background: '#e3f2fd',
        borderRadius: 6,
        marginBottom: 20
      }}>
        <h3 style={{ marginTop: 0 }}>💡 Comment ça fonctionne ?</h3>
        <ol style={{ margin: 0, paddingLeft: 20, lineHeight: 1.8 }}>
          <li>Sélectionnez un manuscrit à analyser</li>
          <li>Choisissez les types d'éléments à extraire</li>
          <li>L'IA analyse le texte et extrait les informations</li>
          <li>Validez ou modifiez chaque élément trouvé</li>
          <li>Créez les entrées dans votre base de données</li>
        </ol>
      </div>

      {currentStep === 'select' && (
        <div>
          <h3>📝 1. Sélection du manuscrit</h3>

          {manuscripts.length === 0 ? (
            <div style={{ padding: 12, background: '#fff3cd', borderRadius: 6, color: '#856404' }}>
              ⚠️ Aucun manuscrit disponible. Créez d'abord un chapitre dans l'onglet Manuscrit.
            </div>
          ) : (
            <>
              <div className="field">
                <label>Manuscrit à analyser</label>
                <select
                  value={selectedManuscript || ''}
                  onChange={(e) => setSelectedManuscript(parseInt(e.target.value))}
                  className="input"
                  style={{ maxWidth: 400 }}
                >
                  {manuscripts.map(ms => (
                    <option key={ms.id} value={ms.id}>
                      Chapitre {ms.chapter}: {ms.title} ({ms.text?.length || 0} caractères)
                    </option>
                  ))}
                </select>
              </div>

              <div className="field">
                <label><strong>Éléments à extraire</strong></label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <label style={{ cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={extractTypes.includes('characters')}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setExtractTypes([...extractTypes, 'characters'])
                        } else {
                          setExtractTypes(extractTypes.filter(t => t !== 'characters'))
                        }
                      }}
                      style={{ marginRight: 8 }}
                    />
                    👥 Personnages (noms, rôles, descriptions...)
                  </label>

                  <label style={{ cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={extractTypes.includes('locations')}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setExtractTypes([...extractTypes, 'locations'])
                        } else {
                          setExtractTypes(extractTypes.filter(t => t !== 'locations'))
                        }
                      }}
                      style={{ marginRight: 8 }}
                    />
                    📍 Lieux (villes, planètes, bâtiments...)
                  </label>

                  <label style={{ cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={extractTypes.includes('timeline')}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setExtractTypes([...extractTypes, 'timeline'])
                        } else {
                          setExtractTypes(extractTypes.filter(t => t !== 'timeline'))
                        }
                      }}
                      style={{ marginRight: 8 }}
                    />
                    ⏰ Événements chronologiques
                  </label>

                  <label style={{ cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={extractTypes.includes('lore')}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setExtractTypes([...extractTypes, 'lore'])
                        } else {
                          setExtractTypes(extractTypes.filter(t => t !== 'lore'))
                        }
                      }}
                      style={{ marginRight: 8 }}
                    />
                    📚 Lore / World-building (magie, technologie, factions...)
                  </label>
                </div>
              </div>

              <button
                className="primary"
                onClick={handleExtract}
                disabled={loading || !selectedManuscript || extractTypes.length === 0}
                style={{ fontSize: '1.1em', padding: '12px 24px' }}
              >
                {loading ? '🔍 Extraction en cours...' : '🚀 Lancer l\'extraction'}
              </button>
            </>
          )}
        </div>
      )}

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

      {currentStep === 'validate' && results && (
        <div style={{ marginTop: 20 }}>
          <h3>✅ 2. Validation des éléments extraits</h3>

          <div style={{
            padding: 12,
            background: '#fff3cd',
            borderRadius: 6,
            marginBottom: 20,
            color: '#856404'
          }}>
            <strong>⚠️ Important :</strong> Vérifiez chaque élément avant de créer les entrées.
            Vous pouvez modifier les informations et décocher celles qui sont incorrectes.
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            gap: 12,
            marginBottom: 20
          }}>
            <div style={{ padding: 12, background: '#e8f5e9', borderRadius: 6, textAlign: 'center' }}>
              <div style={{ fontSize: '2em' }}>👥</div>
              <div><strong>{getApprovedCount('characters')}/{getTotalCount('characters')}</strong></div>
              <div style={{ fontSize: '0.85em' }}>Personnages</div>
            </div>
            <div style={{ padding: 12, background: '#e3f2fd', borderRadius: 6, textAlign: 'center' }}>
              <div style={{ fontSize: '2em' }}>📍</div>
              <div><strong>{getApprovedCount('locations')}/{getTotalCount('locations')}</strong></div>
              <div style={{ fontSize: '0.85em' }}>Lieux</div>
            </div>
            <div style={{ padding: 12, background: '#f3e5f5', borderRadius: 6, textAlign: 'center' }}>
              <div style={{ fontSize: '2em' }}>⏰</div>
              <div><strong>{getApprovedCount('timeline')}/{getTotalCount('timeline')}</strong></div>
              <div style={{ fontSize: '0.85em' }}>Événements</div>
            </div>
            <div style={{ padding: 12, background: '#fff3e0', borderRadius: 6, textAlign: 'center' }}>
              <div style={{ fontSize: '2em' }}>📚</div>
              <div><strong>{getApprovedCount('lore')}/{getTotalCount('lore')}</strong></div>
              <div style={{ fontSize: '0.85em' }}>Lore</div>
            </div>
          </div>

          {results.characters && results.characters.length > 0 && (
            <div style={{ marginBottom: 30 }}>
              <h4>👥 Personnages ({results.characters.length})</h4>
              {results.characters.map((char, idx) => (
                <div key={idx} style={{
                  padding: 16,
                  background: validationState.characters[idx]?.approved ? '#e8f5e9' : '#fafafa',
                  border: `2px solid ${validationState.characters[idx]?.approved ? '#4caf50' : '#ddd'}`,
                  borderRadius: 6,
                  marginBottom: 12
                }}>
                  <div style={{ display: 'flex', alignItems: 'start', gap: 12 }}>
                    <input
                      type="checkbox"
                      checked={validationState.characters[idx]?.approved || false}
                      onChange={() => toggleApproval('characters', idx)}
                      style={{ marginTop: 4 }}
                    />
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 8 }}>
                        <input
                          value={char.name || ''}
                          onChange={(e) => updateItem('characters', idx, 'name', e.target.value)}
                          placeholder="Nom"
                          className="input"
                          style={{ fontWeight: 'bold' }}
                        />
                        <input
                          value={char.surname || ''}
                          onChange={(e) => updateItem('characters', idx, 'surname', e.target.value)}
                          placeholder="Prénom"
                          className="input"
                        />
                      </div>
                      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 8, marginBottom: 8 }}>
                        <input
                          value={char.role || ''}
                          onChange={(e) => updateItem('characters', idx, 'role', e.target.value)}
                          placeholder="Rôle"
                          className="input"
                        />
                        <input
                          type="number"
                          value={char.age || ''}
                          onChange={(e) => updateItem('characters', idx, 'age', parseInt(e.target.value) || null)}
                          placeholder="Âge"
                          className="input"
                        />
                      </div>
                      <textarea
                        value={char.physical_description || ''}
                        onChange={(e) => updateItem('characters', idx, 'physical_description', e.target.value)}
                        placeholder="Description physique"
                        rows={2}
                        className="input"
                        style={{ marginBottom: 8 }}
                      />
                      <textarea
                        value={char.personality || ''}
                        onChange={(e) => updateItem('characters', idx, 'personality', e.target.value)}
                        placeholder="Personnalité"
                        rows={2}
                        className="input"
                      />
                      <div style={{ fontSize: '0.85em', color: '#666', marginTop: 8 }}>
                        Confiance: {(char.confidence * 100).toFixed(0)}%
                        {validationState.characters[idx]?.edited &&
                          <span style={{ marginLeft: 8, color: '#ff9800' }}>✏️ Modifié</span>
                        }
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {results.locations && results.locations.length > 0 && (
            <div style={{ marginBottom: 30 }}>
              <h4>📍 Lieux ({results.locations.length})</h4>
              {results.locations.map((loc, idx) => (
                <div key={idx} style={{
                  padding: 16,
                  background: validationState.locations[idx]?.approved ? '#e3f2fd' : '#fafafa',
                  border: `2px solid ${validationState.locations[idx]?.approved ? '#2196f3' : '#ddd'}`,
                  borderRadius: 6,
                  marginBottom: 12
                }}>
                  <div style={{ display: 'flex', alignItems: 'start', gap: 12 }}>
                    <input
                      type="checkbox"
                      checked={validationState.locations[idx]?.approved || false}
                      onChange={() => toggleApproval('locations', idx)}
                      style={{ marginTop: 4 }}
                    />
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 8, marginBottom: 8 }}>
                        <input
                          value={loc.name || ''}
                          onChange={(e) => updateItem('locations', idx, 'name', e.target.value)}
                          placeholder="Nom du lieu"
                          className="input"
                          style={{ fontWeight: 'bold' }}
                        />
                        <input
                          value={loc.type || ''}
                          onChange={(e) => updateItem('locations', idx, 'type', e.target.value)}
                          placeholder="Type"
                          className="input"
                        />
                      </div>
                      <textarea
                        value={loc.summary || ''}
                        onChange={(e) => updateItem('locations', idx, 'summary', e.target.value)}
                        placeholder="Description"
                        rows={2}
                        className="input"
                      />
                      <div style={{ fontSize: '0.85em', color: '#666', marginTop: 8 }}>
                        Confiance: {(loc.confidence * 100).toFixed(0)}%
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {results.timeline && results.timeline.length > 0 && (
            <div style={{ marginBottom: 30 }}>
              <h4>⏰ Événements ({results.timeline.length})</h4>
              {results.timeline.map((evt, idx) => (
                <div key={idx} style={{
                  padding: 16,
                  background: validationState.timeline[idx]?.approved ? '#f3e5f5' : '#fafafa',
                  border: `2px solid ${validationState.timeline[idx]?.approved ? '#9c27b0' : '#ddd'}`,
                  borderRadius: 6,
                  marginBottom: 12
                }}>
                  <div style={{ display: 'flex', alignItems: 'start', gap: 12 }}>
                    <input
                      type="checkbox"
                      checked={validationState.timeline[idx]?.approved || false}
                      onChange={() => toggleApproval('timeline', idx)}
                      style={{ marginTop: 4 }}
                    />
                    <div style={{ flex: 1 }}>
                      <input
                        value={evt.title || ''}
                        onChange={(e) => updateItem('timeline', idx, 'title', e.target.value)}
                        placeholder="Titre de l'événement"
                        className="input"
                        style={{ fontWeight: 'bold', marginBottom: 8 }}
                      />
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 100px', gap: 8, marginBottom: 8 }}>
                        <input
                          value={evt.date || ''}
                          onChange={(e) => updateItem('timeline', idx, 'date', e.target.value)}
                          placeholder="Date"
                          className="input"
                        />
                        <input
                          type="number"
                          value={evt.sort_order || 0}
                          onChange={(e) => updateItem('timeline', idx, 'sort_order', parseInt(e.target.value))}
                          placeholder="Ordre"
                          className="input"
                        />
                      </div>
                      <textarea
                        value={evt.summary || ''}
                        onChange={(e) => updateItem('timeline', idx, 'summary', e.target.value)}
                        placeholder="Résumé"
                        rows={2}
                        className="input"
                        style={{ marginBottom: 8 }}
                      />
                      {evt.character_names && evt.character_names.length > 0 && (
                        <div style={{ fontSize: '0.9em', color: '#666', marginBottom: 4 }}>
                          <strong>Personnages:</strong> {evt.character_names.join(', ')}
                        </div>
                      )}
                      {evt.location_name && (
                        <div style={{ fontSize: '0.9em', color: '#666', marginBottom: 4 }}>
                          <strong>Lieu:</strong> {evt.location_name}
                        </div>
                      )}
                      <div style={{ fontSize: '0.85em', color: '#666', marginTop: 8 }}>
                        Confiance: {(evt.confidence * 100).toFixed(0)}%
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {results.lore && results.lore.length > 0 && (
            <div style={{ marginBottom: 30 }}>
              <h4>📚 Lore ({results.lore.length})</h4>
              {results.lore.map((lore, idx) => (
                <div key={idx} style={{
                  padding: 16,
                  background: validationState.lore[idx]?.approved ? '#fff3e0' : '#fafafa',
                  border: `2px solid ${validationState.lore[idx]?.approved ? '#ff9800' : '#ddd'}`,
                  borderRadius: 6,
                  marginBottom: 12
                }}>
                  <div style={{ display: 'flex', alignItems: 'start', gap: 12 }}>
                    <input
                      type="checkbox"
                      checked={validationState.lore[idx]?.approved || false}
                      onChange={() => toggleApproval('lore', idx)}
                      style={{ marginTop: 4 }}
                    />
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 8, marginBottom: 8 }}>
                        <input
                          value={lore.title || ''}
                          onChange={(e) => updateItem('lore', idx, 'title', e.target.value)}
                          placeholder="Titre"
                          className="input"
                          style={{ fontWeight: 'bold' }}
                        />
                        <input
                          value={lore.category || ''}
                          onChange={(e) => updateItem('lore', idx, 'category', e.target.value)}
                          placeholder="Catégorie"
                          className="input"
                        />
                      </div>
                      <textarea
                        value={lore.content || ''}
                        onChange={(e) => updateItem('lore', idx, 'content', e.target.value)}
                        placeholder="Contenu"
                        rows={3}
                        className="input"
                      />
                      <div style={{ fontSize: '0.85em', color: '#666', marginTop: 8 }}>
                        Confiance: {(lore.confidence * 100).toFixed(0)}%
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
            <button
              className="primary"
              onClick={handleValidateAndCreate}
              disabled={loading}
              style={{ fontSize: '1.1em', padding: '12px 24px' }}
            >
              {loading ? '⏳ Création en cours...' : '✅ Valider et créer les entrées'}
            </button>
            <button
              onClick={() => {
                setCurrentStep('select')
                setResults(null)
              }}
            >
              Annuler
            </button>
          </div>
        </div>
      )}

      {currentStep === 'create' && (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <div style={{ fontSize: '4em', marginBottom: 20 }}>⏳</div>
          <h3>Création des entrées en cours...</h3>
          <p style={{ color: '#666' }}>Veuillez patienter pendant la création des éléments dans la base de données.</p>
        </div>
      )}
    </div>
  )
}