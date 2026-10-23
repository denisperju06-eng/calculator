#include <iostream>
#include <string>
#include <vector>

using namespace std;

vector<int> aplatizeaza(const string& s, int& index) {
    vector<int> rezultat;

    while (index < (int)s.size()) {
        char c = s[index];

        // 1. Daca gaseste '[' sau '{' -> incepe un nou array
        if (c == '[' || c == '{') {
            index++;
            vector<int> sub = aplatizeaza(s, index);
            rezultat.insert(rezultat.end(), sub.begin(), sub.end());
        }
        // 2. Daca gaseste ']' sau '}' -> se termina array-ul curent
        else if (c == ']' || c == '}') {
            index++;
            return rezultat;
        }
        // 3. Daca este cifra -> extragem numarul
        else if (c >= '0' && c <= '9') {
            int numar = 0;
            while (index < (int)s.size() && s[index] >= '0' && s[index] <= '9') {
                numar = numar * 10 + (s[index] - '0');
                index++;
            }
            rezultat.push_back(numar);
        }
        // 4. Ignoram virgule, spatii etc.
        else {
            index++;
        }
    }

    return rezultat;
}

int main() {
    string s;
    getline(cin, s);

    int index = 0;
    vector<int> rezultat = aplatizeaza(s, index);

    cout << "[";
    for (int i = 0; i < (int)rezultat.size(); i++) {
        cout << rezultat[i];
        if (i + 1 < (int)rezultat.size()) {
            cout << ", ";
        }
    }
    cout << "]\n";

    return 0;
}
