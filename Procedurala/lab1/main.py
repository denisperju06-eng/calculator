#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Lucrarea de Laborator 1 - Paradigma Procedurala
Redactor de Text Procedural cu Instrumente de Analiza si Prelucrare Textuala

Acest modul implementeaza un redactor de text complet functional folosind biblioteca
tkinter, respectand strict principiile paradigmei procedurale:
- Fara utilizarea claselor (zero clase definite).
- Descompunerea problemei in subrutine/proceduri/functii modulare si independente.
- Gestionarea starii aplicatiei prin intermediul variabilelor globale explicite si parametrilor.
- Utilizarea structurilor de date fundamentale: liste, dictionare, tupluri, seturi si siruri de caractere.

Cerinte implementate conform 'conditii.md':
a. Cautarea celui mai frecvent cuvant sau expresie (gaseste_cel_mai_frecvent)
b. Hash-ul textului (hash_text: DJB2 si suma ASCII)
c. Codificarea si decodificarea textului (codifica_text, decodifica_text + verificare)
d. Analiza propozitiilor (imparte_in_propozitii, medie cuvinte, propozitia maxima)
e. Statistica textuala (total cuvinte, caractere cu/fara spatii, linii, cuvinte unice)
f. Sortarea liniilor si propozitiilor dupa lungime (sorteaza_linii_dupa_lungime)
"""

import os
import re
import sys
import tkinter as tk
from tkinter import filedialog, messagebox, simpledialog, ttk

# ==============================================================================
# VARIABILE GLOBALE DE STARE (PARADIGMA PROCEDURALA)
# ==============================================================================
FEREASTRA_PRINCIPALA = None
ZONA_TEXT = None
CALE_FISIER_CURENT = None
ETICHETA_STARE = None
TEXT_MODIFICAT = False
ULTIMA_DEPLASARE = 3

TEXT_EXEMPLU_DEMO = (
    "Paradigma procedurala este un model fundamental de programare.\n"
    "Programarea procedurala se bazeaza pe apeluri de functii si proceduri independente!\n"
    "Fiecare procedura rezolva o problema specifica.\n"
    "Oare este eficienta paradigma procedurala in prelucrarea textelor?\n"
    "Da, paradigma procedurala este simpla, eleganta si foarte usor de urmarit.\n"
    "In laboratorul 1 analizam frecventa cuvintelor si calculam statistici diverse.\n"
    "Succes in explorarea conceptelor procedurale!"
)


# ==============================================================================
# A. CAUTAREA CELUI MAI FRECVENT CUVANT SAU EXPRESIE (2 puncte)
# ==============================================================================
def extrage_cuvinte(text):
    """
    Procedura care extrage toate cuvintele dintr-un text, eliminand punctuatia.
    Returneaza o lista de cuvinte convertite la minuscule.
    """
    if not text:
        return []
    # Folosim regex ce recunoaste litere, inclusiv caractere cu diacritice
    return re.findall(r"\b\w+\b", text.lower())


def calculeaza_frecventa_cuvinte(text):
    """
    Procedura care calculeaza frecventa de aparitie a fiecarui cuvant din text.
    Returneaza un dictionar {cuvant: numar_aparitii}.
    """
    cuvinte = extrage_cuvinte(text)
    frecvente = {}
    for cuv in cuvinte:
        frecvente[cuv] = frecvente.get(cuv, 0) + 1
    return frecvente


def calculeaza_frecventa_expresii(text, lungime=2):
    """
    Procedura care calculeaza frecventa secventelor de 'lungime' cuvinte consecutive (n-grame).
    Returneaza un dictionar {expresie: numar_aparitii}.
    """
    cuvinte = extrage_cuvinte(text)
    frecvente = {}
    if len(cuvinte) < lungime:
        return frecvente

    for i in range(len(cuvinte) - lungime + 1):
        expresie = " ".join(cuvinte[i : i + lungime])
        frecvente[expresie] = frecvente.get(expresie, 0) + 1

    return frecvente


def calculeaza_frecventa_expresie_specifica(text, expresie):
    """
    Procedura care calculeaza numarul de aparitii ale unei expresii sau cuvant specific in text.
    Cautarea este insensibila la majuscule/minuscule.
    """
    if not text or not expresie:
        return 0
    pattern = re.escape(expresie.strip().lower())
    aparitii = re.findall(pattern, text.lower())
    return len(aparitii)


def gaseste_cel_mai_frecvent(text=None, tip="cuvant", lungime_expresie=2):
    """
    Procedura ceruta: gaseste_cel_mai_frecvent()
    - Calculeaza frecventa cuvintelor sau expresiilor;
    - Gaseste cuvantul sau expresia care apare cel mai des;
      Are functionalitate SI pentru cuvant SI pentru expresie (n-grame);
    - Returneaza tuplu: (element_maxim, numar_aparitii, dictionar_frecvente).
    Daca text este None, se preia textul din zona de editare.
    """
    if text is None:
        text = obtine_text_curent()

    if tip == "cuvant":
        frecvente = calculeaza_frecventa_cuvinte(text)
    elif tip == "expresie":
        frecvente = calculeaza_frecventa_expresii(text, lungime=lungime_expresie)
    else:
        raise ValueError("Parametrul 'tip' trebuie sa fie 'cuvant' sau 'expresie'.")

    if not frecvente:
        return None, 0, {}

    # Identificam cheia cu valoarea maxima
    element_maxim = max(frecvente, key=lambda k: frecvente[k])
    numar_aparitii = frecvente[element_maxim]
    return element_maxim, numar_aparitii, frecvente


# ==============================================================================
# B. HASH-UL TEXTULUI (1 punct)
# ==============================================================================
def hash_ascii_sum(text):
    """
    Procedura care calculeaza un hash simplu prin suma valorilor codurilor caracterelor (ASCII/Unicode).
    Returneaza valoarea numerica intreaga.
    """
    suma = 0
    for caracter in text:
        suma += ord(caracter)
    return suma


def hash_djb2(text):
    """
    Procedura care implementeaza algoritmul clasic de hashing DJB2 (Dan Bernstein):
    hash(i) = hash(i - 1) * 33 + c
    Returneaza hash-ul pe 64 de biti.
    """
    hash_val = 5381
    for caracter in text:
        # Echivalent cu hash_val * 33 + ord(caracter)
        hash_val = ((hash_val << 5) + hash_val) + ord(caracter)
        hash_val = hash_val & 0xFFFFFFFFFFFFFFFF  # Masca pe 64 biti
    return hash_val


def hash_text(text=None, algoritm="djb2"):
    """
    Procedura ceruta: hash_text()
    - Primeste un sir de caractere (sau preia textul din editor daca text este None);
    - Calculeaza un hash simplu (DJB2 sau suma codurilor ASCII);
    - Returneaza valoarea calculata.
    """
    if text is None:
        text = obtine_text_curent()

    if algoritm == "djb2":
        return hash_djb2(text)
    elif algoritm == "ascii_sum":
        return hash_ascii_sum(text)
    else:
        raise ValueError("Algoritmul de hash trebuie sa fie 'djb2' sau 'ascii_sum'.")


# ==============================================================================
# C. CODIFICAREA SI DECODIFICAREA TEXTULUI (2 puncte)
# ==============================================================================
def codifica_text(text=None, deplasare=3):
    """
    Procedura ceruta: codifica_text()
    - Aplica o codificare simpla prin deplasarea caracterelor in codul ASCII/Unicode (cifrul Cezar);
    - Pastreaza caracterul newline ('\\n') intact pentru a conserva structura pe linii;
    - Returneaza textul codificat.
    """
    if text is None:
        text = obtine_text_curent()

    caractere_codificate = []
    for c in text:
        if c == "\n":
            caractere_codificate.append("\n")
        else:
            # Deplasare ciclica in spatiul valorilor de caractere Unicode
            cod_nou = (ord(c) + deplasare) % 1114112
            caractere_codificate.append(chr(cod_nou))

    return "".join(caractere_codificate)


def decodifica_text(text=None, deplasare=3):
    """
    Procedura ceruta: decodifica_text()
    - Efectueaza operatia inversa codificarii (deplasare inapoi);
    - Returneaza textul decodificat.
    """
    if text is None:
        text = obtine_text_curent()

    caractere_decodificate = []
    for c in text:
        if c == "\n":
            caractere_decodificate.append("\n")
        else:
            cod_nou = (ord(c) - deplasare) % 1114112
            caractere_decodificate.append(chr(cod_nou))

    return "".join(caractere_decodificate)


def verifica_codificare_decodificare(text=None, deplasare=3):
    """
    Procedura care verifica daca textul original este decodificat corect:
    decodifica_text(codifica_text(text, deplasare), deplasare) == text.
    Returneaza un tuplu (este_corect: bool, text_codificat: str, text_decodificat: str).
    """
    if text is None:
        text = obtine_text_curent()

    text_codificat = codifica_text(text, deplasare)
    text_decodificat = decodifica_text(text_codificat, deplasare)
    este_corect = text_decodificat == text
    return este_corect, text_codificat, text_decodificat


# ==============================================================================
# D. ANALIZA PROPOZITIILOR (2 puncte)
# ==============================================================================
def imparte_in_propozitii(text):
    """
    Procedura separata care imparte textul in propozitii.
    Foloseste delimitatorii terminali clasici: punct, semnul exclamarii, semnul intrebarii sau linii noi.
    Returneaza o lista de propozitii curatate (fara spatii albe inutile).
    """
    if not text or not text.strip():
        return []

    # Delimitare dupa . ! ? urmate de spatiu sau linie noua
    bucati = re.split(r"(?<=[.!?])\s+|\n+", text.strip())
    propozitii = []
    for prop in bucati:
        curatat = prop.strip()
        if curatat:
            propozitii.append(curatat)
    return propozitii


def calculeaza_lungime_medie_propozitii(propozitii_sau_text):
    """
    Procedura separata care calculeaza lungimea medie a propozitiilor in cuvinte.
    Returneaza valoarea medie (float rotunjit la 2 zecimale).
    """
    if isinstance(propozitii_sau_text, str):
        propozitii = imparte_in_propozitii(propozitii_sau_text)
    else:
        propozitii = propozitii_sau_text

    if not propozitii:
        return 0.0

    total_cuvinte = sum(numara_cuvinte(prop) for prop in propozitii)
    medie = total_cuvinte / len(propozitii)
    return round(medie, 2)


def gaseste_propozitia_cea_mai_lunga(propozitii_sau_text):
    """
    Procedura separata care gaseste si returneaza propozitia cea mai lunga (dupa numarul de cuvinte).
    Returneaza tuplu: (propozitia: str, numar_cuvinte: int, numar_caractere: int).
    """
    if isinstance(propozitii_sau_text, str):
        propozitii = imparte_in_propozitii(propozitii_sau_text)
    else:
        propozitii = propozitii_sau_text

    if not propozitii:
        return "", 0, 0

    propozitie_max = max(propozitii, key=lambda p: (numara_cuvinte(p), len(p)))
    nr_cuvinte = numara_cuvinte(propozitie_max)
    nr_caractere = len(propozitie_max)
    return propozitie_max, nr_cuvinte, nr_caractere


def analizeaza_propozitii(text=None):
    """
    Procedura agregata care apeleaza procedurile de analiza a propozitiilor
    si returneaza un dictionar complet cu rezultatele.
    """
    if text is None:
        text = obtine_text_curent()

    propozitii = imparte_in_propozitii(text)
    lungime_medie = calculeaza_lungime_medie_propozitii(propozitii)
    prop_lunga, cuvinte_max, caractere_max = gaseste_propozitia_cea_mai_lunga(propozitii)

    return {
        "propozitii": propozitii,
        "numar_propozitii": len(propozitii),
        "lungime_medie_cuvinte": lungime_medie,
        "propozitie_maxima": prop_lunga,
        "cuvinte_propozitie_maxima": cuvinte_max,
        "caractere_propozitie_maxima": caractere_max,
    }


# ==============================================================================
# E. STATISTICA TEXTUALA (2 puncte)
# ==============================================================================
def numara_cuvinte(text):
    """
    Procedura separata care calculeaza numarul total de cuvinte din text.
    """
    if not text:
        return 0
    return len(extrage_cuvinte(text))


def numara_caractere(text, include_spatii=True):
    """
    Procedura separata care calculeaza numarul de caractere (cu sau fara spatii).
    """
    if not text:
        return 0
    if include_spatii:
        return len(text)
    # Eliminam toate tipurile de spatii albe
    return len(re.sub(r"\s", "", text))


def numara_linii(text):
    """
    Procedura separata care calculeaza numarul de linii din text.
    """
    if not text:
        return 0
    return len(text.splitlines()) or 1


def numara_cuvinte_unice(text):
    """
    Procedura separata care calculeaza numarul de cuvinte unice din text.
    """
    if not text:
        return 0
    cuvinte = extrage_cuvinte(text)
    return len(set(cuvinte))


def calculeaza_statistici_text(text=None):
    """
    Procedura care apeleaza toate functiile de calcul statistic si
    returneaza un dictionar cu rezultatele complete.
    """
    if text is None:
        text = obtine_text_curent()

    return {
        "numar_cuvinte": numara_cuvinte(text),
        "numar_caractere_cu_spatii": numara_caractere(text, include_spatii=True),
        "numar_caractere_fara_spatii": numara_caractere(text, include_spatii=False),
        "numar_linii": numara_linii(text),
        "numar_cuvinte_unice": numara_cuvinte_unice(text),
    }


# ==============================================================================
# F. SORTAREA LINIILOR SI PROPOZITIILOR DUPA LUNGIME (1 punct)
# ==============================================================================
def sorteaza_linii_dupa_lungime(text=None, descrescator=False, elimina_goale=False):
    """
    Procedura ceruta: sorteaza liniile textului in functie de lungime.
    - descrescator: False (crescator), True (descrescator);
    - elimina_goale: daca este True, exclude liniile formate doar din spatii albe.
    Returneaza o lista de linii sortate.
    """
    if text is None:
        text = obtine_text_curent()

    if not text:
        return []

    linii = text.splitlines()
    if elimina_goale:
        linii = [linie for linie in linii if linie.strip()]

    # Sortare stabila dupa lungimea liniei (numar de caractere)
    return sorted(linii, key=len, reverse=descrescator)


def sorteaza_propozitii_dupa_lungime(text=None, descrescator=False, criteriu="caractere"):
    """
    Procedura care sorteaza propozitiile textului in functie de lungime.
    - criteriu: 'caractere' sau 'cuvinte'.
    Returneaza lista de propozitii sortate.
    """
    if text is None:
        text = obtine_text_curent()

    propozitii = imparte_in_propozitii(text)
    if criteriu == "cuvinte":
        return sorted(propozitii, key=numara_cuvinte, reverse=descrescator)
    return sorted(propozitii, key=len, reverse=descrescator)


# ==============================================================================
# G. OPERATIUNI CU TEXTUL SI FISIERELE DIN EDITOR
# ==============================================================================
def obtine_text_curent():
    """
    Procedura auxiliara care extrage textul complet din zona de editare.
    Daca zona de editare nu este initializata, returneaza un sir vid.
    """
    global ZONA_TEXT
    if ZONA_TEXT is None:
        return ""
    # Tkinter text widget adauga automat un newline la final ("end-1c" il omite)
    return ZONA_TEXT.get("1.0", "end-1c")


def seteaza_text_curent(text_nou):
    """
    Procedura auxiliara care inlocuieste continutul zonei de editare cu un nou text.
    """
    global ZONA_TEXT, TEXT_MODIFICAT
    if ZONA_TEXT is None:
        return
    ZONA_TEXT.delete("1.0", tk.END)
    ZONA_TEXT.insert("1.0", text_nou)
    TEXT_MODIFICAT = True
    actualizeaza_titlu_fereastra()
    actualizeaza_bara_stare()


def actualizeaza_titlu_fereastra():
    """
    Procedura care actualizeaza titlul ferestrei principale cu numele fisierului deschis
    si indicatorul de modificare (*).
    """
    global FEREASTRA_PRINCIPALA, CALE_FISIER_CURENT, TEXT_MODIFICAT
    if FEREASTRA_PRINCIPALA is None:
        return

    nume = os.path.basename(CALE_FISIER_CURENT) if CALE_FISIER_CURENT else "Fisier Nesalvat"
    modificat = " *" if TEXT_MODIFICAT else ""
    titlu = f"{nume}{modificat} - Redactor de Text Procedural (Laborator 1)"
    FEREASTRA_PRINCIPALA.title(titlu)


def actualizeaza_bara_stare(event=None):
    """
    Procedura apelata la modificarea textului pentru a actualiza informatiile din bara de stare:
    pozitie cursor, linii, cuvinte, caractere.
    """
    global ETICHETA_STARE, ZONA_TEXT
    if ETICHETA_STARE is None or ZONA_TEXT is None:
        return

    text = obtine_text_curent()
    linii = numara_linii(text)
    cuvinte = numara_cuvinte(text)
    caractere = len(text)

    # Aflam pozitia cursorului
    cursor_idx = ZONA_TEXT.index(tk.INSERT)
    linia, coloana = cursor_idx.split(".")

    text_stare = (
        f" Pozitie: Ln {linia}, Col {coloana}  |  "
        f"Linii: {linii}  |  "
        f"Cuvinte: {cuvinte}  |  "
        f"Caractere: {caractere}  |  "
        f"Codare: UTF-8"
    )
    ETICHETA_STARE.config(text=text_stare)


def on_text_modificat(event=None):
    """
    Procedura de tratare a evenimentului de modificare a textului din widget.
    """
    global TEXT_MODIFICAT, ZONA_TEXT
    if ZONA_TEXT and ZONA_TEXT.edit_modified():
        TEXT_MODIFICAT = True
        actualizeaza_titlu_fereastra()
        actualizeaza_bara_stare()
        # Resetam flag-ul intern tkinter pentru a detecta urmatoarele modificari
        ZONA_TEXT.edit_modified(False)


def fisier_nou():
    """
    Procedura pentru crearea unui document nou.
    Solicita confirmare daca exista modificari nesalvate.
    """
    global CALE_FISIER_CURENT, TEXT_MODIFICAT
    if not confirma_salvare_inainte_de_actiune("Creare document nou"):
        return

    seteaza_text_curent("")
    CALE_FISIER_CURENT = None
    TEXT_MODIFICAT = False
    actualizeaza_titlu_fereastra()
    actualizeaza_bara_stare()


def deschide_fisier():
    """
    Procedura pentru deschiderea unui fisier existent de pe disc.
    """
    global CALE_FISIER_CURENT, TEXT_MODIFICAT
    if not confirma_salvare_inainte_de_actiune("Deschidere fisier"):
        return

    cale = filedialog.askopenfilename(
        title="Deschide fisier text",
        filetypes=[
            ("Fisiere Text", "*.txt"),
            ("Fisiere Markdown", "*.md"),
            ("Fisiere Python", "*.py"),
            ("Toate Fisierele", "*.*"),
        ],
    )
    if not cale:
        return

    try:
        with open(cale, "r", encoding="utf-8") as f:
            continut = f.read()
        seteaza_text_curent(continut)
        CALE_FISIER_CURENT = cale
        TEXT_MODIFICAT = False
        actualizeaza_titlu_fereastra()
        actualizeaza_bara_stare()
    except Exception as e:
        messagebox.showerror("Eroare la deschidere", f"Nu s-a putut deschide fisierul:\n{e}")


def salveaza_fisier():
    """
    Procedura pentru salvarea fisierului curent.
    Daca nu are o cale asociata, apeleaza salveaza_fisier_ca().
    """
    global CALE_FISIER_CURENT, TEXT_MODIFICAT
    if CALE_FISIER_CURENT is None:
        return salveaza_fisier_ca()

    try:
        continut = obtine_text_curent()
        with open(CALE_FISIER_CURENT, "w", encoding="utf-8") as f:
            f.write(continut)
        TEXT_MODIFICAT = False
        actualizeaza_titlu_fereastra()
        messagebox.showinfo("Salvare", "Fisierul a fost salvat cu succes!")
        return True
    except Exception as e:
        messagebox.showerror("Eroare la salvare", f"Nu s-a putut salva fisierul:\n{e}")
        return False


def salveaza_fisier_ca():
    """
    Procedura pentru salvarea fisierului cu selectarea unei cai noi.
    """
    global CALE_FISIER_CURENT, TEXT_MODIFICAT
    cale = filedialog.asksaveasfilename(
        title="Salveaza fisierul ca...",
        defaultextension=".txt",
        filetypes=[
            ("Fisiere Text", "*.txt"),
            ("Fisiere Markdown", "*.md"),
            ("Toate Fisierele", "*.*"),
        ],
    )
    if not cale:
        return False

    CALE_FISIER_CURENT = cale
    return salveaza_fisier()


def confirma_salvare_inainte_de_actiune(actiune="Actiune"):
    """
    Procedura care verifica daca textul a fost modificat si ofera optiunea de salvare.
    Returneaza True daca se poate continua, False daca actiunea a fost anulata.
    """
    global TEXT_MODIFICAT
    if not TEXT_MODIFICAT:
        return True

    raspuns = messagebox.askyesnocancel(
        "Modificari nesalvate",
        f"Exista modificari nesalvate in document.\nDoriti sa salvati inainte de '{actiune}'?",
    )
    if raspuns is True:
        return salveaza_fisier()
    elif raspuns is False:
        return True  # Continua fara salvare
    else:
        return False  # Anuleaza actiunea


def inchide_aplicatia():
    """
    Procedura pentru inchiderea controlata a aplicatiei.
    """
    global FEREASTRA_PRINCIPALA
    if confirma_salvare_inainte_de_actiune("Iesire"):
        FEREASTRA_PRINCIPALA.destroy()


def incarca_text_exemplu():
    """
    Procedura utilitara care incarca in editor un text demonstrativ cu diacritice,
    cuvinte repetate si propozitii variate, optim pentru testarea tuturor cerintelor.
    """
    if confirma_salvare_inainte_de_actiune("Incarcare text exemplu"):
        seteaza_text_curent(TEXT_EXEMPLU_DEMO)


# ==============================================================================
# H. FERESTRE SI DIALOGURI UI PENTRU PROCEDURILE LABORATORULUI
# ==============================================================================
def afiseaza_fereastra_rezultat(
    titlu, text_raport, permite_inlocuire=False, text_de_inlocuit=None
):
    """
    Procedura reutilizabila care afiseaza un dialog continand rezultate formatate,
    cu posibilitate de copiere in clipboard sau inlocuire directa in editor.
    """
    global FEREASTRA_PRINCIPALA
    dialog = tk.Toplevel(FEREASTRA_PRINCIPALA)
    dialog.title(titlu)
    dialog.geometry("620x460")
    dialog.transient(FEREASTRA_PRINCIPALA)
    dialog.grab_set()

    # Centrare fereastra dialog
    dialog.update_idletasks()
    x = FEREASTRA_PRINCIPALA.winfo_x() + (FEREASTRA_PRINCIPALA.winfo_width() - 620) // 2
    y = FEREASTRA_PRINCIPALA.winfo_y() + (FEREASTRA_PRINCIPALA.winfo_height() - 460) // 2
    dialog.geometry(f"+{max(0, x)}+{max(0, y)}")

    cadru_continut = ttk.Frame(dialog, padding="10")
    cadru_continut.pack(fill=tk.BOTH, expand=True)

    # Zona text cu scrollbar
    cadru_text = ttk.Frame(cadru_continut)
    cadru_text.pack(fill=tk.BOTH, expand=True, pady=(0, 10))

    scroll_v = ttk.Scrollbar(cadru_text, orient=tk.VERTICAL)
    zona_afisare = tk.Text(
        cadru_text,
        wrap="word",
        font=("Courier", 11),
        yscrollcommand=scroll_v.set,
        bg="#fcfcfc",
        relief=tk.SOLID,
        bd=1,
    )
    scroll_v.config(command=zona_afisare.yview)
    scroll_v.pack(side=tk.RIGHT, fill=tk.Y)
    zona_afisare.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

    zona_afisare.insert("1.0", text_raport)
    zona_afisare.config(state=tk.DISABLED)

    # Bara de butoane
    cadru_butoane = ttk.Frame(cadru_continut)
    cadru_butoane.pack(fill=tk.X)

    def copiaza_in_clipboard():
        dialog.clipboard_clear()
        dialog.clipboard_append(text_raport)
        messagebox.showinfo("Copiat", "Rezultatul a fost copiat in clipboard!", parent=dialog)

    btn_copiaza = ttk.Button(
        cadru_butoane, text="📋 Copiaza rezultatul", command=copiaza_in_clipboard
    )
    btn_copiaza.pack(side=tk.LEFT, padx=4)

    if permite_inlocuire and text_de_inlocuit is not None:

        def inlocuieste():
            if messagebox.askyesno(
                "Confirmare",
                "Doriti sa inlocuiti continutul din editor cu rezultatul sortat?",
                parent=dialog,
            ):
                seteaza_text_curent(text_de_inlocuit)
                dialog.destroy()

        btn_inlocuieste = ttk.Button(
            cadru_butoane, text="🔄 Aplica in Editor", command=inlocuieste
        )
        btn_inlocuieste.pack(side=tk.LEFT, padx=4)

    btn_inchide = ttk.Button(cadru_butoane, text="Inchide", command=dialog.destroy)
    btn_inchide.pack(side=tk.RIGHT, padx=4)


def actiune_frecvent_cuvant():
    """
    Procedura UI: Apeleaza gaseste_cel_mai_frecvent(tip='cuvant') si afiseaza rezultatele.
    """
    text = obtine_text_curent()
    if not text.strip():
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    cuvant_max, nr_aparitii, frecvente = gaseste_cel_mai_frecvent(text, tip="cuvant")

    # Sortam primele 15 cuvinte cele mai frecvente pentru o vizualizare utila
    top_cuvinte = sorted(frecvente.items(), key=lambda x: x[1], reverse=True)[:15]

    linii_raport = [
        "============================================================",
        "  CAUTAREA CELUI MAI FRECVENT CUVANT (Cerinta a)",
        "============================================================",
        f"Cel mai frecvent cuvant : '{cuvant_max}'",
        f"Numar de aparitii       : {nr_aparitii}",
        f"Total cuvinte procesate : {len(extrage_cuvinte(text))}",
        f"Total cuvinte unice     : {len(frecvente)}",
        "------------------------------------------------------------",
        "Top 15 cuvinte dupa frecventa:",
        "------------------------------------------------------------",
    ]
    for idx, (cuv, frecv) in enumerate(top_cuvinte, 1):
        linii_raport.append(f"{idx:2d}. {cuv:<25} -> {frecv} aparitii")

    afiseaza_fereastra_rezultat("Cel Mai Frecvent Cuvant", "\n".join(linii_raport))


def actiune_frecvent_expresie():
    """
    Procedura UI: Solicita dimensiunea n-gramei si gaseste cea mai frecventa expresie.
    """
    text = obtine_text_curent()
    if not text.strip():
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    dimensiune = simpledialog.askinteger(
        "Dimensiune Expresie",
        "Introduceti numarul de cuvinte dintr-o expresie (ex: 2 sau 3):",
        initialvalue=2,
        minvalue=2,
        maxvalue=10,
    )
    if not dimensiune:
        return

    expresie_max, nr_aparitii, frecvente = gaseste_cel_mai_frecvent(
        text, tip="expresie", lungime_expresie=dimensiune
    )

    if not expresie_max:
        messagebox.showinfo(
            "Rezultat",
            f"Nu exista suficiente cuvinte in text pentru a forma expresii de {dimensiune} cuvinte.",
        )
        return

    top_expresii = sorted(frecvente.items(), key=lambda x: x[1], reverse=True)[:15]

    linii_raport = [
        "============================================================",
        f"  CAUTAREA CELEI MAI FRECVENTE EXPRESII ({dimensiune} cuvinte)",
        "============================================================",
        f"Cea mai frecventa expresie : '{expresie_max}'",
        f"Numar de aparitii          : {nr_aparitii}",
        f"Total expresii unice       : {len(frecvente)}",
        "------------------------------------------------------------",
        f"Top 15 expresii ({dimensiune} cuvinte) dupa frecventa:",
        "------------------------------------------------------------",
    ]
    for idx, (expr, frecv) in enumerate(top_expresii, 1):
        linii_raport.append(f"{idx:2d}. '{expr}' -> {frecv} aparitii")

    afiseaza_fereastra_rezultat("Cea Mai Frecventa Expresie", "\n".join(linii_raport))


def actiune_cauta_expresie_specifica():
    """
    Procedura UI: Solicita utilizatorului un cuvant sau expresie si ii calculeaza frecventa.
    """
    text = obtine_text_curent()
    if not text.strip():
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    expresie = simpledialog.askstring(
        "Cauta Expresie", "Introduceti cuvantul sau expresia pe care doriti sa o cautati:"
    )
    if not expresie:
        return

    nr = calculeaza_frecventa_expresie_specifica(text, expresie)
    messagebox.showinfo(
        "Frecventa Expresie",
        f"Expresia: '{expresie}'\nNumar de aparitii in text: {nr}",
    )


def actiune_hash_text():
    """
    Procedura UI: Calculeaza si afiseaza hash-urile textului curent (DJB2 si Suma ASCII).
    """
    text = obtine_text_curent()
    if not text:
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    hash_djb2_val = hash_text(text, algoritm="djb2")
    hash_ascii_val = hash_text(text, algoritm="ascii_sum")

    linii_raport = [
        "============================================================",
        "  HASH-UL TEXTULUI (Cerinta b)",
        "============================================================",
        f"Caractere procesate      : {len(text)}",
        "------------------------------------------------------------",
        "1. Algoritmul DJB2 (Dan Bernstein - 64-bit):",
        f"   Valoare zecimala      : {hash_djb2_val}",
        f"   Valoare hexazecimala  : {hex(hash_djb2_val)}",
        "",
        "2. Suma Codurilor ASCII / Unicode:",
        f"   Valoare zecimala      : {hash_ascii_val}",
        f"   Valoare hexazecimala  : {hex(hash_ascii_val)}",
        "============================================================",
        "Nota: Functia procedurala 'hash_text(text, algoritm)' calculeaza",
        "oricare dintre aceste doua hash-uri conform specificatiei din lab.",
    ]
    afiseaza_fereastra_rezultat("Hash-ul Textului", "\n".join(linii_raport))


def actiune_codifica_text():
    """
    Procedura UI: Solicita cheia de deplasare si codifica textul direct in editor.
    """
    global ULTIMA_DEPLASARE
    text = obtine_text_curent()
    if not text:
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    deplasare = simpledialog.askinteger(
        "Codificare Text",
        "Introduceti valoarea de deplasare (cheia cifrului Cezar):",
        initialvalue=ULTIMA_DEPLASARE,
        minvalue=1,
        maxvalue=1000,
    )
    if deplasare is None:
        return

    ULTIMA_DEPLASARE = deplasare
    text_codificat = codifica_text(text, deplasare=deplasare)
    seteaza_text_curent(text_codificat)
    messagebox.showinfo(
        "Codificare Reusita",
        f"Textul a fost codificat cu succes folosind o deplasare de +{deplasare} pozitii.",
    )


def actiune_decodifica_text():
    """
    Procedura UI: Solicita cheia de deplasare si decodifica textul direct in editor.
    """
    global ULTIMA_DEPLASARE
    text = obtine_text_curent()
    if not text:
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    deplasare = simpledialog.askinteger(
        "Decodificare Text",
        "Introduceti valoarea de deplasare folosita la codificare:",
        initialvalue=ULTIMA_DEPLASARE,
        minvalue=1,
        maxvalue=1000,
    )
    if deplasare is None:
        return

    ULTIMA_DEPLASARE = deplasare
    text_decodificat = decodifica_text(text, deplasare=deplasare)
    seteaza_text_curent(text_decodificat)
    messagebox.showinfo(
        "Decodificare Reusita",
        f"Textul a fost decodificat cu succes folosind o deplasare de -{deplasare} pozitii.",
    )


def actiune_verifica_codificare():
    """
    Procedura UI: Verifica ca textul original este decodificat corect
    fara a altera textul din editor.
    """
    global ULTIMA_DEPLASARE
    text = obtine_text_curent()
    if not text:
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    deplasare = simpledialog.askinteger(
        "Verificare Codificare/Decodificare",
        "Introduceti valoarea de deplasare pentru test:",
        initialvalue=ULTIMA_DEPLASARE,
        minvalue=1,
        maxvalue=1000,
    )
    if deplasare is None:
        return

    este_corect, text_codificat, text_decodificat = verifica_codificare_decodificare(
        text, deplasare
    )

    esantion_original = text[:150] + ("..." if len(text) > 150 else "")
    esantion_codificat = text_codificat[:150] + ("..." if len(text_codificat) > 150 else "")
    esantion_decodificat = text_decodificat[:150] + (
        "..." if len(text_decodificat) > 150 else ""
    )

    linii_raport = [
        "============================================================",
        "  VERIFICARE CODIFICARE SI DECODIFICARE (Cerinta c)",
        "============================================================",
        f"Deplasare utilizata  : {deplasare}",
        f"Lungime text         : {len(text)} caractere",
        f"Rezultat Verificare  : {'[SUCCES] Text decodificat 100% IDENTIC' if este_corect else '[EROARE] Nepotrivire'}",
        "------------------------------------------------------------",
        "Esantion Text Original:",
        esantion_original,
        "------------------------------------------------------------",
        "Esantion Text Codificat:",
        esantion_codificat,
        "------------------------------------------------------------",
        "Esantion Text Decodificat:",
        esantion_decodificat,
        "============================================================",
        "Proprietatea: decodifica_text(codifica_text(T, k), k) == T este respectata!",
    ]
    afiseaza_fereastra_rezultat("Verificare Codificare / Decodificare", "\n".join(linii_raport))


def actiune_analiza_propozitii():
    """
    Procedura UI: Calculeaza si afiseaza raportul de analiza a propozitiilor (Cerinta d).
    """
    text = obtine_text_curent()
    if not text.strip():
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    rezultate = analizeaza_propozitii(text)
    propozitii = rezultate["propozitii"]

    linii_raport = [
        "============================================================",
        "  ANALIZA PROPOZITIILOR (Cerinta d)",
        "============================================================",
        f"Numar total de propozitii         : {rezultate['numar_propozitii']}",
        f"Lungimea medie (in cuvinte)       : {rezultate['lungime_medie_cuvinte']} cuvinte/propozitie",
        "------------------------------------------------------------",
        "PROPOZITIA CEA MAI LUNGA:",
        f"Numar de cuvinte   : {rezultate['cuvinte_propozitie_maxima']}",
        f"Numar de caractere : {rezultate['caractere_propozitie_maxima']}",
        f"Textul propozitiei : \"{rezultate['propozitie_maxima']}\"",
        "------------------------------------------------------------",
        "LISTA TUTUROR PROPOZITIILOR IDENTIFICATE:",
        "------------------------------------------------------------",
    ]

    for idx, prop in enumerate(propozitii, 1):
        nr_c = numara_cuvinte(prop)
        linii_raport.append(f"[{idx:02d}] ({nr_c:2d} cuv, {len(prop):3d} car) : {prop}")

    afiseaza_fereastra_rezultat("Analiza Propozitiilor", "\n".join(linii_raport))


def actiune_statistica_textuala():
    """
    Procedura UI: Calculeaza si afiseaza statisticile textuale cerute (Cerinta e).
    """
    text = obtine_text_curent()
    if not text:
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    stat = calculeaza_statistici_text(text)

    # Calculam si frecventa propozitiilor daca exista
    propozitii = imparte_in_propozitii(text)
    medie_prop = calculeaza_lungime_medie_propozitii(propozitii)

    linii_raport = [
        "============================================================",
        "  STATISTICA TEXTUALA (Cerinta e)",
        "============================================================",
        f"1. Numar total de cuvinte           : {stat['numar_cuvinte']}",
        f"2. Numar de caractere (cu spatii)   : {stat['numar_caractere_cu_spatii']}",
        f"   Numar de caractere (fara spatii) : {stat['numar_caractere_fara_spatii']}",
        f"3. Numar de linii                   : {stat['numar_linii']}",
        f"4. Numar de cuvinte unice           : {stat['numar_cuvinte_unice']}",
        "------------------------------------------------------------",
        "INFORMATII SUPLIMENTARE:",
        f"- Numar de propozitii               : {len(propozitii)}",
        f"- Lungime medie propozitie          : {medie_prop} cuvinte",
        f"- Densitate lexicala (unice/total)  : {round((stat['numar_cuvinte_unice'] / stat['numar_cuvinte'] * 100), 2) if stat['numar_cuvinte'] else 0}%",
        "============================================================",
        "Toate valorile au fost calculate prin proceduri dedicate:",
        "numara_cuvinte(), numara_caractere(), numara_linii(), numara_cuvinte_unice()",
    ]

    afiseaza_fereastra_rezultat("Statistica Textuala", "\n".join(linii_raport))


def actiune_sorteaza_linii():
    """
    Procedura UI: Sorteaza liniile textului dupa lungime si afiseaza rezultatul (Cerinta f).
    Permite aplicarea rezultatului inapoi in editor.
    """
    text = obtine_text_curent()
    if not text:
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    # Dialog pentru a alege ordinea
    ordine = messagebox.askyesno(
        "Ordine de sortare",
        "Doriti sortare CRESCATOARE dupa lungime?\n\n(Apasati 'Yes' pentru Crescator, 'No' pentru Descrescator)",
    )
    descrescator = not ordine

    linii_sortate = sorteaza_linii_dupa_lungime(
        text, descrescator=descrescator, elimina_goale=False
    )
    text_sortat = "\n".join(linii_sortate)

    directie = "DESCRESCATOR" if descrescator else "CRESCATOR"
    linii_raport = [
        "============================================================",
        f"  SORTAREA LINIILOR DUPA LUNGIME ({directie}) (Cerinta f)",
        "============================================================",
        f"Total linii sortate: {len(linii_sortate)}",
        "Puteti apasa 'Aplica in Editor' pentru a inlocui textul curent.",
        "------------------------------------------------------------",
    ]
    for idx, linie in enumerate(linii_sortate, 1):
        linii_raport.append(f"[{len(linie):3d} caractere] : {linie}")

    afiseaza_fereastra_rezultat(
        f"Linii Sortate ({directie})",
        "\n".join(linii_raport),
        permite_inlocuire=True,
        text_de_inlocuit=text_sortat,
    )


def actiune_sorteaza_propozitii():
    """
    Procedura UI: Sorteaza propozitiile textului dupa lungime (caractere sau cuvinte).
    """
    text = obtine_text_curent()
    if not text.strip():
        messagebox.showwarning("Atentie", "Editorul de text este gol!")
        return

    ordine = messagebox.askyesno(
        "Ordine de sortare",
        "Doriti sortare CRESCATOARE dupa lungime?\n\n(Apasati 'Yes' pentru Crescator, 'No' pentru Descrescator)",
    )
    descrescator = not ordine

    propozitii_sortate = sorteaza_propozitii_dupa_lungime(
        text, descrescator=descrescator, criteriu="caractere"
    )
    text_sortat = " ".join(propozitii_sortate)

    directie = "DESCRESCATOR" if descrescator else "CRESCATOR"
    linii_raport = [
        "============================================================",
        f"  SORTAREA PROPOZITIILOR DUPA LUNGIME ({directie})",
        "============================================================",
        f"Total propozitii sortate: {len(propozitii_sortate)}",
        "Puteti apasa 'Aplica in Editor' pentru a inlocui textul curent.",
        "------------------------------------------------------------",
    ]
    for idx, prop in enumerate(propozitii_sortate, 1):
        linii_raport.append(
            f"[{len(prop):3d} car, {numara_cuvinte(prop):2d} cuv] : {prop}"
        )

    afiseaza_fereastra_rezultat(
        f"Propozitii Sortate ({directie})",
        "\n".join(linii_raport),
        permite_inlocuire=True,
        text_de_inlocuit=text_sortat,
    )


def actiune_despre():
    """
    Procedura UI: Afiseaza dialogul Despre Aplicatie.
    """
    mesaj = (
        "Redactor de Text Procedural\n"
        "Lucrarea de Laborator Nr. 1 - Paradigma Procedurala\n\n"
        "Caracteristici cheie ale paradigmei:\n"
        "• Fara clase (programare pur procedurala)\n"
        "• Modularitate prin proceduri si functii descompuse logic\n"
        "• Variabile globale de stare bine definite\n"
        "• Structuri de date de baza: liste, tupluri, dictionare\n\n"
        "Functionalitati implementate:\n"
        "a. Cel mai frecvent cuvant / expresie\n"
        "b. Hash-ul textului (DJB2 & Suma ASCII)\n"
        "c. Codificare / Decodificare Cezar\n"
        "d. Analiza propozitiilor (impartire, medie, propozitie maxima)\n"
        "e. Statistica textuala (cuvinte, caractere, linii, cuvinte unice)\n"
        "f. Sortarea liniilor si propozitiilor dupa lungime"
    )
    messagebox.showinfo("Despre Redactor Procedural", mesaj)


# ==============================================================================
# I. CONSTRUIREA INTERFETEI GRAFICE (TKINTER PROCEDURAL)
# ==============================================================================
def creeaza_meniu_aplicatie(fereastra):
    """
    Procedura care construieste bara de meniu a aplicatiei.
    """
    bara_meniu = tk.Menu(fereastra)

    # 1. Meniul Fisier
    meniu_fisier = tk.Menu(bara_meniu, tearoff=0)
    meniu_fisier.add_command(label="📄 Nou", command=fisier_nou, accelerator="Ctrl+N")
    meniu_fisier.add_command(
        label="📂 Deschide...", command=deschide_fisier, accelerator="Ctrl+O"
    )
    meniu_fisier.add_command(
        label="💾 Salveaza", command=salveaza_fisier, accelerator="Ctrl+S"
    )
    meniu_fisier.add_command(
        label="💾 Salveaza ca...", command=salveaza_fisier_ca, accelerator="Ctrl+Shift+S"
    )
    meniu_fisier.add_separator()
    meniu_fisier.add_command(label="💡 Incarca Text Exemplu", command=incarca_text_exemplu)
    meniu_fisier.add_separator()
    meniu_fisier.add_command(label="❌ Iesire", command=inchide_aplicatia)
    bara_meniu.add_cascade(label="Fisier", menu=meniu_fisier)

    # 2. Meniul Editare
    meniu_editare = tk.Menu(bara_meniu, tearoff=0)

    def undo():
        try:
            ZONA_TEXT.edit_undo()
        except tk.TclError:
            pass

    def redo():
        try:
            ZONA_TEXT.edit_redo()
        except tk.TclError:
            pass

    def decupeaza():
        ZONA_TEXT.event_generate("<<Cut>>")

    def copiaza():
        ZONA_TEXT.event_generate("<<Copy>>")

    def lipeste():
        ZONA_TEXT.event_generate("<<Paste>>")

    def selecteaza_tot():
        ZONA_TEXT.tag_add("sel", "1.0", "end")

    meniu_editare.add_command(label="Anuleaza (Undo)", command=undo, accelerator="Ctrl+Z")
    meniu_editare.add_command(label="Refa (Redo)", command=redo, accelerator="Ctrl+Y")
    meniu_editare.add_separator()
    meniu_editare.add_command(label="Decupeaza", command=decupeaza, accelerator="Ctrl+X")
    meniu_editare.add_command(label="Copiaza", command=copiaza, accelerator="Ctrl+C")
    meniu_editare.add_command(label="Lipeste", command=lipeste, accelerator="Ctrl+V")
    meniu_editare.add_separator()
    meniu_editare.add_command(label="Selecteaza tot", command=selecteaza_tot, accelerator="Ctrl+A")
    bara_meniu.add_cascade(label="Editare", menu=meniu_editare)

    # 3. Meniul Proceduri Laborator 1
    meniu_lab = tk.Menu(bara_meniu, tearoff=0)

    # a. Frecventa
    meniu_frecventa = tk.Menu(meniu_lab, tearoff=0)
    meniu_frecventa.add_command(
        label="Cel mai frecvent cuvant", command=actiune_frecvent_cuvant
    )
    meniu_frecventa.add_command(
        label="Cea mai frecventa expresie (n-grame)...", command=actiune_frecvent_expresie
    )
    meniu_frecventa.add_command(
        label="Cauta frecventa expresie specifica...", command=actiune_cauta_expresie_specifica
    )
    meniu_lab.add_cascade(label="a. Frecventa Cuvinte / Expresii", menu=meniu_frecventa)

    # b. Hash
    meniu_lab.add_command(label="b. Calculeaza Hash Text (DJB2 / ASCII)", command=actiune_hash_text)

    # c. Codificare / Decodificare
    meniu_codificare = tk.Menu(meniu_lab, tearoff=0)
    meniu_codificare.add_command(label="Codifica textul curent...", command=actiune_codifica_text)
    meniu_codificare.add_command(
        label="Decodifica textul curent...", command=actiune_decodifica_text
    )
    meniu_codificare.add_separator()
    meniu_codificare.add_command(
        label="Verifica corectitudine codificare/decodificare",
        command=actiune_verifica_codificare,
    )
    meniu_lab.add_cascade(label="c. Codificare / Decodificare", menu=meniu_codificare)

    # d. Analiza Propozitiilor
    meniu_propozitii = tk.Menu(meniu_lab, tearoff=0)
    meniu_propozitii.add_command(
        label="Raport complet analiza propozitii", command=actiune_analiza_propozitii
    )
    meniu_lab.add_cascade(label="d. Analiza Propozitiilor", menu=meniu_propozitii)

    # e. Statistica Textuala
    meniu_lab.add_command(
        label="e. Statistica Textuala Completa", command=actiune_statistica_textuala
    )

    # f. Sortare Linii / Propozitii
    meniu_sortare = tk.Menu(meniu_lab, tearoff=0)
    meniu_sortare.add_command(
        label="Sorteaza liniile dupa lungime...", command=actiune_sorteaza_linii
    )
    meniu_sortare.add_command(
        label="Sorteaza propozitiile dupa lungime...", command=actiune_sorteaza_propozitii
    )
    meniu_lab.add_cascade(label="f. Sortare dupa Lungime", menu=meniu_sortare)

    bara_meniu.add_cascade(label="Laborator 1", menu=meniu_lab)

    # 4. Meniul Ajutor
    meniu_ajutor = tk.Menu(bara_meniu, tearoff=0)
    meniu_ajutor.add_command(label="Despre Proiect", command=actiune_despre)
    bara_meniu.add_cascade(label="Ajutor", menu=meniu_ajutor)

    fereastra.config(menu=bara_meniu)


def creeaza_bara_instrumente(parinte):
    """
    Procedura care adauga o bara de butoane rapide in partea de sus a ferestrei.
    """
    cadru_toolbar = ttk.Frame(parinte, padding="3")
    cadru_toolbar.pack(side=tk.TOP, fill=tk.X)

    # Butoane Fisier
    btn_nou = ttk.Button(cadru_toolbar, text="📄 Nou", width=7, command=fisier_nou)
    btn_nou.pack(side=tk.LEFT, padx=2)

    btn_deschide = ttk.Button(cadru_toolbar, text="📂 Deschide", width=10, command=deschide_fisier)
    btn_deschide.pack(side=tk.LEFT, padx=2)

    btn_salveaza = ttk.Button(cadru_toolbar, text="💾 Salveaza", width=10, command=salveaza_fisier)
    btn_salveaza.pack(side=tk.LEFT, padx=2)

    ttk.Separator(cadru_toolbar, orient=tk.VERTICAL).pack(
        side=tk.LEFT, fill=tk.Y, padx=6, pady=2
    )

    # Butoane Proceduri Laborator
    btn_statistici = ttk.Button(
        cadru_toolbar, text="📊 Statistici", width=10, command=actiune_statistica_textuala
    )
    btn_statistici.pack(side=tk.LEFT, padx=2)

    btn_frecvent = ttk.Button(
        cadru_toolbar, text="🔠 Frecventa", width=10, command=actiune_frecvent_cuvant
    )
    btn_frecvent.pack(side=tk.LEFT, padx=2)

    btn_hash = ttk.Button(
        cadru_toolbar, text="🔒 Hash", width=8, command=actiune_hash_text
    )
    btn_hash.pack(side=tk.LEFT, padx=2)

    btn_codifica = ttk.Button(
        cadru_toolbar, text="🔐 Codifica", width=9, command=actiune_codifica_text
    )
    btn_codifica.pack(side=tk.LEFT, padx=2)

    btn_decodifica = ttk.Button(
        cadru_toolbar, text="🔓 Decodifica", width=10, command=actiune_decodifica_text
    )
    btn_decodifica.pack(side=tk.LEFT, padx=2)

    btn_propozitii = ttk.Button(
        cadru_toolbar, text="📜 Propozitii", width=10, command=actiune_analiza_propozitii
    )
    btn_propozitii.pack(side=tk.LEFT, padx=2)

    btn_sortare = ttk.Button(
        cadru_toolbar, text="↕️ Sorteaza Linii", width=13, command=actiune_sorteaza_linii
    )
    btn_sortare.pack(side=tk.LEFT, padx=2)

    ttk.Separator(cadru_toolbar, orient=tk.VERTICAL).pack(
        side=tk.LEFT, fill=tk.Y, padx=6, pady=2
    )

    btn_demo = ttk.Button(
        cadru_toolbar, text="💡 Text Exemplu", width=13, command=incarca_text_exemplu
    )
    btn_demo.pack(side=tk.LEFT, padx=2)


def creeaza_zona_editare(parinte):
    """
    Procedura care creeaza zona principala de editare a textului cu scrollbars.
    """
    global ZONA_TEXT
    cadru_central = ttk.Frame(parinte)
    cadru_central.pack(fill=tk.BOTH, expand=True)

    scroll_y = ttk.Scrollbar(cadru_central, orient=tk.VERTICAL)
    scroll_x = ttk.Scrollbar(cadru_central, orient=tk.HORIZONTAL)

    ZONA_TEXT = tk.Text(
        cadru_central,
        wrap="word",
        undo=True,
        maxundo=-1,
        font=("Menlo", 12) if sys.platform == "darwin" else ("Consolas", 11),
        yscrollcommand=scroll_y.set,
        xscrollcommand=scroll_x.set,
        bg="#ffffff",
        fg="#1e1e1e",
        insertbackground="#000000",
        relief=tk.FLAT,
        padx=10,
        pady=10,
    )

    scroll_y.config(command=ZONA_TEXT.yview)
    scroll_x.config(command=ZONA_TEXT.xview)

    scroll_y.pack(side=tk.RIGHT, fill=tk.Y)
    scroll_x.pack(side=tk.BOTTOM, fill=tk.X)
    ZONA_TEXT.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

    # Legaturi de evenimente pentru bara de stare si detectarea modificarilor
    ZONA_TEXT.bind("<<Modified>>", on_text_modificat)
    ZONA_TEXT.bind("<KeyRelease>", actualizeaza_bara_stare)
    ZONA_TEXT.bind("<ButtonRelease-1>", actualizeaza_bara_stare)


def creeaza_bara_stare(parinte):
    """
    Procedura care creeaza bara de stare in partea de jos a ferestrei.
    """
    global ETICHETA_STARE
    cadru_stare = ttk.Frame(parinte, relief=tk.SUNKEN, padding="2")
    cadru_stare.pack(side=tk.BOTTOM, fill=tk.X)

    ETICHETA_STARE = ttk.Label(
        cadru_stare,
        text=" Pozitie: Ln 1, Col 0  |  Linii: 0  |  Cuvinte: 0  |  Caractere: 0  |  Codare: UTF-8",
        anchor=tk.W,
    )
    ETICHETA_STARE.pack(side=tk.LEFT, fill=tk.X)


def configureaza_scurtaturi_tastatura(fereastra):
    """
    Procedura care configureaza scurtaturile standard de tastatura pentru aplicatie.
    """
    # Suport atat pentru Windows/Linux (Control) cat si pentru Mac (Command)
    modificatori = ["<Control-n>", "<Command-n>"]
    for m in modificatori:
        fereastra.bind(m, lambda e: (fisier_nou(), "break")[1])

    modificatori_o = ["<Control-o>", "<Command-o>"]
    for m in modificatori_o:
        fereastra.bind(m, lambda e: (deschide_fisier(), "break")[1])

    modificatori_s = ["<Control-s>", "<Command-s>"]
    for m in modificatori_s:
        fereastra.bind(m, lambda e: (salveaza_fisier(), "break")[1])

    modificatori_as = ["<Control-Shift-S>", "<Command-Shift-S>"]
    for m in modificatori_as:
        fereastra.bind(m, lambda e: (salveaza_fisier_ca(), "break")[1])


def creeaza_fereastra_principala():
    """
    Procedura de asamblare a intregii interfete grafice a aplicatiei.
    Initializeaza fereastra tkinter, aplica tema si construieste componentele.
    """
    global FEREASTRA_PRINCIPALA
    FEREASTRA_PRINCIPALA = tk.Tk()
    FEREASTRA_PRINCIPALA.geometry("960x640")
    FEREASTRA_PRINCIPALA.minsize(700, 400)

    # Protocol de inchidere fereastra
    FEREASTRA_PRINCIPALA.protocol("WM_DELETE_WINDOW", inchide_aplicatia)

    # Construire componente
    creeaza_meniu_aplicatie(FEREASTRA_PRINCIPALA)
    creeaza_bara_instrumente(FEREASTRA_PRINCIPALA)
    creeaza_bara_stare(FEREASTRA_PRINCIPALA)
    creeaza_zona_editare(FEREASTRA_PRINCIPALA)
    configureaza_scurtaturi_tastatura(FEREASTRA_PRINCIPALA)

    # Incarcare text demonstrativ initial
    seteaza_text_curent(TEXT_EXEMPLU_DEMO)
    actualizeaza_titlu_fereastra()
    actualizeaza_bara_stare()

    # Focalizare pe zona de text
    if ZONA_TEXT:
        ZONA_TEXT.focus_set()

    # Aducere fereastra in prim-plan
    FEREASTRA_PRINCIPALA.lift()
    FEREASTRA_PRINCIPALA.attributes("-topmost", True)
    FEREASTRA_PRINCIPALA.after(200, lambda: FEREASTRA_PRINCIPALA.attributes("-topmost", False))

    return FEREASTRA_PRINCIPALA


# ==============================================================================
# J. SUITA DE TESTE AUTOMATE PENTRU VERIFICAREA PROCEDURILOR
# ==============================================================================
def ruleaza_teste_automate():
    """
    Procedura care testeaza automat si riguros toate functiile implementate,
    fara a necesita lansarea interfetei grafice.
    Poate fi rulata cu: python3 main.py --test
    """
    print("=" * 60)
    print("RULARE SUITA DE TESTE AUTOMATE PENTRU PROCEDURILE DIN LAB 1")
    print("=" * 60)

    text_test = (
        "Ana are mere rosii si dulci. "
        "Ionel are mere verzi si pere galbene! "
        "Bunica vrea mere proaspete. "
        "Merele sunt foarte bune? "
        "Da, merele sunt delicioase."
    )

    # Test a: Frecventa
    print("\n[TEST A] Frecventa cuvinte si expresii...")
    cuv_max, nr_c, frecv_c = gaseste_cel_mai_frecvent(text_test, tip="cuvant")
    print(f"  -> Cel mai frecvent cuvant: '{cuv_max}' cu {nr_c} aparitii.")
    assert cuv_max == "mere", f"Asteptat 'mere', obtinut '{cuv_max}'"
    assert nr_c == 3, f"Asteptat 3, obtinut {nr_c}"

    expr_max, nr_e, frecv_e = gaseste_cel_mai_frecvent(text_test, tip="expresie", lungime_expresie=2)
    print(f"  -> Cea mai frecventa expresie (2 cuv): '{expr_max}' cu {nr_e} aparitii.")
    assert nr_e >= 1, "Trebuie sa existe cel putin o expresie de 2 cuvinte"

    nr_sp = calculeaza_frecventa_expresie_specifica(text_test, "are mere")
    print(f"  -> Frecventa expresiei 'are mere': {nr_sp}")
    assert nr_sp == 2, f"Asteptat 2 pentru 'are mere', obtinut {nr_sp}"
    print("  [OK] Testul A a trecut cu succes.")

    # Test b: Hash
    print("\n[TEST B] Hash-ul textului...")
    h_djb2 = hash_text("test", algoritm="djb2")
    h_ascii = hash_text("ABC", algoritm="ascii_sum")
    print(f"  -> Hash DJB2 ('test'): {h_djb2} ({hex(h_djb2)})")
    print(f"  -> Hash ASCII sum ('ABC'): {h_ascii} (A=65 + B=66 + C=67 = 198)")
    assert h_ascii == 65 + 66 + 67, f"Asteptat 198, obtinut {h_ascii}"
    assert h_djb2 > 0, "Hash DJB2 trebuie sa fie pozitiv"
    print("  [OK] Testul B a trecut cu succes.")

    # Test c: Codificare si Decodificare
    print("\n[TEST C] Codificare si decodificare...")
    original = "Programare Procedurala 2026! Diacritice: ăâîșț."
    codificat = codifica_text(original, deplasare=5)
    decodificat = decodifica_text(codificat, deplasare=5)
    print(f"  -> Original   : {original}")
    print(f"  -> Codificat  : {codificat}")
    print(f"  -> Decodificat: {decodificat}")
    assert decodificat == original, "Decodificarea nu corespunde cu textul original!"
    este_ok, _, _ = verifica_codificare_decodificare(original, 7)
    assert este_ok is True, "Functia verifica_codificare_decodificare a returnat False!"
    print("  [OK] Testul C a trecut cu succes.")

    # Test d: Analiza propozitiilor
    print("\n[TEST D] Analiza propozitiilor...")
    propozitii = imparte_in_propozitii(text_test)
    medie = calculeaza_lungime_medie_propozitii(propozitii)
    prop_lunga, c_max, car_max = gaseste_propozitia_cea_mai_lunga(propozitii)
    print(f"  -> Propozitii identificate ({len(propozitii)}):")
    for i, p in enumerate(propozitii, 1):
        print(f"     {i}. {p}")
    print(f"  -> Lungime medie: {medie} cuvinte/propozitie")
    print(f"  -> Propozitia cea mai lunga: '{prop_lunga}' ({c_max} cuvinte)")
    assert len(propozitii) == 5, f"Asteptat 5 propozitii, obtinut {len(propozitii)}"
    assert c_max > 0, "Propozitia cea mai lunga trebuie sa aiba cuvinte"
    print("  [OK] Testul D a trecut cu succes.")

    # Test e: Statistica textuala
    print("\n[TEST E] Statistica textuala...")
    c_tot = numara_cuvinte(text_test)
    car_tot = numara_caractere(text_test, include_spatii=True)
    car_fara = numara_caractere(text_test, include_spatii=False)
    linii_tot = numara_linii(text_test)
    unice_tot = numara_cuvinte_unice(text_test)
    print(f"  -> Total cuvinte       : {c_tot}")
    print(f"  -> Caractere cu spatii : {car_tot}")
    print(f"  -> Caractere fara spatii: {car_fara}")
    print(f"  -> Linii               : {linii_tot}")
    print(f"  -> Cuvinte unice       : {unice_tot}")
    assert c_tot > 0 and car_tot > car_fara and linii_tot >= 1 and unice_tot <= c_tot
    print("  [OK] Testul E a trecut cu succes.")

    # Test f: Sortare dupa lungime
    print("\n[TEST F] Sortarea liniilor si propozitiilor dupa lungime...")
    text_linii = "O linie scurta\nAceasta este o linie mult mai lunga decat prima\nMedie"
    sort_cresc = sorteaza_linii_dupa_lungime(text_linii, descrescator=False)
    sort_desc = sorteaza_linii_dupa_lungime(text_linii, descrescator=True)
    print(f"  -> Crescator: {sort_cresc}")
    print(f"  -> Descrescator: {sort_desc}")
    assert len(sort_cresc[0]) <= len(sort_cresc[-1]), "Sortarea crescatoare a esuat"
    assert len(sort_desc[0]) >= len(sort_desc[-1]), "Sortarea descrescatoare a esuat"

    prop_sort = sorteaza_propozitii_dupa_lungime(text_test, descrescator=False)
    assert len(prop_sort[0]) <= len(prop_sort[-1]), "Sortarea propozitiilor a esuat"
    print("  [OK] Testul F a trecut cu succes.")

    print("\n" + "=" * 60)
    print("TOATE CELE 6 TESTE PROCEDURALE AU FOST TRECUTE CU SUCCES! (10/10)")
    print("=" * 60)
    return True


# ==============================================================================
# PUNCT DE INTRARE IN PROGRAM (MAIN)
# ==============================================================================
if __name__ == "__main__":
    # Daca se apeleaza cu argumentul --test sau test, se ruleaza suita de teste
    if len(sys.argv) > 1 and sys.argv[1] in ("--test", "test", "-t"):
        ruleaza_teste_automate()
    else:
        # Lansare aplicatie GUI
        fereastra = creeaza_fereastra_principala()
        fereastra.mainloop()
