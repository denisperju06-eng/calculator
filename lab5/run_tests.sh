#!/usr/bin/env bash
# Script de compilare și rulare teste automate POO Lab 5

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"

echo "========================================================="
echo "  Rulare Suită Teste Automate - POO Lab 5               "
echo "========================================================="

cd "$SRC_DIR" || exit 1
javac -d . *.java

if [ $? -eq 0 ]; then
    java ResidentTestSuite "$@"
else
    echo "❌ Eroare la compilarea testelor!"
    exit 1
fi
