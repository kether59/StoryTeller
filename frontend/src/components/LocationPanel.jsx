import React from 'react'
import useEntityCrud from '../hooks/useEntityCrud'

export default function LocationPanel({ story }) {
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
    endpoint: '/api/locations',
    storyId: story?.id,
  })

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>
        Lieux (Locations) : <em>{story?.title || 'Aucun roman sélectionné'}</em>
      </h2>

      {!story?.id && (
        <div style={{ color: 'red', marginBottom: 12 }}>
          ⚠️ Sélectionnez d’abord un roman pour gérer les lieux.
        </div>
      )}

      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.length === 0 && (
              <div style={{ color: '#999', padding: 8 }}>Aucun lieu.</div>
            )}
            {list.map((it) => (
              <div
                key={it.id}
                className="item"
                style={{
                  borderLeft: '3px solid #63b3ed',
                  paddingLeft: 6,
                  marginBottom: 6,
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
              value={form.name || ''}
              onChange={(e) => updateField('name', e.target.value)}
              placeholder="Nom du lieu (ex: Château de l'Ombre)"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.type || ''}
              onChange={(e) => updateField('type', e.target.value)}
              placeholder="Type (Ville, Planète, Bâtiment, etc.)"
              className="input small"
            />
          </div>
          <div className="field">
            <textarea
              value={form.summary || ''}
              onChange={(e) => updateField('summary', e.target.value)}
              placeholder="Description et importance du lieu"
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
