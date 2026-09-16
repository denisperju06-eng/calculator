#!/bin/bash
# ==============================================================================
# Sarcina 2.9: Monitorizeaza un proces si afiseaza mesajul "Proces activ" daca ruleaza
# Utilizare: ./sarcina2_9.sh [nume_proces]
# ==============================================================================

nume_proces="${1}"

if [ -z "$nume_proces" ]; then
    read -rp "Introdu numele procesului de verificat: " nume_proces
fi

# Cautam procesul dupa nume/pattern excluzand procesul curent
pids=$(pgrep -f "$nume_proces" 2>/dev/null | grep -v "$$")

if [ -n "$pids" ]; then
    echo "Proces activ"
    echo "--------------------------------------------------"
    echo "Procesul '$nume_proces' ruleaza cu PID: $pids"
    # Afisam detalii despre procesele gasite
    ps -p $pids -o pid,comm,user 2>/dev/null || true
else
    echo "Procesul '$nume_proces' NU ruleaza (inactiv)."
fi
