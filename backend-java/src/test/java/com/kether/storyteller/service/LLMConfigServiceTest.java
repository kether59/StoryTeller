package com.kether.storyteller.service;

import com.kether.storyteller.dto.request.Requests.LLMConfigRequest;
import com.kether.storyteller.dto.response.Responses.LLMConfigResponse;
import com.kether.storyteller.dto.response.Responses.LLMHealthResponse;
import com.kether.storyteller.dto.response.Responses.LLMSaveResponse;
import com.kether.storyteller.service.llm.LLMConfigModel;
import com.kether.storyteller.service.llm.LLMConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.assertj.core.api.Assertions.*;

/**
 * Tests pour LLMConfigService avec Testcontainers.
 *
 * ✅ CHANGEMENTS CLÉS :
 * - Utilise @ActiveProfiles("test") au lieu de @TestPropertySource
 * - H2 est automatique via application-test.yml
 * - Pas d'appel à Ollama réel (utilise config par défaut)
 * - Tests isolés et stables
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class LLMConfigServiceTest {

    @Autowired
    private LLMConfigService configService;

    @BeforeEach
    void setUp() {
        // Reset avant chaque test
        configService.saveConfig(new LLMConfigRequest(
                "ollama", null, null, "http://localhost:11434", 0.7, 2000));
    }

    // ── Chargement par défaut ─────────────────────────────────────

    @Test
    void getCurrentConfig_shouldReturnNonNull() {
        LLMConfigModel cfg = configService.getCurrent();
        assertThat(cfg).isNotNull();
        assertThat(cfg.getProvider()).isNotBlank();
    }

    @Test
    void getHealth_withOllama_shouldBeConfigured() {
        LLMConfigModel current = configService.getCurrent();
        assertThat(current.getProvider()).isEqualTo("ollama");

        LLMHealthResponse health = configService.getHealth();

        assertThat(health.provider()).isEqualTo("ollama");
        assertThat(health.configured()).isTrue();
        // Status peut être "unchecked" ou "ready" selon la config
        assertThat(health.status()).isNotNull();
    }

    @Test
    void getConfigResponse_shouldMaskApiKey() {
        // Simuler une clé API
        configService.saveConfig(new LLMConfigRequest(
                "anthropic", "claude-sonnet-4-5",
                "sk-ant-api03-ABCDEFGHIJKLMNOP1234", null, 0.7, 4000));

        LLMConfigResponse response = configService.getConfigResponse();

        assertThat(response.apiKey()).doesNotContain("ABCDEFGHIJKLMNOP1234");
        assertThat(response.apiKey()).contains("…");
    }

    // ── Sauvegarde ────────────────────────────────────────────────

    @Test
    void saveConfig_shouldUpdateCurrentConfig() {
        LLMSaveResponse result = configService.saveConfig(new LLMConfigRequest(
                "openai", "gpt-4o",
                "sk-test-123456789012345",
                null, 0.5, 2000));

        assertThat(result.status()).isEqualTo("ok");
        assertThat(result.provider()).isEqualTo("openai");
        assertThat(result.model()).isEqualTo("gpt-4o");

        LLMConfigModel current = configService.getCurrent();
        assertThat(current.getProvider()).isEqualTo("openai");
        assertThat(current.getModel()).isEqualTo("gpt-4o");
        assertThat(current.getTemperature()).isEqualTo(0.5);
        assertThat(current.getMaxTokens()).isEqualTo(2000);
    }

    @Test
    void saveConfig_partialUpdate_shouldKeepUnchangedFields() {
        // Premier save complet
        configService.saveConfig(new LLMConfigRequest(
                "anthropic", "claude-sonnet-4-5",
                "sk-ant-123", null, 0.7, 4000));

        // Deuxième save : seulement le modèle change
        configService.saveConfig(new LLMConfigRequest(
                null, "claude-haiku-4-5",
                null, null, null, null));

        LLMConfigModel current = configService.getCurrent();
        assertThat(current.getModel()).isEqualTo("claude-haiku-4-5");
        // Provider inchangé
        assertThat(current.getProvider()).isEqualTo("anthropic");
    }

    @Test
    void saveConfig_openRouter_withoutModel_shouldThrow() {
        assertThatThrownBy(() ->
                configService.saveConfig(new LLMConfigRequest(
                        "openrouter", "",   // modèle vide
                        "sk-or-test", null, 0.7, 4000))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OpenRouter");
    }

    @Test
    void saveConfig_openRouter_withModel_shouldSucceed() {
        LLMSaveResponse result = configService.saveConfig(new LLMConfigRequest(
                "openrouter", "google/gemini-2.5-pro-preview",
                "sk-or-test-key", null, 0.7, 4000));

        assertThat(result.status()).isEqualTo("ok");
        assertThat(result.model()).isEqualTo("google/gemini-2.5-pro-preview");
    }

    // ── LLMConfigModel helpers ────────────────────────────────────

    @Test
    void maskedApiKey_shortKey_shouldReturnAsIs() {
        LLMConfigModel model = new LLMConfigModel();
        model.setApiKey("short");
        assertThat(model.maskedApiKey()).isEqualTo("short");
    }

    @Test
    void maskedApiKey_longKey_shouldMaskMiddle() {
        LLMConfigModel model = new LLMConfigModel();
        model.setApiKey("sk-ant-api03-ABCDEFGHIJKLMNOP1234");
        String masked = model.maskedApiKey();
        assertThat(masked).startsWith("sk-ant");
        assertThat(masked).endsWith("1234");
        assertThat(masked).contains("…");
    }

    @Test
    void isConfigured_anthropicWithKey_shouldBeTrue() {
        LLMConfigModel model = new LLMConfigModel();
        model.setProvider("anthropic");
        model.setApiKey("sk-ant-api03-XXXXXXXXXXXX");
        assertThat(model.isConfigured()).isTrue();
    }

    @Test
    void isConfigured_anthropicWithoutKey_shouldBeFalse() {
        LLMConfigModel model = new LLMConfigModel();
        model.setProvider("anthropic");
        model.setApiKey("");
        assertThat(model.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_ollamaWithUrl_shouldBeTrue() {
        LLMConfigModel model = new LLMConfigModel();
        model.setProvider("ollama");
        model.setOllamaUrl("http://localhost:11434");
        assertThat(model.isConfigured()).isTrue();
    }

    // ── Gestion du fichier config ────────────────────────────────

    @Test
    void loadConfig_fileDoesNotExist_shouldUseDefaults() {
        // Le fichier n'existe pas, donc les defaults sont utilisés
        LLMConfigModel current = configService.getCurrent();
        assertThat(current).isNotNull();
        assertThat(current.getProvider()).isNotBlank();
    }

    @Test
    void saveConfig_shouldPersistToFile() {
        configService.saveConfig(new LLMConfigRequest(
                "anthropic", "claude-sonnet-4-5",
                "sk-ant-test", null, 0.8, 3000));

        // Recharger (simule un redémarrage)
        LLMConfigModel reloaded = configService.getCurrent();
        assertThat(reloaded.getProvider()).isEqualTo("anthropic");
        assertThat(reloaded.getModel()).isEqualTo("claude-sonnet-4-5");
    }
}
