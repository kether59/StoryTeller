package com.kether.storyteller.controller;

import com.kether.storyteller.beforerefacto.usecase.character.*;
import com.kether.storyteller.beforerefacto.usecase.location.*;
import com.kether.storyteller.beforerefacto.usecase.lore.*;
import com.kether.storyteller.beforerefacto.usecase.story.*;
import com.kether.storyteller.infrastructure.web.rest.dto.Requests;
import com.kether.storyteller.infrastructure.web.rest.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/stories")
class StoryController {
    private final CreateStoryUseCase createStory;
    private final FindAllStoriesUseCase findAllStories;
    private final FindStoryByIdUseCase findStoryById;
    private final UpdateStoryUseCase updateStory;
    private final DeleteStoryUseCase deleteStory;

    StoryController(CreateStoryUseCase createStory, FindAllStoriesUseCase findAllStories,
                    FindStoryByIdUseCase findStoryById, UpdateStoryUseCase updateStory,
                    DeleteStoryUseCase deleteStory) {
        this.createStory = createStory;
        this.findAllStories = findAllStories;
        this.findStoryById = findStoryById;
        this.updateStory = updateStory;
        this.deleteStory = deleteStory;
    }

    @GetMapping
    public List<Responses.StoryResponse> list() {
        return findAllStories.execute().stream().map(Responses.StoryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public Responses.StoryResponse get(@PathVariable Long id) {
        return Responses.StoryResponse.from(findStoryById.execute(id));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.StoryResponse create(@Valid @RequestBody Requests.StoryCreate req) {
        return Responses.StoryResponse.from(createStory.execute(req.title(), req.synopsis(), req.blurb()));
    }

    @PutMapping("/{id}")
    public Responses.StoryResponse update(@PathVariable Long id, @RequestBody Requests.StoryUpdate req) {
        return Responses.StoryResponse.from(updateStory.execute(id, req.title(), req.synopsis(), req.blurb()));
    }

    @DeleteMapping("/{id}")
    public Responses.OkResponse delete(@PathVariable Long id) {
        deleteStory.execute(id);
        return Responses.OkResponse.ok();
    }
}

@RestController @RequestMapping("/api/characters")
class CharacterController {
    private final CreateCharacterUseCase createCharacter;
    private final FindCharactersByStoryUseCase findByStory;
    private final UpdateCharacterUseCase updateCharacter;
    private final DeleteCharacterUseCase deleteCharacter;

    CharacterController(CreateCharacterUseCase createCharacter, FindCharactersByStoryUseCase findByStory,
                        UpdateCharacterUseCase updateCharacter, DeleteCharacterUseCase deleteCharacter) {
        this.createCharacter = createCharacter;
        this.findByStory = findByStory;
        this.updateCharacter = updateCharacter;
        this.deleteCharacter = deleteCharacter;
    }

    @GetMapping
    public List<Responses.CharacterResponse> list(@RequestParam Long storyId) {
        return findByStory.execute(storyId).stream().map(Responses.CharacterResponse::from).toList();
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.CharacterResponse create(@Valid @RequestBody Requests.CharacterCreate req) {
        return Responses.CharacterResponse.from(
                createCharacter.execute(req.storyId(), req.name(), req.role(), req.personality()));
    }

    @PutMapping("/{id}")
    public Responses.CharacterResponse update(@PathVariable Long id, @RequestBody Requests.CharacterUpdate req) {
        return Responses.CharacterResponse.from(updateCharacter.execute(
                id, req.name(), req.role(), req.personality(), req.physicalDescription(), req.age(),
                req.motivation(), req.goal(), req.flaw(), req.characterArc(), req.skills(),
                req.notes(), req.surname(), req.born(), req.history()));
    }

    @DeleteMapping("/{id}")
    public Responses.OkResponse delete(@PathVariable Long id) {
        deleteCharacter.execute(id);
        return Responses.OkResponse.ok();
    }
}

@RestController @RequestMapping("/api/locations")
class LocationController {
    private final CreateLocationUseCase createLocation;
    private final FindLocationsByStoryUseCase findByStory;
    private final UpdateLocationUseCase updateLocation;
    private final DeleteLocationUseCase deleteLocation;

    LocationController(CreateLocationUseCase createLocation, FindLocationsByStoryUseCase findByStory,
                       UpdateLocationUseCase updateLocation, DeleteLocationUseCase deleteLocation) {
        this.createLocation = createLocation;
        this.findByStory = findByStory;
        this.updateLocation = updateLocation;
        this.deleteLocation = deleteLocation;
    }

    @GetMapping
    public List<Responses.LocationResponse> list(@RequestParam Long storyId) {
        return findByStory.execute(storyId).stream().map(Responses.LocationResponse::from).toList();
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.LocationResponse create(@Valid @RequestBody Requests.LocationCreate req) {
        return Responses.LocationResponse.from(
                createLocation.execute(req.storyId(), req.name(), req.type(), req.summary()));
    }

    @PutMapping("/{id}")
    public Responses.LocationResponse update(@PathVariable Long id, @RequestBody Requests.LocationUpdate req) {
        return Responses.LocationResponse.from(
                updateLocation.execute(id, req.name(), req.type(), req.summary()));
    }

    @DeleteMapping("/{id}")
    public Responses.OkResponse delete(@PathVariable Long id) {
        deleteLocation.execute(id);
        return Responses.OkResponse.ok();
    }
}

@RestController @RequestMapping("/api/lore")
class LoreController {
    private final CreateLoreEntryUseCase createLore;
    private final FindLoreEntriesByStoryUseCase findByStory;
    private final UpdateLoreEntryUseCase updateLore;
    private final DeleteLoreEntryUseCase deleteLore;

    LoreController(CreateLoreEntryUseCase createLore, FindLoreEntriesByStoryUseCase findByStory,
                   UpdateLoreEntryUseCase updateLore, DeleteLoreEntryUseCase deleteLore) {
        this.createLore = createLore;
        this.findByStory = findByStory;
        this.updateLore = updateLore;
        this.deleteLore = deleteLore;
    }

    @GetMapping
    public List<Responses.LoreEntryResponse> list(@RequestParam Long storyId) {
        return findByStory.execute(storyId).stream().map(Responses.LoreEntryResponse::from).toList();
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Responses.LoreEntryResponse create(@Valid @RequestBody Requests.LoreEntryCreate req) {
        return Responses.LoreEntryResponse.from(
                createLore.execute(req.storyId(), req.title(), req.category(), req.content()));
    }

    @PutMapping("/{id}")
    public Responses.LoreEntryResponse update(@PathVariable Long id, @RequestBody Requests.LoreEntryUpdate req) {
        return Responses.LoreEntryResponse.from(
                updateLore.execute(id, req.title(), req.category(), req.content()));
    }

    @DeleteMapping("/{id}")
    public Responses.OkResponse delete(@PathVariable Long id) {
        deleteLore.execute(id);
        return Responses.OkResponse.ok();
    }
}