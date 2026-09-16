#!/usr/bin/env bash
# Script de compilare și pornire Aplicație Rezidentă POO Lab 5

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"

echo "========================================================="
echo "  POO Lab 5: Rețeaua Internet + Aplicație Rezident      "
echo "========================================================="

cd "$SRC_DIR" || exit 1
echo "Compilare surse Java..."
javac -d . *.java

if [ $? -eq 0 ]; then
    echo "✔ Compilare reușită! Pornire Aplicație Rezidentă..."
    java Main "$@"
else
    echo "❌ Eroare la compilare!"
    exit 1
fi
