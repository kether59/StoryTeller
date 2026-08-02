package com.kether.storyteller.beforerefacto.usecase.character;

import com.kether.storyteller.domain.entity.StoryCharacter;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCharacterUseCase {

    private final CharacterRepositoryPort characterRepo;

    public StoryCharacter execute(Long id, String name, String role, String personality,
                                  String physicalDescription, Integer age, String motivation,
                                  String goal, String flaw, String characterArc, String skills,
                                  String notes, String surname, String born, String history) {
        StoryCharacter character = characterRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Character", id));

        if (name != null) character.setName(name);
        if (surname != null) character.setSurname(surname);
        if (role != null) character.setRole(role);
        if (age != null) character.setAge(age);
        if (born != null) character.setBorn(born);
        if (physicalDescription != null) character.setPhysicalDescription(physicalDescription);
        if (personality != null) character.setPersonality(personality);
        if (history != null) character.setHistory(history);
        if (motivation != null) character.setMotivation(motivation);
        if (goal != null) character.setGoal(goal);
        if (flaw != null) character.setFlaw(flaw);
        if (characterArc != null) character.setCharacterArc(characterArc);
        if (skills != null) character.setSkills(skills);
        if (notes != null) character.setNotes(notes);

        return characterRepo.save(character);
    }
}