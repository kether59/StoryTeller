import React from 'react'
import API from '../api/api'

export default function AiPanel(){
  const [out,setOut]=React.useState('')
  async function runSuggest(){ const r = await API.post('/api/ai/suggest', {intent:'link_characters'}); setOut(JSON.stringify(r.data,null,2)) }
  async function runTimelineCheck(){ const r = await API.post('/api/ai/suggest', {intent:'timeline_conflicts'}); setOut(JSON.stringify(r.data,null,2)) }
  return (
    <div className="panel">
      <h2>Assistant IA</h2>
      <div style={{display:'flex',gap:8}}>
        <button className="primary" onClick={runSuggest}>Trouver liens personnages</button>
        <button onClick={runTimelineCheck}>Vérifier chronologie</button>
      </div>
      <pre style={{whiteSpace:'pre-wrap',marginTop:12,background:'#fafafa',padding:8}}>{out}</pre>
    </div>
  )
}