import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function TimelinePanel({ story }) {
  const [list, setList] = useState([])
  const [chars, setChars] = useState([])
  const [locations, setLocations] = useState([])
  const [form, setForm] = useState({ characters: [], story_id: story?.id })

  useEffect(() => {
    if (story?.id) fetchAll()
  }, [story])

  function clearForm() {
    setForm({ characters: [], story_id: story.id })
  }

  async function fetchAll() {
    try {
      const [r, c, l] = await Promise.all([
        API.get(`/api/timeline?story_id=${story.id}`),
        API.get(`/api/characters?story_id=${story.id}`),
        API.get(`/api/locations?story_id=${story.id}`)
      ])
      setList(r.data)
      setChars(c.data)
      setLocations(l.data)
    } catch (err) {
      console.error('Erreur de chargement de la chronologie :', err)
    }
  }

  async function save() {
    try {
      const payload = { ...form, story_id: story.id }

      if (form.id) {
        // Mise à jour
        await API.put(`/api/timeline/${form.id}`, payload)
      } else {
        // Création
        await API.post('/api/timeline', payload)
      }

      clearForm()
      fetchAll()
    } catch (err) {
      console.error('Erreur enregistrement événement :', err)
      alert('Erreur lors de l\'enregistrement')
    }
  }

  async function del(id) {
    if (!confirm('Supprimer cet événement ?')) return
    try {
      // ✅ Correction : DELETE avec ID dans l'URL
      await API.delete(`/api/timeline/${id}`)
      fetchAll()
    } catch (err) {
      console.error('Erreur suppression événement :', err)
      alert('Erreur lors de la suppression')
    }
  }

  function toggleChar(id) {
    let arr = form.characters || []
    if (arr.includes(id)) {
      arr = arr.filter(x => x !== id)
    } else {
      arr.push(id)
    }
    setForm({ ...form, characters: arr })
  }

  function edit(it) {
    setForm({ ...it })
  }

  if (!story) {
    return <div>⚠️ Sélectionnez d'abord un roman pour accéder à sa chronologie.</div>
  }

  return (
    <div className="panel">
      <h2>Chronologie : <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.length === 0 && <div>Aucun événement enregistré pour ce roman.</div>}
            {list.map(it => (
              <div key={it.id} className="item">
                <strong>{it.title}</strong> : {it.date}
                <p style={{ fontSize: '0.9em', color: '#666' }}>{it.summary}</p>
                {it.characters && it.characters.length > 0 && (
                  <div style={{ fontSize: '0.85em', color: '#999' }}>
                    Personnages: {it.characters.map(cid => {
                      const char = chars.find(c => c.id === cid)
                      return char ? char.name : `ID:${cid}`
                    }).join(', ')}
                  </div>
                )}
                <div style={{ marginTop: 6 }}>
                  <button onClick={() => edit(it)}>Éditer</button>{' '}
                  <button onClick={() => del(it.id)}>Supprimer</button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{ flex: 1 }}>
          <h3>Ajouter / Éditer événement</h3>
          <div className="field">
            <input
              value={form.title || ''}
              onChange={e => setForm({ ...form, title: e.target.value })}
              placeholder="Titre de l'événement"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.date || ''}
              onChange={e => setForm({ ...form, date: e.target.value })}
              placeholder="Date (ex: 2024-01-01 ou Année 120)"
              className="input"
            />
          </div>
          <div className="field">
            <input
              type="number"
              value={form.sort_order || 0}
              onChange={e => setForm({ ...form, sort_order: parseInt(e.target.value) || 0 })}
              placeholder="Ordre de tri (nombre)"
              className="input small"
            />
          </div>
          <div className="field">
            <label>Lieu (optionnel)</label>
            <select
              value={form.location_id || ''}
              onChange={e => setForm({ ...form, location_id: e.target.value ? parseInt(e.target.value) : null })}
              className="input"
            >
              <option value="">-- Aucun lieu --</option>
              {locations.map(loc => (
                <option key={loc.id} value={loc.id}>
                  {loc.name}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <textarea
              value={form.summary || ''}
              onChange={e => setForm({ ...form, summary: e.target.value })}
              placeholder="Résumé de l'événement"
              rows={4}
            />
          </div>

          <div className="field">
            <label><strong>Personnages impliqués</strong></label>
            {chars.length === 0 && <p style={{color: '#999'}}>Aucun personnage disponible</p>}
            {chars.map(c => (
              <div key={c.id} style={{ marginBottom: 4 }}>
                <label style={{ cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={(form.characters || []).includes(c.id)}
                    onChange={() => toggleChar(c.id)}
                  />{' '}
                  {c.name} {c.surname && `(${c.surname})`}
                </label>
              </div>
            ))}
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