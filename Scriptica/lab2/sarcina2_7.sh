#!/bin/bash
# ==============================================================================
# Sarcina 2.7: Parcurge o lista de fisiere si afiseaza marimea fiecaruia in octeti,
#              iar la final spatiul total ocupat de acestea.
# Utilizare: ./sarcina2_7.sh [fisier1 fisier2 ...]
#            (Daca nu se dau parametri, se analizeaza fisierele din directorul curent)
# ==============================================================================

total_bytes=0

if [ $# -eq 0 ]; then
    echo "Nu au fost specificate fisiere ca argumente. Se analizeaza fisierele din directorul curent:"
    # Preluam fisierele din directorul curent
    shopt -s nullglob
    files=( * )
    shopt -u nullglob
else
    files=( "$@" )
fi

echo "======================================================================"
printf "%-40s | %s\n" "Fisier" "Marime (octeti)"
echo "----------------------------------------------------------------------"

gasit=0
for f in "${files[@]}"; do
    if [ -f "$f" ]; then
        # Pe macOS, stat -f %z returneaza marimea exacta in bytes
        if stat -f %z "$f" >/dev/null 2>&1; then
            size=$(stat -f %z "$f")
        else
            size=$(wc -c < "$f" | tr -d ' ')
        fi
        printf "%-40s | %15d octeti\n" "$f" "$size"
        total_bytes=$((total_bytes + size))
        gasit=1
    elif [ -d "$f" ]; then
        echo "Ignorat: '$f' este un director."
    elif [ -e "$f" ]; then
        echo "Ignorat: '$f' nu este un fisier obisnuit."
    else
        echo "Eroare: Fisierul '$f' nu exista."
    fi
done

echo "======================================================================"
if [ $gasit -eq 0 ]; then
    echo "Nu a fost gasit niciun fisier valid pentru calcul."
else
    printf "%-40s | %15d octeti\n" "TOTAL SPATIU SUMAR OCUPAT" "$total_bytes"
fi
echo "======================================================================"
