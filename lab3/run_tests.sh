#!/usr/bin/env bash
# Script de rulare teste automate integrate

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"

echo "=========================================="
echo "  Rulare Teste Automate de Integrare      "
echo "=========================================="

cd "$SRC_DIR" || exit 1
javac *.java

if [ $? -eq 0 ]; then
    java ChatIntegrationTest
else
    echo "❌ Eroare la compilare!"
    exit 1
fi
