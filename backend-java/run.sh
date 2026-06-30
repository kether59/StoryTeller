#!/usr/bin/env bash
# run.sh – Démarrage rapide du backend Java StoryTeller
# Équivalent de run.py (Python)
#
# Usage :
#   ./run.sh              → mode développement (avec rechargement auto via spring-boot:run)
#   ./run.sh prod         → mode production (JAR compilé)
#   ./run.sh build        → compiler uniquement
#   ./run.sh test         → lancer les tests

set -euo pipefail

MODE=${1:-dev}
PORT=${SERVER_PORT:-8000}
JAR="target/storyteller-api-2.0.0.jar"

echo "════════════════════════════════════════════════════════════"
echo "  StoryTeller API 2.0 – Java 25 / Spring Boot 4.1.0"
echo "════════════════════════════════════════════════════════════"

# ── Vérification Java 25 ──────────────────────────────────────────
JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "${JAVA_VER:-0}" -lt 25 ] 2>/dev/null; then
  echo "⚠️  Java 25+ requis. Version détectée : ${JAVA_VER}"
  echo "   Téléchargez : https://adoptium.net"
fi

# ── Charger .env si présent ───────────────────────────────────────
if [ -f ".env" ]; then
  echo "📁 Chargement de .env"
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

case "$MODE" in

  dev | "")
    echo "🚀 Mode DÉVELOPPEMENT (spring-boot:run)"
    echo "   API : http://localhost:${PORT}"
    echo "   Arrêt : Ctrl+C"
    echo "════════════════════════════════════════════════════════════"
    mvn spring-boot:run \
      -Dspring-boot.run.jvmArguments="-Xmx512m" \
      -Dserver.port="${PORT}"
    ;;

  prod)
    echo "🏭 Mode PRODUCTION (JAR)"
    if [ ! -f "${JAR}" ]; then
      echo "⚙️  JAR absent, compilation..."
      mvn package -DskipTests -B -q
    fi
    echo "   API : http://localhost:${PORT}"
    echo "════════════════════════════════════════════════════════════"
    java -jar "${JAR}" \
      --server.port="${PORT}" \
      --spring.jpa.show-sql=false
    ;;

  build)
    echo "⚙️  Compilation..."
    mvn package -DskipTests -B
    echo "✅ JAR créé : ${JAR}"
    ;;

  test)
    echo "🧪 Lancement des tests..."
    mvn test
    ;;

  *)
    echo "Usage : ./run.sh [dev|prod|build|test]"
    exit 1
    ;;

esac