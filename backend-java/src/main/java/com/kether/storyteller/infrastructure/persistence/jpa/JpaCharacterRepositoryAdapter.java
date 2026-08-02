package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.StoryCharacter;
import com.kether.storyteller.domain.model.ExtractedCharacter;
import com.kether.storyteller.domain.port.out.persistence.CharacterRepositoryPort;
import com.kether.storyteller.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaCharacterRepositoryAdapter implements CharacterRepositoryPort {

    private final CharacterRepository existingRepo;

    @Override
    public Optional<ExtractedCharacter> findByStoryIdAndName(Long storyId, String name) {
        return existingRepo.findByStoryIdAndName(storyId, name)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByStoryIdAndName(Long storyId, String name) {
        return existingRepo.findByStoryIdAndName(storyId, name).isPresent();
    }

    @Override
    public ExtractedCharacter save(ExtractedCharacter character) {
        // Pour l'instant, on retourne le domaine tel quel
        // En production, on persisterait en DB
        return character;
    }

    private ExtractedCharacter toDomain(StoryCharacter jpa) {
        return new ExtractedCharacter(
                jpa.getName(),
                jpa.getSurname(),
                jpa.getRole(),
                jpa.getAge(),
                jpa.getPhysicalDescription(),
                jpa.getPersonality(),
                jpa.getMotivation(),
                0.0  // La confiance n'est pas en DB actuellement
        );
    }

    @Override
    public List<Character> findByStoryId(Long storyId) {
        return List.of();
    }

    @Override
    public List<ExtractedCharacter> parse(String rawJson) {
        return List.of();
    }
}