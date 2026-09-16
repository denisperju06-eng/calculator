#!/usr/bin/env bash
# Script de compilare și pornire Client Chat POO

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"

echo "=========================================="
echo "  Compilare și Pornire Client Chat (POO)  "
echo "=========================================="

cd "$SRC_DIR" || exit 1
javac *.java

if [ $? -eq 0 ]; then
    echo "✔ Compilare reușită! Pornire Client GUI..."
    java Client "$@"
else
    echo "❌ Eroare la compilare!"
    exit 1
fi
