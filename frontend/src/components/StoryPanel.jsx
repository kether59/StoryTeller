import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function StoryPanel() {
  const [story, setStory] = useState({})
  const [form, setForm] = useState({ story_id: story.id });
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function loadStory() {
      try {
        const r = await API.get('/api/story')
        setStory((r.data && r.data[0]) || {})
      } catch (err) {
        console.error('Erreur de chargement de lhistoire :', err)
        setError("Impossible de charger l'histoire.")
      } finally {
        setLoading(false)
      }
    }
    loadStory()
  }, [])

  async function save() {
    try {
      await API.post('/api/story', story)
      const r = await API.get('/api/story')
      setStory((r.data && r.data[0]) || {})
    } catch (err) {
      alert("Erreur lors de l'enregistrement.")
    }
  }

  async function exportMD() {
    try {
      const r = await API.get('/export/markdown', { responseType: 'blob' })
      const blob = new Blob([r.data], { type: 'text/markdown' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'storyteller_export.md'
      a.click()
    } catch (err) {
      alert('Erreur export Markdown')
    }
  }

  async function exportPDF() {
    window.open('http://127.0.0.1:5000/export/pdf', '_blank')
  }

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>Histoire / Intrigue</h2>
      <div className="field">
        <input
          value={story.title || ''}
          onChange={e => setStory({ ...story, title: e.target.value })}
          placeholder="Titre"
          className="input"
        />
      </div>
      <div className="field">
        <textarea
          value={story.synopsis || ''}
          onChange={e => setStory({ ...story, synopsis: e.target.value })}
          placeholder="Synopsis"
          rows={6}
        />
      </div>
      <div className="field">
        <textarea
          value={story.blurb || ''}
          onChange={e => setStory({ ...story, blurb: e.target.value })}
          placeholder="Quatrième de couverture"
          rows={4}
        />
      </div>
      <div>
        <button className="primary" onClick={save}>Enregistrer</button>
        <button style={{ marginLeft: 8 }} onClick={exportMD}>Export Markdown</button>
        <button style={{ marginLeft: 8 }} onClick={exportPDF}>Export PDF</button>
      </div>
    </div>
  )
}
