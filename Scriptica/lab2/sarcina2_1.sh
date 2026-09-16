#!/bin/bash
# ==============================================================================
# Sarcina 2.1: Verifica daca un fisier exista si afiseaza un mesaj corespunzator
# Utilizare: ./sarcina2_1.sh [cale_fisier]
# ==============================================================================

fisier="${1}"

if [ -z "$fisier" ]; then
    read -rp "Introdu calea/numele fisierului: " fisier
fi

if [ -e "$fisier" ]; then
    if [ -f "$fisier" ]; then
        echo "Fisierul '$fisier' exista si este un fisier obisnuit."
    elif [ -d "$fisier" ]; then
        echo "'$fisier' exista, dar este un director."
    else
        echo "Elementul '$fisier' exista in sistem."
    fi
else
    echo "Fisierul '$fisier' NU exista."
fi
