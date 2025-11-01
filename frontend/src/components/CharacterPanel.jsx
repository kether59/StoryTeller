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
      if (form.id) {
        await API.put(`/api/characters/${form.id}`, form)
      }
      else {
        await API.post('/api/characters', form)
      }
      clearForm()
      fetchList()
    } catch (err) {
      alert('Erreur lors de la sauvegarde.')
    }
  }

  function clearForm() {
    setForm({ story_id: story.id })
  }

    async function del(id) {
    if (!confirm('Supprimer ?')) return

    await API.delete(`/api/characters/${id}`)

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
            value={form.surname || ''}
            onChange={e => setForm({ ...form, surname: e.target.value })}
            placeholder="Prénom"
            className="input"
          />
      <div className="field">
          <input
            value={form.role || ''}
            onChange={e => setForm({ ...form, role: e.target.value })}
            placeholder="Rôle (ex: Protagoniste)"
            className="input"
          />
        </div>
        </div>
        <div className="field">
            <input
              type="number"
              value={form.age || ''}
              onChange={e => setForm({ ...form, age: parseInt(e.target.value) || null })}
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
              value={form.physical_description || ''}
              onChange={e => setForm({ ...form, physical_description: e.target.value })}
              placeholder="Description physique"
            />
          </div>
          <div className="field">
            <textarea
              value={form.personality || ''}
              onChange={e => setForm({ ...form, personality: e.target.value })}
              placeholder="Personnalité"
            />
          </div>
          <div className="field">
            <textarea
              value={form.history || ''}
              onChange={e => setForm({ ...form, history: e.target.value })}
              placeholder="Histoire / Passé"
            />
          </div>
          <div className="field">
            <textarea
              value={form.motivation || ''}
              onChange={e => setForm({ ...form, motivation: e.target.value })}
              placeholder="Motivation (Qu'est-ce qui le fait avancer ?)"
            />
          </div>
          <div className="field">
            <textarea
              value={form.goal || ''}
              onChange={e => setForm({ ...form, goal: e.target.value })}
              placeholder="Objectif (But concret dans l'histoire)"
            />
          </div>
          <div className="field">
            <textarea
              value={form.flaw || ''}
              onChange={e => setForm({ ...form, flaw: e.target.value })}
              placeholder="Défaut / Faille principale"
            />
          </div>
          <div className="field">
            <textarea
              value={form.character_arc || ''}
              onChange={e => setForm({ ...form, character_arc: e.target.value })}
              placeholder="Arc narratif (Comment va-t-il évoluer ?)"
            />
          </div>
           <div className="field">
            <textarea
              value={form.skills || ''}
              onChange={e => setForm({ ...form, skills: e.target.value })}
              placeholder="Compétences / Pouvoirs"
            />
          </div>
           <div className="field">
            <textarea
              value={form.notes || ''}
              onChange={e => setForm({ ...form, notes: e.target.value })}
              placeholder="Notes libres"
            />
          </div>
          <div>
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
    </div>
  )
}
