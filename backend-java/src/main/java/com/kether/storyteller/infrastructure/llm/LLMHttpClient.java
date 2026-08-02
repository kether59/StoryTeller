package com.kether.storyteller.infrastructure.llm;

import com.kether.storyteller.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Client HTTP partagé pour tous les providers LLM.
 *
 * FONCTIONNEMENT :
 * - Centralise la logique HTTP (POST, GET, timeouts, headers)
 * - Utilise java.net.http.HttpClient (Java 11+)
 * - Gère les erreurs HTTP (>= 400 lance ServiceUnavailableException)
 * - Distingue les services locaux (localhost) des services distants
 *   pour éviter les problèmes de CORS/preflight
 *
 * AVANT : C'était des méthodes statiques dans LLMProviders (non testables,
 * non injectables). Maintenant c'est un bean Spring propre.
 */
@Component
public class LLMHttpClient {

    private static final Logger log = LoggerFactory.getLogger(LLMHttpClient.class);

    private final HttpClient httpClient;
    private final HttpClient httpLocalClient;

    public LLMHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.httpLocalClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public String postJson(String url, Map<String, String> headers, String body) throws Exception {
        log.debug("POST JSON to {}", url);

        var reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(300))
                .header("User-Agent", "StoryTeller/2.0 (Java)")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        headers.forEach(reqBuilder::header);

        HttpClient client = isLocal(url) ? httpLocalClient : httpClient;
        HttpResponse<String> response = client.send(
                reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ServiceUnavailableException(
                    "Erreur HTTP " + response.statusCode() + " : " + response.body());
        }
        return response.body();
    }

    public String getJson(String url, Map<String, String> headers, int timeoutSeconds) throws Exception {
        log.debug("GET JSON from {} (timeout={}s)", url, timeoutSeconds);

        var reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(Math.max(10, timeoutSeconds)))
                .header("User-Agent", "StoryTeller/2.0 (Java)")
                .header("Accept", "application/json")
                .GET();

        headers.forEach(reqBuilder::header);

        HttpResponse<String> response = httpLocalClient.send(
                reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ServiceUnavailableException(
                    "Erreur HTTP " + response.statusCode() + " : " + response.body());
        }
        return response.body();
    }

    private boolean isLocal(String url) {
        return url.contains("localhost") || url.contains("127.0.0.1");
    }
}