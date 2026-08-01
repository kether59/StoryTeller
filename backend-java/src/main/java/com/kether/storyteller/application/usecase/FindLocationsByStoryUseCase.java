package com.kether.storyteller.application.usecase;

import com.kether.storyteller.dto.response.Responses.LocationResponse;
import com.kether.storyteller.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindLocationsByStoryUseCase {

    private final LocationRepository locationRepo;

    public List<LocationResponse> execute(Long storyId) {
        return locationRepo.findByStoryIdOrderByNameAsc(storyId)
            .stream()
            .map(LocationResponse::from)
            .toList();
    }
}
