#include <iostream>
#include <vector>

using namespace std;

// ====================================================================
// FUNCTII AJUTATOARE (pentru a imita programarea functionala in C++)
// Programarea functionala se bazeaza pe liste. Orice lista are un "cap"
// (primul element) si o "coada" (restul listei).
// ====================================================================

// Returneaza primul element din lista
int head(vector<int> L) {
    return L[0];
}

// Functie ajutatoare pentru a copia elementele recursiv
void copiaza_lista(vector<int> sursa, vector<int>& dest, int index) {
    if (index >= sursa.size()) return; // Conditia de oprire
    dest.push_back(sursa[index]);
    copiaza_lista(sursa, dest, index + 1); // Apel recursiv pentru urmatorul
}

// Returneaza lista fara primul element (coada listei)
vector<int> tail(vector<int> L) {
    vector<int> rest;
    copiaza_lista(L, rest, 1); // incepem copierea de la pozitia 1 (fara cap)
    return rest;
}

// Concateneaza un element la inceputul listei
vector<int> pune_in_fata(int element, vector<int> L) {
    vector<int> rezultat;
    rezultat.push_back(element);
    copiaza_lista(L, rezultat, 0); // copiem restul elementelor dupa el
    return rezultat;
}


// ====================================================================
// 1. CONSTRUCTIA LISTELOR FINITE
// ====================================================================

// 1.a Lista patratelor pana la N (folosind recursivitate)
vector<int> patrate(int n) {
    // Conditia de oprire a recursivitatii
    if (n == 0) {
        return {0};
    }
    
    // Calculam intai lista pentru n-1
    vector<int> rezultat = patrate(n - 1);
    // Apoi adaugam patratul curent la final
    rezultat.push_back(n * n);
    
    return rezultat;
}

// 1.b Lista factorialelor pana la N
int factorial(int n) {
    if (n == 0) return 1;
    return n * factorial(n - 1); // ex: 4! = 4 * 3!
}

vector<int> factoriale(int n) {
    if (n == 0) {
        return {1}; // 0! este 1
    }
    vector<int> rezultat = factoriale(n - 1);
    rezultat.push_back(factorial(n));
    return rezultat;
}


// ====================================================================
// 2. FUNCTII DE CALCUL AL SIRURILOR
// ====================================================================

// 2.a F(x, n) = x * n (folosind adunare repetata, recursiv)
int inmultire(int x, int n) {
    if (n == 0) return 0;
    return x + inmultire(x, n - 1);
}

// 2.b F(n) = suma de la 1 la n (adica: n + n-1 + ... + 1)
int suma(int n) {
    if (n == 0) return 0;
    return n + suma(n - 1);
}

// 2.c F(n) = suma de la j=1 la n din (suma de la 1 la j)
int suma_de_sume(int n) {
    if (n == 0) return 0;
    return suma(n) + suma_de_sume(n - 1);
}


// ====================================================================
// 3. FUNCTII PENTRU LUCRUL CU LISTE
// ====================================================================

// 3.a GetN(L, n) - extragere elementul de pe pozitia n (incepand de la 0) recursiv
int GetN(vector<int> L, int n) {
    if (n == 0) {
        return head(L); // daca n e 0, vrem primul element (capul)
    }
    // Daca nu e 0, taiem capul si cautam in coada pozitia n-1
    return GetN(tail(L), n - 1);
}

// O functie ajutatoare pentru a verifica daca un element exista deja in lista (recursiv)
bool exista(vector<int> L, int element) {
    if (L.size() == 0) return false;
    if (head(L) == element) return true;
    return exista(tail(L), element);
}

// 3.b Set(L) - elimina duplicatele din lista
vector<int> Set(vector<int> L) {
    if (L.size() == 0) return {};
    
    int primul = head(L);
    vector<int> rest = tail(L);
    
    vector<int> rest_rezolvat = Set(rest);
    
    // Daca primul element a mai aparut in restul listei, il ignoram
    if (exista(rest_rezolvat, primul)) {
        return rest_rezolvat;
    } else {
        // Altfel, il adaugam la inceputul listei finale
        return pune_in_fata(primul, rest_rezolvat);
    }
}

// 3.c OddEven(L) - inverseaza intre ele elementele vecine DACA unul este par si celalalt impar
vector<int> OddEven(vector<int> L) {
    if (L.size() <= 1) return L; // Daca lista e prea mica, se returneaza cum e
    
    int primul = head(L);
    int al_doilea = head(tail(L));
    
    // Verificam paritatea (daca unul e par si celalalt impar, vor fi diferite)
    bool primul_e_par = (primul % 2 == 0);
    bool al_doilea_e_par = (al_doilea % 2 == 0);
    
    if (primul_e_par != al_doilea_e_par) {
        // Sunt diferite! Le interschimbam si sarim peste amandoua
        vector<int> restul = tail(tail(L));
        vector<int> rest_rezolvat = OddEven(restul);
        return pune_in_fata(al_doilea, pune_in_fata(primul, rest_rezolvat));
    } else {
        // Au aceeasi paritate! Nu le interschimbam. 
        // Pastram primul element la locul lui si incercam cu incepere de la al doilea
        vector<int> rest_rezolvat = OddEven(tail(L));
        return pune_in_fata(primul, rest_rezolvat);
    }
}

// 3.d FuncList(L1, L2) - combinam 2 liste (in acest caz, le adunam element cu element)
// Daca listele nu au aceeasi lungime, elementele ramase din lista mai lunga se adauga la final.
vector<int> FuncList(vector<int> L1, vector<int> L2) {
    // Daca prima lista s-a terminat, o returnam pe a doua (care poate mai are elemente)
    if (L1.size() == 0) return L2; 
    // Daca a doua lista s-a terminat, o returnam pe prima (care poate mai are elemente)
    if (L2.size() == 0) return L1; 
    
    int element_nou = head(L1) + head(L2); 
    
    vector<int> rest_rezolvat = FuncList(tail(L1), tail(L2));
    
    return pune_in_fata(element_nou, rest_rezolvat);
}

// ====================================================================
// FUNCTIE PENTRU AFISARE
// ====================================================================
void afiseaza_recursiv(vector<int> L, int index) {
    if (index >= L.size()) return;
    cout << L[index] << " ";
    afiseaza_recursiv(L, index + 1);
}

void afiseaza(vector<int> L) {
    afiseaza_recursiv(L, 0);
    cout << endl;
}

void ruleaza_date_predefinite() {
    cout << "\n--- 1. CONSTRUCTIA LISTELOR ---\n";
    cout << "1.a Patrate pana la 4: ";
    afiseaza(patrate(4));
    
    cout << "1.b Factoriale pana la 4: ";
    afiseaza(factoriale(4));
    
    cout << "\n--- 2. FUNCTII DE CALCUL ---\n";
    cout << "2.a Inmultire 3 * 4 = " << inmultire(3, 4) << endl;
    cout << "2.b Suma numerelor pana la 4 (4+3+2+1) = " << suma(4) << endl;
    cout << "2.c Suma de sume pana la 4 = " << suma_de_sume(4) << endl;
    
    cout << "\n--- 3. FUNCTII LISTE ---\n";
    vector<int> lista_get = {10, 20, 30, 40};
    cout << "3.a GetN( {10, 20, 30, 40}, 2 ) = " << GetN(lista_get, 2) << " (indexat de la 0)" << endl;
    
    vector<int> lista_set = {1, 2, 2, 3, 1, 4};
    cout << "3.b Set (fara duplicate) pt {1, 2, 2, 3, 1, 4} : ";
    afiseaza(Set(lista_set));
    
    vector<int> lista_oddeven = {4, 2, 3, 4, 5, 6};
    cout << "3.c OddEven pt {4, 2, 3, 4, 5, 6} : ";
    afiseaza(OddEven(lista_oddeven));
    
    vector<int> L1 = {1, 2, 3};
    vector<int> L2 = {10, 20};
    cout << "3.d FuncList (adunam) {1, 2, 3} cu {10, 20} : ";
    afiseaza(FuncList(L1, L2));
}

void citeste_elemente_recursiv(vector<int>& L, int n) {
    if (n == 0) return;
    int x;
    cin >> x;
    L.push_back(x);
    citeste_elemente_recursiv(L, n - 1);
}

vector<int> citeste_lista() {
    int n;
    cout << "Cate elemente va avea lista? ";
    cin >> n;
    vector<int> L;
    if (n > 0) {
        cout << "Introdu cele " << n << " elemente separate prin spatiu: ";
        citeste_elemente_recursiv(L, n);
    }
    return L;
}

void ruleaza_date_manual() {
    int n, x;
    
    cout << "\n--- 1. CONSTRUCTIA LISTELOR ---\n";
    cout << "1.a Introdu un numar N pentru lista de patrate: ";
    cin >> n;
    cout << "Patrate pana la " << n << ": ";
    afiseaza(patrate(n));
    
    cout << "1.b Introdu un numar N pentru lista de factoriale: ";
    cin >> n;
    cout << "Factoriale pana la " << n << ": ";
    afiseaza(factoriale(n));
    
    cout << "\n--- 2. FUNCTII DE CALCUL ---\n";
    cout << "2.a Inmultire (F(x, n) = x * n). Introdu x si n: ";
    cin >> x >> n;
    cout << "Rezultat " << x << " * " << n << " = " << inmultire(x, n) << endl;
    
    cout << "2.b Suma de la 1 la n. Introdu n: ";
    cin >> n;
    cout << "Suma pana la " << n << " = " << suma(n) << endl;
    
    cout << "2.c Suma de sume (tetraedrice). Introdu n: ";
    cin >> n;
    cout << "Suma de sume pana la " << n << " = " << suma_de_sume(n) << endl;
    
    cout << "\n--- 3. FUNCTII LISTE ---\n";
    cout << "3.a GetN - Construieste lista:\n";
    vector<int> lista_get = citeste_lista();
    cout << "Introdu indexul n (de la 0): ";
    cin >> n;
    if (lista_get.size() > n) {
        cout << "Elementul este: " << GetN(lista_get, n) << endl;
    } else {
        cout << "Index prea mare pentru lista!" << endl;
    }
    
    cout << "\n3.b Set (fara duplicate) - Construieste lista:\n";
    vector<int> lista_set = citeste_lista();
    cout << "Lista fara duplicate: ";
    afiseaza(Set(lista_set));
    
    cout << "\n3.c OddEven - Construieste lista:\n";
    vector<int> lista_oddeven = citeste_lista();
    cout << "Lista inversata: ";
    afiseaza(OddEven(lista_oddeven));
    
    cout << "\n3.d FuncList (adunare elemente) - Construieste lista 1:\n";
    vector<int> L1 = citeste_lista();
    cout << "Construieste lista 2:\n";
    vector<int> L2 = citeste_lista();
    cout << "Listele adunate: ";
    afiseaza(FuncList(L1, L2));
}

int main() {
    int optiune;
    cout << "========================================\n";
    cout << "Alege o varianta de rulare:\n";
    cout << "1. Date gata puse (predefinite)\n";
    cout << "2. Date introduse manual (de la tastatura)\n";
    cout << "Optiunea ta (1 sau 2): ";
    cin >> optiune;
    cout << "========================================\n";

    if (optiune == 1) {
        ruleaza_date_predefinite();
    } else if (optiune == 2) {
        ruleaza_date_manual();
    } else {
        cout << "Optiune invalida! Iesire...\n";
    }

    return 0;
}
