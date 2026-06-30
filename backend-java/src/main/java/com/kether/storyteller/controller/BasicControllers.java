package com.kether.storyteller.controller;

import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
import com.kether.storyteller.service.StoryService;
import com.kether.storyteller.service.CharacterService;
import com.kether.storyteller.service.LocationService;
import com.kether.storyteller.service.LoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/stories")
class StoryController {
    private final StoryService svc;
    StoryController(StoryService svc) { this.svc = svc; }
    @GetMapping public List<StoryResponse> list() { return svc.findAll(); }
    @GetMapping("/{id}") public StoryResponse get(@PathVariable Long id) { return svc.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public StoryResponse create(@Valid @RequestBody StoryCreate req) { return svc.create(req); }
    @PutMapping("/{id}") public StoryResponse update(@PathVariable Long id, @RequestBody StoryUpdate req) { return svc.update(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { svc.delete(id); return OkResponse.ok(); }
}

@RestController @RequestMapping("/api/characters")
class CharacterController {
    private final CharacterService svc;
    CharacterController(CharacterService svc) { this.svc = svc; }
    @GetMapping public List<CharacterResponse> list(@RequestParam Long storyId) { return svc.findByStory(storyId); }
    @GetMapping("/{id}") public CharacterResponse get(@PathVariable Long id) { return svc.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CharacterResponse create(@Valid @RequestBody CharacterCreate req) { return svc.create(req); }
    @PutMapping("/{id}") public CharacterResponse update(@PathVariable Long id, @RequestBody CharacterUpdate req) { return svc.update(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { svc.delete(id); return OkResponse.ok(); }
}

@RestController @RequestMapping("/api/locations")
class LocationController {
    private final LocationService svc;
    LocationController(LocationService svc) { this.svc = svc; }
    @GetMapping public List<LocationResponse> list(@RequestParam Long storyId) { return svc.findByStory(storyId); }
    @GetMapping("/{id}") public LocationResponse get(@PathVariable Long id) { return svc.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@Valid @RequestBody LocationCreate req) { return svc.create(req); }
    @PutMapping("/{id}") public LocationResponse update(@PathVariable Long id, @RequestBody LocationUpdate req) { return svc.update(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { svc.delete(id); return OkResponse.ok(); }
}

@RestController @RequestMapping("/api/lore")
class LoreController {
    private final LoreService svc;
    LoreController(LoreService svc) { this.svc = svc; }
    @GetMapping public List<LoreEntryResponse> list(@RequestParam Long storyId) { return svc.findByStory(storyId); }
    @GetMapping("/{id}") public LoreEntryResponse get(@PathVariable Long id) { return svc.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LoreEntryResponse create(@Valid @RequestBody LoreEntryCreate req) { return svc.create(req); }
    @PutMapping("/{id}") public LoreEntryResponse update(@PathVariable Long id, @RequestBody LoreEntryUpdate req) { return svc.update(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { svc.delete(id); return OkResponse.ok(); }
}
