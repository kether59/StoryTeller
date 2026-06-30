package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// ══════════════════════════════════════════════════════════════════════
//  StoryService   – équivalent stories.py
// ══════════════════════════════════════════════════════════════════════
@Service
@Transactional

public class CharacterService {

    private final CharacterRepository characterRepo;
    private final StoryRepository     storyRepo;

    CharacterService(CharacterRepository characterRepo, StoryRepository storyRepo) {
        this.characterRepo = characterRepo;
        this.storyRepo     = storyRepo;
    }

    public List<CharacterResponse> findByStory(Long storyId) {
        return characterRepo.findByStoryIdOrderByNameAsc(storyId)
                .stream().map(CharacterResponse::from).toList();
    }

    public CharacterResponse findById(Long id) {
        return CharacterResponse.from(
            characterRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Personnage", id)));
    }

    public CharacterResponse create(CharacterCreate req) {
        Story story = storyRepo.findById(req.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", req.storyId()));
        var c = new StoryCharacter();
        c.setStory(story);
        applyCreate(c, req);
        return CharacterResponse.from(characterRepo.save(c));
    }

    public CharacterResponse update(Long id, CharacterUpdate req) {
        var c = characterRepo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Personnage", id));
        applyUpdate(c, req);
        return CharacterResponse.from(characterRepo.save(c));
    }

    public void delete(Long id) {
        if (!characterRepo.existsById(id)) throw ResourceNotFoundException.of("Personnage", id);
        characterRepo.deleteById(id);
    }

    private void applyCreate(StoryCharacter c, CharacterCreate r) {
        c.setName(r.name());
        c.setSurname(r.surname());
        c.setRole(r.role());
        c.setAge(r.age());
        c.setBorn(r.born());
        c.setPhysicalDescription(r.physicalDescription());
        c.setPersonality(r.personality());
        c.setHistory(r.history());
        c.setMotivation(r.motivation());
        c.setGoal(r.goal());
        c.setFlaw(r.flaw());
        c.setCharacterArc(r.characterArc());
        c.setSkills(r.skills());
        c.setNotes(r.notes());
    }

    private void applyUpdate(StoryCharacter c, CharacterUpdate r) {
        if (r.name()               != null) c.setName(r.name());
        if (r.surname()            != null) c.setSurname(r.surname());
        if (r.role()               != null) c.setRole(r.role());
        if (r.age()                != null) c.setAge(r.age());
        if (r.born()               != null) c.setBorn(r.born());
        if (r.physicalDescription()!= null) c.setPhysicalDescription(r.physicalDescription());
        if (r.personality()        != null) c.setPersonality(r.personality());
        if (r.history()            != null) c.setHistory(r.history());
        if (r.motivation()         != null) c.setMotivation(r.motivation());
        if (r.goal()               != null) c.setGoal(r.goal());
        if (r.flaw()               != null) c.setFlaw(r.flaw());
        if (r.characterArc()       != null) c.setCharacterArc(r.characterArc());
        if (r.skills()             != null) c.setSkills(r.skills());
        if (r.notes()              != null) c.setNotes(r.notes());
    }
}