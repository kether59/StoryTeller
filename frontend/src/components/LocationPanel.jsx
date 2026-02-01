import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function LocationPanel({ story }) {
  const [list, setList] = useState([])
  const [form, setForm] = useState({ story_id: story?.id })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!story?.id) return
    fetchLocations()
  }, [story])

  async function fetchLocations() {
    try {
      setLoading(true)
      const r = await API.get(`/api/locations?story_id=${story.id}`)
      setList(r.data)
    } catch (err) {
      console.error('Erreur de chargement des lieux :', err)
      setError('Impossible de charger les lieux.')
    } finally {
      setLoading(false)
    }
  }

  function clearForm() {
    setForm({ story_id: story.id })
  }

  async function save() {
    if (!story?.id) {
      alert('Veuillez d’abord sélectionner un roman.')
      return
    }
    try {
      const payload = { ...form, story_id: story.id }

      // Gestion POST (création) vs. PUT (mise à jour)
      if (form.id) {
        await API.put(`/api/locations/${form.id}`, payload)
      } else {
        await API.post('/api/locations', payload)
      }

      clearForm()
      fetchLocations()
    } catch (err) {
      console.error('Erreur lors de l’enregistrement :', err)
      alert("Erreur lors de l'enregistrement du lieu.")
    }
  }

  async function del(id) {
    if (!confirm('Supprimer ?')) return
    await API.delete(`/api/locations/${id}`)
    fetchLocations()
  }

  function edit(it) {
    setForm({ ...it, story_id: story.id })
  }

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>Lieux (Locations) : <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>

      {!story?.id && (
        <div style={{ color: 'red', marginBottom: 12 }}>
          ⚠️ Sélectionnez d’abord un roman pour gérer les lieux.
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
                  borderLeft: '3px solid #63b3ed',
                  paddingLeft: 6,
                  marginBottom: 6
                }}
              >
                <strong>{it.name}</strong> <small>({it.type})</small>
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
              value={form.name || ''} // Le modèle a 'name'
              onChange={e => setForm({ ...form, name: e.target.value })}
              placeholder="Nom du lieu (ex: Château de l'Ombre)"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.type || ''}
              onChange={e => setForm({ ...form, type: e.target.value })}
              placeholder="Type (Ville, Planète, Bâtiment, etc.)"
              className="input small"
            />
          </div>
          <div className="field">
            <textarea
              value={form.summary || ''}
              onChange={e => setForm({ ...form, summary: e.target.value })}
              placeholder="Description et importance du lieu"
            />
          </div>
          <button className="primary" onClick={save}>
             {form.id ? 'Mettre à jour' : 'Enregistrer'}
          </button>
          {form.id && (
            <button style={{marginLeft: 8}} onClick={clearForm}>
                Annuler
            </button>
          )}
        </div>
      </div>
    </div>
  )
}