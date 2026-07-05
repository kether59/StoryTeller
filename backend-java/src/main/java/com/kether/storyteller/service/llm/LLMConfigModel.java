package com.kether.storyteller.service.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Modèle de configuration LLM persisté dans llm_config.json.
 * Miroir exact du modèle Pydantic Python :
 * <pre>
 * class LLMConfig(BaseModel):
 *     provider: Literal["anthropic","openai","openrouter","ollama"] = "anthropic"
 *     model: str = "claude-sonnet-4-5"
 *     api_key: Optional[str] = None
 *     ollama_url: str = "http://localhost:11434"
 *     temperature: float = 0.7
 *     max_tokens: int = 4000
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class LLMConfigModel {

    /* ── Getters / Setters ── */
    private String provider    = "anthropic";
    private String model       = "claude-sonnet-4-5";

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("ollama_url")
    private String ollamaUrl   = "http://localhost:11434";

    private double temperature = 0.7;

    @JsonProperty("max_tokens")
    private int maxTokens      = 4000;

    @JsonProperty("gemini_api_key")
    private String geminiApiKey;

    @JsonProperty("lmstudio_url")
    private String lmstudioUrl = "http://localhost:1234";

    @JsonProperty("llamacpp_url")
    private String llamacppUrl = "http://127.0.0.1:8080";


    /* ── Factory method ── */
    public static LLMConfigModel defaults() {
        return new LLMConfigModel();
    }

    public String maskedApiKey() {
        if (apiKey == null || apiKey.length() <= 10) return apiKey;
        return apiKey.substring(0, 6) + "…" + apiKey.substring(apiKey.length() - 4);
    }

    public boolean isConfigured() {
        if ("ollama".equals(provider)) {
            return ollamaUrl != null && !ollamaUrl.isBlank();
        }
        return apiKey != null && apiKey.length() > 8;
    }
}