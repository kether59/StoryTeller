package com.kether.storyteller.service.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class LLMConfigModel {

    private String provider    = "anthropic";
    private String model       = "claude-sonnet-4-5";

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("ollama_url")
    private String ollamaUrl   = "http://localhost:11434";

    private double temperature = 0.7;

    @JsonProperty("max_tokens")
    private int maxTokens      = 4000;

    /* ── Constructeur par défaut requis par Jackson ── */
    public LLMConfigModel() {}

    /* ── Factory method ── */
    public static LLMConfigModel defaults() {
        return new LLMConfigModel();
    }

    /* ── Getters / Setters ── */
    public String getProvider()     { return provider; }
    public String getModel()        { return model; }
    public String getApiKey()       { return apiKey; }
    public String getOllamaUrl()    { return ollamaUrl; }
    public double getTemperature()  { return temperature; }
    public int    getMaxTokens()    { return maxTokens; }

    public void setProvider(String provider)       { this.provider = provider; }
    public void setModel(String model)             { this.model = model; }
    public void setApiKey(String apiKey)           { this.apiKey = apiKey; }
    public void setOllamaUrl(String ollamaUrl)     { this.ollamaUrl = ollamaUrl; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setMaxTokens(int maxTokens)        { this.maxTokens = maxTokens; }

    /** Masque la clé API pour l'affichage (équivalent Python masked = ...). */
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