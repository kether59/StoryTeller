import React from 'react'
import useEntityCrud from '../hooks/useEntityCrud'

export default function LorePanel({ story }) {
  const {
    list,
    form,
    updateField,
    loading,
    error,
    saving,
    save,
    del,
    edit,
    clearForm,
  } = useEntityCrud({
    endpoint: '/api/lore',
    storyId: story?.id,
  })

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>
        Lore / World-Building : <em>{story?.title || 'Aucun roman sélectionné'}</em>
      </h2>

      {!story?.id && (
        <div style={{ color: 'red', marginBottom: 12 }}>
          ⚠️ Sélectionnez d’abord un roman pour gérer les éléments du monde.
        </div>
      )}

      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.length === 0 && (
              <div style={{ color: '#999', padding: 8 }}>Aucune entrée de lore.</div>
            )}
            {list.map((it) => (
              <div
                key={it.id}
                className="item"
                style={{
                  borderLeft: '3px solid #ccc',
                  paddingLeft: 6,
                  marginBottom: 6,
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
              onChange={(e) => updateField('title', e.target.value)}
              placeholder="Titre"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.category || ''}
              onChange={(e) => updateField('category', e.target.value)}
              placeholder="Catégorie (Magie, Faction, Lieu...)"
              className="input small"
            />
          </div>
          <div className="field">
            <textarea
              value={form.content || ''}
              onChange={(e) => updateField('content', e.target.value)}
              placeholder="Contenu / Description complète"
            />
          </div>
          <button className="primary" onClick={save} disabled={saving}>
            {saving ? 'Enregistrement…' : form.id ? 'Mettre à jour' : 'Enregistrer'}
          </button>
          {form.id && (
            <button style={{ marginLeft: 8 }} onClick={clearForm}>
              Annuler
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
