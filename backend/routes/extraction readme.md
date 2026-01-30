# 🔍 Guide d'utilisation - Extraction automatique

## Vue d'ensemble

L'extraction automatique utilise l'IA pour analyser vos manuscrits et en extraire automatiquement :
- 👥 **Personnages** : noms, rôles, descriptions, personnalités
- 📍 **Lieux** : villes, planètes, bâtiments avec descriptions
- ⏰ **Chronologie** : événements clés avec dates et participants
- 📚 **Lore** : éléments de world-building (magie, technologie, factions...)

## 🎯 Cas d'usage

### 1. Démarrer un nouveau projet depuis un texte existant
Vous avez écrit quelques chapitres à la main et voulez structurer votre histoire.

### 2. Enrichir une histoire existante
Vous avez déjà des personnages mais voulez extraire plus de détails depuis vos chapitres.

### 3. Vérifier la cohérence
Comparez ce que l'IA extrait avec vos fiches existantes pour détecter des incohérences.

---

## 📖 Mode d'emploi

### Étape 1 : Préparer le manuscrit

1. Allez dans l'onglet **"Manuscrit"**
2. Créez ou sélectionnez un chapitre existant
3. Assurez-vous qu'il contient au moins **500 mots** (l'IA a besoin de contexte)

**Conseil :** Plus le texte est long et détaillé, meilleurs seront les résultats.

---

### Étape 2 : Lancer l'extraction

1. Allez dans l'onglet **"🔍 Extraction"**
2. Sélectionnez le manuscrit à analyser
3. Cochez les types d'éléments à extraire :
    - ✅ Personnages
    - ✅ Lieux
    - ✅ Chronologie
    - ✅ Lore
4. Cliquez sur **"🚀 Lancer l'extraction"**
5. Attendez 30-90 secondes (selon la longueur du texte)

---

### Étape 3 : Valider les résultats

L'IA vous présente tous les éléments trouvés. Pour chaque élément :

#### ✅ Éléments pré-approuvés
Les éléments avec **confiance > 70%** sont automatiquement cochés (mais vous pouvez les décocher).

#### 🔧 Modification
Vous pouvez modifier directement :
- Les noms
- Les descriptions
- Les rôles
- Tous les champs

#### ❌ Rejet
Décochez les éléments incorrects ou non pertinents.

---

### Étape 4 : Création des entrées

1. Vérifiez le résumé en haut :
    - 👥 X/Y Personnages
    - 📍 X/Y Lieux
    - ⏰ X/Y Événements
    - 📚 X/Y Lore

2. Cliquez sur **"✅ Valider et créer les entrées"**

3. Le système crée les entrées dans cet ordre :
    - **1er** : Personnages
    - **2e** : Lieux
    - **3e** : Lore
    - **4e** : Chronologie (nécessite les personnages/lieux existants)

4. Un message de confirmation s'affiche avec le nombre d'éléments créés

---

## 💡 Conseils pour de meilleurs résultats

### ✅ Bonnes pratiques

**1. Texte clair et structuré**
```
Mauvais : "Il arriva là et vit le truc"
Bon : "Jean-Marc arriva à Paris et découvrit la Tour Eiffel"
```

**2. Mentions explicites**
```
Mauvais : "Elle était grande"
Bon : "Marie, la protagoniste, était grande et avait les cheveux roux"
```

**3. Contexte pour les lieux**
```
Mauvais : "Dans la ville"
Bon : "À Londres, capitale brumeuse de l'Angleterre victorienne"
```

**4. Dates claires pour la chronologie**
```
Mauvais : "Après quelques jours"
Bon : "Le 15 mars 2024, trois jours après leur première rencontre"
```

### ❌ Pièges à éviter

1. **Texte trop court** : < 500 mots → pas assez de contexte
2. **Trop de pronoms** : "Il", "elle", "lui" sans rappel des noms
3. **Descriptions implicites** : L'IA ne peut pas deviner ce qui n'est pas écrit
4. **Orthographe approximative** : L'IA peut mal interpréter les noms

---

## 🔄 Workflow recommandé

### Pour un nouveau projet

```
1. Écrire 2-3 chapitres (3000-5000 mots minimum)
2. Lancer l'extraction sur le premier chapitre
3. Valider et créer les personnages/lieux principaux
4. Lancer l'extraction sur le deuxième chapitre
5. Vérifier les doublons (l'IA détecte les personnages existants)
6. Compléter manuellement les fiches créées
```

### Pour enrichir un projet existant

```
1. Avoir déjà des personnages/lieux de base
2. Écrire un nouveau chapitre
3. Lancer l'extraction pour trouver de nouveaux éléments
4. Comparer avec vos fiches existantes
5. Ajouter uniquement les nouveaux éléments ou détails manquants
```

---

## 🎨 Exemples de résultats

### Exemple 1 : Personnage détecté

**Texte original :**
> "Elara Vancian, une jeune marchande de 25 ans aux cheveux châtains, était connue pour son caractère sceptique. Malgré sa loyauté, elle craignait profondément l'autorité de l'Ordre Immuable."

**Extraction :**
```json
{
  "name": "Vancian",
  "surname": "Elara",
  "role": "Protagoniste",
  "age": 25,
  "physical_description": "Cheveux châtains",
  "personality": "Sceptique, loyale, craint l'autorité",
  "confidence": 0.95
}
```

### Exemple 2 : Lieu détecté

**Texte original :**
> "Le Bazar des Échos, un marché souterrain illégal niché sous les rues d'Aethel, était le lieu de prédilection des marchands de souvenirs."

**Extraction :**
```json
{
  "name": "Le Bazar des Échos",
  "type": "Marché Noir",
  "summary": "Marché souterrain illégal sous Aethel où l'on vend des souvenirs",
  "confidence": 0.88
}
```

### Exemple 3 : Événement chronologique

**Texte original :**
> "Le 3 janvier 2024, Elara et Kellan s'affrontèrent dans les rues brumeuses d'Aethel. Cette course-poursuite changea le destin de la cité."

**Extraction :**
```json
{
  "title": "Course-poursuite à Aethel",
  "date": "2024-01-03",
  "summary": "Affrontement entre Elara et Kellan qui changea le destin de la cité",
  "character_names": ["Elara", "Kellan"],
  "location_name": "Aethel",
  "sort_order": 2,
  "confidence": 0.92
}
```

---

## 🐛 Dépannage

### "Manuscrit trop court pour être analysé"
→ Votre texte fait moins de 100 caractères. Écrivez au moins 500 mots.

### "Aucun personnage trouvé"
→ L'IA n'a pas détecté de noms clairs. Utilisez des noms propres explicites.

### "Confiance très faible (< 0.5)"
→ L'IA n'est pas sûre. Vérifiez attentivement avant de valider.

### "Doublon détecté"
→ Un élément similaire existe déjà. Normal, l'IA ne créera pas de doublon.

### "Erreur lors de l'extraction"
→ Vérifiez que votre clé API LLM est configurée dans `.env`

---

## ⚙️ Configuration avancée

### Ajuster la sensibilité

Dans `routes/extraction.py`, vous pouvez modifier les prompts pour :

```python
# Plus strict (moins d'éléments, plus de confiance)
system_prompt = """Tu es très strict. 
N'extrait que les éléments dont tu es ABSOLUMENT certain."""

# Plus permissif (plus d'éléments, confiance variable)
system_prompt = """Tu es permissif. 
Extrait même les éléments dont tu n'es pas totalement certain."""
```

### Limiter la longueur analysée

```python
# Dans analyze_manuscript()
manuscript.text[:8000]  # Analyser seulement 8000 caractères
manuscript.text[:20000] # Analyser 20000 caractères
```

**Note :** Plus de texte = meilleure qualité mais coût API plus élevé et temps plus long.

---

## 💰 Coût estimé

**Avec Anthropic Claude :**
- Extraction de 5000 mots : ~$0.10-0.20
- Extraction de 10000 mots : ~$0.20-0.40

**Avec OpenAI GPT-4 :**
- Similaire ou légèrement plus cher

**Avec Ollama (local) :**
- Gratuit, mais plus lent sans GPU

---

## 🚀 Fonctionnalités futures

- [ ] Extraction depuis PDF/DOCX
- [ ] Détection automatique des relations entre personnages
- [ ] Génération d'une frise chronologique visuelle
- [ ] Export des fiches en PDF
- [ ] Détection de personnages secondaires vs principaux
- [ ] Analyse de l'arc narratif des personnages

---

## 📞 Questions fréquentes

**Q : Puis-je extraire depuis plusieurs chapitres à la fois ?**
R : Actuellement non, mais vous pouvez lancer l'extraction plusieurs fois. Les doublons sont automatiquement détectés.

**Q : L'extraction modifie-t-elle mes manuscrits ?**
R : Non, l'extraction est en lecture seule. Vos textes ne sont jamais modifiés.

**Q : Puis-je annuler la création après validation ?**
R : Non. Une fois créés, vous devrez supprimer manuellement les éléments indésirables.

**Q : L'IA peut-elle extraire des dialogues ?**
R : Elle détecte les personnages mentionnés dans les dialogues, mais n'extrait pas les dialogues eux-mêmes (ils restent dans le manuscrit).

**Q : Comment améliorer la détection ?**
R : Soyez explicite dans votre écriture. Mentionnez régulièrement les noms complets, décrivez clairement les lieux, et indiquez les dates.

---

**Bonne extraction ! 🔍✨**