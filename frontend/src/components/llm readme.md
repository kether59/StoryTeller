# 🤖 Configuration de l'Assistant d'Écriture LLM

## Vue d'ensemble

L'assistant d'écriture utilise des LLM (Large Language Models) pour vous aider à :
- ✍️ Générer des chapitres complets
- ➡️ Continuer l'écriture de manuscrits existants
- 🔄 Réécrire et améliorer des textes
- 💡 Suggérer des idées de scènes

## Providers supportés

### 1. Anthropic Claude (Recommandé) ⭐

Claude est excellent pour l'écriture créative et respecte bien les contextes longs.

#### Installation

```bash
pip install anthropic
```

#### Configuration

Créez un fichier `.env` dans le dossier `backend/` :

```env
LLM_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-api03-xxxxxxxxxxxx
```

**Obtenir une clé API :**
1. Allez sur https://console.anthropic.com/
2. Créez un compte
3. Allez dans "API Keys"
4. Créez une nouvelle clé
5. Copiez-la dans votre `.env`

**Prix :** ~$3-8 pour 1 million de tokens d'entrée, ~$15-24 pour 1 million de tokens de sortie

---

### 2. OpenAI GPT-4

Alternative populaire avec GPT-4.

#### Installation

```bash
pip install openai
```

#### Configuration

```env
LLM_PROVIDER=openai
OPENAI_API_KEY=sk-xxxxxxxxxxxx
```

**Obtenir une clé API :**
1. Allez sur https://platform.openai.com/
2. Créez un compte
3. Ajoutez un moyen de paiement
4. Créez une clé API
5. Copiez-la dans votre `.env`

**Prix :** Varie selon le modèle (GPT-4 Turbo ~$10/1M tokens entrée, $30/1M tokens sortie)

---

### 3. Ollama (Gratuit, Local) 🆓

Pour exécuter des modèles localement sans coût ni limite.

#### Installation

**Sur Windows :**
1. Téléchargez Ollama : https://ollama.com/download
2. Installez et lancez l'application

**Sur Linux/Mac :**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### Télécharger un modèle

```bash
# Modèles français recommandés
ollama pull mistral              # 7B - Rapide et bon
ollama pull mixtral              # 8x7B - Plus puissant
ollama pull llama3               # Alternative
```

#### Configuration

```env
LLM_PROVIDER=ollama
OLLAMA_URL=http://localhost:11434
```

**Avantages :**
- ✅ Gratuit
- ✅ Données privées (tout est local)
- ✅ Pas de limite d'utilisation

**Inconvénients :**
- ⚠️ Nécessite un GPU pour être rapide
- ⚠️ Qualité légèrement inférieure aux modèles cloud

---

## 📦 Installation des dépendances

Mettez à jour votre `requirements.txt` :

```txt
# LLM Providers (choisir selon vos besoins)
anthropic>=0.18.0  # Pour Claude
openai>=1.10.0     # Pour OpenAI
httpx>=0.26.0      # Pour Ollama

# Existant
fastapi>=0.104.1
uvicorn[standard]>=0.24.0
sqlalchemy>=2.0.23
pydantic>=2.5.0
pydantic-settings>=2.1.0
spacy>=3.7.0
```

Puis installez :

```bash
pip install -r requirements.txt
```

---

## 🚀 Lancement

### 1. Avec variables d'environnement

```bash
# Dans backend/
export ANTHROPIC_API_KEY="sk-ant-api03-xxxxx"
export LLM_PROVIDER="anthropic"

uvicorn main:app --reload
```

### 2. Avec fichier .env

Créez `backend/.env` :

```env
LLM_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-api03-xxxxxxxxxxxxx

# Ou pour OpenAI
# LLM_PROVIDER=openai
# OPENAI_API_KEY=sk-xxxxxxxxxxxxx

# Ou pour Ollama
# LLM_PROVIDER=ollama
# OLLAMA_URL=http://localhost:11434
```

Puis lancez normalement :

```bash
uvicorn main:app --reload
```

---

## 🧪 Test de configuration

### 1. Via l'API

```bash
curl http://localhost:8000/api/llm/health
```

Réponse attendue :
```json
{
  "provider": "anthropic",
  "configured": true
}
```

### 2. Via l'interface

1. Ouvrez http://localhost:5173
2. Allez dans l'onglet "✍️ Assistant d'écriture"
3. Vous devriez voir : "🤖 LLM: anthropic ✅ Configuré"

---

## 💡 Utilisation

### Générer un chapitre complet

1. Onglet "✍️ Assistant d'écriture"
2. Mode "📝 Générer un chapitre"
3. Remplissez :
    - Résumé de ce qui doit se passer
    - Style, longueur, ton
    - Sélectionnez personnages et lieux
4. Cliquez sur "✨ Générer le chapitre"
5. Attendez 20-60 secondes
6. Sauvegardez le résultat ou copiez-le

### Continuer l'écriture

1. Mode "➡️ Continuer l'écriture"
2. Sélectionnez un manuscrit existant
3. Indiquez la direction souhaitée
4. Cliquez sur "➡️ Continuer l'écriture"

### Réécrire un texte

1. Mode "🔄 Réécrire un texte"
2. Collez votre texte
3. Donnez des instructions (ex: "Rendre plus descriptif")
4. Comparez l'original et la version réécrite

### Suggestions de scènes

1. Mode "💡 Suggérer une scène"
2. Décrivez la situation actuelle
3. Obtenez 5 idées de scènes possibles

---

## 💰 Gestion des coûts

### Anthropic Claude

**Estimation pour un roman :**
- Génération de 10 chapitres (15000 mots chacun) : ~$5-15
- Le contexte (personnages, lieux, lore) est envoyé à chaque fois

**Conseils :**
- Limitez le nombre de personnages/lieux envoyés dans le contexte
- Utilisez la longueur "court" pour tester
- Surveillez votre usage sur https://console.anthropic.com/

### OpenAI

**Estimation similaire :** $10-20 pour 10 chapitres avec GPT-4

### Ollama

**Gratuit !** Mais nécessite :
- 8-16 GB de RAM
- GPU recommandé (sinon très lent)
- 4-7 GB d'espace disque par modèle

---

## 🔧 Personnalisation

### Modifier les prompts

Éditez `backend/routes/llm.py` :

```python
def build_system_prompt(context: dict) -> str:
    # Personnalisez ici le prompt système
    prompt = f"""Tu es un assistant d'écriture...
    
    [Ajoutez vos instructions personnalisées]
    """
    return prompt
```

### Ajuster les longueurs

```python
# Dans la route /generate-chapter
length_guide = {
    "court": "500-800 mots",      # Modifiez ici
    "moyen": "1000-1500 mots",
    "long": "2000-3000 mots"
}
```

### Changer de modèle

**Claude :**
```python
model="claude-sonnet-4-20250514"  # Plus intelligent
model="claude-opus-4-20250514"    # Le meilleur mais plus cher
```

**OpenAI :**
```python
model="gpt-4-turbo-preview"   # Plus rapide
model="gpt-4"                 # Plus classique
```

**Ollama :**
```env
# Dans .env ou au lancement
OLLAMA_MODEL=mistral
# ou
OLLAMA_MODEL=mixtral
```

---

## 🐛 Dépannage

### "LLM provider 'anthropic' non supporté"

→ Vérifiez que `LLM_PROVIDER` est bien défini dans `.env`

### "ANTHROPIC_API_KEY non configurée"

→ Ajoutez votre clé API dans `.env`

### "Module 'anthropic' non installé"

```bash
pip install anthropic
```

### Génération très lente

- **Ollama :** Normal sans GPU. Essayez un modèle plus petit (`mistral` au lieu de `mixtral`)
- **Cloud :** Vérifiez votre connexion internet

### Erreur "Rate limit exceeded"

→ Vous avez dépassé les limites de votre compte. Attendez ou ajoutez des crédits.

### Le LLM ne respecte pas le contexte

→ Votre contexte est peut-être trop long. Limitez le nombre de personnages/lieux envoyés.

---

## 📚 Ressources

- **Claude :** https://docs.anthropic.com/
- **OpenAI :** https://platform.openai.com/docs
- **Ollama :** https://ollama.com/library

---

## ⚖️ Considérations légales

- Les textes générés par IA peuvent être protégés par le droit d'auteur selon votre juridiction
- Vérifiez toujours et éditez les textes générés
- L'IA est un **assistant**, pas un **remplaçant** de votre créativité

---

**Bon usage de l'assistant d'écriture ! ✍️✨**