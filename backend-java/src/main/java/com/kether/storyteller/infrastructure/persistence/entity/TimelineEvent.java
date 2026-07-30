package com.kether.storyteller.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Équivalent Python : class TimelineEvent(Base)
 *
 * La table de jointure Many-to-Many timeline_character est reproduite
 * via @JoinTable avec les mêmes noms de colonnes que le schéma SQLite Python.
 */
@Entity
@Table(name = "timeline_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(length = 255)
    private String title;

    @Column(length = 50)
    private String date;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private StoryLocation location;

    /**
     * Many-to-Many avec StoryCharacter.
     * Table de jointure : timeline_character (identique au schéma Python).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "timeline_character",
            joinColumns        = @JoinColumn(name = "timeline_event_id"),
            inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    private Set<StoryCharacter> characters = new HashSet<>();

    /* ── Helper : IDs des personnages (équivalent get_characters()) ─ */
    public List<Long> getCharacterIds() {
        return characters.stream()
                .map(StoryCharacter::getId)
                .collect(Collectors.toList());
    }

    /* ── Getters / Setters ─────────────────────────────── */
    public Long         getId()        { return id; }
    public Story        getStory()     { return story; }
    public String       getTitle()     { return title; }
    public String       getDate()      { return date; }
    public Integer      getSortOrder() { return sortOrder; }
    public String       getSummary()   { return summary; }
    public StoryLocation getLocation() { return location; }
    public Set<StoryCharacter> getCharacters() { return characters; }

    public void setId(Long id)                   { this.id = id; }
    public void setStory(Story story)            { this.story = story; }
    public void setTitle(String title)           { this.title = title; }
    public void setDate(String date)             { this.date = date; }
    public void setSortOrder(Integer sortOrder)  { this.sortOrder = sortOrder; }
    public void setSummary(String summary)       { this.summary = summary; }
    public void setLocation(StoryLocation loc)   { this.location = loc; }
    public void setCharacters(Set<StoryCharacter> c) { this.characters = c; }
}