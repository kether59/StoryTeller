package com.kether.storyteller.infrastructure.persistence.jpa;

import com.kether.storyteller.domain.entity.StoryLocation;
import com.kether.storyteller.domain.model.ExtractedLocation;
import com.kether.storyteller.domain.port.out.persistence.LocationRepositoryPort;
import com.kether.storyteller.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaLocationRepositoryAdapter implements LocationRepositoryPort {

    private final LocationRepository existingRepo;

    @Override
    public Optional<ExtractedLocation> findByStoryIdAndName(Long storyId, String name) {
        return existingRepo.findByStoryIdAndName(storyId, name)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByStoryIdAndName(Long storyId, String name) {
        return existingRepo.findByStoryIdAndName(storyId, name).isPresent();
    }

    @Override
    public ExtractedLocation save(ExtractedLocation location) {
        // Pour l'instant, on retourne le domaine tel quel
        return location;
    }

    private ExtractedLocation toDomain(StoryLocation jpa) {
        return new ExtractedLocation(
                jpa.getName(),
                jpa.getType(),
                jpa.getSummary(),
                0.0  // La confiance n'est pas en DB actuellement
        );
    }
}
