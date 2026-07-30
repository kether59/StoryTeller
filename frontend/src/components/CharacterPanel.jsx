import React from 'react'
import useEntityCrud from '../hooks/useEntityCrud'

export default function CharacterPanel({ story }) {
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
    endpoint: '/api/characters',
    storyId: story?.id,
  })

  if (loading) return <div>Chargement...</div>
  if (error) return <div>❌ {error}</div>

  return (
    <div className="panel">
      <h2>
        Personnages : <em>{story?.title || 'Aucun roman sélectionné'}</em>
      </h2>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>
          <div className="list">
            {list.length === 0 && (
              <div style={{ color: '#999', padding: 8 }}>Aucun personnage.</div>
            )}
            {list.map((it) => (
              <div key={it.id} className="item">
                <strong>{it.name}</strong>
                {it.surname ? ` ${it.surname}` : ''}
                {it.age != null ? ` : ${it.age} ans` : ''}
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
              onChange={(e) => updateField('name', e.target.value)}
              placeholder="Nom"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.surname || ''}
              onChange={(e) => updateField('surname', e.target.value)}
              placeholder="Prénom"
              className="input"
            />
          </div>
          <div className="field">
            <input
              value={form.role || ''}
              onChange={(e) => updateField('role', e.target.value)}
              placeholder="Rôle (ex: Protagoniste)"
              className="input"
            />
          </div>
          <div className="field">
            <input
              type="number"
              value={form.age ?? ''}
              onChange={(e) =>
                updateField('age', e.target.value === '' ? null : parseInt(e.target.value, 10))
              }
              placeholder="Âge"
              className="input small"
            />
          </div>
          <div className="field">
            <input
              value={form.born || ''}
              onChange={(e) => updateField('born', e.target.value)}
              placeholder="Date naissance (YYYY-MM-DD)"
              className="input"
            />
          </div>
          <div className="field">
            <textarea
              value={form.physical_description || ''}
              onChange={(e) => updateField('physical_description', e.target.value)}
              placeholder="Description physique"
            />
          </div>
          <div className="field">
            <textarea
              value={form.personality || ''}
              onChange={(e) => updateField('personality', e.target.value)}
              placeholder="Personnalité"
            />
          </div>
          <div className="field">
            <textarea
              value={form.history || ''}
              onChange={(e) => updateField('history', e.target.value)}
              placeholder="Histoire / Passé"
            />
          </div>
          <div className="field">
            <textarea
              value={form.motivation || ''}
              onChange={(e) => updateField('motivation', e.target.value)}
              placeholder="Motivation (Qu'est-ce qui le fait avancer ?)"
            />
          </div>
          <div className="field">
            <textarea
              value={form.goal || ''}
              onChange={(e) => updateField('goal', e.target.value)}
              placeholder="Objectif (But concret dans l'histoire)"
            />
          </div>
          <div className="field">
            <textarea
              value={form.flaw || ''}
              onChange={(e) => updateField('flaw', e.target.value)}
              placeholder="Défaut / Faille principale"
            />
          </div>
          <div className="field">
            <textarea
              value={form.character_arc || ''}
              onChange={(e) => updateField('character_arc', e.target.value)}
              placeholder="Arc narratif (Comment va-t-il évoluer ?)"
            />
          </div>
          <div className="field">
            <textarea
              value={form.skills || ''}
              onChange={(e) => updateField('skills', e.target.value)}
              placeholder="Compétences / Pouvoirs"
            />
          </div>
          <div className="field">
            <textarea
              value={form.notes || ''}
              onChange={(e) => updateField('notes', e.target.value)}
              placeholder="Notes libres"
            />
          </div>

          <div>
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
    </div>
  )
}
