# Redactor Grafic (Java Paint) - Laboratorul 6: Paradigma Orientată pe Obiecte

Aplicație completă de redactare grafică (Paint) dezvoltată în **Java** folosind **Java Swing** și **Java 2D (Graphics2D)**, conform cerințelor din [conditii.md](file:///Users/preference/Desktop/calculator/lab6/conditii.md).

---

## 📋 Cerințe îndeplinite

| Cerință | Descriere | Implementare |
| :--- | :--- | :--- |
| **a. Operațiuni cu imagini** | Crearea, deschiderea, modificarea și salvarea imaginilor (PNG, JPG, BMP) | Meniu complet `Fișier` -> Nou / Deschide / Salvează / Salvează ca... Dialog de confirmare modificări nesalvate. |
| **b. Bara de instrumente** | Crearea barei de unelte interactive | Clasa [`ToolBar`](file:///Users/preference/Desktop/calculator/lab6/src/paint/ui/ToolBar.java) cu instrumente: Creion, Linie, Dreptunghi, Oval, Bezier (3P/4P), Găleată, Radieră, Pipetă, selector grosime, umplere figură, Undo/Redo/Clear. |
| **c. Bara de culori** | Selectarea culorii de desen (1) și a culorii de fundal (2) | Clasa [`ColorBar`](file:///Users/preference/Desktop/calculator/lab6/src/paint/ui/ColorBar.java) cu afișare Culoare 1 & Culoare 2, buton inversare (Swap), paletă rapidă cu 28 de culori prestabilite (Click stânga = Culoare 1, Click dreapta = Culoare 2) și selector avansat `JColorChooser`. |
| **d. Figuri geometrice simple** | Utilizarea figurilor drepte, dreptunghi, oval prin POO | Clasa de bază abstractă [`Shape`](file:///Users/preference/Desktop/calculator/lab6/src/paint/model/Shape.java) și derivatele [`Line`](file:///Users/preference/Desktop/calculator/lab6/src/paint/model/Line.java), [`Rectangle`](file:///Users/preference/Desktop/calculator/lab6/src/paint/model/Rectangle.java), [`Oval`](file:///Users/preference/Desktop/calculator/lab6/src/paint/model/Oval.java). |
| **e. Colorare spații închise (Bucket)** | Algoritm Flood Fill pentru umplerea suprafețelor închise | Clasa [`FloodFill`](file:///Users/preference/Desktop/calculator/lab6/src/paint/tools/FloodFill.java): algoritm BFS non-recursiv de înaltă performanță (cu coadă indexată și `BitSet`) pentru viteză maximă și prevenirea `StackOverflowError`. |
| **f. Desenare Curbe Bezier** | Curbe Bezier interactive (referință [bezier.method.ac](https://bezier.method.ac)) | Clasa [`BezierCurve`](file:///Users/preference/Desktop/calculator/lab6/src/paint/model/BezierCurve.java) și [`BezierMath`](file:///Users/preference/Desktop/calculator/lab6/src/paint/tools/BezierMath.java): suport pentru curbe pătratice (3 puncte - QuadCurve2D) și cubice (4 puncte - CubicCurve2D), calcul matematic explicit prin polinoame Bernstein și ghidaje vizuale (puncte de control și tangente). |

---

## 🧱 Arhitectura Orientată pe Obiecte (POO)

Aplicația respectă cu strictețe principiile de bază ale Programării Orientate pe Obiecte:

1. **Abstracție**:
   - Clasa abstractă `Shape` definește contractul comun pentru orice entitate grafică prin metoda abstractă `draw(Graphics2D g2d)`.
   - Ascunde detaliile particulare de desenare ale fiecărei forme.

2. **Moștenire (Inheritance)**:
   - Toate figurile geometrice extind `Shape`:
     - `Line extends Shape`
     - `Rectangle extends Shape`
     - `Oval extends Shape`
     - `BezierCurve extends Shape`
     - `FreehandStroke extends Shape`

3. **Polimorfism**:
   - `PaintCanvas` manipulează instanțe `Shape` fără să cunoască tipul lor concret la momentul randării `previewShape.draw(g2d)`.
   - `BezierCurve` suportă două motoare de calcul interschimbabile la rulare: Java2D (`QuadCurve2D`/`CubicCurve2D`) și motor matematic discret (`BezierMath`).

4. **Încapsulare**:
   - Câmpurile din `Shape`, `PaintCanvas`, `ColorBar`, etc. sunt protejate (`protected`) sau private (`private`), accesul fiind controlat prin metode de tip getter/setter și validări.

5. **Separarea responsabilităților (Separation of Concerns)**:
   - Pachetul `paint.model`: Reprezentarea datelor și a figurilor geometrice.
   - Pachetul `paint.tools`: Algoritmi independenți de interfață (Flood Fill, Calcul matematic Bezier, Enum ToolType).
   - Pachetul `paint.ui`: Componente Swing specializate (Pânză, Bare de unelte, Bare de culori, Meniu, Bară de stare).
   - Pachetul `paint`: Fereastra principală `PaintApp` și clasa `Main`.

---

## 🗂 Structura proiectului

```
lab6/
├── conditii.md                      # Cerințele laboratorului
├── README.md                        # Documentația proiectului
├── compile.sh                       # Script de compilare
├── run.sh                           # Script de rulare
├── src/
│   └── paint/
│       ├── Main.java                # Punctul de intrare (main)
│       ├── PaintApp.java            # Fereastra principală JFrame
│       ├── model/
│       │   ├── Shape.java           # Clasa de bază abstractă
│       │   ├── Line.java            # Segment de dreaptă
│       │   ├── Rectangle.java       # Dreptunghi (contur + umplere)
│       │   ├── Oval.java            # Oval/Elipsă (contur + umplere)
│       │   ├── BezierCurve.java     # Curbă Bezier (Quad/Cubic, ghidaje interactive)
│       │   └── FreehandStroke.java  # Desenare liberă cu creionul/radiera
│       ├── tools/
│       │   ├── ToolType.java        # Enum pentru instrumente
│       │   ├── FloodFill.java       # Algoritm BFS Flood Fill
│       │   └── BezierMath.java      # Formule matematice polinoame Bernstein
│       ├── ui/
│       │   ├── PaintCanvas.java     # Pânză de desen cu Undo/Redo (30 pași)
│       │   ├── ToolBar.java         # Bara de instrumente
│       │   ├── ColorBar.java        # Bara de culori (Culoare 1 & 2, paletă, picker)
│       │   ├── StatusBar.java       # Bara de stare (coordonate, dimensiune, indicii)
│       │   └── PaintMenuBar.java    # Meniul principal (Fișier, Editare, Imagine, Ajutor)
│       └── test/
│           └── PaintTest.java       # Suită completă de teste unitare și de integrare
```

---

## 📐 Detalii Curbe Bezier & Algoritm Flood Fill

### 1. Curbe Bezier (Matematică & Java2D)
Conform cerințelor și referinței [bezier.method.ac](https://bezier.method.ac), utilizatorul poate trasa curbe fie prin clasele Java2D (`QuadCurve2D`, `CubicCurve2D`), fie prin calcul matematic polinomial discret:

- **Curbă Pătratică (3 puncte de control)**:
  $$B(t) = (1-t)^2 P_0 + 2(1-t)t P_1 + t^2 P_2, \quad t \in [0, 1]$$
- **Curbă Cubică (4 puncte de control)**:
  $$B(t) = (1-t)^3 P_0 + 3(1-t)^2 t P_1 + 3(1-t) t^2 P_2 + t^3 P_3, \quad t \in [0, 1]$$

**Interacțiune**:
1. **Pasul 1**: Se apasă și se trage mouse-ul pentru a defini linia de bază (de la $P_0$ la $P_2$ sau $P_3$).
2. **Pasul 2**: Se deplasează mouse-ul în timp real (curba și liniile directoare se actualizează dinamic) și se face un **clic** pentru a fixa punctul de control $P_1$.
3. **Pasul 3** (doar pentru cubică): Se deplasează mouse-ul și se face un **clic** pentru a fixa punctul de control $P_2$.
4. Tasta **ESC** anulează desenarea curbei în orice moment.

### 2. Algoritmul Flood Fill (Găleată de vopsea)
- Implementat iterativ cu o coadă BFS (`int[] queue`) și un `BitSet` de memorie compactă (~60 KB pentru o imagine de 800x600).
- Verifică cei 4-vecini (stânga, dreapta, sus, jos) cu prag de toleranță cromatică.
- Garantează că nu se blochează în bucle infinite și nu provoacă `StackOverflowError`.

---

## 🚀 Compilare și Rulare

### 1. Compilare:
```bash
./compile.sh
```
sau manual:
```bash
javac -d bin src/paint/*.java src/paint/*/*.java
```

### 2. Rulare aplicație:
```bash
./run.sh
```
sau manual:
```bash
java -cp bin paint.Main
```

### 3. Rulare teste unitare:
```bash
java -Djava.awt.headless=true -cp bin paint.test.PaintTest
```
Toate cele 6 teste unitare și de integrare vor fi validate automat.
