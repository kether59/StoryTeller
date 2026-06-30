package com.kether.storyteller.exception;

/** Équivalent : raise HTTPException(503, "Module non installé / service down") */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
