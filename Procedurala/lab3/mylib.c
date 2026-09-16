#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* Macro pentru exportul functiilor din biblioteca partajata */
#if defined(_WIN32) || defined(__CYGWIN__)
  #define DLL_EXPORT __declspec(dllexport)
#else
  #define DLL_EXPORT __attribute__((visibility("default")))
#endif

/*
 ============================================================================
 CERINTA C: Crearea unei DLL cu resurse proprii
 - Constante globale exportate
 - Structura de date pentru elemente de tip resursa
 - Catalog intern static de resurse
 - Functii de interogare si alocare/eliberare de resurse dinamice
 ============================================================================
*/

/* Resurse globale exportate */
DLL_EXPORT const char* const NUME_BIBLIOTECA = "Biblioteca Procedurala C - Laborator 3";
DLL_EXPORT const char* const VERSIUNE_BIBLIOTECA = "1.0.0";
DLL_EXPORT const double CONSTANTA_PI = 3.14159265358979323846;

/* Definitia unei structuri de resursa interna */
typedef struct {
    int id;
    char denumire[64];
    double pret;
    int cantitate;
} ProdusResursa;

/* Catalog intern de resurse (resursa proprie stocata in biblioteca) */
static const ProdusResursa CATALOG_RESURSE[] = {
    {101, "Laptop Ultrabook Pro", 4899.99, 10},
    {102, "Mouse Wireless Ergonomic", 149.50, 45},
    {103, "Tastatura Mecanica RGB", 399.00, 20},
    {104, "Monitor IPS 27 inch 4K", 1750.00, 8},
    {105, "Casti Audio Hi-Fi", 620.00, 15}
};
static const int TOTAL_RESURSE = sizeof(CATALOG_RESURSE) / sizeof(CATALOG_RESURSE[0]);

/* Functie de obtinere a numarului total de resurse */
DLL_EXPORT int get_numar_resurse(void) {
    return TOTAL_RESURSE;
}

/* Functie de extragere a unei resurse dupa index */
DLL_EXPORT int get_resursa_dupa_index(int index, ProdusResursa *out_resursa) {
    if (index < 0 || index >= TOTAL_RESURSE || out_resursa == NULL) {
        return 0; /* Index invalid sau pointer nul */
    }
    *out_resursa = CATALOG_RESURSE[index];
    return 1; /* Succes */
}

/* Functie care intoarce o resursa text informationala */
DLL_EXPORT const char* get_descriere_biblioteca(void) {
    return "DLL dezvoltat in C (macOS dylib) continand functii matematice, procesare de text si resurse interne.";
}

/* Resursa dinamica alocata pe heap in biblioteca partajata */
DLL_EXPORT char* genereaza_raport_resurse(void) {
    size_t dimensiune = 1024;
    char *buffer = (char*)malloc(dimensiune);
    if (!buffer) {
        return NULL;
    }

    snprintf(buffer, dimensiune,
             "--- RAPORT INTERN RESURSE (Generat in C) ---\n"
             "Biblioteca : %s\n"
             "Versiune   : %s\n"
             "Elemente   : %d produse inregistrate in catalog.\n",
             NUME_BIBLIOTECA, VERSIUNE_BIBLIOTECA, TOTAL_RESURSE);
    return buffer;
}

/* Eliberarea resurselor alocate dinamic de catre biblioteca */
DLL_EXPORT void elibereaza_memorie(void *ptr) {
    if (ptr != NULL) {
        free(ptr);
    }
}

/*
 ============================================================================
 CERINTELE A si B: Crearea unei DLL cu functii proprii
 a. Conectarea si apelul unei functii din DLL (cerinta minima nota 5)
 b. Functii matematice si de manipulare de siruri
 ============================================================================
*/

/* Functie simpla pentru cerinta minima (Cerinta A) */
DLL_EXPORT int adunare(int a, int b) {
    return a + b;
}

/* Functii matematice proprii (Cerinta B) */
DLL_EXPORT int inmultire(int a, int b) {
    return a * b;
}

DLL_EXPORT double putere(double baza, int exponent) {
    double rez = 1.0;
    int exp_abs = (exponent < 0) ? -exponent : exponent;
    for (int i = 0; i < exp_abs; i++) {
        rez *= baza;
    }
    if (exponent < 0) {
        return 1.0 / rez;
    }
    return rez;
}

DLL_EXPORT long long factorial(int n) {
    if (n < 0) return -1;
    long long f = 1;
    for (int i = 2; i <= n; i++) {
        f *= i;
    }
    return f;
}

DLL_EXPORT int cmmdc(int a, int b) {
    if (a < 0) a = -a;
    if (b < 0) b = -b;
    while (b != 0) {
        int rest = a % b;
        a = b;
        b = rest;
    }
    return a;
}

/* Functii pentru manipularea sirurilor de caractere (Cerinta B) */
DLL_EXPORT int numara_vocale(const char *sir) {
    if (!sir) return 0;
    int contor = 0;
    for (int i = 0; sir[i] != '\0'; i++) {
        char c = (char)tolower((unsigned char)sir[i]);
        if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
            contor++;
        }
    }
    return contor;
}

DLL_EXPORT void inverseaza_sir(char *sir) {
    if (!sir) return;
    int stanga = 0;
    int dreapta = (int)strlen(sir) - 1;
    while (stanga < dreapta) {
        char temp = sir[stanga];
        sir[stanga] = sir[dreapta];
        sir[dreapta] = temp;
        stanga++;
        dreapta--;
    }
}
