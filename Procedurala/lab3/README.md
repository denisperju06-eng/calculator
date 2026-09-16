# Lucrarea de Laborator 3: Paradigma Procedurala - Lucrul cu DLL / Biblioteci Partajate

Aceasta lucrare implementeaza cerintele specificate in [conditii.md](file:///Users/preference/Desktop/calculator/Procedurala/lab3/conditii.md) folosind **C** si **Python**, respectand principiile **paradigmei procedurale**.

---

## 1. Structura Proiectului

- [`mylib.c`](file:///Users/preference/Desktop/calculator/Procedurala/lab3/mylib.c): Codul sursa C al bibliotecii partajate, continand functii matematice, manipulare de siruri si gestiunea resurselor proprii.
- [`mylib.h`](file:///Users/preference/Desktop/calculator/Procedurala/lab3/mylib.h): Header-ul C cu semnaturile functiilor, definitia structurii `ProdusResursa` si constantele exportate.
- [`mylib.dylib`](file:///Users/preference/Desktop/calculator/Procedurala/lab3/mylib.dylib): Biblioteca partajata compilata pentru macOS (`.dylib`).
- [`main.py`](file:///Users/preference/Desktop/calculator/Procedurala/lab3/main.py): Script Python scris procedural care incarca biblioteca prin `ctypes`, apeleaza functiile, citeste resursele si o descarca din memorie.
- [`Makefile`](file:///Users/preference/Desktop/calculator/Procedurala/lab3/Makefile): Fisier de automatizare a compilarii si rularii (`make`, `make run`, `make clean`).

---

## 2. Corespondenta cu Cerintele din `conditii.md`

### a. Conectarea si apelul unei functii din DLL (cerinta minima pentru nota 5)
- **Fisier C**: Functia `adunare(int a, int b)`.
- **Fisier Python**: Functia procedurala `executa_cerinta_a(biblioteca)` conecteaza biblioteca prin `ctypes.CDLL`, configureaza `argtypes` si `restype` si apeleaza functia, validand rezultatul.

### b. Crearea unei DLL cu functii proprii (2 puncte)
- **Fisier C**:
  - `inmultire(int a, int b)`: Produsul a doua numere intregi.
  - `putere(double baza, int exponent)`: Ridicarea la putere intreaga (pozitiva sau negativa).
  - `factorial(int n)`: Calculul factorialului unui numar intreg.
  - `cmmdc(int a, int b)`: Cel mai mare divizor comun prin algoritmul lui Euclid.
  - `numara_vocale(const char *sir)`: Numararea vocalelor dintr-un sir de caractere.
  - `inverseaza_sir(char *sir)`: Inversarea pe loc a caracterelor unui sir din memorie.
- **Fisier Python**: Procedura `executa_cerinta_b(biblioteca)` testeaza fiecare functie in parte si afiseaza rezultatele.

### c. Crearea unei DLL cu resurse proprii (2 puncte)
In C si in bibliotecile dinamice, resursele proprii includ date constante exportate, cataloage/tabele interne si structuri alocate si administrate de biblioteca:
- **Resurse globale exportate direct**: `NUME_BIBLIOTECA`, `VERSIUNE_BIBLIOTECA`, `CONSTANTA_PI` (accesate din Python prin `ctypes.c_char_p.in_dll` si `ctypes.c_double.in_dll`).
- **Resurse structurate (Catalog de date)**: Structura `ProdusResursa` si tabloul static `CATALOG_RESURSE` cu functiile de interogare `get_numar_resurse()` si `get_resursa_dupa_index(index, out_resursa)`.
- **Resurse dinamice**: Functia `genereaza_raport_resurse()` care aloca memorie pe heap in interiorul C si `elibereaza_memorie(ptr)` pentru eliberarea acesteia.
- **Fisier Python**: Procedura `executa_cerinta_c(biblioteca)` afiseaza catalogul de produse si gestioneaza raportul dinamic.

### d. Conectarea dinamica a DLL la necesitate si eliminarea din memorie dupa utilizare (1 punct)
- Incarcarea la cerere este demonstrata in `executa_cerinta_d_demonstratie_la_necesitate(cale)`: biblioteca este incarcata strict la momentul cand este necesar un calcul, dupa care este imediat descarcata.
- **Eliminarea din memorie**: Procedura `descarca_biblioteca(biblioteca)` apeleaza `_ctypes.dlclose(handle)` (pe macOS / POSIX) sau `FreeLibrary` (pe Windows), eliberand spatiul de adrese si resursele sistemului de operare.

---

## 3. Instructiuni de Compilare si Rulare

### Compilare manuala (macOS):
```bash
gcc -dynamiclib -Wall -Wextra -O2 -fPIC -o mylib.dylib mylib.c
```

### Rulare:
```bash
python3 main.py
```

### Sau folosind Makefile:
```bash
# Compilare si executie automata:
make run

# Curatare fisiere compilate:
make clean
```
