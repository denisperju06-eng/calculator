#!/usr/bin/env bash
# Script de pornire a aplicației Redactor Grafic (Paint)
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

if [ ! -d "bin" ] || [ ! -f "bin/paint/Main.class" ]; then
    echo "Directorul bin nu există sau nu este compilat. Se rulează compilarea..."
    ./compile.sh
fi

echo "Pornire Redactor Grafic..."
java -cp bin paint.Main
