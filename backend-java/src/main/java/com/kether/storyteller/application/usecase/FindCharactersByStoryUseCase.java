package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.response.Responses.CharacterResponse;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindCharactersByStoryUseCase {

    private final CharacterRepository characterRepo;

    public List<CharacterResponse> execute(Long storyId) {
        return characterRepo.findByStoryIdOrderByNameAsc(storyId)
            .stream()
            .map(CharacterResponse::from)
            .toList();
    }
}
