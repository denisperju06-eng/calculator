#!/bin/bash
# ==============================================================================
# Sarcina 2.2: Numara cate fisiere sunt in directorul curent
# Utilizare: ./sarcina2_2.sh [cale_director_optional]
# ==============================================================================

dir="${1:-.}"

if [ ! -d "$dir" ]; then
    echo "Eroare: Directorul '$dir' nu exista."
    exit 1
fi

# Numara fisierele obisnuite (fara subdirectoare) din directorul specificat
numar_fisiere=$(find "$dir" -maxdepth 1 -type f | wc -l | tr -d ' ')

echo "In directorul '$dir' sunt $numar_fisiere fisiere."
