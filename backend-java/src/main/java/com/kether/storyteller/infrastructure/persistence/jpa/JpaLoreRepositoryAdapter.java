package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.LoreEntry;
import com.kether.storyteller.domain.model.ExtractedLore;
import com.kether.storyteller.domain.port.out.persistence.LoreEntryRepositoryPort;
import com.kether.storyteller.repository.LoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaLoreRepositoryAdapter implements LoreEntryRepositoryPort {

    private final LoreEntryRepository existingRepo;

    @Override
    public Optional<ExtractedLore> findByStoryIdAndTitle(Long storyId, String title) {
        return existingRepo.findByStoryIdAndTitle(storyId, title)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByStoryIdAndTitle(Long storyId, String title) {
        return existingRepo.findByStoryIdAndTitle(storyId, title).isPresent();
    }

    @Override
    public ExtractedLore save(ExtractedLore lore) {
        // Pour l'instant, on retourne le domaine tel quel
        return lore;
    }

    @Override
    public List<ExtractedLore> parse(String jsonResponse) {
        return List.of();
    }

    private ExtractedLore toDomain(LoreEntry jpa) {
        return new ExtractedLore(
                jpa.getTitle(),
                jpa.getCategory(),
                jpa.getContent(),
                0.0  // La confiance n'est pas en DB actuellement
        );
    }
}
