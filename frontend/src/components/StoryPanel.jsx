import React, { useEffect, useState } from 'react'
import API from '../api/api'

export default function StoryPanel({ story, onStoryUpdate }) {

if (!story || !story.id) {
    return <div>Sélectionnez ou créez un roman dans la barre de navigation.</div>;
  }

async function save() {
    try {
      await API.put(`/api/stories/${story.id}`, story)
      alert("Histoire enregistrée !");

    } catch (err) {
      console.error("Erreur lors de la sauvegarde de l'histoire :", err);
      alert("Erreur lors de la sauvegarde. Vérifiez le backend.");
    }
  }

async function exportMD() {
    try {
      const r = await API.get('/export/markdown', { responseType: 'blob' })
      const blob = new Blob([r.data], { type: 'text/markdown' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'storyteller_export.md'
      a.click()
    } catch (err) {
      alert('Erreur export Markdown')
    }
  }

  async function exportPDF() {
    window.open('http://127.0.0.1:5000/export/pdf', '_blank')
  }

return (
    <div className="panel">
      <h2>Histoire / Intrigue — <em>{story.title}</em></h2>

      {/* ⚠️ Utilisation de la prop onStoryUpdate pour propager le changement à App.jsx */}
      <div className="field">
        <input
          value={story.title || ''}
          onChange={e => onStoryUpdate({ ...story, title: e.target.value })}
          placeholder="Titre"
          className="input"
        />
      </div>
      <div className="field">
        <textarea
          value={story.synopsis || ''}
          onChange={e => onStoryUpdate({ ...story, synopsis: e.target.value })}
          placeholder="Synopsis"
          rows={6}
        />
      </div>
      <div className="field">
        <textarea
          value={story.blurb || ''}
          onChange={e => onStoryUpdate({ ...story, blurb: e.target.value })}
          placeholder="Quatrième de couverture"
          rows={4}
        />
      </div>

      <div>
        <button className="primary" onClick={save}>
          Enregistrer les modifications
        </button>
      </div>

      <div style={{ marginTop: 20 }}>
        <h3>Exportation</h3>
        <button onClick={exportMD}>Exporter tout en Markdown</button>
        <button onClick={exportPDF}>Exporter le manuscrit en PDF</button>
      </div>
    </div>
  )
}