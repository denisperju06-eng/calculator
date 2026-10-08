/**
 * Lucrarea de Laborator Nr. 1 — Paradigma Funcțională (C++17)
 * ============================================================
 * 
 * Acest program transpune cerințele din Python în C++, demonstrând
 * conceptele fundamentale ale Paradigmei Funcționale în C++:
 *   - Imutabilitate (valori 'const', fără efecte laterale / fără mutații de stare)
 *   - Funcții pure și transparență referențială
 *   - Funcții de ordin superior (Higher-Order Functions: map/transform, fold/accumulate)
 *   - Recursivitate structurală (descompunere head/tail)
 *   - Combinatori funcționali: compose, pipe, currying
 *   - Expresii lambda și programare generică (templates)
 * 
 * Structura cerințelor conform conditii.md:
 *   1. Construcția listelor finite (2 puncte)
 *      a. Lista pătratelor numerelor naturale până la N
 *      b. Lista factorialelor până la N
 *   2. Funcții de calcul al șirurilor (3 puncte)
 *      a. F(x, n) = x * n
 *      b. F(n) = sum_{i=1}^n i
 *      c. F(n) = sum_{j=1}^n (sum_{i=1}^j i)
 *   3. Funcții pentru lucrul cu șiruri / liste (5 puncte)
 *      a. GetN(L, n)          — extragerea celui de-al n-lea element dintr-o listă dată
 *      b. Set(L)              — lista cu o singură apariție pentru fiecare atom (deduplicare stabilă)
 *      c. OddEven(L)          — inversarea elementelor pare și impare vecine (index / valoare)
 *      d. FuncList(L1, L2, F) — combinarea atomilor corespunzători conform funcției F
 */

#include <iostream>
#include <vector>
#include <string>
#include <functional>
#include <numeric>
#include <algorithm>
#include <stdexcept>
#include <sstream>
#include <cassert>
#include <cmath>

// ==============================================================================
// 0. UTILITĂȚI ȘI COMBINATORI FUNCȚIONALI (Functional Helpers & Combinators)
// ==============================================================================

/**
 * Extrage primul element dintr-un vector (analogul 'head' / 'car' din Lisp).
 */
template <typename T>
T head(const std::vector<T>& seq) {
    if (seq.empty()) {
        throw std::out_of_range("head() aplicat pe o secvență vidă.");
    }
    return seq.front();
}

/**
 * Extrage coada vectorului, fără primul element (analogul 'tail' / 'cdr' din Lisp).
 */
template <typename T>
std::vector<T> tail(const std::vector<T>& seq) {
    if (seq.empty()) {
        throw std::out_of_range("tail() aplicat pe o secvență vidă.");
    }
    return std::vector<T>(seq.begin() + 1, seq.end());
}

/**
 * Compunerea matematică a două funcții: (f ∘ g)(x) = f(g(x)).
 */
template <typename F, typename G>
auto compose(F f, G g) {
    return [=](auto x) {
        return f(g(x));
    };
}

/**
 * Operatorul Pipeline funcțional: pipe(val, f, g) == g(f(val)).
 */
template <typename T, typename F>
auto pipe(T&& val, F&& f) {
    return f(std::forward<T>(val));
}

template <typename T, typename F, typename... Rest>
auto pipe(T&& val, F&& f, Rest&&... rest) {
    return pipe(f(std::forward<T>(val)), std::forward<Rest>(rest)...);
}

/**
 * Currying binar: transformă o funcție f(a, b) în f(a)(b).
 */
template <typename F>
auto curry(F f) {
    return [=](auto a) {
        return [=](auto b) {
            return f(a, b);
        };
    };
}

/**
 * Afișare elegantă pentru std::vector (stil tuplu funcțional: (1, 2, 3)).
 */
template <typename T>
std::ostream& operator<<(std::ostream& os, const std::vector<T>& vec) {
    os << "(";
    for (size_t i = 0; i < vec.size(); ++i) {
        os << vec[i];
        if (i + 1 < vec.size()) {
            os << ", ";
        }
    }
    os << ")";
    return os;
}

// Specializare pentru string-uri pentru a afișa ghilimele: ('A', 'B')
inline std::string format_string_vec(const std::vector<std::string>& vec) {
    std::ostringstream oss;
    oss << "(";
    for (size_t i = 0; i < vec.size(); ++i) {
        oss << "'" << vec[i] << "'";
        if (i + 1 < vec.size()) oss << ", ";
    }
    oss << ")";
    return oss.str();
}

// ==============================================================================
// 1. CONSTRUCȚIA LISTELOR FINITE (2 puncte)
// ==============================================================================

// --- 1.a: Lista pătratelor numerelor naturale până la N ---

/**
 * Generează lista pătratelor numerelor naturale până la N.
 * Implementare funcțională prin generare declarativă / transform.
 */
inline std::vector<long long> squares_up_to(int n, bool include_zero = false) {
    if (n < 0) return {};
    int start = include_zero ? 0 : 1;
    if (start > n) return {};

    std::vector<long long> result;
    result.reserve(n - start + 1);
    for (int i = start; i <= n; ++i) {
        result.push_back(1LL * i * i);
    }
    return result;
}

/**
 * Variantă recursivă pură pentru construirea listei pătratelor.
 */
inline std::vector<long long> squares_recursive(int n, bool include_zero = false) {
    if (n < 0) return {};
    int start = include_zero ? 0 : 1;

    auto rec = [](auto& self, int current, int max_n, std::vector<long long> acc) -> std::vector<long long> {
        if (current > max_n) return acc;
        acc.push_back(1LL * current * current);
        return self(self, current + 1, max_n, acc);
    };

    return rec(rec, start, n, {});
}

// --- 1.b: Lista factorialelor până la N ---

/**
 * Calculează factorialul k! în mod funcțional pur prin fold / std::accumulate.
 */
inline long long factorial(int k) {
    if (k < 0) {
        throw std::invalid_argument("Factorialul nu este definit pentru numere negative.");
    }
    if (k == 0 || k == 1) return 1;

    std::vector<long long> nums(k);
    std::iota(nums.begin(), nums.end(), 1LL);
    return std::accumulate(nums.begin(), nums.end(), 1LL, std::multiplies<long long>());
}

/**
 * Generează lista factorialelor până la N: [0!, 1!, ..., N!] sau [1!, ..., N!].
 */
inline std::vector<long long> factorials_up_to(int n, bool include_zero = true) {
    if (n < 0) return {};
    int start = include_zero ? 0 : 1;
    if (start > n) return {};

    std::vector<long long> result;
    result.reserve(n - start + 1);
    for (int i = start; i <= n; ++i) {
        result.push_back(factorial(i));
    }
    return result;
}

/**
 * Variantă optimizată funcțional prin scan / prefix scan cumulativ O(N).
 */
inline std::vector<long long> factorials_scan(int n, bool include_zero = true) {
    if (n < 0) return {};
    if (n == 0) return include_zero ? std::vector<long long>{1} : std::vector<long long>{};

    std::vector<long long> result;
    result.reserve(n + 1);
    long long curr = 1;
    if (include_zero) {
        result.push_back(curr);
    }
    for (int i = 1; i <= n; ++i) {
        curr *= i;
        result.push_back(curr);
    }
    return result;
}

// ==============================================================================
// 2. FUNCȚII DE CALCUL AL ȘIRURILOR (3 puncte)
// ==============================================================================

// --- 2.a: F(x, n) = x * n ---

/**
 * Calculează F(x, n) = x * n conform principiului acumulării funcționale Peano.
 */
inline double series_product(double x, int n) {
    if (n == 0 || x == 0.0) return 0.0;
    int abs_n = std::abs(n);

    // Acumulare pură prin fold
    double acc = 0.0;
    for (int i = 0; i < abs_n; ++i) {
        acc += x;
    }
    return (n > 0) ? acc : -acc;
}

/**
 * Variantă pur recursivă pentru F(x, n) = x * n.
 */
inline double series_product_rec(double x, int n) {
    if (n == 0) return 0.0;
    if (n > 0) return x + series_product_rec(x, n - 1);
    return -series_product_rec(x, -n);
}

// Versiune curried a funcției de multiplicare
inline auto multiply_curried = curry([](double x, int n) {
    return series_product(x, n);
});

// --- 2.b: F(n) = sum_{i=1}^n i ---

/**
 * Calculează F(n) = sum_{i=1}^n i (suma primelor n numere naturale).
 */
inline long long series_sum_linear(int n) {
    if (n <= 0) return 0;
    std::vector<long long> nums(n);
    std::iota(nums.begin(), nums.end(), 1LL);
    return std::accumulate(nums.begin(), nums.end(), 0LL);
}

/**
 * Variantă recursivă structurală pentru F(n) = sum_{i=1}^n i.
 */
inline long long series_sum_linear_rec(int n) {
    if (n <= 0) return 0;
    return n + series_sum_linear_rec(n - 1);
}

// --- 2.c: F(n) = sum_{j=1}^n ( sum_{i=1}^j i ) ---

/**
 * Calculează F(n) = sum_{j=1}^n F_2(j) (numere tetraedrice).
 */
inline long long series_sum_triangular(int n) {
    if (n <= 0) return 0;
    long long total = 0;
    for (int j = 1; j <= n; ++j) {
        total += series_sum_linear(j);
    }
    return total;
}

/**
 * Variantă pur recursivă pentru F(n) = sum_{j=1}^n ( sum_{i=1}^j i ).
 */
inline long long series_sum_triangular_rec(int n) {
    if (n <= 0) return 0;
    return series_sum_triangular_rec(n - 1) + series_sum_linear(n);
}

// Aliase simbolice
inline auto F1 = series_product;
inline auto F2 = series_sum_linear;
inline auto F3 = series_sum_triangular;

// ==============================================================================
// 3. FUNCȚII PENTRU LUCRUL CU ȘIRURI / LISTE (5 puncte)
// ==============================================================================

// --- 3.a: GetN(L, n) ---
// Extragerea celui de-al n-lea element dintr-o listă dată prin recursivitate pură.

template <typename T>
T GetN(const std::vector<T>& L, int n, bool one_indexed = false) {
    int target_idx = one_indexed ? n - 1 : n;

    if (target_idx < 0) {
        throw std::out_of_range("GetN: Indexul " + std::to_string(n) + " este invalid (nu poate fi negativ).");
    }

    auto rec = [](auto& self, const std::vector<T>& seq, int remaining) -> T {
        if (seq.empty()) {
            throw std::out_of_range("GetN: Indexul depășește lungimea secvenței.");
        }
        if (remaining == 0) {
            return head(seq);
        }
        return self(self, tail(seq), remaining - 1);
    };

    return rec(rec, L, target_idx);
}

// --- 3.b: Set(L) ---
// Întoarce lista ce conține o singură apariție pentru fiecare atom (deduplicare stabilă).

template <typename T>
std::vector<T> Set(const std::vector<T>& L) {
    std::vector<T> acc;
    for (const auto& item : L) {
        if (std::find(acc.begin(), acc.end(), item) == acc.end()) {
            acc.push_back(item);
        }
    }
    return acc;
}

/**
 * Variantă pur recursivă pentru Set(L) (analogul 'nub' din Haskell):
 * nub [] = []
 * nub (x:xs) = x : nub (filter (/= x) xs)
 */
template <typename T>
std::vector<T> set_recursive(const std::vector<T>& L) {
    if (L.empty()) return {};

    T h = head(L);
    std::vector<T> t = tail(L);

    // Filtrare pură a aparițiilor ulterioare ale capului
    std::vector<T> filtered_tail;
    for (const auto& x : t) {
        if (x != h) {
            filtered_tail.push_back(x);
        }
    }

    std::vector<T> result = {h};
    auto rest = set_recursive(filtered_tail);
    result.insert(result.end(), rest.begin(), rest.end());
    return result;
}

// --- 3.c: OddEven(L) ---
// Inversează între ele elementele pare și impare vecine din lista dată.

template <typename T>
std::vector<T> _odd_even_by_index(const std::vector<T>& seq) {
    if (seq.empty()) return {};
    if (seq.size() == 1) return {seq[0]};

    // Inversare pereche adiacentă: first, second -> second, first
    std::vector<T> res = {seq[1], seq[0]};
    std::vector<T> rest(seq.begin() + 2, seq.end());
    auto rest_swapped = _odd_even_by_index(rest);
    res.insert(res.end(), rest_swapped.begin(), rest_swapped.end());
    return res;
}

inline std::vector<int> _odd_even_by_value(const std::vector<int>& seq) {
    if (seq.empty()) return {};
    if (seq.size() == 1) return {seq[0]};

    int first = seq[0];
    int second = seq[1];

    // Dacă un element este par și celălalt impar -> le inversăm
    if ((first % 2 == 0 && second % 2 != 0) || (first % 2 != 0 && second % 2 == 0)) {
        std::vector<int> res = {second, first};
        std::vector<int> rest(seq.begin() + 2, seq.end());
        auto rest_res = _odd_even_by_value(rest);
        res.insert(res.end(), rest_res.begin(), rest_res.end());
        return res;
    } else {
        std::vector<int> res = {first};
        std::vector<int> rest(seq.begin() + 1, seq.end());
        auto rest_res = _odd_even_by_value(rest);
        res.insert(res.end(), rest_res.begin(), rest_res.end());
        return res;
    }
}

template <typename T>
std::vector<T> OddEven(const std::vector<T>& L, const std::string& mode = "index") {
    if (mode == "index") {
        return _odd_even_by_index(L);
    } else {
        throw std::invalid_argument("OddEven generic suporta doar mode='index'. Pentru 'value' folositi vector de numere intregi.");
    }
}

// Suprascriere specializată pentru int, acceptând și mode="value"
inline std::vector<int> OddEven(const std::vector<int>& L, const std::string& mode = "index") {
    if (mode == "index") {
        return _odd_even_by_index(L);
    } else if (mode == "value") {
        return _odd_even_by_value(L);
    } else {
        throw std::invalid_argument("Mod necunoscut: " + mode + ". Opțiuni: 'index', 'value'.");
    }
}

// --- 3.d: FuncList(L1, L2, F) ---
// Combină atomii corespunzători din două liste conform funcției F (zipWith).

template <typename T1, typename T2, typename Func>
auto FuncList(const std::vector<T1>& L1, const std::vector<T2>& L2, Func F) {
    using R = decltype(F(L1[0], L2[0]));
    size_t len = std::min(L1.size(), L2.size());
    std::vector<R> result;
    result.reserve(len);
    for (size_t i = 0; i < len; ++i) {
        result.push_back(F(L1[i], L2[i]));
    }
    return result;
}

// Versiune implicită cu adunare (+)
template <typename T>
std::vector<T> FuncList(const std::vector<T>& L1, const std::vector<T>& L2) {
    return FuncList(L1, L2, [](const T& a, const T& b) { return a + b; });
}

/**
 * Variantă pur recursivă pentru FuncList.
 */
template <typename T1, typename T2, typename Func>
auto func_list_recursive(const std::vector<T1>& L1, const std::vector<T2>& L2, Func F) {
    using R = decltype(F(L1[0], L2[0]));
    if (L1.empty() || L2.empty()) return std::vector<R>{};

    std::vector<R> res = {F(head(L1), head(L2))};
    auto rest = func_list_recursive(tail(L1), tail(L2), F);
    res.insert(res.end(), rest.begin(), rest.end());
    return res;
}

/**
 * Însumarea scalară totală: sum(F(L1[i], L2[i])).
 */
template <typename T1, typename T2, typename Func>
auto func_list_sum(const std::vector<T1>& L1, const std::vector<T2>& L2, Func F) {
    auto list = FuncList(L1, L2, F);
    using R = typename decltype(list)::value_type;
    return std::accumulate(list.begin(), list.end(), R{0});
}

// ==============================================================================
// 4. SUITĂ CUPRINZĂTOARE DE TESTE UNITARE
// ==============================================================================

void run_unit_tests() {
    std::cout << "\n[RULARE AUTOMATĂ TESTE UNITARE]\n";

    // Test 1: squares_up_to
    {
        assert(squares_up_to(5) == (std::vector<long long>{1, 4, 9, 16, 25}));
        assert(squares_up_to(5, true) == (std::vector<long long>{0, 1, 4, 9, 16, 25}));
        assert(squares_up_to(0).empty());
        assert(squares_up_to(0, true) == (std::vector<long long>{0}));
        assert(squares_up_to(-3).empty());
        assert(squares_recursive(4) == (std::vector<long long>{1, 4, 9, 16}));
        std::cout << "test_01_squares_up_to ... OK\n";
    }

    // Test 2: factorials_up_to
    {
        assert(factorials_up_to(0) == (std::vector<long long>{1}));
        assert(factorials_up_to(5) == (std::vector<long long>{1, 1, 2, 6, 24, 120}));
        assert(factorials_up_to(5, false) == (std::vector<long long>{1, 2, 6, 24, 120}));
        assert(factorials_up_to(-1).empty());
        assert(factorials_scan(5) == (std::vector<long long>{1, 1, 2, 6, 24, 120}));
        std::cout << "test_02_factorials_up_to ... OK\n";
    }

    // Test 3: series_product
    {
        assert(series_product(5, 4) == 20.0);
        assert(series_product(7, 0) == 0.0);
        assert(series_product(0, 10) == 0.0);
        assert(series_product(4, -3) == -12.0);
        assert(series_product(2.5, 4) == 10.0);
        assert(series_product_rec(6, 3) == 18.0);
        assert(series_product_rec(6, -2) == -12.0);
        assert(multiply_curried(7)(8) == 56.0);
        std::cout << "test_03_series_product ... OK\n";
    }

    // Test 4: series_sum_linear
    {
        assert(series_sum_linear(0) == 0);
        assert(series_sum_linear(-5) == 0);
        assert(series_sum_linear(1) == 1);
        assert(series_sum_linear(5) == 15);
        assert(series_sum_linear(100) == 5050);
        assert(series_sum_linear_rec(10) == 55);
        std::cout << "test_04_series_sum_linear ... OK\n";
    }

    // Test 5: series_sum_triangular
    {
        assert(series_sum_triangular(0) == 0);
        assert(series_sum_triangular(1) == 1);
        assert(series_sum_triangular(2) == 4);
        assert(series_sum_triangular(3) == 10);
        assert(series_sum_triangular(4) == 20);
        for (int n = 1; n < 15; ++n) {
            long long expected = 1LL * n * (n + 1) * (n + 2) / 6;
            assert(series_sum_triangular(n) == expected);
            assert(series_sum_triangular_rec(n) == expected);
        }
        std::cout << "test_05_series_sum_triangular ... OK\n";
    }

    // Test 6: GetN
    {
        std::vector<std::string> sample = {"alpha", "beta", "gamma", "delta", "epsilon"};
        assert(GetN(sample, 0) == "alpha");
        assert(GetN(sample, 2) == "gamma");
        assert(GetN(sample, 4) == "epsilon");
        assert(GetN(sample, 1, true) == "alpha");
        assert(GetN(sample, 3, true) == "gamma");
        assert(GetN(sample, 5, true) == "epsilon");

        bool caught = false;
        try { GetN(sample, 5); } catch (const std::out_of_range&) { caught = true; }
        assert(caught);

        caught = false;
        try { GetN(sample, -1); } catch (const std::out_of_range&) { caught = true; }
        assert(caught);

        caught = false;
        try { std::vector<int> empty_v; GetN(empty_v, 0); } catch (const std::out_of_range&) { caught = true; }
        assert(caught);

        std::cout << "test_06_get_n ... OK\n";
    }

    // Test 7: Set
    {
        std::vector<int> v1 = {1, 2, 2, 3, 1, 4, 3};
        assert(Set(v1) == (std::vector<int>{1, 2, 3, 4}));

        std::vector<std::string> v2 = {"a", "b", "a", "c", "b"};
        assert(Set(v2) == (std::vector<std::string>{"a", "b", "c"}));

        std::vector<int> empty_v;
        assert(Set(empty_v).empty());

        std::vector<int> single = {42};
        assert(Set(single) == (std::vector<int>{42}));

        std::vector<int> v3 = {5, 4, 3, 2, 1, 5, 2};
        assert(Set(v3) == (std::vector<int>{5, 4, 3, 2, 1}));

        std::vector<int> v4 = {1, 2, 2, 3, 1};
        assert(set_recursive(v4) == (std::vector<int>{1, 2, 3}));

        std::cout << "test_07_set ... OK\n";
    }

    // Test 8: OddEven
    {
        std::vector<int> v1 = {1, 2, 3, 4, 5, 6};
        assert(OddEven(v1) == (std::vector<int>{2, 1, 4, 3, 6, 5}));

        std::vector<int> v2 = {1, 2, 3, 4, 5};
        assert(OddEven(v2) == (std::vector<int>{2, 1, 4, 3, 5}));

        std::vector<std::string> vs = {"a", "b", "c", "d"};
        assert(OddEven(vs) == (std::vector<std::string>{"b", "a", "d", "c"}));

        std::vector<std::string> v_only = {"only"};
        assert(OddEven(v_only) == (std::vector<std::string>{"only"}));

        std::vector<int> empty_v;
        assert(OddEven(empty_v).empty());

        // Swap by value
        std::vector<int> val1 = {1, 2, 3, 4};
        assert(OddEven(val1, "value") == (std::vector<int>{2, 1, 4, 3}));

        std::vector<int> val2 = {2, 1, 4, 3};
        assert(OddEven(val2, "value") == (std::vector<int>{1, 2, 3, 4}));

        std::vector<int> val3 = {2, 4, 1, 3};
        assert(OddEven(val3, "value") == (std::vector<int>{2, 1, 4, 3}));

        std::cout << "test_08_odd_even ... OK\n";
    }

    // Test 9: FuncList
    {
        std::vector<int> l1 = {1, 2, 3, 4};
        std::vector<int> l2 = {10, 20, 30, 40};

        assert(FuncList(l1, l2, [](int a, int b) { return a + b; }) == (std::vector<int>{11, 22, 33, 44}));
        assert(FuncList(l1, l2) == (std::vector<int>{11, 22, 33, 44}));
        assert(FuncList(l1, l2, [](int a, int b) { return a * b; }) == (std::vector<int>{10, 40, 90, 160}));

        std::vector<int> short_l1 = {1, 2};
        std::vector<int> long_l2 = {10, 20, 30};
        assert(FuncList(short_l1, long_l2) == (std::vector<int>{11, 22}));

        std::vector<int> r1 = {1, 2, 3};
        std::vector<int> r2 = {10, 20, 30};
        assert(func_list_recursive(r1, r2, [](int a, int b) { return a * b; }) == (std::vector<int>{10, 40, 90}));

        assert(func_list_sum(l1, l2, [](int a, int b) { return a * b; }) == (10 + 40 + 90 + 160));

        std::cout << "test_09_func_list ... OK\n";
    }

    // Test 10: Combinatori (compose, pipe)
    {
        auto f = compose([](int x) { return x + 1; }, [](int x) { return x * 2; });
        assert(f(5) == 11);

        auto val = pipe(5, [](int x) { return x * 2; }, [](int x) { return x + 1; });
        assert(val == 11);

        std::cout << "test_10_combinators ... OK\n";
    }

    std::cout << "\nToate cele 10 teste au trecut cu succes!\n";
}

// ==============================================================================
// 5. DEMONSTRAȚIE CLI
// ==============================================================================

void run_demonstration() {
    std::string separator(72, '=');
    std::string sub_sep(72, '-');

    std::cout << separator << "\n";
    std::cout << "   LUCRAREA DE LABORATOR NR. 1 — PARADIGMA FUNCTIONALA IN C++\n";
    std::cout << separator << "\n";

    // --------------------------------------------------------------------------
    std::cout << "\n[1] CONSTRUCTIA LISTELOR FINITE (2 puncte)\n";
    std::cout << sub_sep << "\n";

    int n_squares = 8;
    std::cout << "a. Lista patratelor pana la N=" << n_squares << ":\n";
    std::cout << "   * squares_up_to(" << n_squares << ")                     = " << squares_up_to(n_squares) << "\n";
    std::cout << "   * squares_up_to(" << n_squares << ", include_zero=true) = " << squares_up_to(n_squares, true) << "\n";
    std::cout << "   * squares_recursive(" << n_squares << ")                = " << squares_recursive(n_squares) << "\n";

    int n_fact = 6;
    std::cout << "\nb. Lista factorialelor pana la N=" << n_fact << ":\n";
    std::cout << "   * factorials_up_to(" << n_fact << ")                    = " << factorials_up_to(n_fact) << "\n";
    std::cout << "   * factorials_up_to(" << n_fact << ", include_zero=false)= " << factorials_up_to(n_fact, false) << "\n";
    std::cout << "   * factorials_scan(" << n_fact << ") (O(N) fold)        = " << factorials_scan(n_fact) << "\n";

    // --------------------------------------------------------------------------
    std::cout << "\n[2] FUNCTII DE CALCUL AL SIRURILOR (3 puncte)\n";
    std::cout << sub_sep << "\n";

    double x_val = 7.0;
    int n_prod = 5;
    std::cout << "a. F(x, n) = x * n:\n";
    std::cout << "   * F1(" << x_val << ", " << n_prod << ")                     = " << F1(x_val, n_prod) << "\n";
    std::cout << "   * series_product_rec(" << x_val << ", " << n_prod << ")     = " << series_product_rec(x_val, n_prod) << "\n";
    std::cout << "   * multiply_curried(" << x_val << ")(" << n_prod << ")       = " << multiply_curried(x_val)(n_prod) << "\n";

    int n_linear = 10;
    std::cout << "\nb. F(n) = sum_{i=1}^n i (suma numerelor naturale pana la n=" << n_linear << "):\n";
    std::cout << "   * F2(" << n_linear << ")                     = " << F2(n_linear) << "\n";
    std::cout << "   * series_sum_linear_rec(" << n_linear << ")  = " << series_sum_linear_rec(n_linear) << "\n";
    std::cout << "   * Formula n*(n+1)//2                 = " << (1LL * n_linear * (n_linear + 1) / 2) << "\n";

    int n_triang = 5;
    std::cout << "\nc. F(n) = sum_{j=1}^n ( sum_{i=1}^j i ) (numere tetraedrice, n=" << n_triang << "):\n";
    std::cout << "   * F3(" << n_triang << ")                         = " << F3(n_triang) << "\n";
    std::cout << "   * series_sum_triangular_rec(" << n_triang << ")  = " << series_sum_triangular_rec(n_triang) << "\n";
    std::cout << "   * Formula n*(n+1)*(n+2)//6               = " << (1LL * n_triang * (n_triang + 1) * (n_triang + 2) / 6) << "\n";

    // --------------------------------------------------------------------------
    std::cout << "\n[3] FUNCTII PENTRU LUCRUL CU SIRURI / LISTE (5 puncte)\n";
    std::cout << sub_sep << "\n";

    std::vector<std::string> test_list = {"A", "B", "C", "D", "E", "F"};
    std::cout << "a. GetN(L, n) - extragerea celui de-al n-lea element:\n";
    std::cout << "   * Lista L = " << format_string_vec(test_list) << "\n";
    std::cout << "   * GetN(L, 0)                     = '" << GetN(test_list, 0) << "' (0-indexed)\n";
    std::cout << "   * GetN(L, 2)                     = '" << GetN(test_list, 2) << "' (0-indexed, al 3-lea)\n";
    std::cout << "   * GetN(L, 1, one_indexed=true)   = '" << GetN(test_list, 1, true) << "' (1-indexed)\n";
    std::cout << "   * GetN(L, 4, one_indexed=true)   = '" << GetN(test_list, 4, true) << "' (1-indexed)\n";

    std::vector<int> dup_list = {1, 2, 3, 2, 4, 1, 5, 3, 6, 2};
    std::cout << "\nb. Set(L) - lista cu o singura aparitie pentru fiecare atom:\n";
    std::cout << "   * Lista initiala: " << dup_list << "\n";
    std::cout << "   * Set(L)                    = " << Set(dup_list) << "\n";
    std::cout << "   * set_recursive(L)          = " << set_recursive(dup_list) << "\n";

    std::vector<int> oe_list = {10, 20, 30, 40, 50, 60, 70};
    std::vector<int> oe_values = {1, 2, 4, 3, 5, 6, 8, 7};
    std::cout << "\nc. OddEven(L) - inversarea elementelor vecine:\n";
    std::cout << "   * Lista pozitii: " << oe_list << "\n";
    std::cout << "   * OddEven(L, mode='index')  = " << OddEven(oe_list, "index") << "\n";
    std::cout << "   * Lista paritate: " << oe_values << "\n";
    std::cout << "   * OddEven(L, mode='value')  = " << OddEven(oe_values, "value") << "\n";

    std::vector<int> l1 = {1, 2, 3, 4, 5};
    std::vector<int> l2 = {10, 20, 30, 40, 50};
    std::cout << "\nd. FuncList(L1, L2, F) - combinarea atomilor dupa pozitie:\n";
    std::cout << "   * L1 = " << l1 << "\n";
    std::cout << "   * L2 = " << l2 << "\n";
    std::cout << "   * FuncList(L1, L2, F=x+y)   = " << FuncList(l1, l2, [](int x, int y) { return x + y; }) << "\n";
    std::cout << "   * FuncList(L1, L2, F=x*y)   = " << FuncList(l1, l2, [](int x, int y) { return x * y; }) << "\n";
    std::cout << "   * func_list_recursive(x-y)  = " << func_list_recursive(l1, l2, [](int x, int y) { return x - y; }) << "\n";
    std::cout << "   * func_list_sum(x*y)        = " << func_list_sum(l1, l2, [](int x, int y) { return x * y; }) << "\n";

    std::cout << separator << "\n";
}

int main(int argc, char* argv[]) {
    // Dacă utilizatorul rulează cu argumentul --test sau -t, se execută doar testele
    if (argc > 1 && (std::string(argv[1]) == "--test" || std::string(argv[1]) == "-t")) {
        run_unit_tests();
        return 0;
    }

    // Execută demonstrația completă
    run_demonstration();

    // Rulare automată a testelor de verificare
    run_unit_tests();

    return 0;
}
