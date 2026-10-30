# Paradigma Funcțională și Procedurală - Laboratorul 4

## Cerință
Scrieți un program în C++ care primește o structură de liste/tablouri imbricate (introdusă de la tastatură, de exemplu `{1,2[2,2,3],2[2,2,3]}` sau `[2, [2, 3], 4, [4]]`) și returnează o listă unidimensională cu aceleași elemente în ordinea originală (`[1, 2, 2, 2, 3, 2, 2, 2, 3]`).

La pornire, utilizatorul alege metoda dorită:
1. **Funcțională:** recursivitate pură, **zero bucle** (fără `while`, fără `for`), imutabilă.
2. **Procedurală:** implementare clasică cu bucle `while` și index incremental.

## Compilare și Rulare
```bash
make run
```
sau:
```bash
clang++ -std=c++17 -Wall -Wextra -O2 main.cpp -o main
./main
```
