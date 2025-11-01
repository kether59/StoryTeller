import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function CharacterPanel({ story }) {
  const [list, setList] = useState([])
  const [form, setForm] = useState({ story_id: story.id })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchList()
  }, [story])

  async function fetchList() {
    try {
      setLoading(true)
      const r = await API.get(`/api/characters?story_id=${story.id}`)
      setList(r.data)
    } catch (err) {
      console.error('Erreur chargement personnages :', err)
      setError('Impossible de charger les personnages.')
    } finally {
      setLoading(false)
    }
  }

  async function save() {
    try {
      await API.post('/api/characters', form)
      setForm({ story_id: story.id })
      fetchList()
    } catch (err) {
      alert('Erreur lors de la sauvegarde.')
    }
  }

  async function del(id) {
    if (!confirm('Supprimer ?')) return
    await API.delete('/api/characters', { data: { id } })
    fetchList()
  }

  function edit(it) {
    setForm(it)
  }

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>Personnages — <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.map(it => (
              <div key={it.id} className="item">
                <strong>{it.name}</strong> — {it.age} ans
                <div style={{ marginTop: 6 }}>
                  <button onClick={() => edit(it)}>Éditer</button>
                  <button onClick={() => del(it.id)} style={{ marginLeft: 8 }}>
                    Supprimer
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{ flex: 1 }}>
          <h3>Ajouter / Éditer</h3>
          <div className="field">
            <input
              value={form.name || ''}
              onChange={e => setForm({ ...form, name: e.target.value })}
              placeholder="Nom"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.age || ''}
              onChange={e => setForm({ ...form, age: e.target.value })}
              placeholder="Âge"
              className="input small"
            />
          </div>
          <div className="field">
            <input
              value={form.born || ''}
              onChange={e => setForm({ ...form, born: e.target.value })}
              placeholder="Date naissance (YYYY-MM-DD)"
              className="input"
            />
          </div>
          <div className="field">
            <textarea
              value={form.description || ''}
              onChange={e => setForm({ ...form, description: e.target.value })}
              placeholder="Description"
            />
          </div>
          <div className="field">
            <textarea
              value={form.personality || ''}
              onChange={e => setForm({ ...form, personality: e.target.value })}
              placeholder="Caractère"
            />
          </div>
          <div className="field">
            <textarea
              value={form.history || ''}
              onChange={e => setForm({ ...form, history: e.target.value })}
              placeholder="Histoire"
            />
          </div>
          <div>
            <button className="primary" onClick={save}>
              Enregistrer
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
