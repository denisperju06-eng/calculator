# Lucrarea de Laborator 2: Paradigma Procedurală
## Interacțiunea cu alte aplicații prin intermediul API

Acest proiect conține implementarea cerințelor din [conditii.md](file:///Users/preference/Desktop/calculator/Procedurala/lab2/conditii.md) utilizând **Python 3** și biblioteca `requests`, respectând cu strictețe **Paradigma Procedurală**.

---

## 📋 Acoperirea Cerințelor din Temă

| Cerință | Descriere conform `conditii.md` | Funcție / Procedură în `main.py` | Endpoint / Detalii tehnice | Punctaj |
| :--- | :--- | :--- | :--- | :---: |
| **a** | **Conectarea și transmiterea informației** într-o aplicație externă prin API | `initializeaza_conexiune()`<br>`transmite_informatie_postare()` | `POST /posts`<br>Transmitere date JSON (titlu, corp, userId) | **Nota 5 (bază)** |
| **b** | **Citirea informației** din aplicația externă | `citeste_detalii_utilizator()` | `GET /users/{id}`<br>Preluare profil utilizator | **+1 punct** |
| **c** | **Salvare + Deschidere** informație recepționată | `salveaza_date_in_fisier()`<br>`deschide_date_din_fisier()` | Salvare în format local `date_api_salvate.json` și recitire/parsare în memorie | **+1 punct** |
| **d** | **Minim 3 operații diferite** prin API | 1. `citeste_detalii_utilizator()`<br>2. `transmite_informatie_postare()`<br>3. `citeste_sarcini_utilizator()`<br>*(Bonus)* `citeste_comentarii_postare()` | 1. `GET /users/1` (profil)<br>2. `POST /posts` (creare resursă)<br>3. `GET /todos?userId=1&completed=false` (filtrare)<br>4. `GET /comments?postId=1` (asociere) | **+2 puncte** |
| **e** | **Închiderea aplicației externe**, fără a închide aplicația dvs. | `inchide_conexiune_aplicatie_externa()`<br>`demonstreaza_continuitate_locala()` | Închidere sesiune HTTP (`sesiune.close()`), eliberare socket-uri de rețea. Aplicația locală continuă procesarea offline a datelor | **+1 punct** |

---

## ⚙️ Principii ale Paradigmei Procedurale Aplicate

1. **Modularitate bazată pe proceduri (funcții):**
   - Codul este împărțit în proceduri autonome cu responsabilități unice (`initializeaza_conexiune`, `transmite_informatie_postare`, etc.).
   - Nu sunt definite clase (`class`) sau ierarhii de obiecte, respectându-se principiul procedural pur.

2. **Flux secvențial de date și parametri:**
   - Starea aplicației este transmisă explicit ca argumente între funcții (ex. obiectul `sesiune`, identificatorii de resurse, căile de fișiere).
   - Datele sunt structurate ca dicționare și liste native Python.

3. **Tratare procedurală a erorilor și stărilor:**
   - Erorile de rețea (`requests.exceptions.RequestException`) și erorile de fișiere (`IOError`) sunt capturate și tratate local în fiecare procedură, fără a întrerupe fluxul general.

---

## 🌐 API-ul Extern Utilizat

A fost ales serviciul public **JSONPlaceholder** (`https://jsonplaceholder.typicode.com`), un REST API gratuit, stabil și standardizat pentru testarea interacțiunilor HTTP:
- **`GET /users/1`**: Returnează informațiile contului de utilizator.
- **`POST /posts`**: Recepționează un payload JSON și simulează crearea unei postări returnând codul `201 Created`.
- **`GET /todos?userId=1&completed=false`**: Filtrează sarcinile nefinalizate.
- **`GET /comments?postId=1`**: Returnează comentariile asociate unei postări.

---

## 🚀 Instrucțiuni de Rulare

### 1. Instalare dependențe (dacă nu sunt instalate)
```bash
python3 -m pip install requests
```

### 2. Rulare flux automat demonstrativ (toate cerințele a - e)
```bash
python3 main.py --auto
```

### 3. Rulare în mod interactiv cu meniu consolă
```bash
python3 main.py
```
*(sau prin alegerea opțiunii `da` la întrebarea inițială)*

---

## 📂 Structura Fișierelor

```
Procedurala/lab2/
├── conditii.md              # Cerințele oficiale ale laboratorului 2
├── main.py                  # Codul sursă procedural Python
├── README.md                # Documentația completă a laboratorului
└── date_api_salvate.json    # Fișierul generat local cu datele recepționate de la API (cerința c)
```
