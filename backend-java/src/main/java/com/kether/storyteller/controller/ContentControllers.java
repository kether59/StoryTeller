package com.kether.storyteller.controller;

import com.kether.storyteller.beforerefacto.usecase.manuscript.CreateManuscriptUseCase;
import com.kether.storyteller.beforerefacto.usecase.manuscript.DeleteManuscriptUseCase;
import com.kether.storyteller.beforerefacto.usecase.manuscript.FindManuscriptsByStoryUseCase;
import com.kether.storyteller.beforerefacto.usecase.manuscript.UpdateManuscriptUseCase;
import com.kether.storyteller.beforerefacto.usecase.timeline.CreateTimelineEventUseCase;
import com.kether.storyteller.beforerefacto.usecase.timeline.DeleteTimelineEventUseCase;
import com.kether.storyteller.beforerefacto.usecase.timeline.FindTimelineEventsByStoryUseCase;
import com.kether.storyteller.beforerefacto.usecase.timeline.UpdateTimelineEventUseCase;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests.*;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses.*;
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

    @GetMapping public List<TimelineEventResponse> list(@RequestParam Long storyId) { return findByStory.execute(storyId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TimelineEventResponse create(@Valid @RequestBody Requests.TimelineEventCreate req) { return createEvent.execute(req); }
    @PutMapping("/{id}") public TimelineEventResponse update(@PathVariable Long id, @RequestBody TimelineEventUpdate req) { return updateEvent.execute(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { deleteEvent.execute(id); return OkResponse.ok(); }
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
    public List<ManuscriptResponse> list(@RequestParam(required = false) Long storyId) {
        if (storyId == null) return List.of();
        return findByStory.execute(storyId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.ManuscriptResponse create(@Valid @RequestBody ManuscriptCreate req) { return createManuscript.execute(req); }
    @PutMapping("/{id}") public ManuscriptResponse update(@PathVariable Long id, @RequestBody ManuscriptUpdate req) { return updateManuscript.execute(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { deleteManuscript.execute(id); return OkResponse.ok(); }
}
