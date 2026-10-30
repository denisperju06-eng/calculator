#include <iostream>
#include <string>
#include <vector>

using namespace std;

// ====================================================================
// 1. PARADIGMA FUNCTIONALA
// - Fara bucle (nici while, nici for), fara pair, fara nimic avansat.
// - Totul se parcurge pur recursiv.
// ====================================================================

// Citire recursiva a cifrelor unui numar (fara while)
void citeste_cifre(const string& s, int& i, int& nr) {
    if (i < (int)s.size() && s[i] >= '0' && s[i] <= '9') {
        nr = nr * 10 + (s[i] - '0');
        i++;
        citeste_cifre(s, i, nr); // apel recursiv pentru urmatoarea cifra
    }
}

void aplatizeaza_functional(const string& s, int& i, vector<int>& rez) {
    // Conditie de oprire: am ajuns la sfarsitul sirului
    if (i >= (int)s.size()) {
        return;
    }

    char c = s[i];

    // Inchidere sub-array
    if (c == ']' || c == '}') {
        i++;
        return;
    }

    // Deschidere sub-array: apel recursiv
    if (c == '[' || c == '{') {
        i++;
        aplatizeaza_functional(s, i, rez); // rezolvam sub-array-ul
    }
    // Daca este cifra: citim numarul si continuam recursiv
    else if (c >= '0' && c <= '9') {
        int nr = 0;
        citeste_cifre(s, i, nr);
        rez.push_back(nr);
        aplatizeaza_functional(s, i, rez);
        return;
    }
    // Ignoram virgule, spatii
    else {
        i++;
    }

    // Trecem recursiv la urmatorul caracter
    aplatizeaza_functional(s, i, rez);
}

// ====================================================================
// 2. PARADIGMA PROCEDURALA
// - Clasica, simpla, cu bucla while si indici
// ====================================================================
void aplatizeaza_procedural(const string& s, int& i, vector<int>& rez) {
    while (i < (int)s.size()) {
        char c = s[i];

        // Daca incepe un nou array -> intram in el
        if (c == '[' || c == '{') {
            i++;
            aplatizeaza_procedural(s, i, rez);
        }
        // Daca se termina array-ul curent -> iesim
        else if (c == ']' || c == '}') {
            i++;
            return;
        }
        // Daca este cifra -> citim numarul
        else if (c >= '0' && c <= '9') {
            int nr = 0;
            while (i < (int)s.size() && s[i] >= '0' && s[i] <= '9') {
                nr = nr * 10 + (s[i] - '0');
                i++;
            }
            rez.push_back(nr);
        }
        // Ignoram virgule, spatii etc.
        else {
            i++;
        }
    }
}

// ====================================================================
// PROGRAM PRINCIPAL
// ====================================================================
int main() {
    cout << "Alege metoda:\n";
    cout << "1. Functionala (pur recursiva, fara while)\n";
    cout << "2. Procedurala (cu while)\n";
    cout << "Optiunea (1 sau 2): ";

    int optiune;
    cin >> optiune;
    cin.ignore(); // curatam buffer-ul dupa citirea optiunii

    cout << "Introdu lista: ";
    string s;
    getline(cin, s);

    vector<int> rezultat;
    int index = 0;

    // Executam doar metoda aleasa
    switch (optiune) {
        case 1:
            aplatizeaza_functional(s, index, rezultat);
            break;
        case 2:
            aplatizeaza_procedural(s, index, rezultat);
            break;
        default:
            cout << "Optiune invalida!\n";
            return 1;
    }

    // Afisare
    cout << "Rezultat: [";
    for (int i = 0; i < (int)rezultat.size(); i++) {
        cout << rezultat[i];
        if (i + 1 < (int)rezultat.size()) {
            cout << ", ";
        }
    }
    cout << "]\n";

    return 0;
}

