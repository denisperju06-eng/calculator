# Documentația Pachetului Funcțional (Logica aplicației de Chat)

Acest document explică pe înțelesul tuturor rolul fiecărei clase și funcții aflate în folderul `core/functional/`. Acest cod este "creierul" aplicației, ocupându-se de rețea, stocarea datelor și transmiterea mesajelor, fără a desena nimic pe ecran.

---

## 1. `Message.java` și `MessageType.java`
Acesta este pachetul poștal al aplicației. Orice informație trimisă între calculatoare (text, poze, logări) este un `Message`.

* **`Message` (Clasă)**: Implementează `Serializable`, ceea ce înseamnă că Java poate transforma acest obiect într-un șir de byți pentru a fi trimis prin internet.
  * **Funcții cheie**:
    * `createTextMessage(...)` - Creează un mesaj simplu de tip text.
    * `createFileMessage(...)` - Creează un mesaj care conține un fișier atașat.
    * `createConnectMessage(...)` / `createDisconnectMessage(...)` - Spune serverului că un utilizator s-a conectat sau deconectat.
    * Gettere (ex: `getContent()`, `getSender()`, `getFormattedTime()`) - Returnează detaliile mesajului.
* **`MessageType` (Enumerație)**: O listă fixă cu tipurile de mesaje permise: `TEXT`, `FILE`, `CONNECT`, `JOIN_ROOM` etc. Ajută calculatorul să știe imediat dacă primește o poză sau un simplu salut.

## 2. `FileAttachment.java`
Un "plic" suplimentar pus în interiorul unui `Message` doar atunci când se trimite un fișier.
* **Funcții cheie**:
  * `getFileData()` - Returnează datele brute ale fișierului.
  * `getFileName()` - Returnează numele fișierului (ex: "proiect.pdf").
  * `getFormattedSize()` - Calculează și returnează dimensiunea într-un format ușor de citit (MB, KB).

---

## 3. `Server.java`
Gândiți-vă la el ca la "Oficiul Poștal Central". Nu trimite el mesaje, dar le primește pe toate și știe unde să le redirecționeze.
* **Funcții cheie**:
  * `start()` / `stop()` - Pornește sau oprește ascultarea pe un port de rețea (ex: 12345).
  * `registerClient(...)` - Notează într-o listă fiecare client nou care se conectează.
  * `createRoom(...)` / `joinRoom(...)` - Gestionează "Camerele" de discuție.
  * `broadcastToRoom(...)` - Trimite un mesaj primit către toți oamenii dintr-o anumită cameră (ex: "General").

## 4. `ClientHandler.java`
Serverul nu are timp să stea de vorbă individual cu toți miile de clienți. Astfel, pentru fiecare om care se conectează, serverul angajează un "asistent personal" - acesta este `ClientHandler`. 
Rulează pe propriul său *Thread* (fir de execuție) și doar așteaptă și citește mesajele de la clientul său, dându-le mai departe către `Server`.

## 5. `ChatRoom.java`
Reprezintă un grup/o cameră (ex: "Laborator POO").
* Reține numele camerei și o listă cu toți `ClientHandler`-ii (clienții) care se află curent în acea cameră, pentru a ști cui să livreze mesajele de grup.

---

## 6. `Client.java`
Programul de rețea de pe calculatorul utilizatorului (ex: calculatorul lui Alice). El ia comenzile de la interfața grafică și le trimite pe rețea spre `Server`.
* **Funcții cheie**:
  * `connect()` / `disconnect()` - Stabilește conexiunea (`Socket`) cu IP-ul serverului.
  * `sendMessage(...)` / `sendFile(...)` - Împachetează textul/fișierul într-un obiect `Message` și îl trimite.
  * *Fir de execuție de ascultare*: În fundal, `Client.java` așteaptă în permanență mesaje noi de la server. Când vine unul, anunță interfața vizuală să-l afișeze.

## 7. `HistoryManager.java`
Responsabil de "memoria" aplicației.
* **Funcții cheie**:
  * `appendMessage(...)` - Salvează automat orice mesaj într-un fișier binar pe hard disk (`.dat`).
  * `loadBinaryHistory(...)` - Când deschizi chat-ul a doua zi, citește fișierul `.dat` și încarcă mesajele vechi.
  * `exportToTextFile(...)` - Permite salvarea unei conversații într-un fișier text normal (`.txt`), pentru a fi citit de om.

---

## 8. Fețele Invizibile (Interfețele și Testele)

* **`ServerEventListener` și `ClientListener`**: Acestea sunt niște *contracte* (Observer Pattern). Partea funcțională se folosește de ele pentru a striga: *"A venit un mesaj nou!"*. Partea Vizuală (GUI) aude acest strigăt și decide să deseneze un balon de chat. Asta ajută ca codul funcțional să fie complet separat de ferestre sau butoane.
* **`ChatIntegrationTest.java`**: Un fișier automatizat de teste. El simulează conectarea a mai multor utilizatori virtuali ("Alice" și "Bob") care își trimit mesaje unii altora pentru a verifica dacă logica scrisă de noi funcționează corect (fără a fi nevoie să deschidem noi manual 3 ferestre).
