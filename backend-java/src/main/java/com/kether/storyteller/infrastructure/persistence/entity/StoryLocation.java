package com.kether.storyteller.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Équivalent Python : class Location(Base)
 * Renommé StoryLocation pour éviter tout conflit éventuel.
 */
@Entity
@Table(name = "locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(length = 255)
    private String name;

    @Column(length = 100)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<TimelineEvent> timelineEvents;

    /* ── Getters / Setters ─────────── */
    public Long    getId()      { return id; }
    public Story   getStory()   { return story; }
    public String  getName()    { return name; }
    public String  getType()    { return type; }
    public String  getSummary() { return summary; }
    public List<TimelineEvent> getTimelineEvents() { return timelineEvents; }

    public void setId(Long id)         { this.id = id; }
    public void setStory(Story story)  { this.story = story; }
    public void setName(String name)   { this.name = name; }
    public void setType(String type)   { this.type = type; }
    public void setSummary(String s)   { this.summary = s; }
}