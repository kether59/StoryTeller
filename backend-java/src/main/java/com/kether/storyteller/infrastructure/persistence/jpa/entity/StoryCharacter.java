package com.kether.storyteller.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Équivalent Python : class Character(Base) – renommé StoryCharacter
 * pour éviter la collision avec java.lang.Character.
 *
 * Exposition REST conservée sous le chemin /api/characters.
 */
@Entity
@Table(name = "characters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String surname;

    @Column(length = 255)
    private String role;

    private Integer age;

    @Column(length = 50)
    private String born;

    @Column(name = "physical_description", columnDefinition = "TEXT")
    private String physicalDescription;

    @Column(columnDefinition = "TEXT")
    private String personality;

    @Column(columnDefinition = "TEXT")
    private String history;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(columnDefinition = "TEXT")
    private String flaw;

    @Column(name = "character_arc", columnDefinition = "TEXT")
    private String characterArc;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Relation inverse Many-to-Many avec TimelineEvent. */
    @ManyToMany(mappedBy = "characters", fetch = FetchType.LAZY)
    private Set<TimelineEvent> timelineEvents = new HashSet<>();

    /* ── Getters / Setters ─────────────────────────────── */
    public Long    getId()                  { return id; }
    public Story   getStory()              { return story; }
    public String  getName()               { return name; }
    public String  getSurname()            { return surname; }
    public String  getRole()               { return role; }
    public Integer getAge()                { return age; }
    public String  getBorn()               { return born; }
    public String  getPhysicalDescription(){ return physicalDescription; }
    public String  getPersonality()        { return personality; }
    public String  getHistory()            { return history; }
    public String  getMotivation()         { return motivation; }
    public String  getGoal()               { return goal; }
    public String  getFlaw()               { return flaw; }
    public String  getCharacterArc()       { return characterArc; }
    public String  getSkills()             { return skills; }
    public String  getNotes()              { return notes; }
    public Set<TimelineEvent> getTimelineEvents() { return timelineEvents; }

    public void setId(Long id)                        { this.id = id; }
    public void setStory(Story story)                 { this.story = story; }
    public void setName(String name)                  { this.name = name; }
    public void setSurname(String surname)            { this.surname = surname; }
    public void setRole(String role)                  { this.role = role; }
    public void setAge(Integer age)                   { this.age = age; }
    public void setBorn(String born)                  { this.born = born; }
    public void setPhysicalDescription(String v)      { this.physicalDescription = v; }
    public void setPersonality(String personality)    { this.personality = personality; }
    public void setHistory(String history)            { this.history = history; }
    public void setMotivation(String motivation)      { this.motivation = motivation; }
    public void setGoal(String goal)                  { this.goal = goal; }
    public void setFlaw(String flaw)                  { this.flaw = flaw; }
    public void setCharacterArc(String characterArc)  { this.characterArc = characterArc; }
    public void setSkills(String skills)              { this.skills = skills; }
    public void setNotes(String notes)                { this.notes = notes; }
}