package com.kether.storyteller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Équivalent Python :
 * <pre>
 * class Story(Base):
 *     __tablename__ = 'stories'
 *     id, title, synopsis, blurb
 *     relations → characters, locations, lore_entries, timeline_events, manuscripts
 * </pre>
 */
@Entity
@Table(name = "stories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(columnDefinition = "TEXT")
    private String blurb;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<StoryCharacter> characters = new ArrayList<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<StoryLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<LoreEntry> loreEntries = new ArrayList<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<TimelineEvent> timelineEvents = new ArrayList<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Manuscript> manuscripts = new ArrayList<>();

    public Story(String title, String synopsis, String blurb) {
        this.title    = title;
        this.synopsis = synopsis;
        this.blurb    = blurb;
    }

    /* ── Getters / Setters ─────────────────────────────────────── */
    public Long   getId()       { return id; }
    public String getTitle()    { return title; }
    public String getSynopsis() { return synopsis; }
    public String getBlurb()    { return blurb; }

    public List<StoryCharacter> getCharacters()     { return characters; }
    public List<StoryLocation>  getLocations()      { return locations; }
    public List<LoreEntry>      getLoreEntries()    { return loreEntries; }
    public List<TimelineEvent>  getTimelineEvents() { return timelineEvents; }
    public List<Manuscript>     getManuscripts()    { return manuscripts; }

    public void setId(Long id)           { this.id = id; }
    public void setTitle(String title)   { this.title = title; }
    public void setSynopsis(String s)    { this.synopsis = s; }
    public void setBlurb(String b)       { this.blurb = b; }
}