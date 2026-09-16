#!/usr/bin/env bash
# Script de compilare a redactorului grafic
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

mkdir -p bin

echo "Compilare surse Java din src/..."
javac -d bin src/paint/*.java src/paint/*/*.java

echo "Compilare finalizată cu succes în directorul bin/!"
