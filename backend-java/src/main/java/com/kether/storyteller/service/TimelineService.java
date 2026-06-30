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

public class TimelineService {

    private final TimelineEventRepository eventRepo;
    private final StoryRepository         storyRepo;
    private final CharacterRepository     characterRepo;
    private final LocationRepository      locationRepo;

    TimelineService(TimelineEventRepository eventRepo, StoryRepository storyRepo,
                    CharacterRepository characterRepo, LocationRepository locationRepo) {
        this.eventRepo     = eventRepo;
        this.storyRepo     = storyRepo;
        this.characterRepo = characterRepo;
        this.locationRepo  = locationRepo;
    }

    public List<TimelineEventResponse> findByStory(Long storyId) {
        return eventRepo.findByStoryIdWithCharacters(storyId)
                .stream().map(TimelineEventResponse::from).toList();
    }

    public TimelineEventResponse findById(Long id) {
        return TimelineEventResponse.from(
            eventRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Événement", id)));
    }

    public TimelineEventResponse create(TimelineEventCreate req) {
        Story story = storyRepo.findById(req.storyId())
            .orElseThrow(() -> ResourceNotFoundException.of("Histoire", req.storyId()));
        var ev = new TimelineEvent();
        ev.setStory(story);
        applyCreate(ev, req);
        setCharacters(ev, req.characters());
        return TimelineEventResponse.from(eventRepo.save(ev));
    }

    public TimelineEventResponse update(Long id, TimelineEventUpdate req) {
        var ev = eventRepo.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Événement", id));
        applyUpdate(ev, req);
        if (req.characters() != null) setCharacters(ev, req.characters());
        return TimelineEventResponse.from(eventRepo.save(ev));
    }

    public void delete(Long id) {
        if (!eventRepo.existsById(id)) throw ResourceNotFoundException.of("Événement", id);
        eventRepo.deleteById(id);
    }

    private void applyCreate(TimelineEvent ev, TimelineEventCreate r) {
        ev.setTitle(r.title());
        ev.setDate(r.date());
        ev.setSortOrder(r.sortOrder() != null ? r.sortOrder() : 0);
        ev.setSummary(r.summary());
        if (r.locationId() != null) {
            locationRepo.findById(r.locationId()).ifPresent(ev::setLocation);
        }
    }

    private void applyUpdate(TimelineEvent ev, TimelineEventUpdate r) {
        if (r.title()     != null) ev.setTitle(r.title());
        if (r.date()      != null) ev.setDate(r.date());
        if (r.sortOrder() != null) ev.setSortOrder(r.sortOrder());
        if (r.summary()   != null) ev.setSummary(r.summary());
        if (r.locationId() != null) {
            locationRepo.findById(r.locationId()).ifPresent(ev::setLocation);
        }
    }

    private void setCharacters(TimelineEvent ev, List<Long> charIds) {
        if (charIds == null) return;
        Set<StoryCharacter> chars = new HashSet<>();
        for (Long cid : charIds) {
            characterRepo.findById(cid).ifPresent(chars::add);
        }
        ev.setCharacters(chars);
    }
}