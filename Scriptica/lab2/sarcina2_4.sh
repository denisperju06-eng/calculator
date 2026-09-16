#!/bin/bash
# ==============================================================================
# Sarcina 2.4: Afiseaza toate fisierele cu extensia .txt dintr-un director dat ca argument
# Utilizare: ./sarcina2_4.sh [director]
# ==============================================================================

dir="${1}"

if [ -z "$dir" ]; then
    read -rp "Introdu calea directorului: " dir
fi

if [ ! -d "$dir" ]; then
    echo "Eroare: Directorul '$dir' nu exista."
    exit 1
fi

echo "Cautare fisiere .txt in directorul '$dir':"
echo "--------------------------------------------------"

txt_files=$(find "$dir" -maxdepth 1 -type f -name "*.txt")

if [ -z "$txt_files" ]; then
    echo "Nu a fost gasit niciun fisier cu extensia .txt in '$dir'."
else
    echo "$txt_files"
fi
