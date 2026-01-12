#!/usr/bin/env bash
set -euo pipefail

# ==========================================
# WedKnots - Interactive Setup (Unix)
# ==========================================
# Reads ../config.env as defaults, prompts, writes overrides to config/config.env

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$APP_ROOT"

PARENT_ENV="$APP_ROOT/../config.env"
CHILD_DIR="$APP_ROOT/config"
CHILD_ENV="$CHILD_DIR/config.env"

mkdir -p "$CHILD_DIR"

# Load defaults from parent
if [[ -f "$PARENT_ENV" ]]; then
  echo "Loading defaults from parent: $PARENT_ENV"
  set -a; source "$PARENT_ENV"; set +a
else
  echo "No parent config.env found. Using defaults."
fi

prompt() {
  local name="$1"; local current="${2:-}"; local fallback="${3:-}";
  if [[ -z "$current" ]]; then current="$fallback"; fi
  read -p "$name [$current]: " input || true
  if [[ -z "$input" ]]; then echo "$current"; else echo "$input"; fi
}

SPRING_PROFILES_ACTIVE=$(prompt "SPRING_PROFILES_ACTIVE" "${SPRING_PROFILES_ACTIVE:-}" "prod")
JASYPT_ENCRYPTOR_PASSWORD=$(prompt "JASYPT_ENCRYPTOR_PASSWORD" "${JASYPT_ENCRYPTOR_PASSWORD:-}" "")
DATABASE_URL=$(prompt "DATABASE_URL" "${DATABASE_URL:-}" "jdbc:postgresql://localhost:5432/wedknots")
DATABASE_USERNAME=$(prompt "DATABASE_USERNAME" "${DATABASE_USERNAME:-}" "wedknots_user")
DATABASE_PASSWORD=$(prompt "DATABASE_PASSWORD" "${DATABASE_PASSWORD:-}" "")
PORT=$(prompt "PORT" "${PORT:-}" "8080")
LOG_LEVEL=$(prompt "LOG_LEVEL" "${LOG_LEVEL:-}" "INFO")
LOG_FILE=$(prompt "LOG_FILE" "${LOG_FILE:-}" "logs/wedknots.log")
WHATSAPP_VERIFY_TOKEN=$(prompt "WHATSAPP_VERIFY_TOKEN" "${WHATSAPP_VERIFY_TOKEN:-}" "")
WHATSAPP_APP_SECRET=$(prompt "WHATSAPP_APP_SECRET" "${WHATSAPP_APP_SECRET:-}" "")

cat > "$CHILD_ENV" <<EOF
# WedKnots Application - Overrides
# Generated: $(date)
SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
JASYPT_ENCRYPTOR_PASSWORD=$JASYPT_ENCRYPTOR_PASSWORD
DATABASE_URL=$DATABASE_URL
DATABASE_USERNAME=$DATABASE_USERNAME
DATABASE_PASSWORD=$DATABASE_PASSWORD
PORT=$PORT
LOG_LEVEL=$LOG_LEVEL
LOG_FILE=$LOG_FILE
WHATSAPP_VERIFY_TOKEN=$WHATSAPP_VERIFY_TOKEN
WHATSAPP_APP_SECRET=$WHATSAPP_APP_SECRET
EOF

echo "Saved overrides to: $CHILD_ENV"
echo "Setup complete."

