package com.kether.storyteller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire d'exceptions global.
 * Renvoie des réponses JSON cohérentes (format RFC 9457 ProblemDetail).
 *
 * Équivalents FastAPI :
 *   404  → ResourceNotFoundException
 *   422  → MethodArgumentNotValidException / IllegalArgumentException
 *   503  → ServiceUnavailableException
 *   500  → Exception générique
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ── 404 Not Found ────────────────────────────────── */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("/errors/not-found"));
        return pd;
    }

    /* ── 422 Validation ───────────────────────────────── */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalide"
                ));

        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,
                "Erreur de validation");
        pd.setType(URI.create("/errors/validation"));
        pd.setProperty("fields", errors);
        return pd;
    }

    /* ── 422 Argument illégal ────────────────────────── */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("/errors/invalid-argument"));
        return pd;
    }

    /* ── 503 Service indisponible ─────────────────────── */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ProblemDetail handleServiceUnavailable(ServiceUnavailableException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        pd.setType(URI.create("/errors/service-unavailable"));
        return pd;
    }

    /* ── 500 Erreur générique ─────────────────────────── */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne : " + ex.getMessage());
        pd.setType(URI.create("/errors/internal"));
        return pd;
    }
}
