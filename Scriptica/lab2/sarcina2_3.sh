#!/bin/bash
# ==============================================================================
# Sarcina 2.3: Copiaza un fisier intr-un alt director, doar daca fisierul exista
# Utilizare: ./sarcina2_3.sh [fisier_sursa] [director_destinatie]
# ==============================================================================

src="${1}"
dest="${2}"

if [ -z "$src" ]; then
    read -rp "Introdu calea fisierului sursa: " src
fi

if [ -z "$dest" ]; then
    read -rp "Introdu directorul destinatie: " dest
fi

if [ ! -e "$src" ]; then
    echo "Eroare: Fisierul sursa '$src' nu exista. Operatiunea de copiere a fost anulata."
    exit 1
fi

if [ ! -f "$src" ]; then
    echo "Eroare: '$src' nu este un fisier obisnuit."
    exit 1
fi

# Daca directorul destinatie nu exista, il cream automat
if [ ! -d "$dest" ]; then
    echo "Directorul destinatie '$dest' nu exista. Se creeaza..."
    mkdir -p "$dest" || { echo "Eroare: Nu s-a putut crea directorul '$dest'."; exit 1; }
fi

cp "$src" "$dest"
if [ $? -eq 0 ]; then
    echo "Fisierul '$src' a fost copiat cu succes in '$dest'."
else
    echo "A aparut o eroare la copierea fisierului."
    exit 1
fi
