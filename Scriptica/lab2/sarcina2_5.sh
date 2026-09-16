#!/bin/bash
# ==============================================================================
# Sarcina 2.5: Verifica spatiul liber pe disc si avertizeaza daca e sub 1 GB
# Utilizare: ./sarcina2_5.sh [cale_optionala]
# ==============================================================================

cale="${1:-.}"

if [ ! -e "$cale" ]; then
    cale="."
fi

# Preluam spatiul liber in megabytes (coloana 4 la df -m)
free_mb=$(df -m "$cale" | awk 'NR==2 {print $4}')

# 1 GB = 1024 MB
limita_mb=1024

echo "Analiza spatiu liber pentru: $(pwd)"
echo "Spatiu liber disponibil: ${free_mb} MB"

if [ "$free_mb" -lt "$limita_mb" ]; then
    echo "=========================================================="
    echo "AVERTISMENT: Spatiul liber pe disc este SUB 1 GB!"
    echo "Spatiu ramas: ${free_mb} MB"
    echo "=========================================================="
else
    # Calcul aproximativ in GB
    free_gb=$(awk -v mb="$free_mb" 'BEGIN {printf "%.2f", mb / 1024}')
    echo "Spatiul liber este suficient: ${free_gb} GB (peste pragul de 1 GB)."
fi
