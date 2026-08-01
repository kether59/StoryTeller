package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.request.Requests.CharacterUpdate;
import com.kether.storyteller.dto.response.Responses.CharacterResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class UpdateCharacterUseCase {

    private final CharacterRepository characterRepo;

    public CharacterResponse execute(Long characterId, CharacterUpdate request) {
        var character = characterRepo.findById(characterId)
            .orElseThrow(() -> ResourceNotFoundException.of("Personnage", characterId));

        if (request.name() != null) character.setName(request.name());
        if (request.surname() != null) character.setSurname(request.surname());
        if (request.role() != null) character.setRole(request.role());
        if (request.age() != null) character.setAge(request.age());
        if (request.born() != null) character.setBorn(request.born());
        if (request.physicalDescription() != null) character.setPhysicalDescription(request.physicalDescription());
        if (request.personality() != null) character.setPersonality(request.personality());
        if (request.history() != null) character.setHistory(request.history());
        if (request.motivation() != null) character.setMotivation(request.motivation());
        if (request.goal() != null) character.setGoal(request.goal());
        if (request.flaw() != null) character.setFlaw(request.flaw());
        if (request.characterArc() != null) character.setCharacterArc(request.characterArc());
        if (request.skills() != null) character.setSkills(request.skills());
        if (request.notes() != null) character.setNotes(request.notes());

        return CharacterResponse.from(characterRepo.save(character));
    }
}
