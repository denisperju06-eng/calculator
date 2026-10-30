# Lucrarea de Laborator Nr. 3: Paradigma Funcțională
## Parser XML și Prelucrarea Funcțională a Arborelui de Etichete

Acest proiect reprezintă rezolvarea completă a cerințelor din [conditii.md](file:///Users/preference/Desktop/univer/calculator/Functionala/lab3/conditii.md), implementată în **C++ (C++17)** conform **Paradigmei Funcționale**.

---

### 1. Principii ale Paradigmei Funcționale Respectate

1. **Imutabilitate:**
   - Arborele XML și tokenii sunt reprezentați prin structuri de date `Node` și `Token`.
   - Nicio structură de date nu este modificată „in-place” în timpul parcurgerilor.
2. **Funcții Pure și Fără Efecte Secundare:**
   - Toate funcțiile depind exclusiv de argumentele primite și returnează date noi fără a modifica starea globală.
3. **Recursivitate Structurală în locul Buclelor:**
   - Nu se folosesc bucle iterative în algoritmii de parsare a corpului XML, traversare, căutare, calcul adâncime, lățime sau numărare noduri pe niveluri.
   - Algoritmii folosesc recursia arborescentă și apelurile recursive.
4. **Funcții de Ordin Superior (Higher-Order Functions):**
   - Utilizarea predicatelor, funcțiilor lambda și a catamorfismului generalizat pe arbori (`fold_tree`).
5. **Transparență Referențială:**
   - Oricare apel de funcție cu aceiași parametri poate fi substituit cu rezultatul său.

---

### 2. Maparea pe Cerințele din `conditii.md` (Total 10 puncte)

| Nr. | Cerință | Punctaj | Funcții Implementate în [`main.cpp`](file:///Users/preference/Desktop/univer/calculator/Functionala/lab3/main.cpp) |
|---|---|---|---|
| 1 | **Elaborarea unui parser pentru documente XML** | 3 p | `tokenize`, `parse_attributes`, `parse_body`, `parse_element`, `parse_xml`, `parse_xml_file` |
| 2 | **Generarea arborelui de etichete pe baza prelucrării XML** | 1 p | `generate_tag_tree`, structura `Node`, `tree_to_string` |
| 3 | **Căutarea unei etichete specifice în arborele de etichete** | 1 p | `find_all_by_tag`, `find_first_by_tag`, `find_by_predicate` |
| 4 | **Generarea unei liste pe baza rezultatului căutării etichetei** | 1 p | `nodes_to_text_list`, `nodes_to_tag_list`, `flatten_tree_to_list`, `tag_search_to_list`, `search_and_extract` |
| 5 | **Obținerea adâncimii arborelui de etichete rezultat** | 1 p | `get_tree_depth`, validat și prin catamorfismul `fold_tree` |
| 6 | **Obținerea lățimii arborelui de etichete rezultat** | 1 p | `get_tree_width` (numărul maxim de noduri pe un nivel), `get_tree_leaf_count` (număr frunze), `get_max_branching_factor` |
| 7 | **Obținerea numărului de noduri de pe un anumit nivel pornind de la un nod dat** | 2 p | `get_nodes_at_level`, `count_nodes_at_level`, `count_all_levels` |

---

### 3. Compilare și Rulare (C++)

Folosind Makefile-ul inclus:
```bash
cd Functionala/lab3

# Rulare cu documentul XML demonstrativ intern:
make run

# Rulare specificand fisierul XML extern (catalog.xml):
make run-file
```

Sau manual prin `clang++`:
```bash
clang++ -std=c++17 -Wall -Wextra -O2 main.cpp -o main
./main catalog.xml
```
