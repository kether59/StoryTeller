package com.kether.storyteller.domain.port.in.llm;

import com.kether.storyteller.application.dto.ChapterGenerationCommand;
import com.kether.storyteller.application.dto.GeneratedChapterResult;

/**
 * Cas d'usage : Générer un chapitre complet à partir du contexte d'une histoire.
 *
 * FONCTIONNEMENT :
 * - Le controller appelle cette interface
 * - L'implémentation (dans application) orchestre :
 *   1. Récupération des données (persos, lieux, lore) via les Ports OUT
 *   2. Construction du prompt système via PromptBuilder (domaine)
 *   3. Appel au LLM via LLMGenerationPort
 *   4. Retour du texte généré
 *
 * POURQUOI une interface ici ?
 * Le domaine définit CE QU'IL FAUT FAIRE. Comment c'est fait, c'est le problème
 * de la couche Application. Cela permet de tester le domaine sans Spring.
 */
public interface GenerateChapterUseCase {
    GeneratedChapterResult generate(ChapterGenerationCommand command);
}