import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function LorePanel({ story }) {
  const [list, setList] = useState([])
  const [form, setForm] = useState({ story_id: story?.id })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

useEffect(() => {
    if (!story?.id) return
    fetchLore()
  }, [story])

  async function fetchLore() {
    try {
      setLoading(true)
      const r = await API.get(`/api/lore?story_id=${story.id}`)
      setList(r.data)
    } catch (err) {
      console.error('Erreur de chargement du Lore :', err)
      setError('Impossible de charger les entrées de Lore')
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

      if (form.id) {
        await API.put(`/api/lore/${form.id}`, payload)
      } else {
        await API.post('/api/lore', payload)
      }

      clearForm()
      fetchLore()
    } catch (err) {
      console.error('Erreur lors de l’enregistrement :', err)
      alert("Erreur lors de l'enregistrement de l'entrée de Lore.")
    }
  }

  async function del(id) {
    if (!confirm('Supprimer ?')) return
    await API.delete(`/api/lore/${id}`)
    fetchLore()
  }

  function edit(it) {
    setForm({ ...it, story_id: story.id })
  }

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

return (
    <div className="panel">
      <h2>Lore / World-Building : <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>

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
          <strong>{it.title}</strong> <small>({it.category})</small>
                <p style={{ fontSize: '0.9em', marginTop: 4 }}>{it.content}</p>
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
               value={form.category || ''}
               onChange={e => setForm({ ...form, category: e.target.value })}
               placeholder="Catégorie (Magie, Faction, Lieu...)"
               className="input small"
             />
           </div>
           <div className="field">
             <textarea
               value={form.content || ''} //
               onChange={e => setForm({ ...form, content: e.target.value })}
               placeholder="Contenu / Description complète"
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
