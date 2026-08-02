package com.kether.storyteller.application.service;

import com.kether.storyteller.application.dto.RewriteCommand;
import com.kether.storyteller.application.dto.RewriteResult;
import com.kether.storyteller.domain.port.in.llm.RewriteUseCase;
import com.kether.storyteller.domain.port.out.llm.LLMGenerationPort;
import com.kether.storyteller.domain.service.PromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RewriteService implements RewriteUseCase {

    private static final Logger log = LoggerFactory.getLogger(RewriteService.class);
    private final LLMGenerationPort llmPort;
    private final PromptBuilder promptBuilder;

    public RewriteService(LLMGenerationPort llmPort, PromptBuilder promptBuilder) {
        this.llmPort = llmPort;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public RewriteResult rewrite(RewriteCommand cmd) {
        log.info("rewrite — instructionLen={}, textLen={}",
                cmd.instruction() != null ? cmd.instruction().length() : 0,
                cmd.text() != null ? cmd.text().length() : 0);

        String systemPrompt = """
            Tu es un écrivain professionnel expert en révision et réécriture de textes.
            Ne jamais ajouter de commentaires ou d'explications.
            La réponse contient uniquement le texte réécrit.
            """;

        String userPrompt = promptBuilder.buildRewriteUserPrompt(cmd.instruction(), cmd.text());

        try {
            String rewritten = llmPort.generate(systemPrompt, userPrompt, 2000);
            return new RewriteResult(true, cmd.text(), rewritten, cmd.instruction());
        } catch (Exception e) {
            log.error("Error during rewrite", e);
            throw new RuntimeException("Erreur lors de la réécriture : " + e.getMessage(), e);
        }
    }
}