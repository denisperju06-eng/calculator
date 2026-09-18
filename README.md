# Proiect Universitar - Paradigma Orientată pe Obiecte (POO) & Paradigma Funcțională

Acest repository conține toate cele 6 lucrări de laborator realizate pentru cursul de **Paradigme de Programare**. Proiectele acoperă o gamă largă de aplicații desktop, web și utilitare, dezvoltate în principal în **Java (Swing/AWT)** pentru POO, dar incluzând și concepte din **Paradigma Funcțională**, **Procedurală** și **Scriptică** (Python, JS, C).

---

## 🗂️ Structura Proiectului (Pe Pași și Laboratoare)

### 1. Lab 1: Calculator de Buzunar (POO)
**Ce face:** Un calculator avansat cu interfață grafică inspirată din Windows 11.
**Funcționalități:**
- Operații aritmetice de bază (`+`, `-`, `*`, `/`, `mod`).
- Operații complexe (radicali, puteri, factorial, procente).
- Funcții trigonometrice (sin, cos, tan) și logaritmice (ln, log10).
- Memorie încapsulată (MC, MR, M+, M-, MS).
- Conversii între baze (HEX, DEC, OCT, BIN).
**Cum lucrează:** Dezvoltat complet pe baza conceptelor OOP (Abstractizare, Moștenire, Polimorfism). Motorul de calcul instanțiază dinamic operațiile matematice și folosește un sistem elegant de tratare a excepțiilor (`CalculatorException`).

### 2. Lab 2: Redactor Text Bogat (Rich Text Editor)
**Ce face:** O aplicație stil Notepad++ / WordPad, care permite editarea de text bogat (RTF) cu tab-uri.
**Funcționalități:**
- Salvare, deschidere fișiere RTF / TXT.
- Căutare inteligentă bidirecțională și înlocuire de text.
- Suprapunerea stilurilor (Bold, Italic, Underline, Font, Culoare, Mărime).
- Gestionarea multiplă a fișierelor prin tab-uri (`JTabbedPane`).
**Cum lucrează:** Utilizează `JTextPane` cu `RTFEditorKit`. Modificarea stilurilor se aplică la nivel de selecție sau pe întregul document fără a distruge proprietățile anterioare, aplicând un `StyleService` specializat.

### 3. Lab 3: Aplicație Chat Client-Server Multithreaded
**Ce face:** O platformă de chat multi-cameră (General, Discuții Libere, Proiecte etc.).
**Funcționalități:**
- Server concurent multithreading capabil să gestioneze clienți multipli.
- Interfață GUI cu panouri pentru chat.
- Trimite text, emoji-uri, și fișiere / imagini.
- Sistem de Reply (Răspuns la mesaj) și salvare istoric pe server.
**Cum lucrează:** Folosește **TCP/IP Sockets** (`ServerSocket` și `Socket`). Fiecare conexiune client este preluată de un `ClientHandler` ce rulează pe un thread separat. Comunicarea se face serializând obiecte de tip `Message`.

### 4. Lab 4: Web Browser & Search Engine Integrator
**Ce face:** Un browser web integrat, cu tab-uri și suport pentru motoare de căutare multiple.
**Funcționalități:**
- Navigare pe tab-uri, butoane Back, Forward, Refresh.
- Bara de bookmark-uri (Favorite) și istoric de navigare.
- Integrare cu API-uri / Interfețe pentru motoare de căutare (Google, Bing, DuckDuckGo, Wikipedia).
**Cum lucrează:** Implementează conceptul de Factory și Registry pentru instanțierea dinamică a căutărilor web. Paginile sunt afișate într-un mediu dedicat (WebView sau document text) gestionat prin event listeners pe fiecare tab.

### 5. Lab 5: Dashboard Rezident (Vreme & Valute) cu Hotkeys
**Ce face:** O aplicație ce rulează în fundal (System Tray) ce oferă date live.
**Funcționalități:**
- Preia cursul valutar la zi (ex. BNM API) și starea vremii (ex. OpenWeather).
- Fereastră de tip Dashboard activată prin Hotkeys globale (scurtături tastatură independente de fereastra activă).
- Actualizare periodică automată via `ScheduledExecutorService`.
**Cum lucrează:** Rulează fără consolă (Headless) într-un icon din bara de sistem (AWT `SystemTray`). Efectuează request-uri HTTP asincrone, parsează răspunsurile JSON și afișează un GUI interactiv la apăsarea unor combinații de taste.

### 6. Lab 6: Redactor Grafic (Java Paint)
**Ce face:** O aplicație completă pentru desen și grafică bitmap / vectorială de bază.
**Funcționalități:**
- Instrumente: Creion, Dreptunghi, Oval, Linie, Algoritm de umplere (Flood Fill).
- Desenare de curbe Bezier cu manipulare dinamică (3/4 puncte de control).
- Paletă de culori avansată (Culoare 1 / Culoare 2, `JColorChooser`).
**Cum lucrează:** Clasa de bază `Shape` este moștenită de toate uneltele geometrice. Randarea se face în metoda suprascrisă `paintComponent` pe un `BufferedImage` (Java 2D Graphics). Algoritmul Flood Fill folosește un sistem iterativ cu coadă BFS pentru performanță sporită.

---

## 🛠️ Cum se instalează și rulează
Toate laboratoarele POO (1-6) au implementări complete în limbajul **Java** și scripturi ajutătoare.
1. Deschideți terminalul.
2. Navigați în folderul laboratorului respectiv, ex: `cd lab2`
3. Rulați comanda de compilare și execuție, de obicei furnizată în `README.md`-ul specific laboratorului. (ex: `./compile.sh && ./run.sh`)
4. Pentru proiectele cu interfețe web/scriptice, deschideți simplu `index.html` în browser, sau folosiți comenzile Node.js / Python descrise.

Acest proiect demonstrează înțelegerea aprofundată a paradigmelor de programare, design patterns-urilor (Singleton, Factory, Observer, Command), rețelistică și a algoritmicii.
