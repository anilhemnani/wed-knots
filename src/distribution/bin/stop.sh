#!/usr/bin/env bash
set -euo pipefail

# ==========================================
# WedKnots - Stop Script (Unix)
# ==========================================

pkill -f "wed-knots-.*\.jar" || true
sleep 1
echo "Stop requested."

