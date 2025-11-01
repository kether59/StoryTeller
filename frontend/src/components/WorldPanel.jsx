import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function WorldPanel({ story }) {
  const [list, setList] = useState([])
  const [form, setForm] = useState({ story_id: story?.id })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!story?.id) return
    fetchWorlds()
  }, [story])

  async function fetchWorlds() {
    try {
      setLoading(true)
      const r = await API.get(`/api/world/${story.id}`)
      setList(r.data)
    } catch (err) {
      console.error('Erreur de chargement des mondes :', err)
      setError('Impossible de charger les mondes')
    } finally {
      setLoading(false)
    }
  }

  async function save() {
    if (!story?.id) {
      alert('Veuillez d’abord sélectionner un roman.')
      return
    }
    try {
      const payload = { ...form, story_id: story.id }
      await API.post('/api/world', payload)
      setForm({ story_id: story.id })
      fetchWorlds()
    } catch (err) {
      console.error('Erreur lors de l’enregistrement :', err)
      alert("Erreur lors de l'enregistrement du monde.")
    }
  }

  async function del(id) {
    if (!confirm('Supprimer ?')) return
    await API.delete('/api/world', { data: { id } })
    fetchWorlds()
  }

  function edit(it) {
    setForm({ ...it, story_id: story.id })
  }

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>Monde — <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>

      {!story?.id && (
        <div style={{ color: 'red', marginBottom: 12 }}>
          ⚠️ Sélectionnez d’abord un roman pour gérer les éléments du monde.
        </div>
      )}

      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.map(it => (
              <div
                key={it.id}
                className="item"
                style={{
                  borderLeft: '3px solid #ccc',
                  paddingLeft: 6,
                  marginBottom: 6
                }}
              >
                <strong>{it.title}</strong> <small>({it.type})</small>
                <p style={{ fontSize: '0.9em', marginTop: 4 }}>{it.summary}</p>
                <div>
                  <button onClick={() => edit(it)}>Éditer</button>{' '}
                  <button onClick={() => del(it.id)}>Supprimer</button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{ flex: 1 }}>
          <h3>Ajouter / Éditer</h3>
          <div className="field">
            <input
              value={form.title || ''}
              onChange={e => setForm({ ...form, title: e.target.value })}
              placeholder="Titre"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.type || ''}
              onChange={e => setForm({ ...form, type: e.target.value })}
              placeholder="Type (lieu, peuple, objet, etc.)"
              className="input small"
            />
          </div>
          <div className="field">
            <textarea
              value={form.summary || ''}
              onChange={e => setForm({ ...form, summary: e.target.value })}
              placeholder="Résumé"
            />
          </div>
          <button className="primary" onClick={save}>
            Enregistrer
          </button>
        </div>
      </div>
    </div>
  )
}
