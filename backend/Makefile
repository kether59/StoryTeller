.PHONY: help build run stop clean test deploy k8s-deploy k8s-delete

# Variables
IMAGE_NAME = storyteller-api
IMAGE_TAG = 2.0
REGISTRY = your-registry.com
NAMESPACE = storyteller

help: ## Affiche l'aide
	@echo "Commandes disponibles:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ===== DOCKER =====

build: ## Construit l'image Docker
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .
	docker tag $(IMAGE_NAME):$(IMAGE_TAG) $(IMAGE_NAME):latest

build-no-cache: ## Construit l'image Docker sans cache
	docker build --no-cache -t $(IMAGE_NAME):$(IMAGE_TAG) .

run: ## Lance le conteneur en mode développement
	docker-compose up -d

run-prod: ## Lance le conteneur en mode production
	docker-compose -f docker-compose.prod.yml up -d

stop: ## Arrête les conteneurs
	docker-compose down

logs: ## Affiche les logs
	docker-compose logs -f storyteller-api

shell: ## Ouvre un shell dans le conteneur
	docker-compose exec storyteller-api /bin/sh

clean: ## Nettoie les conteneurs, images et volumes
	docker-compose down -v
	docker rmi $(IMAGE_NAME):$(IMAGE_TAG) $(IMAGE_NAME):latest || true

# ===== REGISTRY =====

push: ## Pousse l'image vers le registry
	docker tag $(IMAGE_NAME):$(IMAGE_TAG) $(REGISTRY)/$(IMAGE_NAME):$(IMAGE_TAG)
	docker push $(REGISTRY)/$(IMAGE_NAME):$(IMAGE_TAG)

pull: ## Récupère l'image depuis le registry
	docker pull $(REGISTRY)/$(IMAGE_NAME):$(IMAGE_TAG)

# ===== KUBERNETES =====

k8s-create-namespace: ## Crée le namespace Kubernetes
	kubectl create namespace $(NAMESPACE) || true

k8s-apply-config: ## Applique les ConfigMaps et Secrets
	kubectl apply -f k8s/configmap-secrets.yaml

k8s-deploy: k8s-create-namespace k8s-apply-config ## Déploie sur Kubernetes
	kubectl apply -f k8s/deployment.yaml
	kubectl apply -f k8s/ingress.yaml
	kubectl apply -f k8s/hpa.yaml

k8s-delete: ## Supprime le déploiement Kubernetes
	kubectl delete -f k8s/hpa.yaml || true
	kubectl delete -f k8s/ingress.yaml || true
	kubectl delete -f k8s/deployment.yaml || true

k8s-status: ## Affiche le statut du déploiement
	kubectl get all -n $(NAMESPACE)
	kubectl get ingress -n $(NAMESPACE)

k8s-logs: ## Affiche les logs des pods
	kubectl logs -n $(NAMESPACE) -l app=storyteller-api --tail=100 -f

k8s-describe: ## Décrit les pods
	kubectl describe pods -n $(NAMESPACE) -l app=storyteller-api

k8s-restart: ## Redémarre les pods
	kubectl rollout restart deployment/storyteller-api -n $(NAMESPACE)

k8s-scale: ## Scale le déploiement (usage: make k8s-scale REPLICAS=5)
	kubectl scale deployment/storyteller-api -n $(NAMESPACE) --replicas=$(REPLICAS)

# ===== DATABASE =====

seed: ## Initialise la base de données avec des données de test
	docker-compose exec storyteller-api python scripts/seed_alternative.py

migrate: ## Exécute les migrations (si Alembic est configuré)
	docker-compose exec storyteller-api alembic upgrade head

# ===== TESTS =====

test: ## Lance les tests
	docker-compose exec storyteller-api pytest

test-coverage: ## Lance les tests avec couverture
	docker-compose exec storyteller-api pytest --cov=. --cov-report=html

# ===== DÉVELOPPEMENT =====

dev: ## Lance en mode développement avec hot-reload
	uvicorn main:app --reload --host 0.0.0.0 --port 8000

install: ## Installe les dépendances
	pip install -r requirements.txt

format: ## Formate le code avec black
	black .

lint: ## Vérifie le code avec flake8
	flake8 .