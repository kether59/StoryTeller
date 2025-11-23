import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function AiPanel({ story }) {
  const [manuscripts, setManuscripts] = useState([])
  const [selectedManuscript, setSelectedManuscript] = useState(null)
  const [analysisType, setAnalysisType] = useState('link_characters')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (story?.id) {
      fetchManuscripts()
    }
  }, [story])

  async function fetchManuscripts() {
    try {
      const r = await API.get(`/api/manuscript?story_id=${story.id}`)
      setManuscripts(r.data)
      if (r.data.length > 0 && !selectedManuscript) {
        setSelectedManuscript(r.data[0].id)
      }
    } catch (err) {
      console.error('Erreur chargement manuscrits:', err)
    }
  }

  async function runAnalysis() {
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const payload = {
        intent: analysisType,
        manuscript_id: ['script_consistency', 'character_behavior', 'lore_check'].includes(analysisType)
          ? selectedManuscript
          : null
      }

      const r = await API.post('/api/ai/suggest', payload)
      setResult(r.data)
    } catch (err) {
      console.error('Erreur analyse IA:', err)
      setError(err.response?.data?.detail || 'Erreur lors de l\'analyse')
    } finally {
      setLoading(false)
    }
  }

  if (!story) {
    return (
      <div className="panel">
        <h2>🤖 Assistant IA</h2>
        <p style={{ color: '#999' }}>Sélectionnez d'abord un roman pour utiliser l'assistant IA.</p>
      </div>
    )
  }

  const needsManuscript = ['script_consistency', 'character_behavior', 'lore_check'].includes(analysisType)

  return (
    <div className="panel">
      <h2>🤖 Assistant IA — <em>{story.title}</em></h2>

      <div style={{ marginBottom: 20, padding: 16, background: '#f0f9ff', borderRadius: 6 }}>
        <h3 style={{ marginTop: 0 }}>💡 À propos de l'assistant</h3>
        <p style={{ fontSize: '0.9em', lineHeight: 1.6, margin: 0 }}>
          L'assistant IA utilise le traitement du langage naturel (NLP) pour analyser votre roman
          et vous aider à détecter des incohérences, suggérer des liens entre personnages,
          et vérifier la cohérence narrative.
        </p>
      </div>

      {/* Sélection du type d'analyse */}
      <div style={{ marginBottom: 20 }}>
        <h3>Type d'analyse</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>

          {/* Analyse 1 */}
          <label
            style={{
              padding: 12,
              border: analysisType === 'link_characters' ? '2px solid #2196f3' : '1px solid #ddd',
              borderRadius: 6,
              cursor: 'pointer',
              background: analysisType === 'link_characters' ? '#e3f2fd' : 'white'
            }}
          >
            <input
              type="radio"
              name="analysisType"
              value="link_characters"
              checked={analysisType === 'link_characters'}
              onChange={(e) => setAnalysisType(e.target.value)}
              style={{ marginRight: 8 }}
            />
            <strong>🔗 Liens entre personnages</strong>
            <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
              Trouve les relations familiales et générationnelles
            </div>
          </label>

          {/* Analyse 2 */}
          <label
            style={{
              padding: 12,
              border: analysisType === 'timeline_conflicts' ? '2px solid #2196f3' : '1px solid #ddd',
              borderRadius: 6,
              cursor: 'pointer',
              background: analysisType === 'timeline_conflicts' ? '#e3f2fd' : 'white'
            }}
          >
            <input
              type="radio"
              name="analysisType"
              value="timeline_conflicts"
              checked={analysisType === 'timeline_conflicts'}
              onChange={(e) => setAnalysisType(e.target.value)}
              style={{ marginRight: 8 }}
            />
            <strong>⏰ Conflits chronologiques</strong>
            <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
              Détecte les incohérences de dates
            </div>
          </label>

          {/* Analyse 3 */}
          <label
            style={{
              padding: 12,
              border: analysisType === 'script_consistency' ? '2px solid #2196f3' : '1px solid #ddd',
              borderRadius: 6,
              cursor: 'pointer',
              background: analysisType === 'script_consistency' ? '#e3f2fd' : 'white'
            }}
          >
            <input
              type="radio"
              name="analysisType"
              value="script_consistency"
              checked={analysisType === 'script_consistency'}
              onChange={(e) => setAnalysisType(e.target.value)}
              style={{ marginRight: 8 }}
            />
            <strong>📝 Cohérence du script</strong>
            <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
              Mentions de personnages et lore
            </div>
          </label>

          {/* Analyse 4 */}
          <label
            style={{
              padding: 12,
              border: analysisType === 'character_behavior' ? '2px solid #2196f3' : '1px solid #ddd',
              borderRadius: 6,
              cursor: 'pointer',
              background: analysisType === 'character_behavior' ? '#e3f2fd' : 'white'
            }}
          >
            <input
              type="radio"
              name="analysisType"
              value="character_behavior"
              checked={analysisType === 'character_behavior'}
              onChange={(e) => setAnalysisType(e.target.value)}
              style={{ marginRight: 8 }}
            />
            <strong>👤 Comportement des personnages</strong>
            <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
              Vérifie la cohérence avec leur personnalité
            </div>
          </label>

          {/* Analyse 5 */}
          <label
            style={{
              padding: 12,
              border: analysisType === 'lore_check' ? '2px solid #2196f3' : '1px solid #ddd',
              borderRadius: 6,
              cursor: 'pointer',
              background: analysisType === 'lore_check' ? '#e3f2fd' : 'white'
            }}
          >
            <input
              type="radio"
              name="analysisType"
              value="lore_check"
              checked={analysisType === 'lore_check'}
              onChange={(e) => setAnalysisType(e.target.value)}
              style={{ marginRight: 8 }}
            />
            <strong>📚 Vérification du Lore</strong>
            <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
              Analyse les mentions de concepts du monde
            </div>
          </label>
        </div>
      </div>

      {/* Sélection du manuscrit (si nécessaire) */}
      {needsManuscript && (
        <div style={{ marginBottom: 20 }}>
          <h3>Manuscrit à analyser</h3>
          {manuscripts.length === 0 ? (
            <div style={{ padding: 12, background: '#fff3cd', borderRadius: 6, color: '#856404' }}>
              ⚠️ Aucun manuscrit disponible. Créez d'abord un chapitre dans l'onglet Manuscrit.
            </div>
          ) : (
            <select
              value={selectedManuscript || ''}
              onChange={(e) => setSelectedManuscript(parseInt(e.target.value))}
              className="input"
              style={{ maxWidth: 400 }}
            >
              {manuscripts.map(ms => (
                <option key={ms.id} value={ms.id}>
                  Chapitre {ms.chapter}: {ms.title}
                </option>
              ))}
            </select>
          )}
        </div>
      )}

      {/* Bouton d'analyse */}
      <div style={{ marginBottom: 20 }}>
        <button
          className="primary"
          onClick={runAnalysis}
          disabled={loading || (needsManuscript && manuscripts.length === 0)}
          style={{
            fontSize: '1.1em',
            padding: '12px 24px',
            opacity: loading || (needsManuscript && manuscripts.length === 0) ? 0.6 : 1
          }}
        >
          {loading ? '🔄 Analyse en cours...' : '🚀 Lancer l\'analyse'}
        </button>
      </div>

      {/* Erreur */}
      {error && (
        <div style={{
          padding: 16,
          background: '#fee',
          border: '1px solid #fcc',
          borderRadius: 6,
          marginBottom: 20,
          color: '#c00'
        }}>
          <strong>❌ Erreur:</strong> {error}
          {error.includes('NLP') && (
            <div style={{ marginTop: 8, fontSize: '0.9em' }}>
              💡 Assurez-vous que spaCy est installé:
              <pre style={{ background: '#fff', padding: 8, marginTop: 4, borderRadius: 4 }}>
                pip install spacy{'\n'}
                python -m spacy download fr_core_news_md
              </pre>
            </div>
          )}
        </div>
      )}

      {/* Résultats */}
      {result && (
        <div style={{ marginTop: 20 }}>
          <h3>📊 Résultats de l'analyse</h3>

          {/* LINK CHARACTERS */}
          {analysisType === 'link_characters' && result.suggestions && (
            <div>
              <h4>🔗 Suggestions de liens ({result.suggestions.length})</h4>
              {result.suggestions.length === 0 ? (
                <p style={{ color: '#999' }}>Aucune relation détectée automatiquement.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {result.suggestions.map((sugg, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: 12,
                        background: sugg.type === 'family' ? '#e8f5e9' : '#fff3e0',
                        borderLeft: `4px solid ${sugg.type === 'family' ? '#4caf50' : '#ff9800'}`,
                        borderRadius: 4
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{ fontSize: '1.2em' }}>
                          {sugg.type === 'family' ? '👨‍👩‍👧‍👦' : '👥'}
                        </span>
                        <div style={{ flex: 1 }}>
                          <strong>{sugg.type === 'family' ? 'Lien familial' : 'Même génération'}</strong>
                          <div style={{ fontSize: '0.9em', color: '#666', marginTop: 2 }}>
                            Personnages ID: {sugg.pair.join(' et ')}
                          </div>
                          <div style={{ fontSize: '0.85em', color: '#999', marginTop: 2 }}>
                            {sugg.reason}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TIMELINE CONFLICTS */}
          {analysisType === 'timeline_conflicts' && result.conflicts && (
            <div>
              <h4>⏰ Conflits chronologiques ({result.conflicts.length})</h4>
              {result.conflicts.length === 0 ? (
                <div style={{ padding: 12, background: '#e8f5e9', borderRadius: 6, color: '#2e7d32' }}>
                  ✅ Aucun conflit détecté ! La chronologie semble cohérente.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {result.conflicts.map((conflict, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: 12,
                        background: '#ffebee',
                        borderLeft: '4px solid #f44336',
                        borderRadius: 4
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'start', gap: 8 }}>
                        <span style={{ fontSize: '1.2em' }}>⚠️</span>
                        <div>
                          <strong>Incohérence de date</strong>
                          <div style={{ fontSize: '0.9em', marginTop: 4 }}>
                            {conflict.reason}
                          </div>
                          <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
                            Événement ID: {conflict.event_id} | Personnage ID: {conflict.character_id}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* SCRIPT CONSISTENCY */}
          {analysisType === 'script_consistency' && (
            <div>
              {result.mentions && (
                <div style={{ marginBottom: 20 }}>
                  <h4>👥 Mentions de personnages</h4>
                  {Object.keys(result.mentions).length === 0 ? (
                    <p style={{ color: '#999' }}>Aucun personnage mentionné dans ce manuscrit.</p>
                  ) : (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                      {Object.entries(result.mentions).map(([name, count]) => (
                        <div
                          key={name}
                          style={{
                            padding: '8px 12px',
                            background: '#e3f2fd',
                            borderRadius: 20,
                            fontSize: '0.9em'
                          }}
                        >
                          <strong>{name}</strong>: {count} fois
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {result.lore_mentions && (
                <div>
                  <h4>📚 Mentions du Lore</h4>
                  {result.lore_mentions.length === 0 ? (
                    <p style={{ color: '#999' }}>Aucun élément du lore mentionné.</p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                      {result.lore_mentions.map((lore, idx) => (
                        <div
                          key={idx}
                          style={{
                            padding: 12,
                            background: '#f3e5f5',
                            borderLeft: '4px solid #9c27b0',
                            borderRadius: 4
                          }}
                        >
                          <strong>{lore.title}</strong>
                          <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
                            {lore.info}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* CHARACTER BEHAVIOR */}
          {analysisType === 'character_behavior' && result.behavior_issues && (
            <div>
              <h4>👤 Problèmes de comportement ({result.behavior_issues.length})</h4>
              {result.behavior_issues.length === 0 ? (
                <div style={{ padding: 12, background: '#e8f5e9', borderRadius: 6, color: '#2e7d32' }}>
                  ✅ Aucune incohérence de comportement détectée !
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {result.behavior_issues.map((issue, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: 12,
                        background: '#fff3e0',
                        borderLeft: '4px solid #ff9800',
                        borderRadius: 4
                      }}
                    >
                      <div>
                        <strong>⚠️ {issue.char_name}</strong>
                        <div style={{ fontSize: '0.9em', marginTop: 4 }}>
                          Action trouvée: <code>{issue.action_found}</code>
                        </div>
                        <div style={{ fontSize: '0.85em', color: '#666', marginTop: 2 }}>
                          Trait conflictuel: {issue.conflicting_trait}
                        </div>
                        <div style={{ fontSize: '0.85em', color: '#999', marginTop: 2 }}>
                          {issue.reason}
                        </div>
                        {issue.context && (
                          <div style={{
                            marginTop: 8,
                            padding: 8,
                            background: 'white',
                            borderRadius: 4,
                            fontSize: '0.85em',
                            fontStyle: 'italic'
                          }}>
                            "{issue.context}"
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* LORE CHECK */}
          {analysisType === 'lore_check' && result.lore_analysis && (
            <div>
              <h4>📚 Analyse du Lore ({result.lore_analysis.length})</h4>
              {result.lore_analysis.length === 0 ? (
                <p style={{ color: '#999' }}>Aucun élément du lore trouvé dans ce manuscrit.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {result.lore_analysis.map((item, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: 12,
                        background: '#f3e5f5',
                        borderLeft: '4px solid #9c27b0',
                        borderRadius: 4
                      }}
                    >
                      <strong>📖 {item.title}</strong>
                      <div style={{ fontSize: '0.85em', color: '#666', marginTop: 4 }}>
                        Type: {item.type}
                      </div>
                      <div style={{ fontSize: '0.85em', color: '#666', marginTop: 2 }}>
                        {item.info}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* JSON brut (pour debug) */}
          <details style={{ marginTop: 20 }}>
            <summary style={{ cursor: 'pointer', fontWeight: 'bold', padding: 8 }}>
              🔍 Voir les données brutes (JSON)
            </summary>
            <pre style={{
              whiteSpace: 'pre-wrap',
              background: '#f5f5f5',
              padding: 12,
              borderRadius: 4,
              fontSize: '0.8em',
              maxHeight: '400px',
              overflowY: 'auto',
              marginTop: 8
            }}>
              {JSON.stringify(result, null, 2)}
            </pre>
          </details>
        </div>
      )}
    </div>
  )
}