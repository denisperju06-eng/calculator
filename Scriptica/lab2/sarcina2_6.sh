#!/bin/bash
# ==============================================================================
# Sarcina 2.6: Afiseaza toate directoarele din directorul selectat
# Utilizare: ./sarcina2_6.sh [director]
# ==============================================================================

dir="${1}"

if [ -z "$dir" ]; then
    read -rp "Introdu calea directorului: " dir
fi

if [ ! -d "$dir" ]; then
    echo "Eroare: Directorul '$dir' nu exista."
    exit 1
fi

echo "Directoarele continute in '$dir':"
echo "--------------------------------------------------"

subdirs=$(find "$dir" -mindepth 1 -maxdepth 1 -type d)

if [ -z "$subdirs" ]; then
    echo "Nu exista subdirectoare in directorul '$dir'."
else
    echo "$subdirs"
fi
