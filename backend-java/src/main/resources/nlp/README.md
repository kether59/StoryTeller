# Modèles Apache OpenNLP (fr)

Téléchargez les fichiers suivants depuis https://opennlp.sourceforge.net/models-1.5/
et placez-les dans ce dossier :

| Fichier              | Description                     | Taille |
|----------------------|---------------------------------|--------|
| `fr-sent.bin`        | Détecteur de phrases (fr)       | ~0.7MB |
| `fr-token.bin`       | Tokeniseur (fr)                 | ~1.1MB |
| `fr-ner-person.bin`  | NER : Personnes (fr)            | ~3.5MB |
| `fr-ner-location.bin`| NER : Lieux (fr)                | ~1.8MB |

## Téléchargement automatique (script)

```bash
BASE="https://opennlp.sourceforge.net/models-1.5"
for model in fr-sent fr-token fr-ner-person fr-ner-location; do
  curl -L "${BASE}/${model}.bin" -o "${model}.bin"
done
```

## Mode dégradé

Sans ces modèles, NLPService fonctionne avec :
- Segmentation basique en phrases (sur . ! ?)
- Matching textuel simple pour les mentions de personnages
- Pas d'entités nommées automatiques

Les fonctions suivantes restent pleinement opérationnelles :
- Génération LLM (chapitres, continuation, réécriture)
- Extraction via LLM (personnages, lieux, lore, timeline)
- Analyse IA (liens personnages, conflits chronologiques)
