package com.kether.storyteller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Équivalent Python : class LoreEntry(Base) */
@Entity
@Table(name = "lore_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoreEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(length = 255)
    private String title;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String content;

    /* ── Getters / Setters ─────────── */
    public Long   getId()       { return id; }
    public Story  getStory()    { return story; }
    public String getTitle()    { return title; }
    public String getCategory() { return category; }
    public String getContent()  { return content; }

    public void setId(Long id)           { this.id = id; }
    public void setStory(Story story)    { this.story = story; }
    public void setTitle(String title)   { this.title = title; }
    public void setCategory(String cat)  { this.category = cat; }
    public void setContent(String cont)  { this.content = cont; }
}