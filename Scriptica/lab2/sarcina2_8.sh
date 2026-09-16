#!/bin/bash
# ==============================================================================
# Sarcina 2.8: Primeste un nume de fisier ca argument si ii schimba extensia in .bak
# Utilizare: ./sarcina2_8.sh [nume_sau_cale_fisier]
# ==============================================================================

fisier="${1}"

if [ -z "$fisier" ]; then
    read -rp "Introdu calea/numele fisierului: " fisier
fi

if [ ! -f "$fisier" ]; then
    echo "Eroare: Fisierul '$fisier' nu exista sau nu este un fisier obisnuit."
    exit 1
fi

dir_name=$(dirname "$fisier")
base_name=$(basename "$fisier")

# Daca fisierul contine punct (are extensie), eliminam extensia curenta
if [[ "$base_name" == *.* ]]; then
    nume_baza="${base_name%.*}"
else
    nume_baza="$base_name"
fi

nume_nou="${nume_baza}.bak"

if [ "$dir_name" != "." ]; then
    cale_noua="${dir_name}/${nume_nou}"
else
    cale_noua="${nume_nou}"
fi

if [ "$fisier" = "$cale_noua" ]; then
    echo "Fisierul are deja extensia .bak: '$fisier'"
    exit 0
fi

mv "$fisier" "$cale_noua"
if [ $? -eq 0 ]; then
    echo "Fisierul '$fisier' a fost redenumit cu succes in '$cale_noua'."
else
    echo "Eroare la redenumirea fisierului."
    exit 1
fi
