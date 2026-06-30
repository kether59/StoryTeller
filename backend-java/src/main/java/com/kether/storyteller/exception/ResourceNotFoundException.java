package com.kether.storyteller.exception;

/**
 * Levée quand une ressource n'existe pas en base.
 * Équivalent : raise HTTPException(404, "...") dans FastAPI.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Long id) {
        return new ResourceNotFoundException(resource + " introuvable (id=" + id + ")");
    }
}
