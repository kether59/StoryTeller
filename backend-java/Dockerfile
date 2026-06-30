# ══════════════════════════════════════════════════════════════════
#  Dockerfile – StoryTeller Backend Java 25 / Spring Boot 4.1.0
#  Build multi-stage : compile avec Maven, run avec JRE minimal
# ══════════════════════════════════════════════════════════════════

# ── Étape 1 : Build Maven ──────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build

# Cache des dépendances Maven (couche indépendante du code source)
COPY pom.xml .
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copier les sources et compiler
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ── Étape 2 : Runtime minimal ─────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Dossiers de données persistées
RUN mkdir -p /app/data /app/config /app/nlp

# Copier le JAR depuis l'étape builder
COPY --from=builder /build/target/storyteller-api-*.jar app.jar

# Optionnel : copier les modèles OpenNLP pré-téléchargés
# COPY nlp-models/ /app/nlp/

# Variables d'environnement par défaut
ENV SERVER_PORT=8000
ENV DATABASE_URL=./data/storyteller.db
ENV LLM_CONFIG_PATH=/app/config/llm_config.json
ENV NLP_MODELS_PATH=file:/app/nlp/
ENV JAVA_OPTS="-XX:+UseVirtualThreads -Xmx512m"

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8000/actuator/health || exit 1

# Lancement avec virtual threads activés
ENTRYPOINT ["sh", "-c", \
  "java $JAVA_OPTS \
   -Dspring.datasource.url=jdbc:sqlite:${DATABASE_URL} \
   -Dstoryteller.llm.config-file=${LLM_CONFIG_PATH} \
   -Dstoryteller.nlp.models-path=${NLP_MODELS_PATH} \
   -jar app.jar"]