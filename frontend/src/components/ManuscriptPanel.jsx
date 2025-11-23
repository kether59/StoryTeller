import React, { useEffect, useState, useRef } from 'react'
import API from '../api/api'
import MarkdownIt from 'markdown-it'
import MdEditor from 'react-markdown-editor-lite'
import 'react-markdown-editor-lite/lib/index.css'

const mdParser = new MarkdownIt()

export default function ManuscriptPanel({ story }) {
  const [list, setList] = useState([])
  const [current, setCurrent] = useState(null)
  const [autoSaving, setAutoSaving] = useState(false)
  const timerRef = useRef(null)

  useEffect(() => {
    if (story?.id) fetchList()
  }, [story])

  useEffect(() => {
    if (timerRef.current) clearInterval(timerRef.current)

    // Auto-save toutes les 10 secondes
    timerRef.current = setInterval(() => {
      if (current && current._dirty) {
        saveCurrent()
      }
    }, 10000)

    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [current])

  async function fetchList() {
    try {
      const r = await API.get(`/api/manuscript?story_id=${story.id}`)
      setList(r.data)
    } catch (err) {
      console.error('Erreur chargement manuscrits:', err)
    }
  }

  async function newChapter() {
    const payload = {
      story_id: story.id,
      title: 'Nouveau chapitre',
      chapter: list.length ? Math.max(...list.map(m => m.chapter)) + 1 : 1,
      text: '',
      status: 'Brouillon'
    }

    try {
      const res = await API.post('/api/manuscript', payload)
      fetchList()
      setCurrent({ ...res.data, _dirty: false })
    } catch (err) {
      console.error('Erreur création chapitre:', err)
      alert('Erreur lors de la création du chapitre')
    }
  }

  async function saveCurrent() {
    if (!current || !current.id) return

    try {
      setAutoSaving(true)
      // Utiliser PUT pour mise à jour
      await API.put(`/api/manuscript/${current.id}`, {
        title: current.title,
        chapter: current.chapter,
        text: current.text,
        status: current.status,
        story_id: story.id
      })

      setCurrent({ ...current, _dirty: false })
      fetchList()
    } catch (err) {
      console.error('Erreur sauvegarde:', err)
    } finally {
      setAutoSaving(false)
    }
  }

  async function del(id) {
    if (!confirm('Supprimer ce chapitre ?')) return

    try {
      await API.delete(`/api/manuscript/${id}`)
      setCurrent(null)
      fetchList()
    } catch (err) {
      console.error('Erreur suppression:', err)
      alert('Erreur lors de la suppression')
    }
  }

  function onEditorChange({ text }) {
    setCurrent({ ...current, text, _dirty: true })
  }

  async function runAnalyze(mode) {
    if (!current || !current.id) {
      alert('Sauvegardez d\'abord le manuscrit')
      return
    }

    try {
      const r = await API.get(`/api/manuscript/${current.id}/analyze?mode=${mode}`)
      setCurrent({ ...current, analysis: r.data })
    } catch (err) {
      console.error('Erreur analyse:', err)
      alert('Erreur lors de l\'analyse. Assurez-vous que spaCy est installé.')
    }
  }

  async function exportAnnotated() {
    if (!current) return

    try {
      const r = await API.get(`/api/manuscript/${current.id}`, {
        responseType: 'blob',
        headers: { 'Accept': 'text/markdown' }
      })

      const blob = new Blob([current.text], { type: 'text/markdown' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${current.title.replace(/\s+/g, '_')}_chapitre_${current.chapter}.md`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Erreur export:', err)
      alert('Erreur lors de l\'export')
    }
  }

  if (!story) {
    return <div>⚠️ Sélectionnez d'abord un roman</div>
  }

  return (
    <div className="panel">
      <h2>Manuscrit — <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>
      <div style={{ display: 'flex', gap: 12 }}>
        {/* Liste des chapitres */}
        <div style={{ width: 300 }}>
          <button onClick={newChapter} className="primary" style={{ width: '100%' }}>
            + Nouveau chapitre
          </button>
          <div className="list" style={{ marginTop: 12, maxHeight: '70vh', overflowY: 'auto' }}>
            {list.length === 0 && (
              <div style={{ padding: 8, color: '#999' }}>
                Aucun chapitre. Créez-en un !
              </div>
            )}
            {list.map(it => (
              <div
                key={it.id}
                className="item"
                onClick={() => setCurrent({ ...it, _dirty: false })}
                style={{
                  cursor: 'pointer',
                  background: current && current.id === it.id ? '#e3f2fd' : 'transparent',
                  borderLeft: current && current.id === it.id ? '3px solid #2196f3' : 'none'
                }}
              >
                <strong>{it.title}</strong>
                <div style={{ fontSize: '0.85em', color: '#666' }}>
                  Chapitre {it.chapter}
                </div>
                <div style={{ fontSize: '0.75em', color: '#999' }}>
                  {it.status || 'Brouillon'}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Éditeur */}
        <div style={{ flex: 1 }}>
          {!current && (
            <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
              Sélectionnez ou créez un chapitre pour commencer à écrire
            </div>
          )}

          {current && (
            <div>
              {/* Barre d'outils */}
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 8 }}>
                <input
                  style={{ flex: 1 }}
                  value={current.title || ''}
                  onChange={e => setCurrent({ ...current, title: e.target.value, _dirty: true })}
                  placeholder="Titre du chapitre"
                  className="input"
                />
                <input
                  type="number"
                  style={{ width: 80 }}
                  value={current.chapter || 1}
                  onChange={e => setCurrent({ ...current, chapter: parseInt(e.target.value) || 1, _dirty: true })}
                  className="input"
                  title="Numéro de chapitre"
                />
                <select
                  value={current.status || 'Brouillon'}
                  onChange={e => setCurrent({ ...current, status: e.target.value, _dirty: true })}
                  className="input"
                  style={{ width: 140 }}
                >
                  <option>Brouillon</option>
                  <option>En cours</option>
                  <option>Révision</option>
                  <option>Terminé</option>
                </select>
                <button onClick={saveCurrent} disabled={autoSaving}>
                  {autoSaving ? '💾 Enregistrement...' : '💾 Sauvegarder'}
                </button>
                <button onClick={() => del(current.id)} style={{ color: 'red' }}>
                  🗑️
                </button>
              </div>

              {/* Éditeur Markdown */}
              <div style={{ border: '1px solid #ddd', borderRadius: 4 }}>
                <MdEditor
                  value={current.text || ''}
                  style={{ height: '55vh' }}
                  renderHTML={text => mdParser.render(text)}
                  onChange={onEditorChange}
                  config={{
                    view: { menu: true, md: true, html: true },
                    canView: { menu: true, md: true, html: true, fullScreen: true, hideMenu: true }
                  }}
                />
              </div>

              {/* Actions d'analyse */}
              <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
                <button onClick={() => runAnalyze('fast')}>
                  🔍 Analyse rapide
                </button>
                <button onClick={() => runAnalyze('detailed')}>
                  🔬 Analyse détaillée
                </button>
                <button onClick={exportAnnotated}>
                  📥 Exporter MD
                </button>
              </div>

              {/* Résultats d'analyse */}
              {current.analysis && (
                <div style={{ marginTop: 12 }}>
                  <h3>Résultat d'analyse</h3>
                  <div style={{
                    background: '#f5f5f5',
                    padding: 12,
                    borderRadius: 4,
                    maxHeight: '300px',
                    overflowY: 'auto'
                  }}>
                    {/* Résumé */}
                    {current.analysis.summary && current.analysis.summary.length > 0 && (
                      <div style={{ marginBottom: 12 }}>
                        <h4>📊 Résumé</h4>
                        {current.analysis.summary.map((item, idx) => (
                          <div key={idx} style={{ marginBottom: 8, padding: 8, background: 'white', borderRadius: 4 }}>
                            <strong>{item.type}:</strong> {item.count || item.items?.length || 0}
                            {item.items && item.items.length > 0 && (
                              <ul style={{ marginTop: 4, paddingLeft: 20 }}>
                                {item.items.map((it, i) => (
                                  <li key={i}>{JSON.stringify(it)}</li>
                                ))}
                              </ul>
                            )}
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Entités */}
                    {current.analysis.entities && current.analysis.entities.length > 0 && (
                      <div>
                        <h4>🏷️ Entités détectées ({current.analysis.entities.length})</h4>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                          {current.analysis.entities.slice(0, 20).map((ent, idx) => (
                            <span key={idx} style={{
                              background: '#e3f2fd',
                              padding: '4px 8px',
                              borderRadius: 4,
                              fontSize: '0.85em'
                            }}>
                              {ent.text} <small>({ent.label})</small>
                            </span>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Détails complets */}
                    <details style={{ marginTop: 12 }}>
                      <summary style={{ cursor: 'pointer', fontWeight: 'bold' }}>
                        Voir les détails JSON
                      </summary>
                      <pre style={{
                        whiteSpace: 'pre-wrap',
                        fontSize: '0.8em',
                        marginTop: 8,
                        padding: 8,
                        background: 'white',
                        borderRadius: 4
                      }}>
                        {JSON.stringify(current.analysis, null, 2)}
                      </pre>
                    </details>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}