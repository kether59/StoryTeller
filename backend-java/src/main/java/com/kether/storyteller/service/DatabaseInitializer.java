package com.kether.storyteller.service;

import com.kether.storyteller.infrastructure.persistence.entity.*;
import com.kether.storyteller.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initialise la base de données avec des données de seed au démarrage.
 * Équivalent Java du init_db.py Python.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final StoryRepository storyRepository;
    private final CharacterRepository characterRepository;
    private final LocationRepository locationRepository;
    private final LoreEntryRepository loreEntryRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final ManuscriptRepository manuscriptRepository;

    /**
     * Initialise la base de données avec des données de seed si elle est vide.
     */
    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            log.info("════════════════════════════════════════════════════════════");
            log.info("🚀 INITIALISATION DE LA BASE DE DONNÉES STORYTELLER");
            log.info("════════════════════════════════════════════════════════════");

            // Vérifier si la base de données est déjà initialisée
            if (storyRepository.count() > 0) {
                log.info("✅ Base de données déjà initialisée. Skipping seed.");
                return;
            }

            try {
                seedDatabase();
                log.info("════════════════════════════════════════════════════════════");
                log.info("🎉 SUCCÈS!");
                log.info("════════════════════════════════════════════════════════════");
                log.info("📊 Base de données initialisée avec:");
                log.info("   • 1 histoire");
                log.info("   • 2 personnages");
                log.info("   • 2 lieux");
                log.info("   • 2 entrées de lore");
                log.info("   • 2 événements");
                log.info("   • 1 manuscrit");
                log.info("════════════════════════════════════════════════════════════");

            } catch (Exception e) {
                log.error("❌ ERREUR lors de l'initialisation de la base de données", e);
            }
        };
    }

    /**
     * Remplit la base de données avec des données de départ.
     */
    private void seedDatabase() {
        log.info("\n🧹 Création des tables...");

        // 1. Créer l'histoire principale
        log.info("📖 Création de l'histoire 'Le Sceptre des Échos'...");
        Story story = Story.builder()
                .title("Le Sceptre des Échos")
                .synopsis("Dans un monde où la mémoire est monnaie d'échange, Elara découvre un sceptre ancien.")
                .blurb("Une quête pour la vérité dans l'ombre du passé.")
                .build();
        story = storyRepository.save(story);
        log.info("✅ Histoire créée (ID: {})", story.getId());

        // 2. Créer les personnages
        log.info("\n👥 Création des personnages...");
        StoryCharacter elara = StoryCharacter.builder()
                .story(story)
                .name("Vancian")
                .surname("Elara")
                .role("Protagoniste")
                .age(25)
                .born("1024-06-27")
                .physicalDescription("Petite, agile, cheveux châtains")
                .personality("Sceptique, débrouillarde")
                .build();

        StoryCharacter kellan = StoryCharacter.builder()
                .story(story)
                .name("Fogg")
                .surname("Kellan")
                .role("Antagoniste")
                .age(35)
                .born("1014-03-14")
                .physicalDescription("Grand, froid, armure noire")
                .personality("Cruel, méthodique")
                .build();

        elara = characterRepository.save(elara);
        kellan = characterRepository.save(kellan);
        log.info("✅ 2 personnages créés");

        // 3. Créer les lieux
        log.info("\n📍 Création des lieux...");
        StoryLocation aethel = StoryLocation.builder()
                .story(story)
                .name("Aethel, Cité de la Brume")
                .type("Capitale")
                .summary("Capitale baignée de brume")
                .build();

        StoryLocation bazar = StoryLocation.builder()
                .story(story)
                .name("Le Bazar des Échos")
                .type("Marché Noir")
                .summary("Marché souterrain illégal")
                .build();

        aethel = locationRepository.save(aethel);
        bazar = locationRepository.save(bazar);
        log.info("✅ 2 lieux créés");

        // 4. Créer les entrées de lore
        log.info("\n📚 Création des entrées de lore...");
        LoreEntry lore1 = LoreEntry.builder()
                .story(story)
                .title("Le Système d'Écho")
                .category("Magie")
                .content("La magie des échos cristallisés qui permettent de stocker et d'échanger des souvenirs.")
                .build();

        LoreEntry lore2 = LoreEntry.builder()
                .story(story)
                .title("L'Ordre Immuable")
                .category("Faction")
                .content("Gouvernement dictatorial qui contrôle le flux des échos et des souvenirs.")
                .build();

        lore1 = loreEntryRepository.save(lore1);
        lore2 = loreEntryRepository.save(lore2);
        log.info("✅ 2 entrées de lore créées");

        // 5. Créer les événements de la chronologie
        log.info("\n⏰ Création des événements...");
        TimelineEvent event1 = TimelineEvent.builder()
                .story(story)
                .title("Découverte du Sceptre")
                .date("2024-01-01")
                .sortOrder(100)
                .summary("Elara découvre le sceptre ancien dans le Bazar des Échos")
                .location(bazar)
                .characters(Set.of(elara))
                .build();

        TimelineEvent event2 = TimelineEvent.builder()
                .story(story)
                .title("Course-poursuite")
                .date("2024-01-03")
                .sortOrder(120)
                .summary("Confrontation épique avec Kellan à travers les rues d'Aethel")
                .location(aethel)
                .characters(Set.of(elara, kellan))
                .build();

        event1 = timelineEventRepository.save(event1);
        event2 = timelineEventRepository.save(event2);
        log.info("✅ 2 événements créés et liés");

        // 6. Créer le manuscrit
        log.info("\n📝 Création du manuscrit...");
        Manuscript manuscript = Manuscript.builder()
                .story(story)
                .title("Écho et Argent")
                .chapter(1)
                .text("Le marché sentait le vieux cuir et la poudre d'échos cristallisés. Elara avança prudemment " +
                        "entre les étals, ses yeux gris balayant les ombres. Quelque part ici, dans ce dédale de contrebande, " +
                        "se trouvait le Sceptre dont tout le monde chuchotait.")
                .status("Premier jet")
                .build();

        manuscript = manuscriptRepository.save(manuscript);
        log.info("✅ Manuscrit créé");

        log.info("\n🚀 Lancez le serveur et accédez à http://localhost:8000");
    }
}