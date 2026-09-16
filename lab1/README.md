# Calculator de Buzunar - Paradigma Orientată pe Obiecte (OOP)

Acest proiect reprezintă implementarea completă a temei **„Calculator de buzunar”** conform cerințelor din slide-ul de prezentare:
* **Interfață grafică (Visual):** Realizată modern în **HTML5 & CSS3** (stil fluent Windows 11 Calculator, cu modurile Științific, Standard și Programator).
* **Condiții & Logică de calcul:** Implementate riguros conform **Paradigmei Orientate pe Obiecte (OOP)** în **JavaScript (ES6+)** și disponibil complementar în **Java** (în folderul `java/`).

---

## 📋 Acoperirea Cerințelor din Temă (Punctaj Maxim: 10)

| Cerință | Descriere cerință | Implementare în Proiect | Punctaj |
| :--- | :--- | :--- | :---: |
| **a** | **Operații aritmetice de bază pe numere reale** | Adunare (`+`), Scădere (`−`), Înmulțire (`×`), Împărțire (`÷`), Modulo (`mod`) pe numere reale cu precizie dublă (`double` / `Float64`). Moștenire din `BinaryOperation`. | **Nota 5 (bază)** |
| **b** | **Minim 3 operații adăugătoare** | Radical pătratic (`√x`), Radical cubic (`∛x`), Ridicare la pătrat (`x²`), Ridicare la cub (`x³`), Ridicare la putere arbitrară (`xʸ`), Procent (`%`), Inversul numărului (`1/x`), Valoare absolută (`\|x\|`), Factorial (`n!`). | **+1 punct** |
| **c** | **Minim 3 trigonometrice + Minim 3 logaritmice** | **Trigonometrie:** `sin`, `cos`, `tan`, `sin⁻¹`, `cos⁻¹`, `tan⁻¹` cu comutator **DEG** (grade) și **RAD** (radiani).<br>**Logaritmi:** `ln` (natural), `log₁₀` (baza 10), `log₂` (baza 2), plus `eˣ` și `10ˣ`. | **+1 punct** |
| **d** | **Operații de lucru cu memoria** | Clasa încapsulată `MemoryUnit` oferă: `MC` (Clear), `MR` (Recall), `M+` (Add), `M−` (Subtract), `MS` (Store) cu indicator vizual activ `[M]`. | **+1 punct** |
| **e** | **Prelucrarea erorilor de calcule** | Ierarhie OOP de excepții: `DivisionByZeroError`, `DomainMathError` (radical negativ, logaritm $\le 0$, tangenta la $90^\circ$), `OverflowError`. Mesajele sunt capturate prin `try ... catch` și afișate elegant pe ecran. | **+1 punct** |
| **f** | **Transformarea hexazecimală (sau alt sistem)** | Clasa `BaseConverter` realizează conversii dinamice între **HEX** (baza 16), **DEC** (baza 10), **OCT** (baza 8) și **BIN** (baza 2). În modul Programator, valorile sunt afișate simultan în timp real. | **+1 punct** |

---

## 🏛️ Conceptele OOP Utilizate

1. **Abstractizare (Abstraction):**
   - Clasa abstractă `AbstractOperation` (și interfața `IOperation` în Java) definește contractul comun pentru orice operație matematică (`getName()`, `getSymbol()`, `execute()`).
2. **Moștenire (Inheritance):**
   - `BinaryOperation` și `UnaryOperation` moștenesc din `AbstractOperation`.
   - Fiecare operație concretă (`AddOperation`, `SquareRootOperation`, `SinOperation`, `Log10Operation`) moștenește clasa de bază corespunzătoare.
3. **Polimorfism (Polymorphism):**
   - `CalculatorEngine` stochează instanțele operațiilor într-o colecție (`Map<String, Operation>`). Execuția se face apelând polimorfic metoda `op.execute(...)`, comportamentul variind în funcție de clasa concretă instanțiată.
4. **Încapsulare (Encapsulation):**
   - Câmpurile din `CalculatorEngine` și `MemoryUnit` sunt strict private (`#currentValue`, `#hasStoredValue`, `#history`), accesul și modificarea lor fiind permise doar prin metode și proprietăți publice dedicate.
5. **Tratarea Excepțiilor (Exception Handling):**
   - Ierarhie de erori specializate (`CalculatorError` -> `DivisionByZeroError`, `DomainMathError`, `OverflowError`).

---

## 📁 Structura Fișierelor

```
calculator/
├── index.html                   # Interfața vizuală completă (HTML5)
├── css/
│   └── style.css                # Design modern Fluent / Windows 11
├── js/
│   ├── errors/
│   │   └── CalculatorErrors.js  # Ierarhia de excepții OOP
│   ├── operations/
│   │   ├── BaseOperation.js     # Clase abstracte (AbstractOperation, etc.)
│   │   ├── ArithmeticOps.js     # Operații aritmetice (Cerința a)
│   │   ├── AdditionalOps.js     # Operații adăugătoare (Cerința b)
│   │   ├── TrigOps.js           # Operații trigonometrice DEG/RAD (Cerința c)
│   │   └── LogOps.js            # Operații logaritmice (Cerința c)
│   ├── core/
│   │   ├── MemoryUnit.js        # Gestionare memorie MC, MR, M+, M-, MS (Cerința d)
│   │   ├── BaseConverter.js     # Convertor baze HEX, DEC, OCT, BIN (Cerința f)
│   │   └── CalculatorEngine.js  # Motorul principal OOP (Cerințele a-f)
│   ├── ui/
│   │   └── CalculatorUI.js      # Controller DOM, tastatură și evenimente
│   └── app.js                   # Inițializare aplicație
├── test_engine.js               # Teste automate unitare Node.js
├── java/                        # Implementarea echivalentă 100% în Java
│   └── src/calculator/
│       ├── Main.java            # Executabil consolă cu teste automate
│       ├── core/                # CalculatorEngine, MemoryUnit, BaseConverter
│       ├── operations/          # Operații IOperation, Binary, Unary
│       └── errors/              # CalculatorException, etc.
└── README.md
```

---

## 🚀 Cum se Rulează Aplicația

### 1. Rularea Aplicației Web (HTML / CSS / JS)
* Deschide direct fișierul `index.html` în orice navigator web (Chrome, Safari, Edge, Firefox):
  * **Dublu-click pe `index.html`** sau deschidere directă în browser.
* Sau rulare prin terminal:
  ```bash
  open /Users/preference/Desktop/calculator/index.html
  ```

### 2. Rularea Testelor Automate Unitare (Node.js)
Din folderul `calculator`:
```bash
node test_engine.js
```

### 3. Rularea Versiunii Java
Din folderul `calculator/java`:
```bash
mkdir -p bin && javac -d bin $(find src -name "*.java") && java -cp bin calculator.Main
```
