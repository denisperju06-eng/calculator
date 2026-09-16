# Lucrarea de Laborator Nr. 5: Rețeaua Internet + Aplicație Rezident

**Disciplina:** Paradigma Orientată pe Obiecte (POO)  
**Limbaj:** Java (Standard Edition, Java 25 / Java 17+)  
**Arhitectură:** Orientată pe Obiecte (Abstracție, Încapsulare, Moștenire, Polimorfism, Design Patterns: Observer, Command, Mediator, Strategy)

---

## 1. Cerințele Laboratorului și Corespondența în Cod

| Cerință | Descriere conform `conditii.md` | Implementare în Soluție | Fișiere Cheie |
| :--- | :--- | :--- | :--- |
| **a** | **Conectarea la un serviciu Internet pentru obținerea informației** (schimb valutar, date meteo) *(Cerința minimă nota 5)* | Preluare date prin `java.net.HttpURLConnection` de la două servicii REST publice: Open-Meteo (vreme) și Open Exchange Rates (valută). Parsare JSON autonomă fără dependențe externe. | `WeatherService.java`<br>`CurrencyService.java`<br>`SimpleJson.java`<br>`DataService.java` |
| **b** | **Ascunderea aplicației de pe bara Start cu afișarea în System tray** *(1 punct)* | Integrare rezidentă prin `java.awt.SystemTray` și `TrayIcon`. Închiderea ferestrei (`X`) nu oprește aplicația, ci o ascunde în tray (`HIDE_ON_CLOSE`). Meniu contextual popup bogat și restaurare la clic. | `TrayManager.java`<br>`DashboardFrame.java`<br>`ResidentApp.java` |
| **c** | **Setarea perioadei de recitire a informației din Internet și citirea repetată** *(1 punct)* | Utilizare `java.util.Timer` și `java.util.TimerTask`. Permite setarea dinamică a intervalului din UI (slider/spinner/combobox) și meniul din tray (10s, 30s, 1m, 5m etc.). Include cronometru în timp real (countdown). | `PeriodicScheduler.java`<br>`SettingsDialog.java` |
| **d** | **Modificarea pictogramei aplicației în dependență de starea aplicației sau datelor citite** *(2 puncte)* | Desenare vectorială dinamică High-DPI (`BufferedImage` + `Graphics2D`):<br>• Meteo: Soare cu raze, nori, ploaie, zăpadă, furtună, ceață + insignă temperatură.<br>• Valută: Monedă cu indicator de trend (▲ creștere verde / ▼ scădere roșie).<br>• Stare: Spinner animat de încărcare, triunghi roșu de eroare/offline. | `IconRenderer.java`<br>`TrayManager.java`<br>`WeatherCondition.java`<br>`AppStatus.java` |
| **e** | **Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident** *(1 punct)* | Manager de scurtături cu `KeyEventDispatcher` global, Command Pattern (`HotkeyAction`), reconfigurare interactivă a tastelor în UI și comenzi în consolă pentru mod rezident/background. | `HotkeyManager.java`<br>`HotkeyBinding.java`<br>`HotkeyAction.java`<br>`SettingsDialog.java` |

---

## 2. Principii de Programare Orientată pe Obiecte (POO) Aplicate

1. **Abstracție:**
   - Interfața generică `DataService<T>` definește contractul pentru preluarea datelor prin rețea, ascunzând complexitatea protocoalelor HTTP și a parsării.
   - Interfața funcțională `HotkeyAction` abstractizează execuția comenzilor de la tastatură.
   - Interfața `DataUpdateListener` abstractizează recepția evenimentelor de actualizare.

2. **Încapsulare:**
   - Modelele de date `WeatherData`, `CurrencyData`, `CityLocation`, `HotkeyBinding` au câmpuri private, imutabile sau strict validate prin metode getter și metode de acces sincronizate.
   - Detaliile de conectare HTTP (`HttpURLConnection`, stream-uri, timpi de expirare) sunt complet încapsulate în clasele de serviciu.

3. **Polimorfism:**
   - Clasele `WeatherService` și `CurrencyService` implementează polimorfic aceeași interfață `DataService<T>`.
   - Controlerul `ResidentApp` poate comuta dinamic între servicii la timpul de execuție (Runtime Polymorphism).
   - Generarea pictogramelor în `IconRenderer` tratează polimorfic stările și condițiile meteo/valutare.

4. **Moștenire:**
   - `DashboardFrame` extinde `javax.swing.JFrame`.
   - `SettingsDialog` extinde `javax.swing.JDialog`.
   - Sarcinile planificate extind `java.util.TimerTask`.

5. **Design Patterns Utilizate:**
   - **Observer Pattern:** `DataUpdateListener` permite ca `DashboardFrame` și `TrayManager` să fie notificate automat ori de câte ori sosesc date noi din rețea.
   - **Command Pattern:** `HotkeyAction` și `HotkeyBinding` decuplează declanșarea scurtăturilor de logica lor de execuție.
   - **MVC / Mediator Pattern:** `ResidentApp` funcționează ca mediator central între serviciile de rețea, scheduler, UI și System Tray.
   - **Factory / Procedural Renderer:** `IconRenderer` construiește dinamic obiecte `Image` în funcție de parametri.

---

## 3. Structura Proiectului

```
lab5/
├── conditii.md              # Cerințele originale ale laboratorului
├── README.md                # Documentația completă a soluției
├── run_app.sh               # Script de compilare și lansare a aplicației
├── run_tests.sh             # Script de rulare a suitei de teste automate
└── src/
    ├── Main.java                 # Punctul de intrare (Entry Point)
    ├── ResidentApp.java          # Controlerul central al aplicației rezidente
    ├── AppStatus.java            # Enumerare stări aplicație și conexiune
    ├── WeatherCondition.java     # Enumerare condiții meteo (WMO code mapping)
    ├── WeatherData.java          # Model imutabil date meteo
    ├── CurrencyData.java         # Model imutabil date curs valutar și trend
    ├── DataServiceType.java      # Tip serviciu activ (Meteo / Valută)
    ├── CityLocation.java         # Locații geografice predefinite
    ├── SimpleJson.java           # Parser JSON minimalist autonom (zero dependențe)
    ├── DataService.java          # Interfață generică servicii HTTP
    ├── WeatherService.java       # Implementare client HTTP Open-Meteo
    ├── CurrencyService.java      # Implementare client HTTP Curs Valutar
    ├── DataUpdateListener.java   # Interfață Observer pentru actualizări
    ├── PeriodicScheduler.java    # Planificator periodic bazat pe java.util.Timer
    ├── IconRenderer.java         # Generator dinamic pictograme System Tray
    ├── TrayManager.java          # Manager System Tray, TrayIcon și meniu popup
    ├── HotkeyAction.java         # Interfață funcțională Command Pattern
    ├── HotkeyBinding.java        # Asociere KeyStroke <-> Acțiune <-> Descriere
    ├── HotkeyManager.java        # Dispecer global de taste și ascultător consolă
    ├── DashboardFrame.java       # Interfața grafică principală (Swing)
    ├── SettingsDialog.java       # Dialog configurare interval și combinatii taste
    └── ResidentTestSuite.java    # Suită completă de teste unitare automate
```

---

## 4. Combinații de Taste (Hotkeys) Suportate

Aplicația înregistrează următoarele taste rapide globale (active pe macOS cu `Cmd+Shift` și pe Windows/Linux cu `Ctrl+Shift`):

| Combinație (Mac) | Combinație (Win/Linux) | Comandă Consolă | Acțiune Realizată |
| :--- | :--- | :---: | :--- |
| `Cmd + Shift + R` | `Ctrl + Shift + R` | `r` / `refresh` | **Actualizare forțată imediată** a datelor din rețea |
| `Cmd + Shift + D` | `Ctrl + Shift + D` | `d` / `dash` | **Arată / Ascunde Tabloul** de Comandă în/din System Tray |
| `Cmd + Shift + M` | `Ctrl + Shift + M` | `m` / `mode` | **Comutare mod de lucru** (Meteo ⇄ Curs Valutar) |
| `Cmd + Shift + S` | `Ctrl + Shift + S` | — | **Deschide Fereastra de Setări** și reconfigurare taste |
| `Cmd + Shift + Q` | `Ctrl + Shift + Q` | `q` / `quit` | **Închidere completă** a aplicației rezidente |

> **Notă:** Utilizatorul poate reconfigura oricare dintre aceste combinații deschizând dialogul de setări (`SettingsDialog`) și apăsând combinația dorită de taste.

---

## 5. Compilare și Rulare

### Rulare Rapidă cu Scripturile Incluse:

1. **Pornirea Aplicației Rezidente:**
   ```bash
   ./run_app.sh
   ```

2. **Rularea Testelor Automate:**
   ```bash
   ./run_tests.sh
   ```

### Rulare Manuală din Linie de Comandă:

```bash
cd src
javac -d . *.java
java Main
```
