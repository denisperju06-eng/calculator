#!/bin/bash
# ==============================================================================
# Sarcina 2.10: Creeaza zilnic un fisier jurnal cu data curenta in nume
#               si scrie in el ora exacta la rulare.
# Utilizare: ./sarcina2_10.sh [director_optional]
# ==============================================================================

dir_dest="${1:-.}"

if [ ! -d "$dir_dest" ]; then
    echo "Directorul '$dir_dest' nu exista. Se creeaza..."
    mkdir -p "$dir_dest" || { echo "Eroare la crearea directorului '$dir_dest'."; exit 1; }
fi

# Preluam data curenta in format YYYY-MM-DD si ora exacta in format HH:MM:SS
data_curenta=$(date '+%Y-%m-%d')
ora_curenta=$(date '+%H:%M:%S')

fisier_jurnal="${dir_dest}/jurnal_${data_curenta}.log"

# Scriem / adaugam in fisierul jurnal
echo "[${data_curenta} ${ora_curenta}] Scriptul a fost rulat la ora exacta: ${ora_curenta}" >> "$fisier_jurnal"

echo "Fisierul jurnal: $fisier_jurnal"
echo "A fost adaugata inregistrarea:"
tail -n 1 "$fisier_jurnal"
