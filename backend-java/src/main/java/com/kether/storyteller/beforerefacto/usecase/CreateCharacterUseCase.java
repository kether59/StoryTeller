package com.kether.storyteller.beforerefacto.usecase;

import com.kether.storyteller.infrastructure.web.rest.dto.Requests.CharacterCreate;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.CharacterResponse;
import com.kether.storyteller.domain.entity.Story;
import com.kether.storyteller.domain.entity.StoryCharacter;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.CharacterRepository;
import com.kether.storyteller.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CreateCharacterUseCase {

    private final CharacterRepository characterRepo;
    private final StoryRepository storyRepo;

    public CharacterResponse execute(CharacterCreate request) {
        Story story = storyRepo.findById(request.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", request.storyId()));

        var character = new StoryCharacter();
        character.setStory(story);
        character.setName(request.name());
        character.setSurname(request.surname());
        character.setRole(request.role());
        character.setAge(request.age());
        character.setBorn(request.born());
        character.setPhysicalDescription(request.physicalDescription());
        character.setPersonality(request.personality());
        character.setHistory(request.history());
        character.setMotivation(request.motivation());
        character.setGoal(request.goal());
        character.setFlaw(request.flaw());
        character.setCharacterArc(request.characterArc());
        character.setSkills(request.skills());
        character.setNotes(request.notes());

        return CharacterResponse.from(characterRepo.save(character));
    }
}
