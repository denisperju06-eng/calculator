#include <iostream>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>

using namespace std;

// ====================================================================
// PARADIGMA FUNCȚIONALĂ - LABORATORUL 3
// PARSER XML ȘI PRELUCRAREA ARBORELUI DE ETICHETE
// - Fără bucle (nici for, nici while)
// - Fără mutații in-place (fără x += 1, fără x++, doar y = x + 1)
// - Pur recursiv și simplu
// ====================================================================

// Structura simplă a unui nod din arborele XML
struct Node {
    string tag;             // Numele etichetei (ex: catalog, book, title)
    string attrs;           // Atributele (ex: id="b1" format="hardcover")
    string text;            // Textul din interiorul etichetei
    vector<Node> children;  // Nodurile copil (sub-etichetele)

    // Extrage valoarea unui atribut: ex attr("id") returneaza "b1"
    string attr(const string& name) const {
        auto pos = attrs.find(name + "=\"");
        if (pos == string::npos) return "";
        auto start = pos + name.size() + 2;
        auto end = attrs.find('"', start);
        return (end == string::npos) ? "" : attrs.substr(start, end - start);
    }
};

// ====================================================================
// 1. ELABORAREA PARSERULUI XML (3 puncte)
// ====================================================================
string trim(const string& s) {
    auto start = s.find_first_not_of(" \t\n\r");
    if (start == string::npos) return "";
    auto end = s.find_last_not_of(" \t\n\r");
    return s.substr(start, end - start + 1);
}

string extrage_tag(const string& inside) {
    auto pos = inside.find_first_of(" \t\n\r/");
    if (pos == string::npos) return inside;
    return inside.substr(0, pos);
}

pair<Node, size_t> parse_element(const string& s, size_t i);

// Parsare recursiva a copiilor unui nod
pair<vector<Node>, size_t> parse_children(const string& s, size_t i, const string& parent_tag) {
    size_t lt = s.find('<', i);
    if (lt == string::npos) return {{}, s.size()};

    // Daca gasim </parent_tag>, s-a incheiat nodul curent
    if (lt + 1 < s.size() && s[lt + 1] == '/') {
        size_t gt = s.find('>', lt);
        return {{}, gt + 1};
    }

    // Parsam copilul curent
    auto child = parse_element(s, lt);
    // Parsam recursiv restul copiilor
    auto rest = parse_children(s, child.second, parent_tag);

    vector<Node> all = {child.first};
    all.insert(all.end(), rest.first.begin(), rest.first.end());
    return {all, rest.second};
}

// Parsare recursiva a unui singur element
pair<Node, size_t> parse_element(const string& s, size_t i) {
    size_t lt = s.find('<', i);
    if (lt == string::npos) return {{"", "", "", {}}, s.size()};

    // Ignoram comentarii <!-- ... --> si instructiuni <? ... ?>
    if (lt + 1 < s.size() && (s[lt + 1] == '?' || s[lt + 1] == '!')) {
        size_t gt = s.find('>', lt);
        return parse_element(s, gt + 1);
    }

    size_t gt = s.find('>', lt);
    string inside = s.substr(lt + 1, gt - lt - 1);
    string tag = extrage_tag(inside);

    // Auto-inchidere: <tag ... />
    if (inside.back() == '/') {
        return {{tag, inside, "", {}}, gt + 1};
    }

    size_t next_lt = s.find('<', gt + 1);
    string text = trim(s.substr(gt + 1, next_lt - gt - 1));

    // Daca urmeaza direct inchiderea etichetei: </tag>
    if (next_lt + 1 < s.size() && s[next_lt + 1] == '/') {
        size_t close_gt = s.find('>', next_lt);
        return {{tag, inside, text, {}}, close_gt + 1};
    }

    // Altfel, parsam copiii recursiv
    auto ch = parse_children(s, gt + 1, tag);
    return {{tag, inside, text, ch.first}, ch.second};
}

Node parse_xml(const string& s) {
    return parse_element(s, 0).first;
}

// ====================================================================
// 2. GENERAREA ARBORELUI DE ETICHETE (1 punct)
// ====================================================================

void afiseaza_arbore_copii(const vector<Node>& kids, size_t i, int indent);

void afiseaza_arbore(const Node& node, int indent = 0) {
    cout << string(indent, ' ') << "<" << node.tag;
    if (!node.attr("id").empty()) cout << " id=\"" << node.attr("id") << "\"";
    cout << ">";
    if (!node.text.empty()) cout << " -> " << node.text;
    cout << "\n";
    afiseaza_arbore_copii(node.children, 0, indent + 2);
}

void afiseaza_arbore_copii(const vector<Node>& kids, size_t i, int indent) {
    if (i >= kids.size()) return;
    afiseaza_arbore(kids[i], indent);
    afiseaza_arbore_copii(kids, i + 1, indent);
}

// ====================================================================
// 3. CAUTAREA UNEI ETICHETE SPECIFICE IN ARBORE (1 punct)
// ====================================================================

vector<Node> cauta_in_copii(const vector<Node>& kids, size_t i, const string& tag);

vector<Node> cauta_eticheta(const Node& node, const string& tag) {
    vector<Node> rez = (node.tag == tag) ? vector<Node>{node} : vector<Node>{};
    auto din_copii = cauta_in_copii(node.children, 0, tag);
    rez.insert(rez.end(), din_copii.begin(), din_copii.end());
    return rez;
}

vector<Node> cauta_in_copii(const vector<Node>& kids, size_t i, const string& tag) {
    if (i >= kids.size()) return {};
    auto prim = cauta_eticheta(kids[i], tag);
    auto rest = cauta_in_copii(kids, i + 1, tag);
    prim.insert(prim.end(), rest.begin(), rest.end());
    return prim;
}

// ====================================================================
// 4. GENERAREA UNEI LISTE PE BAZA CAUTARII (1 punct)
// ====================================================================

vector<string> lista_texte(const vector<Node>& noduri, size_t i = 0) {
    if (i >= noduri.size()) return {};
    vector<string> rez = {noduri[i].text};
    auto rest = lista_texte(noduri, i + 1);
    rez.insert(rez.end(), rest.begin(), rest.end());
    return rez;
}

// ====================================================================
// 5. OBTINEREA ADANCIMII ARBORELUI DE ETICHETE (1 punct)
// ====================================================================

int adancime_arbore(const Node& node);

int max_adancime_copii(const vector<Node>& kids, size_t i) {
    if (i >= kids.size()) return 0;
    int cur = adancime_arbore(kids[i]);
    int rest = max_adancime_copii(kids, i + 1);
    return (cur > rest) ? cur : rest;
}

int adancime_arbore(const Node& node) {
    if (node.children.empty()) return 1;
    return 1 + max_adancime_copii(node.children, 0);
}

// ====================================================================
// 6. OBTINEREA LATIMII ARBORELUI DE ETICHETE (1 punct)
// ====================================================================

int numar_noduri_nivel(const Node& node, int nivel);

int max_noduri_pe_nivele(const Node& node, int cur_lvl, int max_lvl) {
    if (cur_lvl > max_lvl) return 0;
    int cur = numar_noduri_nivel(node, cur_lvl);
    int rest = max_noduri_pe_nivele(node, cur_lvl + 1, max_lvl);
    return (cur > rest) ? cur : rest;
}

int latime_arbore(const Node& node) {
    return max_noduri_pe_nivele(node, 1, adancime_arbore(node));
}

// ====================================================================
// 7. NUMARUL DE NODURI DE PE UN NIVEL PORNIND DE LA UN NOD DAT (2 puncte)
// ====================================================================

int suma_noduri_copii(const vector<Node>& kids, size_t i, int nivel) {
    if (i >= kids.size()) return 0;
    return numar_noduri_nivel(kids[i], nivel - 1) + suma_noduri_copii(kids, i + 1, nivel);
}

int numar_noduri_nivel(const Node& node, int nivel) {
    if (nivel < 1) return 0;
    if (nivel == 1) return 1;
    return suma_noduri_copii(node.children, 0, nivel);
}

// ====================================================================
// AFISARI RECURSIVE PENTRU PREZENTARE
// ====================================================================

void afiseaza_lista_string(const vector<string>& v, size_t i = 0) {
    if (i >= v.size()) {
        cout << "\n";
        return;
    }
    cout << "  • \"" << v[i] << "\"\n";
    afiseaza_lista_string(v, i + 1);
}

void afiseaza_nivele(const Node& node, int cur_lvl, int max_lvl) {
    if (cur_lvl > max_lvl) return;
    cout << "  Nivelul " << cur_lvl << ": " << numar_noduri_nivel(node, cur_lvl) << " noduri\n";
    afiseaza_nivele(node, cur_lvl + 1, max_lvl);
}

// ====================================================================
// PROGRAM PRINCIPAL
// ====================================================================

int main(int argc, char* argv[]) {
    string path = (argc > 1) ? argv[1] : "catalog.xml";
    ifstream f(path);
    if (!f.is_open()) {
        // Incercam si calea relativa din radacina proiectului
        f.open("Functionala/lab3/catalog.xml");
        if (!f.is_open()) {
            cout << "Eroare: Nu s-a putut deschide fisierul XML!\n";
            return 1;
        }
    }
    stringstream ss;
    ss << f.rdbuf();

    cout << "========================================================\n";
    cout << "     LABORATORUL 3: PARADIGMA FUNCTIONALA - ARBORE XML  \n";
    cout << "========================================================\n\n";

    // Punctul 1 & 2: Parsare si Generare Arbore
    Node root = parse_xml(ss.str());
    cout << "--- 1 & 2. ARBORELE DE ETICHETE GENERAT ---\n";
    afiseaza_arbore(root);

    // Punctul 3: Cautare eticheta
    cout << "\n--- 3. CAUTARE ETICHETA 'author' ---\n";
    vector<Node> autori = cauta_eticheta(root, "author");
    cout << "S-au gasit: " << autori.size() << " etichete <author>\n";

    // Punctul 4: Generare lista pe baza cautarii
    cout << "\n--- 4. LISTA TITLURILOR DE CARTI (<title>) ---\n";
    vector<Node> titluri_noduri = cauta_eticheta(root, "title");
    vector<string> titluri = lista_texte(titluri_noduri);
    afiseaza_lista_string(titluri);

    // Punctul 5: Adancimea arborelui
    cout << "--- 5. ADANCIMEA ARBORELUI ---\n";
    cout << "Adancime maxima: " << adancime_arbore(root) << "\n\n";

    // Punctul 6: Latimea arborelui
    cout << "--- 6. LATIMEA ARBORELUI ---\n";
    cout << "Latime (max noduri pe un nivel): " << latime_arbore(root) << "\n\n";

    // Punctul 7: Numar noduri pe fiecare nivel
    cout << "--- 7. NUMAR NODURI PE FIECARE NIVEL ---\n";
    afiseaza_nivele(root, 1, adancime_arbore(root));

    return 0;
}
