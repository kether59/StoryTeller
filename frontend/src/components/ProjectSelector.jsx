import React, { useEffect, useState } from "react";
import API from "../api/api";

export default function ProjectSelector({ onSelect }) {
  const [stories, setStories] = useState([]);
  const [newTitle, setNewTitle] = useState("");

  useEffect(() => {
    fetchStories();
  }, []);

async function fetchStories() {
    const res = await API.get("/api/stories");
    setStories(res.data);
  }

async function createStory() {
    if (!newTitle.trim()) return;
    const res = await API.post("/api/stories", { title: newTitle });
    setNewTitle("");
    fetchStories();
    onSelect(res.data);
  }

  return (
    <div className="project-selector">
      <h2>📚 Sélectionnez un roman</h2>
      <ul>
        {stories.map((s) => (
          <li key={s.id}>
            <button onClick={() => onSelect(s)}>{s.title}</button>
          </li>
        ))}
      </ul>

      <div style={{ marginTop: 20 }}>
        <h3>Créer un nouveau roman</h3>
        <input
          placeholder="Titre du roman"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
        />
        <button onClick={createStory}>Créer</button>
      </div>
    </div>
  );
}
