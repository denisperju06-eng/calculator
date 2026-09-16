# Laboratorul 3 - Paradigma Orientată pe Obiecte (POO)
## Aplicație de Chat în Rețea Locală (Java Sockets, Threads, Swing)

Acest proiect reprezintă rezolvarea completă a laboratorului descris în [`conditii.md`](./conditii.md), implementat conform standardelor de calitate POO, modular, fără dependențe externe (utilizează exclusiv Java Standard Library).

---

### 1. Acoperirea Integrală a Cerințelor din `conditii.md`

| Cerință | Punctaj | Descriere & Implementare |
| :--- | :---: | :--- |
| **a. Transmiterea și recepționarea mesajelor în rețea** | **5** (min.) | Implementat cu **TCP Sockets** (`ServerSocket`, `Socket`) și **Multithreading** (`ClientHandler` per conexiune, fir de recepție dedicat pe client). Suportă mesagerie instantanee în timp real. |
| **b. Afișarea istoriei mesajelor primite** | **+1** | La alăturarea într-o cameră, serverul transmite istoricul complet al mesajelor. Toate mesajele sunt stocate în memorie și salvate persistent pe disc (`HistoryManager`). Aplicația client oferă butonul **"📜 Istoric Mesaje / Căutare"** cu filtrare instantanee și buton de **Export în fișier .txt**. |
| **c. Posibilitatea de a răspunde la mesaj (Reply)** | **+1** | Fiecare mesaj din chat dispune de buton **"↩ Răspunde"** și meniu contextual (click dreapta). La selectare, apare bara `ReplyPanel` cu previzualizarea mesajului citat. Mesajul trimis conține citarea stilizată a autorului și a fragmentului original. |
| **d. Transmiterea și recepționarea fișierelor** | **+2** | Buton dedicat **"📎 Fișier"** (`JFileChooser`). Datele sunt împachetate în obiecte serializabile `FileAttachment` (nume, dimensiune, `byte[]`). În chat apare un card dedicat cu pictograma fișierului, dimensiunea lizibilă (KB/MB) și butonul **"💾 Salvează Fișier"** pentru descărcare directă pe disc. |
| **e. Crearea chat-room (camere multiple)** | **+1** | Suport pentru camere multiple inițiale ("General", "Laborator POO", "Proiecte") și **creare dinamică de noi camere** prin butonul **"+ Cameră nouă"**. Izolare completă a mesajelor pe camere, actualizarea automată a listei de camere și a utilizatorilor activi online în fiecare cameră. |
| **TOTAL** | **10 / 10** | **Toate cerințele sunt 100% implementate și testate.** |

---

### 2. Arhitectura Claselor și Principiile POO Aplicate

Codul sursă se găsește în directorul [`src/`](./src/):

```
src/
├── MessageType.java         # Enum cu toate tipurile de pachete de protocol
├── FileAttachment.java      # Clasă POO ce încapsulează fișierele binare transmise
├── Message.java             # Clasă serializabilă pentru mesaje, reply, fișiere și camere
├── ChatRoom.java            # Încapsulează starea unei camere (participanți, istoric)
├── HistoryManager.java      # Persistența pe disc (binar + jurnal text) și export fișiere
├── ServerEventListener.java # Interfață Observer pentru evenimentele serverului
├── ClientHandler.java       # Fir de execuție (Thread) alocat fiecărui client pe server
├── Server.java              # Nucleul serverului TCP Socket și distribuirea mesajelor
├── ServerGUI.java           # Interfață grafică Swing pentru administrarea serverului
├── ClientListener.java      # Interfață Observer pentru decuplarea rețelei de GUI
├── Client.java              # Nucleul clientului de rețea (Sockets, Threads)
├── ReplyPanel.java          # Componentă Swing pentru afișarea stării de răspuns (Reply)
├── MessageBubblePanel.java  # Componentă Swing pentru redarea cardurilor de mesaj / fișier
├── HistoryDialog.java       # Fereastră Swing de căutare, filtrare și export istoric
├── ClientGUI.java           # Fereastra principală Swing a utilizatorului
└── ChatIntegrationTest.java # Suită completă de teste automate end-to-end
```

#### Concepte POO Remarcabile:
- **Încapsulare:** Câmpuri private, gettere/settere bine definite, stări interne protejate prin colecții sincronizate (`Collections.synchronizedSet`, `ConcurrentHashMap`).
- **Polimorfism & Interfețe (Observer Pattern):**
  - `ClientListener` decuplează logica de rețea `Client` de interfața `ClientGUI`.
  - `ServerEventListener` decuplează nucleul `Server` de interfața `ServerGUI`.
- **Factory Method Pattern:** Crearea de mesaje prin metode statice expresive (`Message.createTextMessage`, `Message.createFileMessage`, `Message.createSystemMessage` etc.).
- **Thread Safety:** Operațiile pe fluxurile `ObjectOutputStream` sunt sincronizate (`synchronized`), iar actualizările grafice Swing se execută exclusiv pe Event Dispatch Thread via `SwingUtilities.invokeLater(...)`.

---

### 3. Instrucțiuni de Compilare și Rulare

#### Varianta 1: Cu scripturile automate furnizate

1. **Pornire Server (GUI):**
   ```bash
   ./run_server.sh
   ```
   *Se va deschide panoul de control al Serverului, de unde puteți monitoriza clienții, camerele și log-urile în timp real.*

2. **Pornire Clienți (deschideți într-unul sau mai multe terminale):**
   ```bash
   ./run_client.sh
   ```
   *Puteți lansa mai multe instanțe pentru a simula mai mulți utilizatori (de ex. "Ion", "Maria", "Alex").*

3. **Rulare Teste Automate de Integrare:**
   ```bash
   ./run_tests.sh
   ```
   *Rulează suita completă de 6 teste automate fără a necesita intervenție manuală.*

---

#### Varianta 2: Manual prin comenzi `javac` și `java`

1. **Compilare:**
   ```bash
   cd src
   javac *.java
   ```

2. **Pornire Server:**
   ```bash
   java Server
   ```
   *(Sau în mod consolă fără interfață grafică: `java Server --cli`)*

3. **Pornire Client:**
   ```bash
   java Client
   ```

4. **Rulare Teste:**
   ```bash
   java ChatIntegrationTest
   ```

---

### 4. Ghid de Utilizare în Interfața Grafică (Client GUI)

1. **Conectare:** Introduceți adresa IP a serverului (ex: `localhost` sau IP-ul local din rețea), portul (`12345`) și numele dvs., apoi apăsați **"Conectare"**.
2. **Trimitere Mesaje:** Tastați mesajul în câmpul de jos și apăsați `Enter` sau butonul **"Trimite"**.
3. **Răspuns la Mesaj (Reply):** Apăsați butonul **"↩ Răspunde"** de pe orice mesaj sau faceți click dreapta -> *"Răspunde (Reply)"*. Va apărea bara de citare. Tastați răspunsul și trimiteți.
4. **Trimitere Fișiere:** Apăsați butonul **"📎 Fișier"**, selectați orice fișier (imagine, document PDF, cod sursă etc.) și acesta va fi transmis în cameră.
5. **Descărcare Fișiere:** Pe cardul fișierului primit, apăsați **"💾 Salvează Fișier"** pentru a alege locația de salvare.
6. **Camere de Chat (Rooms):** Selectați o altă cameră din lista din stânga pentru a schimba camera. Pentru a crea o cameră nouă, apăsați butonul **"+ Cameră nouă"**.
7. **Istoric și Căutare:** Apăsați butonul **"📜 Istoric Mesaje / Căutare"** pentru a căuta prin mesaje sau pentru a exporta discuția într-un fișier `.txt`.
