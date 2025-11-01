import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function TimelinePanel({ story }) {
  const [list, setList] = useState([])
  const [chars, setChars] = useState([])
  const [form, setForm] = useState({ characters: [] })

  useEffect(() => {
    if (story?.id) fetchAll()
  }, [story])

  async function fetchAll() {
    try {
      // 🧩 On charge les événements et personnages liés au roman sélectionné
      const [r, c] = await Promise.all([
        API.get(`/api/timeline/${story.id}`),
        API.get(`/api/characters/${story.id}`)
      ])
      setList(r.data)
      setChars(c.data)
    } catch (err) {
      console.error('Erreur de chargement de la chronologie :', err)
    }
  }

  async function save() {
    try {
      const payload = { ...form, story_id: story.id }
      await API.post('/api/timeline', payload)
      setForm({ characters: [] })
      fetchAll()
    } catch (err) {
      console.error('Erreur enregistrement événement :', err)
    }
  }

  async function del(id) {
    if (!confirm('Supprimer ?')) return
    try {
      await API.delete('/api/timeline', { data: { id } })
      fetchAll()
    } catch (err) {
      console.error('Erreur suppression événement :', err)
    }
  }

  function toggleChar(id) {
    let arr = form.characters || []
    if (arr.includes(id)) arr = arr.filter(x => x !== id)
    else arr.push(id)
    setForm({ ...form, characters: arr })
  }

  function edit(it) {
    setForm({ ...it })
  }

  if (!story) return <div>⚠️ Sélectionnez d’abord un roman pour accéder à sa chronologie.</div>

  return (
    <div className="panel">
      <h2>Chronologie — <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.length === 0 && <div>Aucun événement enregistré pour ce roman.</div>}
            {list.map(it => (
              <div key={it.id} className="item">
                <strong>{it.title}</strong> — {it.date}
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
              placeholder="Titre"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.date || ''}
              onChange={e => setForm({ ...form, date: e.target.value })}
              placeholder="Date (YYYY-MM-DD)"
              className="input"
            />
          </div>
          <div className="field">
            <textarea
              value={form.summary || ''}
              onChange={e => setForm({ ...form, summary: e.target.value })}
              placeholder="Résumé"
            />
          </div>

          <div className="field">
            <label>Personnages impliqués</label>
            {chars.map(c => (
              <div key={c.id}>
                <label>
                  <input
                    type="checkbox"
                    checked={(form.characters || []).includes(c.id)}
                    onChange={() => toggleChar(c.id)}
                  />{' '}
                  {c.name}
                </label>
              </div>
            ))}
          </div>

          <button className="primary" onClick={save}>
            Enregistrer
          </button>
        </div>
      </div>
    </div>
  )
}
