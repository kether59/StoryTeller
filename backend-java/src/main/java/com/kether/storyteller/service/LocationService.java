package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.entity.*;
import com.kether.storyteller.exception.ResourceNotFoundException;
import com.kether.storyteller.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ══════════════════════════════════════════════════════════════════════
//  StoryService   – équivalent stories.py
// ══════════════════════════════════════════════════════════════════════
@Service
@Transactional

public class LocationService {

    private final LocationRepository locationRepo;
    private final StoryRepository    storyRepo;

    LocationService(LocationRepository locationRepo, StoryRepository storyRepo) {
        this.locationRepo = locationRepo;
        this.storyRepo    = storyRepo;
    }

    public List<LocationResponse> findByStory(Long storyId) {
        return locationRepo.findByStoryIdOrderByNameAsc(storyId)
                .stream().map(LocationResponse::from).toList();
    }

    public LocationResponse findById(Long id) {
        return LocationResponse.from(
            locationRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Lieu", id)));
    }

    public LocationResponse create(LocationCreate req) {
        Story story = storyRepo.findById(req.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", req.storyId()));
        var l = new StoryLocation();
        l.setStory(story);
        l.setName(req.name());
        l.setType(req.type());
        l.setSummary(req.summary());
        return LocationResponse.from(locationRepo.save(l));
    }

    public LocationResponse update(Long id, LocationUpdate req) {
        var l = locationRepo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Lieu", id));
        if (req.name()    != null) l.setName(req.name());
        if (req.type()    != null) l.setType(req.type());
        if (req.summary() != null) l.setSummary(req.summary());
        return LocationResponse.from(locationRepo.save(l));
    }

    public void delete(Long id) {
        if (!locationRepo.existsById(id)) throw ResourceNotFoundException.of("Lieu", id);
        locationRepo.deleteById(id);
    }
}