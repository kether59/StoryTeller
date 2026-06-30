package com.kether.storyteller.controller;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.service.TimelineService;
import com.kether.storyteller.service.ManuscriptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/timeline")
class TimelineController {
    private final TimelineService svc;
    TimelineController(TimelineService svc) { this.svc = svc; }
    @GetMapping public List<TimelineEventResponse> list(@RequestParam Long storyId) { return svc.findByStory(storyId); }
    @GetMapping("/{id}") public TimelineEventResponse get(@PathVariable Long id) { return svc.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TimelineEventResponse create(@Valid @RequestBody TimelineEventCreate req) { return svc.create(req); }
    @PutMapping("/{id}") public TimelineEventResponse update(@PathVariable Long id, @RequestBody TimelineEventUpdate req) { return svc.update(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { svc.delete(id); return OkResponse.ok(); }
}

@RestController @RequestMapping("/api/manuscript")
class ManuscriptController {
    private final ManuscriptService svc;
    ManuscriptController(ManuscriptService svc) { this.svc = svc; }
    @GetMapping
    public List<ManuscriptResponse> list(@RequestParam(required = false) Long storyId) {
        if (storyId == null) return List.of();
        return svc.findByStory(storyId);
    }
    @GetMapping("/{id}") public ManuscriptResponse get(@PathVariable Long id) { return svc.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ManuscriptResponse create(@Valid @RequestBody ManuscriptCreate req) { return svc.create(req); }
    @PutMapping("/{id}") public ManuscriptResponse update(@PathVariable Long id, @RequestBody ManuscriptUpdate req) { return svc.update(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { svc.delete(id); return OkResponse.ok(); }
    @GetMapping("/{id}/analyze")
    public ManuscriptAnalysis analyze(@PathVariable Long id,
                                      @RequestParam(defaultValue = "fast") String mode) {
        return svc.analyze(id, mode);
    }
}
