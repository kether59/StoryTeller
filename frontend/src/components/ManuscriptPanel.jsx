import React, { useEffect, useState, useRef } from 'react'
import API from '../api/api'
import MarkdownIt from 'markdown-it'
import MdEditor from 'react-markdown-editor-lite'
import 'react-markdown-editor-lite/lib/index.css'

const mdParser = new MarkdownIt()

export default function ManuscriptPanel({ story }) {
  const [list, setList] = useState([])
  const [current, setCurrent] = useState(null)
  const [autoSaving, setAutoSaving] = useState(false)
  const timerRef = useRef(null)

  useEffect(() => {
    fetchList()
  }, [story])

  useEffect(() => {
    if (timerRef.current) clearInterval(timerRef.current)
    timerRef.current = setInterval(() => {
      if (current && current._dirty) saveCurrent()
    }, 10000)
    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [current])

  async function fetchList() {
    const r = await API.get(`/api/manuscript?story_id=${story.id}`)
    setList(r.data)
  }

  async function newChapter() {
    const payload = {
      story_id: story.id,
      title: 'Nouveau chapitre',
      chapter: list.length ? list[list.length - 1].chapter + 1 : 1,
      text: ''
    }
    const res = await API.post('/api/manuscript', payload)
    fetchList()
    setCurrent(res.data)
  }

  async function saveCurrent() {
    if (!current) return
    setAutoSaving(true)
    await API.post('/api/manuscript', current)
    setAutoSaving(false)
    setCurrent({ ...current, _dirty: false })
    fetchList()
  }

  async function del(id) {
    if (!confirm('Supprimer ?')) return
    await API.delete('/api/manuscript', { data: { id } })
    setCurrent(null)
    fetchList()
  }

  function onEditorChange({ text }) {
    setCurrent({ ...current, text, _dirty: true })
  }

  async function runAnalyze(mode) {
    if (!current) return
    const r = await API.get(`/api/manuscript/analyze/${current.id}?mode=${mode}`)
    setCurrent({ ...current, analysis: r.data })
  }

  async function exportAnnotated() {
    if (!current) return
    const r = await API.get(`/export/manuscript/${current.id}`)
    const blob = new Blob([r.data], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `manuscript_${current.id}.md`
    a.click()
  }

  return (
    <div className="panel">
      <h2>Manuscrit — <em>{story?.title || 'Aucun roman sélectionné'}</em></h2>
      <div style={{ display: 'flex', gap: 12 }}>
        <div style={{ width: 300 }}>
          <button onClick={newChapter} className="primary">
            Nouveau chapitre
          </button>
          <div className="list" style={{ marginTop: 12 }}>
            {list.map(it => (
              <div
                key={it.id}
                className="item"
                onClick={() => setCurrent(it)}
                style={{
                  cursor: 'pointer',
                  background:
                    current && current.id === it.id ? '#eef' : 'transparent'
                }}
              >
                <strong>{it.title}</strong>
                <div>
                  chap. {it.chapter} — modifié {it.updated_at}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{ flex: 1 }}>
          {!current && <div>Sélectionne ou crée un chapitre</div>}
          {current && (
            <div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  style={{ flex: 1 }}
                  value={current.title}
                  onChange={e =>
                    setCurrent({
                      ...current,
                      title: e.target.value,
                      _dirty: true
                    })
                  }
                />
                <button onClick={saveCurrent}>
                  {autoSaving ? 'Enregistrement...' : 'Sauvegarder'}
                </button>
                <button onClick={() => del(current.id)}>Supprimer</button>
              </div>
              <div style={{ marginTop: 8 }}>
                <MdEditor
                  value={current.text || ''}
                  style={{ height: '60vh' }}
                  renderHTML={text => mdParser.render(text)}
                  onChange={onEditorChange}
                />
              </div>
              <div style={{ marginTop: 8 }}>
                <button onClick={() => runAnalyze('fast')}>
                  Analyse rapide
                </button>
                <button onClick={() => runAnalyze('detailed')}>
                  Analyse détaillée
                </button>
                <button onClick={exportAnnotated}>Exporter MD annoté</button>
              </div>
              <div style={{ marginTop: 12 }}>
                <h3>Résultat d'analyse</h3>
                <pre
                  style={{
                    whiteSpace: 'pre-wrap',
                    background: '#fafafa',
                    padding: 8
                  }}
                >
                  {JSON.stringify(current.analysis || {}, null, 2)}
                </pre>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
