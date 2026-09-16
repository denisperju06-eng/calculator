# Lucrarea de Laborator Nr. 2: Redactor Text Bogat (Rich Text Editor)
## Paradigma Orientată pe Obiecte (POO) - Java Swing

Proiectul implementează un **Redactor de Text** complet, modular și extensibil în limbajul **Java** folosind biblioteca grafică **Swing**, respectând cu strictețe principiile **Paradigmei Orientate pe Obiecte (POO)**.

---

### 1. Arhitectura și Modularitatea Proiectului

Codul sursă este organizat modular în fișiere `.java` distincte în folderul `src/`, fiecare clasă având o responsabilitate clar definită (Single Responsibility Principle):

1. **`Main.java`**:
   - Punctul de pornire al aplicației (Entry Point).
   - Configurează *Look and Feel*-ul nativ al sistemului de operare și inițializează interfața grafică pe firul de execuție Swing (*Event Dispatch Thread* - EDT).

2. **`MainWindow.java`**:
   - Fereastra principală a aplicației (`JFrame`).
   - Asamblează și coordonează componentele: bara de meniuri (`JMenuBar`), bara de instrumente de formatare (`FormatToolBar`), bara de acțiuni rapide, containerul de file (`TabbedEditorPane`) și bara de stare (`StatusBar`).
   - Gestionează evenimentele globale de închidere cu salvare a modificărilor nesalvate.

3. **`TextEditorTab.java`**:
   - Reprezintă o filă (tab) de editare independentă.
   - Încapsulează un `JTextPane` configurat cu `RTFEditorKit` și `StyledDocument`, un `UndoManager` pentru operațiile Undo/Redo, o referință către fișierul fizic de pe disc (`File`) și starea de modificare (*dirty flag*).
   - Conține o zonă de scroll (`JScrollPane`) cu riglă laterală pentru numerotarea liniilor (`LineNumberView`).

4. **`TabbedEditorPane.java`**:
   - Subclasă a `JTabbedPane` care gestionează lucrul cu mai multe fișiere deschise simultan în file noi (cerința **e**).
   - Permite crearea de noi documente, închiderea tab-urilor cu salvare automată sau dialog de confirmare, comutarea între documente.

5. **`TabHeaderComponent.java`**:
   - Componentă personalizată pentru antetul fiecărei file din `JTabbedPane`.
   - Afișează titlul documentului, indicatorul de modificare (`*`) și un buton de închidere rapidă (`×`) cu efect de hover.

6. **`StyleService.java`**:
   - Serviciu dedicat gestionării stilurilor textului (cerința **d**).
   - Asigură **suprapunerea stilurilor** (*overlapping styles*): aplicarea unui stil (Bold, Italic, Underline, Culoare, Font, Mărime) păstrează nealterate celelalte stiluri existente utilizând `doc.setCharacterAttributes(start, length, attr, false)` (`replace = false`).
   - Gestionează comutarea inteligentă a atributelor pe selecții și la nivelul cursorului (*caret input attributes*).

7. **`SearchService.java`**:
   - Serviciu care implementează algoritmii de căutare, înlocuire și evidențiere a subșirurilor (cerințele **b** și **c**).
   - Suportă căutare bidirecțională (Înainte / Înapoi), sensibilitate la majuscule/minuscule (*match case*) și reluare ciclică (*wrap around*).
   - Permite înlocuirea punctuală, înlocuirea globală (*Replace All*) și evidențierea simultană a tuturor aparițiilor (*Highlight / Select All*).

8. **`SearchResult.java`**:
   - Clasă model imutabilă ce încapsulează rezultatul unei căutări (poziție, lungime, stare de succes, mesaj explicativ).

9. **`FindReplaceDialog.java`**:
   - Dialog nemodal (`JDialog`) pentru interacțiunea utilizatorului cu funcțiile de căutare și înlocuire.
   - Permite utilizatorului să caute înainte/înapoi, să înlocuiască și să evidențieze toate aparițiile.

10. **`FileService.java`**:
    - Serviciu pentru operațiile I/O cu fișiere (cerințele **a** și **f**).
    - Asigură salvarea și încărcarea documentelor în format **RTF (Rich Text Format)** cu păstrarea integrală a stilurilor aplicate, precum și în format Text Simplu (`.txt`).
    - Gestionează dialogurile de avertizare pentru suprascriere și confirmarea salvării la închidere.

11. **`FormatToolBar.java`**:
    - Bară de instrumente cu selector pentru familia de fonturi (combinație între fonturi populare și fonturile de sistem), selector de mărimi de font, butoane comutator Bold, Italic, Underline, paletă de culori text și evidențiere fundal, butoane de aliniere paragraf (stânga, centru, dreapta, justify).
    - Se sincronizează dinamic cu stilul textului de la poziția curentă a cursorului.

12. **`LineNumberView.java`**:
    - Componentă grafică integrată în `JScrollPane` ca *Row Header* pentru afișarea automată a numerelor de linie și evidențierea liniei curente.

13. **`StatusBar.java`**:
    - Bară de stare afișată la baza ferestrei ce raportează linia, coloana, numărul de caractere selectate, totalul caracterelor din document, formatul fișierului și mesaje de stare.

---

### 2. Implementarea Cerințelor din `conditii.md`

| Cerință | Descriere | Implementare |
| :--- | :--- | :--- |
| **a** | **Redactarea textului, salvarea și deschiderea fișierelor** | Redactare completă, suport Undo/Redo, tăiere/copiere/lipire, salvare (`Ctrl+S`), salvare ca (`Ctrl+Shift+S`), deschidere (`Ctrl+O`), detecție format, dialoguri de salvare la modificări nesalvate (`FileService`, `TextEditorTab`). |
| **b** | **Căutarea subșirurilor (ținând cont de direcție)** | Căutare **Înainte (în jos)** și **Înapoi (în sus)**, opțiune de *Case Sensitive*, opțiune de *Wrap Around*, selecție automată și derulare vizuală la textul găsit (`SearchService`, `FindReplaceDialog`). |
| **c** | **Înlocuirea subșirurilor cu posibilitatea de a selecta toate** | Înlocuire curentă (*Replace*), Înlocuire peste tot (*Replace All*), Evidențierea/Selectarea simultană a tuturor aparițiilor din întregul document cu marcaj vizual galben (*Highlight All*) (`SearchService`, `FindReplaceDialog`). |
| **d** | **Modificarea fontului pe subșiruri (font, culoare, mărime, italic, bold, underline) cu stiluri suprapuse** | Posibilitate de aplicare concomitentă a oricărei combinații de font, mărime, culoare, aldin (bold), cursiv (italic) și subliniat (underline). Atributele se suprapun fără a se anula reciproc datorită parametrului `replace = false` din `StyledDocument.setCharacterAttributes` (`StyleService`, `FormatToolBar`). |
| **e** | **Lucrul cu mai multe fișiere în file noi aparte (new tabs)** | `JTabbedPane` modern cu tab-uri independente, buton de închidere `×`, indicator `*` pentru fișiere modificate, scurtături `Ctrl+T` (tab nou) și `Ctrl+W` (închidere tab) (`TabbedEditorPane`, `TabHeaderComponent`). |
| **f** | **Salvarea fișierelor cu păstrarea stilurilor (format RTF)** | Utilizarea standardului `RTFEditorKit` pentru serializarea și deserializarea atributelor grafice de text în format RTF (Rich Text Format). Când fișierul este salvat și redeschis, toate stilurile de font, culori, bold, italic și underline se păstrează fidel (`FileService`). |

---

### 3. Compilare și Rulare

Din linia de comandă (Terminal):

```bash
# Navigare în folderul lab2
cd /Users/preference/Desktop/calculator/lab2

# Compilare surse Java
javac src/*.java

# Rulare aplicație
java -cp src Main
```

---

### 4. Scurtături de la Tastatură (Keybindings)

- `Ctrl + T` : Deschide o filă nouă (tab nou)
- `Ctrl + O` : Deschide un fișier de pe disc (.rtf sau .txt)
- `Ctrl + S` : Salvează fișierul curent
- `Ctrl + Shift + S` : Salvează fișierul curent sub alt nume (Save As)
- `Ctrl + W` : Închide fila curentă
- `Ctrl + Q` : Închide aplicația
- `Ctrl + Z` : Undo (Anulează)
- `Ctrl + Y` : Redo (Refă)
- `Ctrl + X` / `Ctrl + C` / `Ctrl + V` : Taie / Copiază / Lipește
- `Ctrl + A` : Selectează tot
- `Ctrl + B` : Comută stilul Aldin (Bold)
- `Ctrl + I` : Comută stilul Cursiv (Italic)
- `Ctrl + U` : Comută stilul Subliniat (Underline)
- `Ctrl + F` : Deschide fereastra de Căutare și Înlocuire
