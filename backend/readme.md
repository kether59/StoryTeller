# StoryTeller Backend API

API FastAPI pour gérer vos histoires, personnages, chronologies et manuscrits.

## 📋 Table des matières

- [Prérequis](#prérequis)
- [Installation locale](#installation-locale)
- [Docker](#docker)
- [Kubernetes](#kubernetes)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Développement](#développement)
- [Production](#production)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Prérequis

- **Python 3.11+**
- **Docker & Docker Compose** (optionnel)
- **Kubernetes cluster** (pour déploiement K8s)
- **kubectl** configuré (pour déploiement K8s)

---

## 🚀 Installation locale

### 1. Cloner le repository

```bash
git clone https://github.com/votre-repo/storyteller.git
cd storyteller/backend
```

### 2. Créer un environnement virtuel

```bash
python -m venv .venv
source .venv/bin/activate  # Linux/Mac
.venv\Scripts\activate     # Windows
```

### 3. Installer les dépendances

```bash
pip install -r requirements.txt
```

### 4. Initialiser la base de données

```bash
python scripts/seed_alternative.py
```

### 5. Lancer le serveur

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Ouvrez votre navigateur sur : **http://localhost:8000/docs**

---

## 🐳 Docker

### Développement avec Docker Compose

#### Construire et lancer

```bash
docker-compose up -d --build
```

#### Voir les logs

```bash
docker-compose logs -f
```

#### Arrêter

```bash
docker-compose down
```

#### Initialiser la base de données

```bash
docker-compose exec storyteller-api python scripts/seed_alternative.py
```

### Production avec Docker

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Commandes utiles avec Makefile

```bash
make build          # Construire l'image
make run            # Lancer en dev
make run-prod       # Lancer en prod
make logs           # Voir les logs
make stop           # Arrêter
make clean          # Tout nettoyer
```

---

## ☸️ Kubernetes

### Prérequis Kubernetes

1. Cluster Kubernetes opérationnel
2. kubectl configuré et connecté au cluster
3. Nginx Ingress Controller installé (optionnel)
4. Cert-Manager installé (pour SSL automatique, optionnel)

### Déploiement rapide

#### 1. Construire et pousser l'image

```bash
# Construire l'image
docker build -t your-registry.com/storyteller-api:2.0 .

# Pousser vers votre registry
docker push your-registry.com/storyteller-api:2.0
```

Ou avec le Makefile :

```bash
make build
make push REGISTRY=your-registry.com
```

#### 2. Configurer les secrets

Éditez `k8s/configmap-secrets.yaml` et modifiez :

```yaml
stringData:
  database-url: "postgresql://user:password@host:5432/storyteller"
```

#### 3. Déployer sur Kubernetes

```bash
# Avec Makefile (recommandé)
make k8s-deploy

# Ou manuellement
kubectl apply -f k8s/configmap-secrets.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

#### 4. Vérifier le déploiement

```bash
# Statut
make k8s-status

# Ou
kubectl get all -n storyteller
kubectl get ingress -n storyteller
```

#### 5. Accéder à l'API

```bash
# Via port-forward (test local)
kubectl port-forward -n storyteller svc/storyteller-api-service 8000:80

# Via Ingress (production)
https://api.storyteller.yourdomain.com
```

### Commandes Kubernetes utiles

```bash
make k8s-logs              # Voir les logs
make k8s-restart           # Redémarrer les pods
make k8s-scale REPLICAS=5  # Scaler à 5 replicas
make k8s-describe          # Détails des pods
make k8s-delete            # Supprimer le déploiement
```

### Architecture Kubernetes

```
┌─────────────────────────────────────────────┐
│           Ingress (HTTPS)                   │
│     api.storyteller.yourdomain.com          │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         Service (ClusterIP)                 │
│      storyteller-api-service:80             │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         Deployment (3 replicas)             │
│                                             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │  Pod 1  │  │  Pod 2  │  │  Pod 3  │    │
│  │ API:8000│  │ API:8000│  │ API:8000│    │
│  └─────────┘  └─────────┘  └─────────┘    │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│    PersistentVolumeClaim (5Gi)              │
│           /app/data                         │
└─────────────────────────────────────────────┘
```

---

## ⚙️ Configuration

### Variables d'environnement

Créez un fichier `.env` à la racine du backend :

```env
# Base de données
DATABASE_URL=sqlite:///./data/storyteller.db
# ou PostgreSQL
# DATABASE_URL=postgresql://user:password@localhost:5432/storyteller

# CORS
CORS_ORIGINS=["http://localhost:3000","https://yourdomain.com"]

# Environnement
ENV=development  # ou production
```

### Configuration Kubernetes

Modifiez `k8s/configmap-secrets.yaml` pour ajuster :

- URL de base de données
- Origins CORS autorisées
- Autres paramètres d'environnement

---

## 📚 API Documentation

Une fois le serveur lancé, accédez à :

- **Swagger UI** : http://localhost:8000/docs
- **ReDoc** : http://localhost:8000/redoc

### Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/stories` | Liste toutes les histoires |
| POST | `/api/stories` | Crée une nouvelle histoire |
| GET | `/api/characters?story_id=1` | Liste les personnages |
| POST | `/api/characters` | Crée un personnage |
| GET | `/api/locations?story_id=1` | Liste les lieux |
| GET | `/api/timeline?story_id=1` | Liste les événements |
| GET | `/api/manuscript?story_id=1` | Liste les manuscrits |
| POST | `/api/ai/suggest` | Suggestions IA |

---

## 🛠️ Développement

### Structure du projet

```
backend/
├── main.py                 # Point d'entrée FastAPI
├── config.py               # Configuration
├── database.py             # Connexion DB
├── models.py               # Modèles SQLAlchemy
├── schemas.py              # Schémas Pydantic
├── routes/                 # Routes API
│   ├── stories.py
│   ├── characters.py
│   ├── locations.py
│   ├── timeline.py
│   ├── manuscript.py
│   └── ai.py
├── scripts/
│   └── seed_alternative.py # Script d'initialisation
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
└── k8s/                    # Manifests Kubernetes
    ├── deployment.yaml
    ├── configmap-secrets.yaml
    ├── ingress.yaml
    └── hpa.yaml
```

### Ajouter une nouvelle route

1. Créer un fichier dans `routes/` (ex: `routes/new_feature.py`)
2. Définir le router FastAPI
3. L'importer dans `main.py`

Exemple :

```python
# routes/new_feature.py
from fastapi import APIRouter

router = APIRouter(prefix="/api/new", tags=["new"])

@router.get("")
def list_items():
    return {"items": []}
```

```python
# main.py
from .routes import new_feature

app.include_router(new_feature.router)
```

### Tests

```bash
# Installer les dépendances de test
pip install pytest pytest-cov httpx

# Lancer les tests
pytest

# Avec couverture
pytest --cov=. --cov-report=html
```

---

## 🚢 Production

### Checklist avant déploiement

- [ ] Modifier `DATABASE_URL` pour PostgreSQL
- [ ] Configurer les `CORS_ORIGINS` correctement
- [ ] Changer les mots de passe dans secrets
- [ ] Configurer le domaine dans `ingress.yaml`
- [ ] Activer SSL avec cert-manager
- [ ] Configurer les limites de ressources
- [ ] Activer les backups de base de données
- [ ] Configurer la surveillance (Prometheus/Grafana)
- [ ] Tester le health check

### Base de données PostgreSQL

Pour utiliser PostgreSQL en production :

1. Modifiez `DATABASE_URL` :
```
postgresql://storyteller:SECURE_PASSWORD@postgres-service:5432/storyteller
```

2. Décommentez le service PostgreSQL dans `docker-compose.prod.yml`

3. Installez le driver :
```bash
pip install psycopg2-binary
```

### Monitoring

Ajoutez Prometheus metrics :

```python
# requirements.txt
prometheus-fastapi-instrumentator

# main.py
from prometheus_fastapi_instrumentator import Instrumentator

Instrumentator().instrument(app).expose(app)
```

### Backups

Script de backup automatique :

```bash
#!/bin/bash
# backup.sh
kubectl exec -n storyteller deployment/storyteller-api -- \
  tar -czf /tmp/backup.tar.gz /app/data

kubectl cp storyteller/storyteller-api-xxxxx:/tmp/backup.tar.gz \
  ./backups/backup-$(date +%Y%m%d).tar.gz
```

---

## 🐛 Troubleshooting

### Le serveur ne démarre pas

```bash
# Vérifier les logs
docker-compose logs storyteller-api

# Ou sur Kubernetes
kubectl logs -n storyteller -l app=storyteller-api
```

### Erreur de connexion à la base de données

- Vérifiez `DATABASE_URL` dans les secrets
- Assurez-vous que le PVC est correctement monté
- Vérifiez les permissions du volume

### Les pods ne démarrent pas (Kubernetes)

```bash
# Décrire le pod
kubectl describe pod -n storyteller -l app=storyteller-api

# Vérifier les events
kubectl get events -n storyteller --sort-by='.lastTimestamp'
```

### Erreur "ImagePullBackOff"

- Vérifiez que l'image existe dans votre registry
- Vérifiez les credentials du registry secret
- Testez manuellement : `docker pull your-registry.com/storyteller-api:2.0`

### Performance lente

1. Augmentez les ressources :
```yaml
resources:
  limits:
    cpu: 1
    memory: 1Gi
```

2. Activez le HPA pour auto-scaling

3. Vérifiez les requêtes DB (indexation)

---

## 📞 Support

- **Documentation API** : http://localhost:8000/docs
- **Issues** : https://github.com/votre-repo/storyteller/issues
- **Email** : support@storyteller.com

---

## 📝 License

MIT License - voir le fichier LICENSE

---

## 🙏 Contributeurs


- Renny - Développeur principal
- Claude ia

---

**Version** : 2.0  
**Dernière mise à jour** : 2024