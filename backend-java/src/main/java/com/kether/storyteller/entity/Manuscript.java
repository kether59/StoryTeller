package com.kether.storyteller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Équivalent Python : class Manuscript(Base) */
@Entity
@Table(name = "manuscripts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Manuscript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(length = 255)
    private String title;

    @Column(name = "chapter_number")
    private Integer chapter = 1;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(length = 50)
    private String status;

    /* ── Getters / Setters ─────────── */
    public Long    getId()      { return id; }
    public Story   getStory()   { return story; }
    public String  getTitle()   { return title; }
    public Integer getChapter() { return chapter; }
    public String  getText()    { return text; }
    public String  getStatus()  { return status; }

    public void setId(Long id)           { this.id = id; }
    public void setStory(Story story)    { this.story = story; }
    public void setTitle(String title)   { this.title = title; }
    public void setChapter(Integer ch)   { this.chapter = ch; }
    public void setText(String text)     { this.text = text; }
    public void setStatus(String status) { this.status = status; }
}