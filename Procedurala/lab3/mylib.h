#ifndef MYLIB_H
#define MYLIB_H

#if defined(_WIN32) || defined(__CYGWIN__)
  #define DLL_EXPORT __declspec(dllexport)
#else
  #define DLL_EXPORT __attribute__((visibility("default")))
#endif

/* Structura pentru resursa de tip produs */
typedef struct {
    int id;
    char denumire[64];
    double pret;
    int cantitate;
} ProdusResursa;

/* Resurse globale exportate */
extern DLL_EXPORT const char* const NUME_BIBLIOTECA;
extern DLL_EXPORT const char* const VERSIUNE_BIBLIOTECA;
extern DLL_EXPORT const double CONSTANTA_PI;

/* Functii pentru resurse proprii */
DLL_EXPORT int get_numar_resurse(void);
DLL_EXPORT int get_resursa_dupa_index(int index, ProdusResursa *out_resursa);
DLL_EXPORT const char* get_descriere_biblioteca(void);
DLL_EXPORT char* genereaza_raport_resurse(void);
DLL_EXPORT void elibereaza_memorie(void *ptr);

/* Functii matematice proprii */
DLL_EXPORT int adunare(int a, int b);
DLL_EXPORT int inmultire(int a, int b);
DLL_EXPORT double putere(double baza, int exponent);
DLL_EXPORT long long factorial(int n);
DLL_EXPORT int cmmdc(int a, int b);

/* Functii pentru siruri */
DLL_EXPORT int numara_vocale(const char *sir);
DLL_EXPORT void inverseaza_sir(char *sir);

#endif /* MYLIB_H */
