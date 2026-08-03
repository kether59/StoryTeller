package com.kether.storyteller.controller;

import com.kether.storyteller.application.usecase.manuscript.CreateManuscriptUseCase;
import com.kether.storyteller.application.usecase.manuscript.DeleteManuscriptUseCase;
import com.kether.storyteller.application.usecase.manuscript.FindManuscriptsByStoryUseCase;
import com.kether.storyteller.application.usecase.manuscript.UpdateManuscriptUseCase;
import com.kether.storyteller.application.usecase.timeline.CreateTimelineEventUseCase;
import com.kether.storyteller.application.usecase.timeline.DeleteTimelineEventUseCase;
import com.kether.storyteller.application.usecase.timeline.FindTimelineEventsByStoryUseCase;
import com.kether.storyteller.application.usecase.timeline.UpdateTimelineEventUseCase;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/timeline")
class TimelineController {
    private final CreateTimelineEventUseCase createEvent;
    private final FindTimelineEventsByStoryUseCase findByStory;
    private final UpdateTimelineEventUseCase updateEvent;
    private final DeleteTimelineEventUseCase deleteEvent;

    TimelineController(CreateTimelineEventUseCase createEvent, FindTimelineEventsByStoryUseCase findByStory,
                       UpdateTimelineEventUseCase updateEvent, DeleteTimelineEventUseCase deleteEvent) {
        this.createEvent = createEvent;
        this.findByStory = findByStory;
        this.updateEvent = updateEvent;
        this.deleteEvent = deleteEvent;
    }

    @GetMapping
    public List<Responses.TimelineEventResponse> list(@RequestParam Long storyId) {
        return findByStory.execute(storyId).stream().map(Responses.TimelineEventResponse::from).toList();
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.TimelineEventResponse create(@Valid @RequestBody Requests.TimelineEventCreate req) {
        return Responses.TimelineEventResponse.from(
                createEvent.execute(req.storyId(), req.title(), req.date(), req.sortOrder(), req.summary(), req.locationId()));
    }

    @PutMapping("/{id}")
    public Responses.TimelineEventResponse update(@PathVariable Long id, @RequestBody Requests.TimelineEventUpdate req) {
        return Responses.TimelineEventResponse.from(
                updateEvent.execute(id, req.title(), req.date(), req.sortOrder(), req.summary(), req.locationId()));
    }

    @DeleteMapping("/{id}")
    public Responses.OkResponse delete(@PathVariable Long id) {
        deleteEvent.execute(id);
        return Responses.OkResponse.ok();
    }
}

@RestController @RequestMapping("/api/manuscript")
class ManuscriptController {
    private final CreateManuscriptUseCase createManuscript;
    private final FindManuscriptsByStoryUseCase findByStory;
    private final UpdateManuscriptUseCase updateManuscript;
    private final DeleteManuscriptUseCase deleteManuscript;

    ManuscriptController(CreateManuscriptUseCase createManuscript, FindManuscriptsByStoryUseCase findByStory,
                         UpdateManuscriptUseCase updateManuscript, DeleteManuscriptUseCase deleteManuscript) {
        this.createManuscript = createManuscript;
        this.findByStory = findByStory;
        this.updateManuscript = updateManuscript;
        this.deleteManuscript = deleteManuscript;
    }

    @GetMapping
    public List<Responses.ManuscriptResponse> list(@RequestParam(required = false) Long storyId) {
        if (storyId == null) return List.of();
        return findByStory.execute(storyId).stream().map(Responses.ManuscriptResponse::from).toList();
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.ManuscriptResponse create(@Valid @RequestBody Requests.ManuscriptCreate req) {
        return Responses.ManuscriptResponse.from(
                createManuscript.execute(req.storyId(), req.title(), req.chapter(), req.text(), req.status()));
    }

    @PutMapping("/{id}")
    public Responses.ManuscriptResponse update(@PathVariable Long id, @RequestBody Requests.ManuscriptUpdate req) {
        return Responses.ManuscriptResponse.from(
                updateManuscript.execute(id, req.title(), req.chapter(), req.text(), req.status()));
    }

    @DeleteMapping("/{id}")
    public Responses.OkResponse delete(@PathVariable Long id) {
        deleteManuscript.execute(id);
        return Responses.OkResponse.ok();
    }
}