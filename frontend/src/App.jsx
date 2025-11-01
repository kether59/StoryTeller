import React from "react";
import ProjectSelector from "./components/ProjectSelector";
import CharacterPanel from "./components/CharacterPanel";
import WorldPanel from "./components/WorldPanel";
import TimelinePanel from "./components/TimelinePanel";
import StoryPanel from "./components/StoryPanel";
import AiPanel from "./components/AiPanel";
import ManuscriptPanel from "./components/ManuscriptPanel";

export default function App() {
  const [selectedStory, setSelectedStory] = React.useState(null);
  const [tab, setTab] = React.useState("story");

  if (!selectedStory) {
    return <ProjectSelector onSelect={setSelectedStory} />;
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>📖 StoryTeller — {selectedStory.title}</h1>
        <nav>
          <button onClick={() => setTab("story")} className={tab === "story" ? "active" : ""}>
            Histoire
          </button>
          <button
            onClick={() => setTab("manuscript")}
            className={tab === "manuscript" ? "active" : ""}
          >
            Manuscrit
          </button>
          <button
            onClick={() => setTab("characters")}
            className={tab === "characters" ? "active" : ""}
          >
            Personnages
          </button>
          <button onClick={() => setTab("world")} className={tab === "world" ? "active" : ""}>
            Monde
          </button>
          <button
            onClick={() => setTab("timeline")}
            className={tab === "timeline" ? "active" : ""}
          >
            Chronologie
          </button>
          <button onClick={() => setTab("ai")} className={tab === "ai" ? "active" : ""}>
            Assistant IA
          </button>
        </nav>
      </header>

      <main>
        {tab === "story" && <StoryPanel story={selectedStory} />}
        {tab === "manuscript" && <ManuscriptPanel story={selectedStory} />}
        {tab === "characters" && <CharacterPanel story={selectedStory} />}
        {tab === "world" && <WorldPanel story={selectedStory} />}
        {tab === "timeline" && <TimelinePanel story={selectedStory} />}
        {tab === "ai" && <AiPanel story={selectedStory} />}
      </main>
    </div>
  );
}
