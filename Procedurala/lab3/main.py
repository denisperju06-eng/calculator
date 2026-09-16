#!/usr/bin/env python3
"""
Laboratorul 3: Paradigma Procedurala - Lucrul cu Biblioteci Partajate (DLL / dylib)
Fisier: main.py
Descriere: Aplicatie scrisa in stil pur procedural care incarca dinamic o biblioteca C
           (mylib.dylib), configureaza tipurile ctypes, apeleaza functiile proprii,
           acceseaza resursele proprii si descarca biblioteca din memorie dupa utilizare.
"""

import os
import sys
import ctypes
from ctypes import c_int, c_double, c_char_p, c_void_p, POINTER, Structure, create_string_buffer, string_at

# ============================================================================
# DEFINIREA STRUCTURILOR PENTRU RESURSELE DIN BIBLIOTECA (CERINTA C)
# ============================================================================

class ProdusResursa(Structure):
    """
    Structura corespunzatoare 'ProdusResursa' definita in C (mylib.c).
    Reprezinta o resursa proprie din catalogul intern al bibliotecii.
    """
    _fields_ = [
        ("id", c_int),
        ("denumire", ctypes.c_char * 64),
        ("pret", c_double),
        ("cantitate", c_int)
    ]


# ============================================================================
# PROCEDURI PENTRU GESTIUNEA CICLULUI DE VIATA AL BIBLIOTECII (CERINTELE A si D)
# ============================================================================

def obtine_cale_biblioteca():
    """
    Determina calea absoluta catre fisierul bibliotecii dinamice (.dylib / .dll / .so).
    """
    director_curent = os.path.dirname(os.path.abspath(__file__))
    
    if sys.platform == "darwin":
        nume_fisier = "mylib.dylib"
    elif sys.platform == "win32":
        nume_fisier = "mylib.dll"
    else:
        nume_fisier = "mylib.so"
        
    cale = os.path.join(director_curent, nume_fisier)
    return cale


def incarca_biblioteca(cale_biblioteca):
    """
    Incarca dinamic biblioteca partajata folosind modulul ctypes.
    Returneaza instanta CDLL sau None in caz de esec.
    """
    if not os.path.exists(cale_biblioteca):
        print(f"[EROARE] Fisierul bibliotecii nu a fost gasit la calea: {cale_biblioteca}")
        print("Asigurati-va ca ati compilat mylib.c conform instructiunilor din README!")
        return None

    try:
        biblioteca = ctypes.CDLL(cale_biblioteca)
        print(f"[SUCCES] Biblioteca a fost incarcata dinamic din: {cale_biblioteca}")
        print(f"         Handle memorie alocat: {biblioteca._handle}")
        return biblioteca
    except Exception as e:
        print(f"[EROARE] Nu s-a putut incarca biblioteca: {e}")
        return None


def configureaza_semnaturi(biblioteca):
    """
    Procedura care stabileste tipurile argumentelor (argtypes) si ale returnarii (restype)
    pentru fiecare functie exportata de biblioteca C.
    """
    # 1. Functie cerinta minima
    biblioteca.adunare.argtypes = [c_int, c_int]
    biblioteca.adunare.restype = c_int

    # 2. Functii matematice proprii
    biblioteca.inmultire.argtypes = [c_int, c_int]
    biblioteca.inmultire.restype = c_int

    biblioteca.putere.argtypes = [c_double, c_int]
    biblioteca.putere.restype = c_double

    biblioteca.factorial.argtypes = [c_int]
    biblioteca.factorial.restype = ctypes.c_longlong

    biblioteca.cmmdc.argtypes = [c_int, c_int]
    biblioteca.cmmdc.restype = c_int

    # 3. Functii manipulare siruri
    biblioteca.numara_vocale.argtypes = [c_char_p]
    biblioteca.numara_vocale.restype = c_int

    biblioteca.inverseaza_sir.argtypes = [c_char_p]
    biblioteca.inverseaza_sir.restype = None

    # 4. Functii pentru resurse proprii
    biblioteca.get_numar_resurse.argtypes = []
    biblioteca.get_numar_resurse.restype = c_int

    biblioteca.get_resursa_dupa_index.argtypes = [c_int, POINTER(ProdusResursa)]
    biblioteca.get_resursa_dupa_index.restype = c_int

    biblioteca.get_descriere_biblioteca.argtypes = []
    biblioteca.get_descriere_biblioteca.restype = c_char_p

    biblioteca.genereaza_raport_resurse.argtypes = []
    biblioteca.genereaza_raport_resurse.restype = c_void_p

    biblioteca.elibereaza_memorie.argtypes = [c_void_p]
    biblioteca.elibereaza_memorie.restype = None


def descarca_biblioteca(biblioteca):
    """
    CERINTA D: Eliminarea din memorie a bibliotecii dinamice dupa utilizare.
    Efectueaza eliberarea handle-ului din memorie conform platformei (dlclose pe POSIX/Mac,
    FreeLibrary pe Windows).
    """
    if biblioteca is None:
        print("[INFO] Nicio biblioteca de descarcat.")
        return False

    handle = getattr(biblioteca, "_handle", None)
    if handle is None:
        print("[INFO] Handle-ul bibliotecii este deja nul.")
        return False

    print(f"\n[CERINTA D] Descarcare dinamica: eliberare handle {handle} din memoria RAM...")
    
    succes = False
    try:
        if sys.platform == "win32":
            rezultat = ctypes.windll.kernel32.FreeLibrary(handle)
            succes = (rezultat != 0)
        else:
            # Pe macOS si Linux dlopen/dlclose sunt gestionate prin modulul _ctypes
            import _ctypes
            _ctypes.dlclose(handle)
            succes = True
    except Exception as eroare:
        print(f"[EROARE] Nu s-a putut apela dlclose/FreeLibrary: {eroare}")
        succes = False

    if succes:
        print("[SUCCES] Biblioteca partajata a fost eliminata din spatiul de adrese al procesului!")
    return succes


# ============================================================================
# PROCEDURI PENTRU TESTAREA CERINTELOR LABORATORULUI
# ============================================================================

def executa_cerinta_a(biblioteca):
    """
    a. Conectarea si apelul unei functii din DLL (cerinta minima pentru nota 5).
    """
    print("\n" + "=" * 70)
    print("CERINTA A: Conectarea si apelul unei functii din DLL (cerinta minima)")
    print("=" * 70)
    
    val1 = 18
    val2 = 24
    rezultat = biblioteca.adunare(val1, val2)
    print(f"Apel functie C: adunare({val1}, {val2})")
    print(f"Rezultat returnat din DLL: {rezultat}")
    assert rezultat == (val1 + val2), "Rezultatul adunarii nu corespunde!"
    print(">> Cerinta A a fost indeplinita cu succes.")


def executa_cerinta_b(biblioteca):
    """
    b. Crearea unei DLL cu functii proprii (2 puncte).
       Apeleaza functii matematice si de prelucrare siruri implementate in C.
    """
    print("\n" + "=" * 70)
    print("CERINTA B: Crearea unei DLL cu functii proprii (Matematica si Siruri)")
    print("=" * 70)

    # 1. Inmultire
    a, b = 7, 9
    prod = biblioteca.inmultire(a, b)
    print(f"1. Inmultire: inmultire({a}, {b}) = {prod}")

    # 2. Ridicare la putere
    baza, exp = 2.5, 3
    put = biblioteca.putere(baza, exp)
    print(f"2. Ridicare la putere: putere({baza}, {exp}) = {put}")

    # 3. Factorial
    n = 10
    fact = biblioteca.factorial(n)
    print(f"3. Factorial: factorial({n}) = {fact}")

    # 4. CMMDC (Cel mai mare divizor comun)
    x, y = 108, 48
    divizor = biblioteca.cmmdc(x, y)
    print(f"4. CMMDC: cmmdc({x}, {y}) = {divizor}")

    # 5. Numarare vocale in sir
    text = "Programare Procedurala in C si Python"
    vocale = biblioteca.numara_vocale(text.encode("utf-8"))
    print(f"5. Numarare vocale: textul '{text}' contine {vocale} vocale.")

    # 6. Inversare sir in-place (folosind create_string_buffer)
    sir_original = "Antigravity2026"
    buffer_sir = create_string_buffer(sir_original.encode("utf-8"))
    biblioteca.inverseaza_sir(buffer_sir)
    sir_inversat = buffer_sir.value.decode("utf-8")
    print(f"6. Inversare sir (in memorie C): '{sir_original}' -> '{sir_inversat}'")

    print(">> Cerinta B a fost indeplinita cu succes.")


def executa_cerinta_c(biblioteca):
    """
    c. Crearea unei DLL cu resurse proprii (2 puncte).
       Demonstreaza accesarea:
       - Resurselor globale exportate direct ca variabile (in_dll)
       - Descrierii text a bibliotecii
       - Catalogului intern de produse (structuri stocate in DLL)
       - Resurselor dinamice alocate pe heap in C si eliberate dupa utilizare
    """
    print("\n" + "=" * 70)
    print("CERINTA C: Crearea unei DLL cu resurse proprii")
    print("=" * 70)

    # 1. Accesare variabile globale exportate direct din DLL
    nume_lib = c_char_p.in_dll(biblioteca, "NUME_BIBLIOTECA").value.decode("utf-8")
    versiune_lib = c_char_p.in_dll(biblioteca, "VERSIUNE_BIBLIOTECA").value.decode("utf-8")
    pi_const = c_double.in_dll(biblioteca, "CONSTANTA_PI").value

    print("[Resurse globale exportate]:")
    print(f"  - Nume biblioteca: '{nume_lib}'")
    print(f"  - Versiune       : '{versiune_lib}'")
    print(f"  - Constanta PI   : {pi_const}")

    # 2. Resursa text informativa din DLL
    descriere = biblioteca.get_descriere_biblioteca().decode("utf-8")
    print(f"\n[Resursa descriptiva]: {descriere}")

    # 3. Catalogul de produse din structura interna a DLL-ului
    total_resurse = biblioteca.get_numar_resurse()
    print(f"\n[Catalog intern de resurse (Structuri C)] Total elemente: {total_resurse}")
    print(f"  {'ID':<6} | {'Denumire Produs':<28} | {'Pret (RON)':<12} | {'Stoc':<6}")
    print("  " + "-" * 60)

    for i in range(total_resurse):
        resursa = ProdusResursa()
        ok = biblioteca.get_resursa_dupa_index(i, ctypes.byref(resursa))
        if ok:
            denumire_str = resursa.denumire.decode("utf-8", errors="replace")
            print(f"  {resursa.id:<6} | {denumire_str:<28} | {resursa.pret:<12.2f} | {resursa.cantitate:<6}")

    # 4. Resursa dinamica alocata pe heap in C (raport) si eliberata
    print("\n[Resursa dinamica generata pe heap in C]:")
    ptr_raport = biblioteca.genereaza_raport_resurse()
    if ptr_raport:
        text_raport = string_at(ptr_raport).decode("utf-8")
        print(text_raport.strip())
        # Eliberam memoria alocata de C
        biblioteca.elibereaza_memorie(ptr_raport)
        print("  -> Memoria heap pentru raport a fost eliberata de DLL.")

    print(">> Cerinta C a fost indeplinita cu succes.")


def executa_cerinta_d_demonstratie_la_necesitate(cale_biblioteca):
    """
    d. Conectarea dinamica a DLL la necesitate si eliminarea din memorie dupa utilizare (1 punct).
       Procedura demonstreaza un ciclu complet izolat:
       1. Incarcare la momentul exact cand apare necesitatea unui calcul
       2. Executia operatiilor
       3. Descarcarea imediata si eliberarea memoriei
    """
    print("\n" + "=" * 70)
    print("CERINTA D: Conectare dinamica la necesitate si eliminare din memorie")
    print("=" * 70)

    print("Faza 1: Se solicita un calcul specific (ex: calcul factorial mare)...")
    lib_temporar = incarca_biblioteca(cale_biblioteca)
    if lib_temporar is None:
        print("[EROARE] Nu s-a putut incarca biblioteca temporara.")
        return

    configureaza_semnaturi(lib_temporar)

    print("Faza 2: Utilizarea bibliotecii pentru calcul...")
    numar = 8
    rez = lib_temporar.factorial(numar)
    print(f"        Calcul efectuat: factorial({numar}) = {rez}")

    print("Faza 3: Operatia a fost finalizata. Eliminarea bibliotecii din memorie...")
    descarca_biblioteca(lib_temporar)
    print(">> Cerinta D a fost indeplinita cu succes.")


# ============================================================================
# PROCEDURA PRINCIPALA (MAIN PROCEDURAL)
# ============================================================================

def main():
    """
    Punctul de intrare procedural al aplicatiei.
    Orchestreaza secvential indeplinirea tuturor cerintelor laboratorului.
    """
    print("=" * 70)
    print("LABORATORUL 3: PARADIGMA PROCEDURALA - UTILIZARE DLL / DYLIB")
    print("=" * 70)

    cale_lib = obtine_cale_biblioteca()
    print(f"Detectat sistem de operare: {sys.platform}")
    print(f"Cale tinta biblioteca     : {cale_lib}\n")

    # Incarcare biblioteca principala
    lib = incarca_biblioteca(cale_lib)
    if lib is None:
        sys.exit(1)

    # Configurare semnaturi procedurale
    configureaza_semnaturi(lib)

    # Executare cerinte pas cu pas
    executa_cerinta_a(lib)
    executa_cerinta_b(lib)
    executa_cerinta_c(lib)

    # Descarcare biblioteca principala (Cerinta D)
    descarca_biblioteca(lib)

    # Demonstratie specifica pentru conectare doar la necesitate (Cerinta D)
    executa_cerinta_d_demonstratie_la_necesitate(cale_lib)

    print("\n" + "=" * 70)
    print("TOATE CERINTELE AU FOST EXECUTATE CU SUCCES CONFORM CONDITIILOR!")
    print("=" * 70)


if __name__ == "__main__":
    main()
