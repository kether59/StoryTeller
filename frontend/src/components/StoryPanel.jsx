import React from 'react'
import API from '../api/api'

export default function StoryPanel({ story, onStoryUpdate }) {
  if (!story || !story.id) {
    return (
      <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
        Sélectionnez ou créez un roman dans la barre de navigation.
      </div>
    )
  }

  async function save() {
    try {
      await API.put(`/api/stories/${story.id}`, {
        title: story.title,
        synopsis: story.synopsis,
        blurb: story.blurb
      })
      alert('✅ Histoire enregistrée avec succès!')
    } catch (err) {
      console.error('Erreur lors de la sauvegarde de l\'histoire :', err)
      alert('❌ Erreur lors de la sauvegarde. Vérifiez le backend.')
    }
  }

  async function exportAllData() {
    try {
      // Récupérer toutes les données du roman
      const [characters, locations, lore, timeline, manuscripts] = await Promise.all([
        API.get(`/api/characters?story_id=${story.id}`),
        API.get(`/api/locations?story_id=${story.id}`),
        API.get(`/api/lore?story_id=${story.id}`),
        API.get(`/api/timeline?story_id=${story.id}`),
        API.get(`/api/manuscript?story_id=${story.id}`)
      ])

      // Créer un objet JSON complet
      const fullExport = {
        story: {
          title: story.title,
          synopsis: story.synopsis,
          blurb: story.blurb
        },
        characters: characters.data,
        locations: locations.data,
        lore: lore.data,
        timeline: timeline.data,
        manuscripts: manuscripts.data,
        exported_at: new Date().toISOString()
      }

      // Télécharger en JSON
      const blob = new Blob([JSON.stringify(fullExport, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${story.title.replace(/\s+/g, '_')}_export.json`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Erreur export:', err)
      alert('Erreur lors de l\'export')
    }
  }

  async function exportMarkdown() {
    try {
      const [characters, locations, lore, timeline, manuscripts] = await Promise.all([
        API.get(`/api/characters?story_id=${story.id}`),
        API.get(`/api/locations?story_id=${story.id}`),
        API.get(`/api/lore?story_id=${story.id}`),
        API.get(`/api/timeline?story_id=${story.id}`),
        API.get(`/api/manuscript?story_id=${story.id}`)
      ])

      // Générer le Markdown
      let md = `# ${story.title}\n\n`

      if (story.synopsis) {
        md += `## Synopsis\n\n${story.synopsis}\n\n`
      }

      if (story.blurb) {
        md += `## Quatrième de couverture\n\n${story.blurb}\n\n`
      }

      // Personnages
      if (characters.data.length > 0) {
        md += `## Personnages\n\n`
        characters.data.forEach(char => {
          md += `### ${char.name}${char.surname ? ' ' + char.surname : ''}\n\n`
          if (char.role) md += `**Rôle:** ${char.role}\n\n`
          if (char.age) md += `**Âge:** ${char.age} ans\n\n`
          if (char.physical_description) md += `**Description physique:** ${char.physical_description}\n\n`
          if (char.personality) md += `**Personnalité:** ${char.personality}\n\n`
          md += `---\n\n`
        })
      }

      // Lieux
      if (locations.data.length > 0) {
        md += `## Lieux\n\n`
        locations.data.forEach(loc => {
          md += `### ${loc.name}\n\n`
          if (loc.type) md += `**Type:** ${loc.type}\n\n`
          if (loc.summary) md += `${loc.summary}\n\n`
          md += `---\n\n`
        })
      }

      // Lore
      if (lore.data.length > 0) {
        md += `## Lore & World-Building\n\n`
        lore.data.forEach(entry => {
          md += `### ${entry.title}\n\n`
          if (entry.category) md += `**Catégorie:** ${entry.category}\n\n`
          if (entry.content) md += `${entry.content}\n\n`
          md += `---\n\n`
        })
      }

      // Chronologie
      if (timeline.data.length > 0) {
        md += `## Chronologie\n\n`
        timeline.data.forEach(event => {
          md += `### ${event.title}${event.date ? ' - ' + event.date : ''}\n\n`
          if (event.summary) md += `${event.summary}\n\n`
          md += `---\n\n`
        })
      }

      // Manuscrits
      if (manuscripts.data.length > 0) {
        md += `## Manuscrit\n\n`
        manuscripts.data.sort((a, b) => a.chapter - b.chapter).forEach(ms => {
          md += `### Chapitre ${ms.chapter}: ${ms.title}\n\n`
          if (ms.text) md += `${ms.text}\n\n`
          md += `---\n\n`
        })
      }

      // Télécharger
      const blob = new Blob([md], { type: 'text/markdown' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${story.title.replace(/\s+/g, '_')}_export.md`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Erreur export Markdown:', err)
      alert('Erreur lors de l\'export Markdown')
    }
  }

  return (
    <div className="panel">
      <h2>📖 Histoire / Intrigue — <em>{story.title}</em></h2>

      <div className="field">
        <label><strong>Titre du roman</strong></label>
        <input
          value={story.title || ''}
          onChange={e => onStoryUpdate({ ...story, title: e.target.value })}
          placeholder="Titre"
          className="input"
        />
      </div>

      <div className="field">
        <label><strong>Synopsis</strong></label>
        <textarea
          value={story.synopsis || ''}
          onChange={e => onStoryUpdate({ ...story, synopsis: e.target.value })}
          placeholder="Résumé complet de l'intrigue..."
          rows={8}
          className="input"
        />
      </div>

      <div className="field">
        <label><strong>Quatrième de couverture (Blurb)</strong></label>
        <textarea
          value={story.blurb || ''}
          onChange={e => onStoryUpdate({ ...story, blurb: e.target.value })}
          placeholder="Texte accrocheur pour la couverture..."
          rows={4}
          className="input"
        />
      </div>

      <div>
        <button className="primary" onClick={save}>
          💾 Enregistrer les modifications
        </button>
      </div>

      <div style={{ marginTop: 30, paddingTop: 20, borderTop: '1px solid #eee' }}>
        <h3>📤 Exportation</h3>
        <p style={{ color: '#666', fontSize: '0.9em' }}>
          Exportez toutes les données de votre roman dans différents formats.
        </p>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <button onClick={exportMarkdown}>
            📝 Exporter en Markdown
          </button>
          <button onClick={exportAllData}>
            📦 Exporter en JSON (données complètes)
          </button>
        </div>
      </div>

      <div style={{ marginTop: 20, padding: 12, background: '#f0f9ff', borderRadius: 6 }}>
        <h4 style={{ margin: '0 0 8px 0' }}>💡 Conseils d'écriture</h4>
        <ul style={{ margin: 0, paddingLeft: 20, fontSize: '0.9em' }}>
          <li>Le synopsis doit résumer l'intrigue principale en 200-300 mots</li>
          <li>Le blurb doit donner envie de lire sans tout révéler (50-100 mots)</li>
          <li>Pensez à sauvegarder régulièrement vos modifications</li>
        </ul>
      </div>
    </div>
  )
}