#!/usr/bin/env bash
set -euo pipefail

# ==========================================
# WedKnots - Start Script (Unix)
# ==========================================
# - Loads parent ../config.env then config/config.env overrides
# - Stops existing app and starts new version

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$APP_ROOT"

# Load parent env
PARENT_ENV="$APP_ROOT/../config.env"
if [[ -f "$PARENT_ENV" ]]; then
  echo "Loading parent config: $PARENT_ENV"
  set -a; source "$PARENT_ENV"; set +a
else
  echo "No parent config.env found at ../config.env"
fi

# Load child env overrides
CHILD_ENV="$APP_ROOT/config/config.env"
if [[ -f "$CHILD_ENV" ]]; then
  echo "Loading child config: $CHILD_ENV"
  set -a; source "$CHILD_ENV"; set +a
else
  echo "No child config/config.env found"
fi

SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
PORT="${PORT:-8080}"
LOG_FILE="${LOG_FILE:-logs/wedknots.log}"

# Stop existing app
echo "Stopping existing application (if any)..."
pkill -f "wed-knots-.*\.jar" || true
sleep 2

# Ensure logs dir exists
mkdir -p "$APP_ROOT/logs"

# Locate JAR
JAR_FILE=$(ls "$APP_ROOT/app"/wed-knots-*.jar 2>/dev/null | head -n1)
if [[ -z "$JAR_FILE" ]]; then
  echo "ERROR: Spring Boot jar not found in app/"
  exit 1
fi

echo "Starting application with JAR: $JAR_FILE"
echo "Logging to: $LOG_FILE"

exec java -Xms512m -Xmx1024m \
  -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE}" \
  -Dspring.datasource.url="${DATABASE_URL:-}" \
  -Dspring.datasource.username="${DATABASE_USERNAME:-}" \
  -Dspring.datasource.password="${DATABASE_PASSWORD:-}" \
  -Dspring.datasource.driver-class-name="org.postgresql.Driver" \
  -Dserver.port="${PORT}" \
  -Dlogging.file.name="${LOG_FILE}" \
  -Djasypt.encryptor.password="${JASYPT_ENCRYPTOR_PASSWORD:-}" \
  -Dwhatsapp.webhook.verify-token="${WHATSAPP_VERIFY_TOKEN:-}" \
  -Dwhatsapp.webhook.app-secret="${WHATSAPP_APP_SECRET:-}" \
  -jar "$JAR_FILE"

