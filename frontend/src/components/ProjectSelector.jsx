import React, { useEffect, useState } from 'react'
import API from '../api/api'
import { notifyError } from '../utils/notify'

export default function ProjectSelector({ onSelect }) {
  const [stories, setStories] = useState([])
  const [newTitle, setNewTitle] = useState('')
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    fetchStories()
  }, [])

  async function fetchStories() {
    try {
      setLoading(true)
      const res = await API.get('/api/stories')
      setStories(res.data)
    } catch (err) {
      notifyError(err.friendlyMessage || 'Impossible de charger les romans.')
    } finally {
      setLoading(false)
    }
  }

  async function createStory() {
    if (!newTitle.trim()) return
    try {
      setCreating(true)
      const res = await API.post('/api/stories', { title: newTitle.trim() })
      setNewTitle('')
      await fetchStories()
      onSelect(res.data)
    } catch (err) {
      notifyError(err.friendlyMessage || 'Erreur lors de la création.')
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="project-selector">
      <h2>📚 Sélectionnez un roman</h2>
      {loading ? (
        <p>Chargement…</p>
      ) : (
        <ul>
          {stories.length === 0 && (
            <li style={{ color: '#999' }}>Aucun roman pour le moment.</li>
          )}
          {stories.map((s) => (
            <li key={s.id}>
              <button onClick={() => onSelect(s)}>{s.title}</button>
            </li>
          ))}
        </ul>
      )}

      <div style={{ marginTop: 20 }}>
        <h3>Créer un nouveau roman</h3>
        <input
          placeholder="Titre du roman"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && createStory()}
        />
        <button onClick={createStory} disabled={creating || !newTitle.trim()}>
          {creating ? 'Création…' : 'Créer'}
        </button>
      </div>
    </div>
  )
}
