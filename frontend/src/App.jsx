import React from "react";
import ProjectSelector from "./components/ProjectSelector";
import CharacterPanel from "./components/CharacterPanel";
import LocationPanel from "./components/LocationPanel";
import LorePanel from "./components/LorePanel";
import TimelinePanel from "./components/TimelinePanel";
import StoryPanel from "./components/StoryPanel";
import AiPanel from "./components/AiPanel";
import ManuscriptPanel from "./components/ManuscriptPanel";
import WritingAssistantPanel from "./components/WritingAssistantPanel";
import ExtractionPanel from "./components/ExtractionPanel";

export default function App() {
  const [selectedStory, setSelectedStory] = React.useState(null);
  const [tab, setTab] = React.useState("story");

  const handleStoryUpdate = (updatedStory) => {
    setSelectedStory(updatedStory);
  };

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
          <button onClick={() => setTab("locations")} className={tab === "locations" ? "active" : ""}>
            Lieux
          </button>
          <button onClick={() => setTab("lore")} className={tab === "lore" ? "active" : ""}>
            Lore
          </button>
          <button
            onClick={() => setTab("timeline")}
            className={tab === "timeline" ? "active" : ""}
          >
            Chronologie
          </button>
          <button onClick={() => setTab("writing")} className={tab === "writing" ? "active" : ""}>
            ✍️ Assistant
          </button>
          <button onClick={() => setTab("extraction")} className={tab === "extraction" ? "active" : ""}>
            🔍 Extraction
          </button>
          <button onClick={() => setTab("ai")} className={tab === "ai" ? "active" : ""}>
            🤖 Analyse
          </button>
        </nav>
        <button className="secondary" onClick={() => setSelectedStory(null)} style={{marginLeft: 16}}>
          Changer de roman
        </button>
      </header>

      <main>
        {tab === "story" && (
          <StoryPanel
            story={selectedStory}
            onStoryUpdate={handleStoryUpdate}
          />
        )}
        {tab === "manuscript" && <ManuscriptPanel story={selectedStory} />}
        {tab === "characters" && <CharacterPanel story={selectedStory} />}
        {tab === "locations" && <LocationPanel story={selectedStory} />}
        {tab === "lore" && <LorePanel story={selectedStory} />}
        {tab === "timeline" && <TimelinePanel story={selectedStory} />}
        {tab === "writing" && <WritingAssistantPanel story={selectedStory} />}
        {tab === "extraction" && <ExtractionPanel story={selectedStory} />}
        {tab === "ai" && <AiPanel story={selectedStory} />}
      </main>
    </div>
  );
}