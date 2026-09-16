#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Laboratorul 2: Paradigma Procedurala
Tema: Interactiunea cu alte aplicatii prin intermediul API

Cerinte implementate conform conditii.md:
  a. Conectarea si transmiterea informatiei intr-o aplicatie externa utilizand functiile API (POST /posts)
  b. Citirea informatiei din aplicatia externa (GET /users/{id})
  c. Salvarea si deschiderea informatiei receptionate de la API intr-un fisier local (JSON)
  d. Utilizarea a minim 3 operatii diferite prin API:
     - Operatia 1: Preluare detalii utilizator (GET /users/{id})
     - Operatia 2: Transmitere si creare articol/postare noua (POST /posts)
     - Operatia 3: Filtrare si obtinere sarcini to-do (GET /todos?userId={id}&completed=false)
     - (Bonus) Operatia 4: Preluare comentarii (GET /comments?postId={id})
  e. Inchiderea aplicatiei externe (inchiderea sesiunii HTTP/socket-urilor de comunicare)
     fara a opri aplicatia locala, demonstrand continuarea rularii pe date locale.
"""

import os
import sys
import json
import requests

# Constante globale de configurare
API_BASE_URL = "https://jsonplaceholder.typicode.com"
TIMEOUT_SECUNDE = 10
FISIER_DATE_LOCAL = os.path.join(os.path.dirname(__file__), "date_api_salvate.json")


# ============================================================================
# a. Conectarea si transmiterea informatiei in aplicatia externa
# ============================================================================

def initializeaza_conexiune(url_baza=API_BASE_URL):
    """
    Initializeaza o sesiune HTTP pentru comunicarea cu aplicatia externa (API-ul).
    Verifica disponibilitatea serviciului extern (health-check/ping).
    """
    print("\n--- [A] Conectare la aplicatia externa ---")
    print(f"Se initiaza conexiunea cu API-ul: {url_baza} ...")
    sesiune = requests.Session()
    sesiune.headers.update({
        "User-Agent": "Procedural-Client-Lab2/1.0",
        "Content-Type": "application/json; charset=UTF-8"
    })
    
    try:
        raspuns = sesiune.get(f"{url_baza}/users", timeout=TIMEOUT_SECUNDE)
        if raspuns.status_code == 200:
            print(f"[OK] Conexiune stabilita cu succes! Cod raspuns: {raspuns.status_code}")
            return sesiune
        else:
            print(f"[AVERTISMENT] API-ul a raspuns cu codul: {raspuns.status_code}")
            return sesiune
    except requests.exceptions.RequestException as eroare:
        print(f"[EROARE] Nu s-a putut realiza conexiunea: {eroare}")
        return None


def transmite_informatie_postare(sesiune, url_baza, id_utilizator, titlu, corp):
    """
    Cerința a + d (Operatia 2): Transmite informatii catre aplicatia externa
    prin metoda POST pentru a crea o noua resursa.
    """
    print("\n--- [A & D] Transmitere date catre API (POST /posts) ---")
    if sesiune is None:
        print("[EROARE] Sesiunea externa nu este activa!")
        return None
    
    url = f"{url_baza}/posts"
    date_de_transmis = {
        "userId": id_utilizator,
        "title": titlu,
        "body": corp
    }
    
    print(f"URL: {url}")
    print(f"Date transmise (payload):\n{json.dumps(date_de_transmis, indent=2, ensure_ascii=False)}")
    
    try:
        raspuns = sesiune.post(url, json=date_de_transmis, timeout=TIMEOUT_SECUNDE)
        print(f"Status HTTP receptionat: {raspuns.status_code} (Created)")
        resursa_creata = raspuns.json()
        print(f"Raspunsul aplicatiei externe: Resursa creata cu ID-ul {resursa_creata.get('id')}")
        return resursa_creata
    except requests.exceptions.RequestException as eroare:
        print(f"[EROARE] Transmiterea datelor a esuat: {eroare}")
        return None


# ============================================================================
# b. Citirea informatiei din aplicatia externa
# ============================================================================

def citeste_detalii_utilizator(sesiune, url_baza, id_utilizator):
    """
    Cerința b + d (Operatia 1): Citeste date despre un utilizator din aplicatia externa.
    """
    print(f"\n--- [B & D] Citire date utilizator (GET /users/{id_utilizator}) ---")
    if sesiune is None:
        print("[EROARE] Sesiunea externa nu este activa!")
        return None
    
    url = f"{url_baza}/users/{id_utilizator}"
    try:
        raspuns = sesiune.get(url, timeout=TIMEOUT_SECUNDE)
        if raspuns.status_code == 200:
            date_utilizator = raspuns.json()
            print(f"[OK] Date citite pentru utilizatorul: {date_utilizator.get('name')} (@{date_utilizator.get('username')})")
            print(f"     Email: {date_utilizator.get('email')}")
            print(f"     Oras: {date_utilizator.get('address', {}).get('city')}")
            print(f"     Companie: {date_utilizator.get('company', {}).get('name')}")
            return date_utilizator
        else:
            print(f"[EROARE] Citirea a esuat cu codul: {raspuns.status_code}")
            return None
    except requests.exceptions.RequestException as eroare:
        print(f"[EROARE] Nu s-au putut citi datele utilizatorului: {eroare}")
        return None


# ============================================================================
# d. Efectuarea a minim 3 operatii diferite prin API
# ============================================================================

def citeste_sarcini_utilizator(sesiune, url_baza, id_utilizator, doar_nefinalizate=True):
    """
    Cerința d (Operatia 3): Filtreaza si preia lista de sarcini (To-Do) ale unui utilizator.
    """
    stare_text = "nefinalizate" if doar_nefinalizate else "toate"
    print(f"\n--- [D] Operatia 3: Citire sarcini {stare_text} (GET /todos) ---")
    if sesiune is None:
        print("[EROARE] Sesiunea externa nu este activa!")
        return []
    
    url = f"{url_baza}/todos"
    parametri = {"userId": id_utilizator}
    if doar_nefinalizate:
        parametri["completed"] = "false"
        
    try:
        raspuns = sesiune.get(url, params=parametri, timeout=TIMEOUT_SECUNDE)
        if raspuns.status_code == 200:
            sarcini = raspuns.json()
            print(f"[OK] S-au receptionat {len(sarcini)} sarcini {stare_text}:")
            for idx, sarcina in enumerate(sarcini[:5], start=1):
                status_bifat = "✓" if sarcina.get("completed") else "✗"
                print(f"     [{status_bifat}] #{sarcina.get('id')}: {sarcina.get('title')}")
            if len(sarcini) > 5:
                print(f"     ... si inca {len(sarcini) - 5} sarcini.")
            return sarcini
        else:
            print(f"[EROARE] Cererea todos a returnat status {raspuns.status_code}")
            return []
    except requests.exceptions.RequestException as eroare:
        print(f"[EROARE] Nu s-au putut prelua sarcinile: {eroare}")
        return []


def citeste_comentarii_postare(sesiune, url_baza, id_postare):
    """
    Cerința d (Operatia 4 - Bonus): Citeste comentariile asociate unei postari.
    """
    print(f"\n--- [D] Operatia 4 (Bonus): Citire comentarii (GET /comments?postId={id_postare}) ---")
    if sesiune is None:
        print("[EROARE] Sesiunea externa nu este activa!")
        return []
    
    url = f"{url_baza}/comments"
    try:
        raspuns = sesiune.get(url, params={"postId": id_postare}, timeout=TIMEOUT_SECUNDE)
        if raspuns.status_code == 200:
            comentarii = raspuns.json()
            print(f"[OK] S-au receptionat {len(comentarii)} comentarii pentru postarea #{id_postare}:")
            for com in comentarii[:3]:
                print(f"     - De la {com.get('email')}: \"{com.get('name')}\"")
            return comentarii
        else:
            print(f"[EROARE] Preluarea comentariilor a esuat cu status {raspuns.status_code}")
            return []
    except requests.exceptions.RequestException as eroare:
        print(f"[EROARE] Nu s-au putut prelua comentariile: {eroare}")
        return []


# ============================================================================
# c. Salvare + Deschidere informatii receptionate de la API
# ============================================================================

def salveaza_date_in_fisier(date, cale_fisier=FISIER_DATE_LOCAL):
    """
    Salveaza datele receptionate de la API intr-un fisier local in format JSON.
    """
    print(f"\n--- [C] Salvare informatii in fisier local ---")
    try:
        with open(cale_fisier, "w", encoding="utf-8") as f:
            json.dump(date, f, indent=4, ensure_ascii=False)
        dimensiune = os.path.getsize(cale_fisier)
        print(f"[OK] Datele au fost salvate cu succes in: {cale_fisier}")
        print(f"     Dimensiune fisier: {dimensiune} octeti")
        return True
    except (IOError, OSError) as eroare:
        print(f"[EROARE] Salvarea fisierului a esuat: {eroare}")
        return False


def deschide_date_din_fisier(cale_fisier=FISIER_DATE_LOCAL):
    """
    Deschide si citeste informatiile salvate anterior in fisierul JSON local.
    """
    print(f"\n--- [C] Deschidere si citire date din fisier local ---")
    if not os.path.exists(cale_fisier):
        print(f"[EROARE] Fisierul {cale_fisier} nu exista!")
        return None
    
    try:
        with open(cale_fisier, "r", encoding="utf-8") as f:
            date_incarcate = json.load(f)
        print(f"[OK] Fisierul a fost deschis si incarcat in memorie cu succes!")
        chei = list(date_incarcate.keys()) if isinstance(date_incarcate, dict) else f"Lista cu {len(date_incarcate)} elemente"
        print(f"     Continut structurat: {chei}")
        return date_incarcate
    except (IOError, OSError, json.JSONDecodeError) as eroare:
        print(f"[EROARE] Deschiderea/parsarea fisierului a esuat: {eroare}")
        return None


# ============================================================================
# e. Inchiderea aplicatiei externe, fara a inchide aplicatia dvs
# ============================================================================

def inchide_conexiune_aplicatie_externa(sesiune):
    """
    Inchide conexiunea si sesiunea catre aplicatia externa (API-ul extern).
    Elibereaza socket-urile si conexiunile din pool.
    Aplicatia noastra locala ramane activa si continua rularea.
    """
    print("\n--- [E] Inchiderea aplicatiei externe ---")
    if sesiune is not None:
        try:
            sesiune.close()
            print("[OK] Sesiunea de comunicare cu API-ul extern a fost INCHISA.")
            print("     Canalele de retea si socket-urile catre serverul extern au fost eliberate.")
        except Exception as eroare:
            print(f"[AVERTISMENT] Eroare la inchiderea sesiunii: {eroare}")
    else:
        print("[INFO] Sesiunea externa era deja inchisa sau nula.")
    
    print("[STATUS] Aplicatia externa a fost deconectata/inchisa.")
    print("[STATUS] Aplicatia noastra Python continua sa ruleze in siguranta!")
    return None


def demonstreaza_continuitate_locala(cale_fisier=FISIER_DATE_LOCAL):
    """
    Demonstreaza ca dupa deconectarea/inchiderea aplicatiei externe,
    aplicatia noastra continua sa functioneze independent, utilizand datele locale salvate.
    """
    print("\n--- [E] Demonstrare: Prelucrare locala continua dupa inchiderea API-ului extern ---")
    print("Se acceseaza datele salvate local pentru procesare si raportare offline...")
    date_locale = deschide_date_din_fisier(cale_fisier)
    
    if not date_locale:
        print("[EROARE] Nu s-au gasit date locale pentru procesare!")
        return
    
    utilizator = date_locale.get("utilizator", {})
    postare_creata = date_locale.get("postare_creata", {})
    sarcini = date_locale.get("sarcini_nefinalizate", [])
    
    print("\n================== RAPORT LOCAL GENERAT OFFLINE ==================")
    print(f"Utilizator analizat : {utilizator.get('name')} ({utilizator.get('email')})")
    print(f"Companie            : {utilizator.get('company', {}).get('name')}")
    print(f"Postare creata prin API (ID): #{postare_creata.get('id')}")
    print(f"Numar sarcini nefinalizate  : {len(sarcini)}")
    print("Exemple de sarcini restante :")
    for idx, s in enumerate(sarcini[:3], start=1):
        print(f"  {idx}. {s.get('title')}")
    print("==================================================================")
    print("Prelucrarea locala a fost finalizata cu succes fara apeluri externe active!")


# ============================================================================
# Fluxul principal procedural
# ============================================================================

def executa_flux_complet():
    """
    Executa pas cu pas cerintele laboratorului:
    a) Conectare + Transmitere date
    b) Citire date
    c) Salvare + Deschidere date
    d) 3+ operatii diferite
    e) Inchidere aplicatie externa + rulare locala continua
    """
    print("=" * 70)
    print("  LABORATOR 2: PARADIGMA PROCEDURALA - INTERACTIUNEA CU API  ")
    print("=" * 70)
    
    # 1. Conectare
    sesiune = initializeaza_conexiune(API_BASE_URL)
    if sesiune is None:
        print("[FATAL] Nu se poate continua fara conexiune.")
        return
    
    # 2. Operatia 1 (b + d): Citire informatii utilizator (GET /users/1)
    date_utilizator = citeste_detalii_utilizator(sesiune, API_BASE_URL, id_utilizator=1)
    
    # 3. Operatia 2 (a + d): Transmitere informatie catre aplicatia externa (POST /posts)
    postare_creata = transmite_informatie_postare(
        sesiune,
        API_BASE_URL,
        id_utilizator=1,
        titlu="Implementare Procedurala Lab 2",
        corp="Acest articol a fost transmis cu succes din aplicatia Python folosind biblioteca requests."
    )
    
    # 4. Operatia 3 (d): Preluare si filtrare sarcini To-Do (GET /todos)
    sarcini_nefinalizate = citeste_sarcini_utilizator(
        sesiune,
        API_BASE_URL,
        id_utilizator=1,
        doar_nefinalizate=True
    )
    
    # 5. Operatia 4 (d - bonus): Preluare comentarii (GET /comments)
    comentarii_postare = citeste_comentarii_postare(sesiune, API_BASE_URL, id_postare=1)
    
    # 6. Structurarea datelor receptionate pentru salvare (c)
    pachet_date = {
        "sursa_api": API_BASE_URL,
        "utilizator": date_utilizator,
        "postare_creata": postare_creata,
        "sarcini_nefinalizate": sarcini_nefinalizate,
        "comentarii_postare_1": comentarii_postare
    }
    
    # 7. Salvare in fisier local JSON (c)
    salveaza_date_in_fisier(pachet_date, FISIER_DATE_LOCAL)
    
    # 8. Deschidere si citire date din fisierul salvat (c)
    date_recitite = deschide_date_din_fisier(FISIER_DATE_LOCAL)
    
    # 9. Inchiderea aplicatiei externe fara inchiderea aplicatiei noastre (e)
    sesiune = inchide_conexiune_aplicatie_externa(sesiune)
    
    # 10. Demonstrarea functionalitatii aplicatiei locale dupa inchiderea celei externe (e)
    demonstreaza_continuitate_locala(FISIER_DATE_LOCAL)
    
    print("\n" + "=" * 70)
    print("  Toate cerintele laboratorului 2 au fost executate cu succes!  ")
    print("=" * 70)


def afiseaza_meniu_interactiv():
    """
    Afiseaza un meniu interactiv pentru explorare manuala procedurala.
    """
    sesiune = None
    
    while True:
        print("\n" + "-" * 50)
        print("          MENIU PROCEDURAL - LAB 2 API          ")
        print("-" * 50)
        stare_sesiune = "CONECTAT" if sesiune is not None else "DECONECTAT"
        print(f"Stare conexiune externa: [{stare_sesiune}]")
        print("1. Executa fluxul complet demonstrativ (Toate cerintele a-e)")
        print("2. [a] Conecteaza-te la API")
        print("3. [b & d1] Citeste date utilizator (GET /users/{id})")
        print("4. [a & d2] Transmite postare noua (POST /posts)")
        print("5. [d3] Citeste sarcini To-Do (GET /todos)")
        print("6. [c] Salveaza si deschide datele locale (JSON)")
        print("7. [e] Inchide aplicatia externa (pastreaza aplicatia locala)")
        print("8. [e] Ruleaza raportul local offline")
        print("0. Iesire din aplicatia locala")
        print("-" * 50)
        
        try:
            optiune = input("Alege o optiune (0-8): ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\nIesire fortata detectata. La revedere!")
            break
            
        if optiune == "1":
            executa_flux_complet()
        elif optiune == "2":
            sesiune = initializeaza_conexiune(API_BASE_URL)
        elif optiune == "3":
            if sesiune is None:
                print("[INFO] Se initializeaza automat sesiunea...")
                sesiune = initializeaza_conexiune(API_BASE_URL)
            try:
                uid = int(input("Introdu ID utilizator (1-10) [implicit 1]: ") or "1")
            except ValueError:
                uid = 1
            citeste_detalii_utilizator(sesiune, API_BASE_URL, uid)
        elif optiune == "4":
            if sesiune is None:
                print("[INFO] Se initializeaza automat sesiunea...")
                sesiune = initializeaza_conexiune(API_BASE_URL)
            titlu = input("Titlul postarii [implicit 'Titlu test']: ").strip() or "Titlu test"
            corp = input("Continutul postarii [implicit 'Continut test']: ").strip() or "Continut test"
            transmite_informatie_postare(sesiune, API_BASE_URL, 1, titlu, corp)
        elif optiune == "5":
            if sesiune is None:
                print("[INFO] Se initializeaza automat sesiunea...")
                sesiune = initializeaza_conexiune(API_BASE_URL)
            citeste_sarcini_utilizator(sesiune, API_BASE_URL, 1, doar_nefinalizate=True)
        elif optiune == "6":
            date_recitite = deschide_date_din_fisier(FISIER_DATE_LOCAL)
            if not date_recitite:
                print("[INFO] Rulam o salvare initiala rapida...")
                salveaza_date_in_fisier({"mesaj": "Date salvate manual", "timestamp": "activ"}, FISIER_DATE_LOCAL)
                deschide_date_din_fisier(FISIER_DATE_LOCAL)
        elif optiune == "7":
            sesiune = inchide_conexiune_aplicatie_externa(sesiune)
        elif optiune == "8":
            demonstreaza_continuitate_locala(FISIER_DATE_LOCAL)
        elif optiune == "0":
            if sesiune is not None:
                sesiune.close()
            print("Aplicatia locala se inchide. La revedere!")
            break
        else:
            print("Optiune invalida! Va rugam alegeti un numar intre 0 si 8.")


def main():
    """
    Punctul de intrare in program.
    Ruleaza fluxul automat daca se specifica argumentul '--auto' sau daca rularea este non-interactiva.
    Daca rularea este interactiva in terminal, ruleaza fluxul complet o data pentru demonstratie,
    apoi ofera acces la meniul interactiv.
    """
    if "--auto" in sys.argv or not sys.stdin.isatty():
        executa_flux_complet()
    else:
        executa_flux_complet()
        print("\nDoresti sa accesezi meniul interactiv? (d/n) [implicit: n]: ", end="")
        try:
            raspuns = input().strip().lower()
            if raspuns in ("d", "da", "y", "yes"):
                afiseaza_meniu_interactiv()
            else:
                print("Finalizat cu succes.")
        except (EOFError, KeyboardInterrupt):
            print("\nFinalizat.")


if __name__ == "__main__":
    main()
