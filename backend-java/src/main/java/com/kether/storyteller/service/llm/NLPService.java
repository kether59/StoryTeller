package com.kether.storyteller.service.llm;

import com.kether.storyteller.dto.response.Responses.NamedEntity;
import jakarta.annotation.PostConstruct;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

/**
 * Service NLP Java – remplace spaCy (Python) par Apache OpenNLP.
 *
 * Équivalences :
 * <pre>
 *   nlp = spacy.load("fr_core_news_md")  →  NLPService (initialisé @PostConstruct)
 *   doc = nlp(text)                       →  NLPResult process(text)
 *   doc.ents                              →  NLPResult.entities()
 *   doc.sents                             →  NLPResult.sentences()
 *   token.pos_  / token.dep_             →  disponible via AnalyzedToken
 * </pre>
 *
 * Modèles OpenNLP à télécharger depuis https://opennlp.sourceforge.net/models-1.5/
 * et placer dans src/main/resources/nlp/ :
 *   - fr-sent.bin  (SentenceDetector)
 *   - fr-token.bin (Tokenizer)
 *   - fr-ner-person.bin   (NER personnes)
 *   - fr-ner-location.bin (NER lieux)
 *
 * En mode dégradé (modèles absents), le service reste opérationnel
 * avec une segmentation basique par ponctuation.
 */
@Service
public class NLPService {

    private static final Logger log = LoggerFactory.getLogger(NLPService.class);

    @Value("${storyteller.nlp.models-path:classpath:nlp/}")
    private String modelsPath;

    private final ResourceLoader resourceLoader;

    private SentenceDetectorME sentenceDetector;
    private TokenizerME        tokenizer;
    private NameFinderME       personFinder;
    private NameFinderME       locationFinder;

    private boolean fullyLoaded = false;

    public NLPService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // ══════════════════════════════════════════════════════════════
    //  Initialisation (équivalent nlp = spacy.load("fr_core_news_md"))
    // ══════════════════════════════════════════════════════════════

    @PostConstruct
    public void init() {
        int loaded = 0;
        loaded += loadSentenceDetector() ? 1 : 0;
        loaded += loadTokenizer()        ? 1 : 0;
        loaded += loadPersonFinder()     ? 1 : 0;
        loaded += loadLocationFinder()   ? 1 : 0;

        fullyLoaded = (loaded == 4);
        log.info("NLP initialisé ({}/4 modèles chargés). Mode : {}",
                loaded, fullyLoaded ? "complet" : "dégradé");
    }

    private boolean loadSentenceDetector() {
        try (InputStream is = openModel("fr-sent.bin")) {
            if (is == null) return false;
            sentenceDetector = new SentenceDetectorME(new SentenceModel(is));
            return true;
        } catch (Exception e) {
            log.warn("Modèle SentenceDetector non disponible : {}", e.getMessage());
            return false;
        }
    }

    private boolean loadTokenizer() {
        try (InputStream is = openModel("fr-token.bin")) {
            if (is == null) return false;
            tokenizer = new TokenizerME(new TokenizerModel(is));
            return true;
        } catch (Exception e) {
            log.warn("Modèle Tokenizer non disponible : {}", e.getMessage());
            return false;
        }
    }

    private boolean loadPersonFinder() {
        try (InputStream is = openModel("fr-ner-person.bin")) {
            if (is == null) return false;
            personFinder = new NameFinderME(new TokenNameFinderModel(is));
            return true;
        } catch (Exception e) {
            log.warn("Modèle NER-Person non disponible : {}", e.getMessage());
            return false;
        }
    }

    private boolean loadLocationFinder() {
        try (InputStream is = openModel("fr-ner-location.bin")) {
            if (is == null) return false;
            locationFinder = new NameFinderME(new TokenNameFinderModel(is));
            return true;
        } catch (Exception e) {
            log.warn("Modèle NER-Location non disponible : {}", e.getMessage());
            return false;
        }
    }

    private InputStream openModel(String filename) {
        try {
            Resource res = resourceLoader.getResource(modelsPath + filename);
            return res.exists() ? res.getInputStream() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAvailable() { return true; }  // toujours disponible (mode dégradé OK)
    public boolean isFullyLoaded() { return fullyLoaded; }

    // ══════════════════════════════════════════════════════════════
    //  Traitement principal (équivalent doc = nlp(text))
    // ══════════════════════════════════════════════════════════════

    /**
     * Analyse un texte et retourne sentences + entités nommées.
     * Équivalent Python : doc = nlp(manuscript.text)
     */
    public NLPResult process(String text) {
        if (text == null || text.isBlank()) {
            return new NLPResult(List.of(), List.of());
        }

        List<String> sentences = splitSentences(text);
        List<NamedEntity> entities = new ArrayList<>();

        for (String sentence : sentences) {
            entities.addAll(extractEntities(sentence, text));
        }

        return new NLPResult(sentences, entities);
    }

    // ── Segmentation en phrases ───────────────────────────────────

    private List<String> splitSentences(String text) {
        if (sentenceDetector != null) {
            return Arrays.asList(sentenceDetector.sentDetect(text));
        }
        // Fallback basique : découpe sur . ! ?
        return Arrays.stream(text.split("(?<=[.!?])\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    // ── Extraction d'entités nommées ──────────────────────────────

    private List<NamedEntity> extractEntities(String sentence, String fullText) {
        List<NamedEntity> result = new ArrayList<>();
        String[] tokens = tokenize(sentence);
        if (tokens.length == 0) return result;

        int offset = fullText.indexOf(sentence);

        result.addAll(findEntities(tokens, sentence, offset, personFinder,   "PER"));
        result.addAll(findEntities(tokens, sentence, offset, locationFinder, "LOC"));

        return result;
    }

    private List<NamedEntity> findEntities(String[] tokens, String sentence,
                                           int offset, NameFinderME finder, String label) {
        if (finder == null) return List.of();
        List<NamedEntity> result = new ArrayList<>();
        Span[] spans = finder.find(tokens);
        for (Span span : spans) {
            String entityText = String.join(" ",
                    Arrays.copyOfRange(tokens, span.getStart(), span.getEnd()));
            int start = offset + sentence.indexOf(entityText);
            int end   = start + entityText.length();
            result.add(new NamedEntity(entityText, label, Math.max(0, start), end, sentence));
        }
        finder.clearAdaptiveData(); // important : évite les biais entre phrases
        return result;
    }

    private String[] tokenize(String sentence) {
        if (tokenizer != null) {
            return tokenizer.tokenize(sentence);
        }
        return sentence.split("\\s+");
    }

    // ══════════════════════════════════════════════════════════════
    //  Recherche de mentions de personnages dans un texte
    //  (équivalent find_mentions_in_doc Python)
    // ══════════════════════════════════════════════════════════════

    public Map<String, Integer> findMentions(String text, List<String> names) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String textLower = text.toLowerCase();
        for (String name : names) {
            if (name == null || name.isBlank()) continue;
            // Recherche exacte par mots entiers (équivalent token matching spaCy)
            int count = countWordOccurrences(textLower, name.toLowerCase());
            if (count > 0) result.put(name, count);
        }
        return result;
    }

    private int countWordOccurrences(String text, String word) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            // Vérifier les limites de mot
            boolean beforeOk = (idx == 0 || !Character.isLetterOrDigit(text.charAt(idx - 1)));
            boolean afterOk  = (idx + word.length() >= text.length()
                    || !Character.isLetterOrDigit(text.charAt(idx + word.length())));
            if (beforeOk && afterOk) count++;
            idx += word.length();
        }
        return count;
    }

    // ══════════════════════════════════════════════════════════════
    //  Record résultat NLP
    // ══════════════════════════════════════════════════════════════

    public record NLPResult(List<String> sentences, List<NamedEntity> entities) {}
}