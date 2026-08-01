package com.kether.storyteller.controller;

import com.kether.storyteller.application.usecase.*;
import com.kether.storyteller.dto.request.Requests.*;
import com.kether.storyteller.dto.response.Responses.*;
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

    @GetMapping public List<StoryResponse> list() { return findAllStories.execute(); }
    @GetMapping("/{id}") public StoryResponse get(@PathVariable Long id) { return findStoryById.execute(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public StoryResponse create(@Valid @RequestBody StoryCreate req) { return createStory.execute(req); }
    @PutMapping("/{id}") public StoryResponse update(@PathVariable Long id, @RequestBody StoryUpdate req) { return updateStory.execute(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { deleteStory.execute(id); return OkResponse.ok(); }
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

    @GetMapping public List<CharacterResponse> list(@RequestParam Long storyId) { return findByStory.execute(storyId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CharacterResponse create(@Valid @RequestBody CharacterCreate req) { return createCharacter.execute(req); }
    @PutMapping("/{id}") public CharacterResponse update(@PathVariable Long id, @RequestBody CharacterUpdate req) { return updateCharacter.execute(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { deleteCharacter.execute(id); return OkResponse.ok(); }
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

    @GetMapping public List<LocationResponse> list(@RequestParam Long storyId) { return findByStory.execute(storyId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@Valid @RequestBody LocationCreate req) { return createLocation.execute(req); }
    @PutMapping("/{id}") public LocationResponse update(@PathVariable Long id, @RequestBody LocationUpdate req) { return updateLocation.execute(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { deleteLocation.execute(id); return OkResponse.ok(); }
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

    @GetMapping public List<LoreEntryResponse> list(@RequestParam Long storyId) { return findByStory.execute(storyId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LoreEntryResponse create(@Valid @RequestBody LoreEntryCreate req) { return createLore.execute(req); }
    @PutMapping("/{id}") public LoreEntryResponse update(@PathVariable Long id, @RequestBody LoreEntryUpdate req) { return updateLore.execute(id, req); }
    @DeleteMapping("/{id}") public OkResponse delete(@PathVariable Long id) { deleteLore.execute(id); return OkResponse.ok(); }
}
