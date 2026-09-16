"""
Lucrarea de Laborator Nr. 1 — Paradigma Funcțională
=====================================================

Acest modul implementează integral cerințele lucrării de laborator utilizând
în mod strict conceptele fundamentale ale Paradigmei Funcționale:
  - Imutabilitate strictă (utilizarea tuplurilor imutabile, eliminarea mutațiilor de stare)
  - Funcții pure și transparență referențială (fără efecte laterale / side-effects)
  - Funcții de ordin superior (Higher-Order Functions: map, filter, reduce, starmap)
  - Recursivitate structurală (head/tail decomposition) și mecanisme de trampolining
  - Evaluare leneșă (Lazy evaluation) prin generatoare și iteratori (Streams)
  - Combinatori funcționali: compunere (compose), currying (curry), pipeline (pipe)
  - Sintaxă declarativă și expresii lambda

Structura cerințelor:
  1. Construcția listelor finite (2 puncte)
     a. Lista pătratelor numerelor naturale până la N
     b. Lista factorialelor până la N
  2. Funcții de calcul al șirurilor (3 puncte)
     a. F(x, n) = x * n
     b. F(n) = sum_{i=1}^n i
     c. F(n) = sum_{j=1}^n (sum_{i=1}^j i)
  3. Funcții pentru lucrul cu șiruri / liste (5 puncte)
     a. GetN(L, n)      — extragerea celui de-al n-lea element dintr-o listă dată
     b. Set(L)          — eliminarea duplicatelor (păstrarea unei singure apariții a fiecărui atom)
     c. OddEven(L)      — inversarea elementelor pare și impare vecine
     d. FuncList(L1, L2, F) — combinarea atomilor corespunzători din două liste conform lui F
"""

from __future__ import annotations

import functools
import itertools
import operator
import sys
import unittest
from typing import (
    Any,
    Callable,
    Generator,
    Iterable,
    Iterator,
    Optional,
    Sequence,
    Tuple,
    TypeVar,
)

T = TypeVar("T")
T1 = TypeVar("T1")
T2 = TypeVar("T2")
R = TypeVar("R")


# ==============================================================================
# 0. COMBINATORI ȘI UTILITĂȚI FUNCȚIONALE (Functional Primitives & Combinators)
# ==============================================================================

def compose(*funcs: Callable[[Any], Any]) -> Callable[[Any], Any]:
    """
    Compunerea matematică a funcțiilor: (f ∘ g ∘ h)(x) = f(g(h(x))).
    
    Exemplu:
        >>> add1 = lambda x: x + 1
        >>> sqr = lambda x: x * x
        >>> compose(add1, sqr)(3)  # sqr(3) = 9 -> add1(9) = 10
        10
    """
    return functools.reduce(
        lambda acc_f, g: lambda *args, **kwargs: acc_f(g(*args, **kwargs)),
        funcs,
        lambda x: x,
    )


def pipe(initial_val: Any, *funcs: Callable[[Any], Any]) -> Any:
    """
    Operatorul pipeline (analog cu '|>' din Elixir/F#):
    pipe(x, f, g) == g(f(x)).
    
    Exemplu:
        >>> pipe(3, lambda x: x * 2, lambda x: x + 1)
        7
    """
    return functools.reduce(lambda acc, f: f(acc), funcs, initial_val)


def curry(func: Callable) -> Callable:
    """
    Currying automat pentru funcții de ordin superior.
    Transformă o funcție f(a, b, c) într-o funcție f(a)(b)(c).
    
    Exemplu:
        >>> add = curry(lambda a, b: a + b)
        >>> add5 = add(5)
        >>> add5(10)
        15
    """
    num_args = func.__code__.co_argcount

    def curried(*args: Any, **kwargs: Any) -> Any:
        if len(args) + len(kwargs) >= num_args:
            return func(*args, **kwargs)
        return lambda *more_args, **more_kwargs: curried(
            *(args + more_args), **{**kwargs, **more_kwargs}
        )

    return curried


class Thunk:
    """Reprezentare imutabilă a unui thunk pentru simularea recursivității de coadă (trampoline)."""
    __slots__ = ("fn", "args", "kwargs")

    def __init__(self, fn: Callable, *args: Any, **kwargs: Any) -> None:
        self.fn = fn
        self.args = args
        self.kwargs = kwargs

    def __call__(self) -> Any:
        return self.fn(*self.args, **self.kwargs)


def trampoline(fn: Callable) -> Callable:
    """
    Optimizator de recursivitate de coadă prin mecanismul de Trampoline.
    Permite apeluri recursive adânci fără depășirea limitei stivei de execuție Python.
    """
    @functools.wraps(fn)
    def trampolined(*args: Any, **kwargs: Any) -> Any:
        res = fn(*args, **kwargs)
        while isinstance(res, Thunk):
            res = res()
        return res

    return trampolined


def head(seq: Sequence[T]) -> T:
    """Extrage primul element dintr-o secvență (car în Lisp)."""
    if not seq:
        raise IndexError("head() aplicat pe o secvență vidă.")
    return seq[0]


def tail(seq: Sequence[T]) -> Tuple[T, ...]:
    """Extrage coada unei secvențe fără primul element (cdr în Lisp)."""
    if not seq:
        raise IndexError("tail() aplicat pe o secvență vidă.")
    return tuple(seq[1:])


# ==============================================================================
# 1. CONSTRUCȚIA LISTELOR FINITE (2 puncte)
# ==============================================================================

# --- 1.a: Lista pătratelor numerelor naturale până la N ---

def squares_up_to(n: int, include_zero: bool = False) -> Tuple[int, ...]:
    """
    Generează lista (tuplu imutabil) pătratelor numerelor naturale până la N.
    
    Implementare funcțională pură prin `map` și `lambda`.
    
    Complexitate:
      - Timp: O(N)
      - Spațiu: O(N) imutabil
      
    Exemple:
        >>> squares_up_to(5)
        (1, 4, 9, 16, 25)
        >>> squares_up_to(4, include_zero=True)
        (0, 1, 4, 9, 16)
        >>> squares_up_to(0)
        ()
    """
    if n < 0:
        return ()
    start = 0 if include_zero else 1
    if start > n:
        return ()
    return tuple(map(lambda x: x * x, range(start, n + 1)))


def squares_recursive(n: int, include_zero: bool = False) -> Tuple[int, ...]:
    """
    Variantă pur recursivă pentru construirea listei pătratelor numerelor naturale.
    Folosește recursivitate structurală și un acumulator imutabil.
    """
    if n < 0:
        return ()
    start = 0 if include_zero else 1

    def _rec(current: int, acc: Tuple[int, ...]) -> Tuple[int, ...]:
        if current > n:
            return acc
        return _rec(current + 1, acc + (current * current,))

    return _rec(start, ())


def squares_stream() -> Generator[int, None, None]:
    """
    Flux infinit (stream / lazy evaluation) al pătratelor numerelor naturale: 1, 4, 9, 16, ...
    Permite extragerea leneșă a primelor N elemente cu itertools.islice.
    """
    return (x * x for x in itertools.count(1))


# --- 1.b: Lista factorialelor până la N ---

def factorial(k: int) -> int:
    """
    Calculează factorialul k! în mod funcțional pur folosind `reduce` și `operator.mul`.
    
    Proprietate: 0! = 1, k! = 1 * 2 * ... * k.
    
    Exemple:
        >>> factorial(0)
        1
        >>> factorial(5)
        120
    """
    if k < 0:
        raise ValueError("Factorialul nu este definit pentru numere negative.")
    if k in (0, 1):
        return 1
    return functools.reduce(operator.mul, range(1, k + 1), 1)


def factorials_up_to(n: int, include_zero: bool = True) -> Tuple[int, ...]:
    """
    Generează lista factorialelor până la N (adică [0!, 1!, ..., N!] sau [1!, ..., N!]).
    
    Implementare funcțională pură prin `map` de ordin superior.
    
    Exemple:
        >>> factorials_up_to(5)
        (1, 1, 2, 6, 24, 120)
        >>> factorials_up_to(5, include_zero=False)
        (1, 2, 6, 24, 120)
    """
    if n < 0:
        return ()
    start = 0 if include_zero else 1
    if start > n:
        return ()
    return tuple(map(factorial, range(start, n + 1)))


def factorials_scan(n: int, include_zero: bool = True) -> Tuple[int, ...]:
    """
    Variantă optimizată funcțional prin scan / fold cumulativ (itertools.accumulate).
    Calculează lista factorialelor în O(N) operații aritmetice fără recalcule redundante.
    """
    if n < 0:
        return ()
    if n == 0:
        return (1,) if include_zero else ()
    
    acc = itertools.accumulate(range(1, n + 1), operator.mul, initial=1)
    res = tuple(acc)
    return res if include_zero else res[1:]


def factorials_stream() -> Generator[int, None, None]:
    """Flux infinit leneș (lazy stream) al factorialelor: 0!, 1!, 2!, 3!, ..."""
    def _gen() -> Generator[int, None, None]:
        val = 1
        yield val
        for i in itertools.count(1):
            val *= i
            yield val
    return _gen()


# ==============================================================================
# 2. FUNCȚII DE CALCUL AL ȘIRURILOR (3 puncte)
# ==============================================================================
# Din formula documentului original:
#   (a) F(x, n) = x * n
#   (b) F(n)    = sum_{i=1}^n i
#   (c) F(n)    = sum_{j=1}^n ( sum_{i=1}^j i )

# --- 2.a: F(x, n) = x * n ---

def series_product(x: float | int, n: int) -> float | int:
    """
    Calculează F(x, n) = x * n conform paradigmei funcționale.
    
    Pentru a evidenția fundamentul funcțional matematic (aritmetica Peano),
    produsul este reprezentat ca o acumulare / fold de adunări repetate
    prin `reduce`, acceptând și calculul direct pur.
    
    Exemple:
        >>> series_product(4, 5)
        20
        >>> series_product(3.5, 2)
        7.0
        >>> series_product(7, 0)
        0
        >>> series_product(6, -3)
        -18
    """
    if n == 0 or x == 0:
        return 0
    abs_n = abs(n)
    # Acumulare pură prin fold
    accumulated = functools.reduce(operator.add, (x for _ in range(abs_n)), 0)
    return accumulated if n > 0 else -accumulated


def series_product_rec(x: float | int, n: int) -> float | int:
    """
    Variantă pur recursivă pentru F(x, n) = x * n.
    F(x, 0) = 0
    F(x, n) = x + F(x, n - 1)  (pentru n > 0)
    """
    if n == 0:
        return 0
    if n > 0:
        return x + series_product_rec(x, n - 1)
    return -series_product_rec(x, -n)


# Versiune curried a funcției de multiplicare F(x)(n)
multiply_curried = curry(lambda x, n: series_product(x, n))


# --- 2.b: F(n) = sum_{i=1}^n i ---

def series_sum_linear(n: int) -> int:
    """
    Calculează F(n) = sum_{i=1}^n i (suma primelor n numere naturale nenule).
    
    Implementare funcțională pură prin fold/reduce:
        reduce(+, [1, 2, ..., n], 0)
        
    Proprietate matematică: sum_{i=1}^n i = n * (n + 1) / 2 (numere triunghiulare).
    
    Exemple:
        >>> series_sum_linear(0)
        0
        >>> series_sum_linear(5)  # 1 + 2 + 3 + 4 + 5 = 15
        15
        >>> series_sum_linear(10)
        55
    """
    if n <= 0:
        return 0
    return functools.reduce(operator.add, range(1, n + 1), 0)


def series_sum_linear_rec(n: int) -> int:
    """
    Variantă recursivă structurală pentru F(n) = sum_{i=1}^n i.
    F(0) = 0
    F(n) = n + F(n - 1)
    """
    if n <= 0:
        return 0
    return n + series_sum_linear_rec(n - 1)


# --- 2.c: F(n) = sum_{j=1}^n ( sum_{i=1}^j i ) ---

def series_sum_triangular(n: int) -> int:
    """
    Calculează F(n) = sum_{j=1}^n ( sum_{i=1}^j i ) = sum_{j=1}^n F_2(j).
    
    Reprezintă suma numerelor triunghiulare (numere tetraedrice).
    Formula închisă de verificare: n * (n + 1) * (n + 2) / 6.
    
    Implementare prin compunere funcțională de ordin superior:
      reduce(+, map(series_sum_linear, 1..n), 0)
      
    Exemple:
        >>> series_sum_triangular(0)
        0
        >>> series_sum_triangular(1)  # F2(1) = 1
        1
        >>> series_sum_triangular(3)  # F2(1) + F2(2) + F2(3) = 1 + 3 + 6 = 10
        10
        >>> series_sum_triangular(4)  # 10 + F2(4) = 10 + 10 = 20
        20
    """
    if n <= 0:
        return 0
    # Mapare a funcției liniare F2 urmată de reducerea prin adunare
    return functools.reduce(
        operator.add,
        map(series_sum_linear, range(1, n + 1)),
        0,
    )


def series_sum_triangular_rec(n: int) -> int:
    """
    Variantă pur recursivă pentru F(n) = sum_{j=1}^n ( sum_{i=1}^j i ).
    Relație de recurență:
      F(0) = 0
      F(n) = F(n - 1) + series_sum_linear(n)
    """
    if n <= 0:
        return 0
    return series_sum_triangular_rec(n - 1) + series_sum_linear(n)


# Aliase simbolice conforme cu notația matematică din cerințe
F1 = series_product
F2 = series_sum_linear
F3 = series_sum_triangular


# ==============================================================================
# 3. FUNCȚII PENTRU LUCRUL CU ȘIRURI / LISTE (5 puncte)
# ==============================================================================

# --- 3.a: GetN(L, n) ---
# Funcția de extragere a celui de-al n-lea element dintr-o listă dată.

def GetN(L: Sequence[T], n: int, one_indexed: bool = False) -> T:
    """
    Extrage cel de-al n-lea element dintr-o listă dată L folosind recursivitate pură.
    
    Parametri:
      - L: secvența de intrare (listă, tuplu)
      - n: indexul elementului căutat
      - one_indexed: dacă True, indexarea începe de la 1 (convenție Lisp / limbaj natural:
                     al 1-lea element este capul listei). Implicit False (0-indexed, convenție Python).
                     
    Funcționare funcțională:
      Se utilizează descompunerea structurală (head/tail pattern matching)
      fără indexare directă sau mutație de variabilă.
      
    Exemple:
        >>> GetN(('a', 'b', 'c', 'd'), 2)
        'c'
        >>> GetN(('a', 'b', 'c', 'd'), 1, one_indexed=True)
        'a'
        >>> GetN([10, 20, 30], 0)
        10
    """
    target_idx = n - 1 if one_indexed else n

    if target_idx < 0:
        raise IndexError(
            f"GetN: Indexul {n} este invalid (nu poate fi negativ)."
        )

    def _rec(seq: Sequence[T], remaining: int) -> T:
        if not seq:
            raise IndexError(
                f"GetN: Indexul {n} depășește lungimea secvenței."
            )
        h, *t = seq
        if remaining == 0:
            return h
        return _rec(t, remaining - 1)

    return _rec(L, target_idx)


get_n = GetN  # Alias idiomatic conform PEP 8


def get_n_stream(iterable: Iterable[T], n: int, one_indexed: bool = False) -> T:
    """
    Variantă funcțională leneșă pentru fluxuri/iteratori utilizând itertools.islice.
    Permite extragerea celui de-al n-lea element dintr-un flux infinit sau generator.
    """
    target_idx = n - 1 if one_indexed else n
    if target_idx < 0:
        raise IndexError(f"get_n_stream: Index invalid {n}.")
    sliced = itertools.islice(iterable, target_idx, target_idx + 1)
    res = list(sliced)
    if not res:
        raise IndexError(f"get_n_stream: Index {n} în afara limitelor fluxului.")
    return res[0]


# --- 3.b: Set(L) ---
# Funcția care întoarce lista ce conține o singură apariție pentru fiecare atom din lista dată.

def Set(L: Sequence[Any], deep: bool = False) -> Tuple[Any, ...]:
    """
    Întoarce o colecție imutabilă (tuplu) ce conține o singură apariție pentru
    fiecare atom din lista dată, păstrând ordinea primei apariții (deduplicare stabilă).
    
    Respectă strict paradigma funcțională:
      - Nicio mutație asupra listei de intrare
      - Niciun apel la metode mutabile precum `set.add`
      - Implementat prin `reduce` cu acumulator imutabil (tuplu)
      
    Parametru opțional:
      - deep: dacă True, aplatizează recursiv sublistele (extrage atomii la nivel profund,
              conform conceptului de 'atom' din Lisp / Prolog).
              
    Exemple:
        >>> Set((1, 2, 2, 3, 1, 4, 3))
        (1, 2, 3, 4)
        >>> Set(['a', 'b', 'a', 'c', 'b'])
        ('a', 'b', 'c')
        >>> Set([1, [2, 3], 2, [3, 4]], deep=True)
        (1, 2, 3, 4)
    """
    def _flatten_atoms(items: Iterable[Any]) -> Tuple[Any, ...]:
        """Aplatizare recursivă funcțională a atomilor dintr-o structură imbricată."""
        return functools.reduce(
            lambda acc, x: acc + (_flatten_atoms(x) if isinstance(x, (list, tuple)) else (x,)),
            items,
            (),
        )

    elements: Iterable[Any] = _flatten_atoms(L) if deep else L

    # Acumulare funcțională pură prin fold
    return functools.reduce(
        lambda acc, item: acc if item in acc else acc + (item,),
        elements,
        (),
    )


def set_recursive(L: Sequence[T]) -> Tuple[T, ...]:
    """
    Variantă pur recursivă pentru Set(L) (analogul clasic 'nub' din Haskell):
      nub [] = []
      nub (x:xs) = x : nub (filter (/= x) xs)
    """
    if not L:
        return ()
    h, *t = L
    # Filtrare funcțională pură a aparițiilor ulterioare ale capului
    filtered_tail = tuple(filter(lambda x: x != h, t))
    return (h,) + set_recursive(filtered_tail)


set_fn = Set  # Alias idiomatic


# --- 3.c: OddEven(L) ---
# Funcția care inversează între ele elementele pare și impare vecine din lista dată.

def OddEven(L: Sequence[T], mode: str = "index") -> Tuple[T, ...]:
    """
    Inversează între ele elementele pare și impare vecine din lista dată L.
    
    Această funcție suportă ambele interpretări canonice întâlnite în teoria limbajelor:
      1. mode="index" (interpretarea canonică Lisp / structurală):
         Inversează elementele de pe poziții pare și impare vecine (vecinii adiacenți):
         (x0, x1, x2, x3, ...) -> (x1, x0, x3, x2, ...)
         Funcționează universal pentru orice tip de atomi (simboluri, șiruri, numere).
         
      2. mode="value" (interpretarea aritmetică pe baza parității valorilor numerice):
         Parcurge lista și, dacă întâlnește o pereche de elemente vecine de parități diferite
         (unul par și unul impar), le inversează ordinea.
         
    Implementarea este pur recursivă, fără cicluri for/while și fără mutații.
    
    Exemple:
        >>> OddEven((1, 2, 3, 4, 5, 6))
        (2, 1, 4, 3, 6, 5)
        >>> OddEven(('a', 'b', 'c', 'd', 'e'))
        ('b', 'a', 'd', 'c', 'e')
        >>> OddEven((2, 1, 4, 3), mode="value")
        (1, 2, 3, 4)
    """
    if mode == "index":
        return _odd_even_by_index(L)
    elif mode == "value":
        return _odd_even_by_value(L)  # type: ignore
    else:
        raise ValueError(f"Mod necunoscut: {mode}. Opțiuni valide: 'index', 'value'.")


def _odd_even_by_index(seq: Sequence[T]) -> Tuple[T, ...]:
    """Recursivitate structurală pe perechi de poziții adiacente."""
    if not seq:
        return ()
    if len(seq) == 1:
        return (seq[0],)
    first, second, *rest = seq
    return (second, first) + _odd_even_by_index(rest)


def _odd_even_by_value(seq: Sequence[int]) -> Tuple[int, ...]:
    """Recursivitate structurală ce inversează elementele vecine dacă au parități opuse."""
    if not seq:
        return ()
    if len(seq) == 1:
        return (seq[0],)
    first, second, *rest = seq
    # Verificare dacă un număr este par și celălalt impar
    if (first % 2 == 0 and second % 2 != 0) or (first % 2 != 0 and second % 2 == 0):
        return (second, first) + _odd_even_by_value(rest)
    return (first,) + _odd_even_by_value((second, *rest))


odd_even = OddEven  # Alias idiomatic


# --- 3.d: FuncList(L1, L2, F) ---
# Funcția care însumează atomii corespunzători după poziție din listele L1 și L2, conform funcției F.

def FuncList(
    L1: Sequence[T1],
    L2: Sequence[T2],
    F: Callable[[T1, T2], R] = operator.add,  # type: ignore
) -> Tuple[R, ...]:
    """
    Combină / însumează atomii corespunzători după poziție din listele L1 și L2,
    aplicând funcția de combinare F (analogul lui `zipWith` din Haskell sau `mapcar` din Lisp).
    
    Dacă listele au lungimi diferite, procesarea se oprește la lungimea listei mai scurte
    (comportament canonic funcțional conform `zip`).
    
    Parametri:
      - L1: prima secvență
      - L2: a doua secvență
      - F: funcție binară pură F(a, b) -> R (implicit operator.add pentru însumare directă)
      
    Implementare funcțională pură prin `map` și `zip` (sau recursivitate structurală).
    
    Exemple:
        >>> FuncList((1, 2, 3), (10, 20, 30), lambda x, y: x + y)
        (11, 22, 33)
        >>> FuncList((2, 3, 4), (5, 6, 7), lambda x, y: x * y)
        (10, 18, 28)
        >>> FuncList((10, 20), (3, 5), operator.sub)
        (7, 15)
        >>> FuncList((1, 2, 3), (4, 5, 6))  # operator.add implicit
        (5, 7, 9)
    """
    return tuple(map(lambda pair: F(pair[0], pair[1]), zip(L1, L2)))


def func_list_recursive(
    L1: Sequence[T1],
    L2: Sequence[T2],
    F: Callable[[T1, T2], R],
) -> Tuple[R, ...]:
    """
    Variantă pur recursivă (fără funcții built-in) pentru FuncList(L1, L2, F).
    Deconstruiește concomitent ambele liste (h1, *t1 și h2, *t2).
    """
    if not L1 or not L2:
        return ()
    h1, *t1 = L1
    h2, *t2 = L2
    return (F(h1, h2),) + func_list_recursive(t1, t2, F)


def func_list_sum(
    L1: Sequence[T1],
    L2: Sequence[T2],
    F: Callable[[T1, T2], Any] = operator.add,  # type: ignore
) -> Any:
    """
    În cazul în care cerința „însumează atomii corespunzători conform funcției F”
    este interpretată ca returnând suma scalară totală: sum(F(L1[i], L2[i])).
    
    Exemplu:
        >>> func_list_sum((1, 2, 3), (4, 5, 6), operator.mul)  # 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        32
    """
    res_list = FuncList(L1, L2, F)
    if not res_list:
        return 0
    return functools.reduce(operator.add, res_list)


func_list = FuncList  # Alias idiomatic


# ==============================================================================
# 4. SUITĂ CUPRINZĂTOARE DE TESTE UNITARE (Unit Tests)
# ==============================================================================

class TestParadigmaFunctionalaLab1(unittest.TestCase):
    """Teste automate pentru verificarea corectitudinii tuturor cerințelor."""

    def test_01_squares_up_to(self) -> None:
        self.assertEqual(squares_up_to(5), (1, 4, 9, 16, 25))
        self.assertEqual(squares_up_to(5, include_zero=True), (0, 1, 4, 9, 16, 25))
        self.assertEqual(squares_up_to(0), ())
        self.assertEqual(squares_up_to(0, include_zero=True), (0,))
        self.assertEqual(squares_up_to(-3), ())
        # Test varianta recursivă
        self.assertEqual(squares_recursive(4), (1, 4, 9, 16))
        # Test stream
        first_5_stream = tuple(itertools.islice(squares_stream(), 5))
        self.assertEqual(first_5_stream, (1, 4, 9, 16, 25))

    def test_02_factorials_up_to(self) -> None:
        self.assertEqual(factorials_up_to(0), (1,))
        self.assertEqual(factorials_up_to(5), (1, 1, 2, 6, 24, 120))
        self.assertEqual(factorials_up_to(5, include_zero=False), (1, 2, 6, 24, 120))
        self.assertEqual(factorials_up_to(-1), ())
        # Test scan
        self.assertEqual(factorials_scan(5), (1, 1, 2, 6, 24, 120))
        # Test stream
        first_4_stream = tuple(itertools.islice(factorials_stream(), 4))
        self.assertEqual(first_4_stream, (1, 1, 2, 6))

    def test_03_series_product(self) -> None:
        self.assertEqual(series_product(5, 4), 20)
        self.assertEqual(series_product(7, 0), 0)
        self.assertEqual(series_product(0, 10), 0)
        self.assertEqual(series_product(4, -3), -12)
        self.assertEqual(series_product(2.5, 4), 10.0)
        # Test recursiv
        self.assertEqual(series_product_rec(6, 3), 18)
        self.assertEqual(series_product_rec(6, -2), -12)
        # Test curried
        self.assertEqual(multiply_curried(7)(8), 56)

    def test_04_series_sum_linear(self) -> None:
        self.assertEqual(series_sum_linear(0), 0)
        self.assertEqual(series_sum_linear(-5), 0)
        self.assertEqual(series_sum_linear(1), 1)
        self.assertEqual(series_sum_linear(5), 15)   # 1+2+3+4+5
        self.assertEqual(series_sum_linear(100), 5050)
        # Test recursiv
        self.assertEqual(series_sum_linear_rec(10), 55)

    def test_05_series_sum_triangular(self) -> None:
        self.assertEqual(series_sum_triangular(0), 0)
        self.assertEqual(series_sum_triangular(1), 1)
        self.assertEqual(series_sum_triangular(2), 1 + 3)  # 4
        self.assertEqual(series_sum_triangular(3), 1 + 3 + 6)  # 10
        self.assertEqual(series_sum_triangular(4), 1 + 3 + 6 + 10)  # 20
        # Verificare cu formula închisă n*(n+1)*(n+2)//6
        for n in range(1, 15):
            expected = n * (n + 1) * (n + 2) // 6
            self.assertEqual(series_sum_triangular(n), expected)
            self.assertEqual(series_sum_triangular_rec(n), expected)

    def test_06_get_n(self) -> None:
        sample = ("alpha", "beta", "gamma", "delta", "epsilon")
        # 0-indexed
        self.assertEqual(GetN(sample, 0), "alpha")
        self.assertEqual(GetN(sample, 2), "gamma")
        self.assertEqual(GetN(sample, 4), "epsilon")
        # 1-indexed
        self.assertEqual(GetN(sample, 1, one_indexed=True), "alpha")
        self.assertEqual(GetN(sample, 3, one_indexed=True), "gamma")
        self.assertEqual(GetN(sample, 5, one_indexed=True), "epsilon")
        # Erori la index invalid
        with self.assertRaises(IndexError):
            GetN(sample, 5)
        with self.assertRaises(IndexError):
            GetN(sample, -1)
        with self.assertRaises(IndexError):
            GetN((), 0)
        # Test stream
        self.assertEqual(get_n_stream(iter(sample), 1), "beta")

    def test_07_set(self) -> None:
        self.assertEqual(Set((1, 2, 2, 3, 1, 4, 3)), (1, 2, 3, 4))
        self.assertEqual(Set(("a", "b", "a", "c", "b")), ("a", "b", "c"))
        self.assertEqual(Set(()), ())
        self.assertEqual(Set((42,)), (42,))
        # Păstrarea ordinii
        self.assertEqual(Set((5, 4, 3, 2, 1, 5, 2)), (5, 4, 3, 2, 1))
        # Nested / Deep flattening de atomi
        nested = (1, (2, 3), 2, (3, (4, 1)))
        self.assertEqual(Set(nested, deep=True), (1, 2, 3, 4))
        # Test varianta recursivă
        self.assertEqual(set_recursive((1, 2, 2, 3, 1)), (1, 2, 3))

    def test_08_odd_even(self) -> None:
        # Swap by index (poziții pare/impare adiacente)
        self.assertEqual(OddEven((1, 2, 3, 4, 5, 6)), (2, 1, 4, 3, 6, 5))
        self.assertEqual(OddEven((1, 2, 3, 4, 5)), (2, 1, 4, 3, 5))
        self.assertEqual(OddEven(("a", "b", "c", "d")), ("b", "a", "d", "c"))
        self.assertEqual(OddEven(("only",)), ("only",))
        self.assertEqual(OddEven(()), ())

        # Swap by value (după paritatea valorilor numerice)
        self.assertEqual(OddEven((1, 2, 3, 4), mode="value"), (2, 1, 4, 3))
        self.assertEqual(OddEven((2, 1, 4, 3), mode="value"), (1, 2, 3, 4))
        self.assertEqual(OddEven((2, 4, 1, 3), mode="value"), (2, 1, 4, 3))

    def test_09_func_list(self) -> None:
        l1 = (1, 2, 3, 4)
        l2 = (10, 20, 30, 40)
        # Sumă conform F
        self.assertEqual(FuncList(l1, l2, lambda x, y: x + y), (11, 22, 33, 44))
        self.assertEqual(FuncList(l1, l2), (11, 22, 33, 44))  # implicit operator.add
        # Produs conform F
        self.assertEqual(FuncList(l1, l2, lambda x, y: x * y), (10, 40, 90, 160))
        # Lungimi diferite
        self.assertEqual(FuncList((1, 2), (10, 20, 30)), (11, 22))
        # Test recursiv
        self.assertEqual(
            func_list_recursive((1, 2, 3), (10, 20, 30), operator.mul),
            (10, 40, 90),
        )
        # Test sumă scalară
        self.assertEqual(func_list_sum(l1, l2, operator.mul), 10 + 40 + 90 + 160)

    def test_10_combinators(self) -> None:
        # compose
        f = compose(lambda x: x + 1, lambda x: x * 2)
        self.assertEqual(f(5), 11)  # 5 * 2 = 10 -> + 1 = 11

        # pipe
        val = pipe(5, lambda x: x * 2, lambda x: x + 1, str)
        self.assertEqual(val, "11")


# ==============================================================================
# 5. DEMONSTRAȚIE INTERACTIVĂ (Main Execution CLI)
# ==============================================================================

def run_demonstration() -> None:
    """Afișează o prezentare structurată și elegantă a rezultatelor tuturor punctelor de laborator."""
    separator = "═" * 72
    sub_sep = "─" * 72

    print(separator)
    print("   LUCRAREA DE LABORATOR NR. 1 — PARADIGMA FUNCȚIONALĂ ÎN PYTHON")
    print(separator)

    # --------------------------------------------------------------------------
    print("\n[1] CONSTRUCȚIA LISTELOR FINITE (2 puncte)")
    print(sub_sep)
    
    n_squares = 8
    res_squares = squares_up_to(n_squares)
    res_squares_zero = squares_up_to(n_squares, include_zero=True)
    print(f"a. Lista pătratelor până la N={n_squares}:")
    print(f"   • squares_up_to({n_squares})                     = {res_squares}")
    print(f"   • squares_up_to({n_squares}, include_zero=True) = {res_squares_zero}")
    print(f"   • squares_recursive({n_squares})                = {squares_recursive(n_squares)}")
    stream_first_5 = tuple(itertools.islice(squares_stream(), 5))
    print(f"   • squares_stream() (primele 5 valori)     = {stream_first_5}")

    n_fact = 6
    res_fact = factorials_up_to(n_fact)
    res_fact_nonzero = factorials_up_to(n_fact, include_zero=False)
    print(f"\nb. Lista factorialelor până la N={n_fact}:")
    print(f"   • factorials_up_to({n_fact})                    = {res_fact}")
    print(f"   • factorials_up_to({n_fact}, include_zero=False)= {res_fact_nonzero}")
    print(f"   • factorials_scan({n_fact}) (O(N) fold)        = {factorials_scan(n_fact)}")

    # --------------------------------------------------------------------------
    print("\n[2] FUNCȚII DE CALCUL AL ȘIRURILOR (3 puncte)")
    print(sub_sep)
    
    x_val, n_prod = 7, 5
    print(f"a. F(x, n) = x * n:")
    print(f"   • F1({x_val}, {n_prod})                     = {series_product(x_val, n_prod)}")
    print(f"   • series_product_rec({x_val}, {n_prod})     = {series_product_rec(x_val, n_prod)}")
    print(f"   • multiply_curried({x_val})({n_prod})       = {multiply_curried(x_val)(n_prod)}")

    n_linear = 10
    print(f"\nb. F(n) = sum_{{i=1}}^n i (suma numerelor naturale până la n={n_linear}):")
    print(f"   • F2({n_linear})                     = {series_sum_linear(n_linear)}")
    print(f"   • series_sum_linear_rec({n_linear})  = {series_sum_linear_rec(n_linear)}")
    print(f"   • Formulă n*(n+1)//2                 = {n_linear * (n_linear + 1) // 2}")

    n_triang = 5
    print(f"\nc. F(n) = sum_{{j=1}}^n ( sum_{{i=1}}^j i ) (numere tetraedrice, n={n_triang}):")
    print(f"   • F3({n_triang})                         = {series_sum_triangular(n_triang)}")
    print(f"   • series_sum_triangular_rec({n_triang})  = {series_sum_triangular_rec(n_triang)}")
    print(f"   • Formulă n*(n+1)*(n+2)//6               = {n_triang * (n_triang + 1) * (n_triang + 2) // 6}")

    # --------------------------------------------------------------------------
    print("\n[3] FUNCȚII PENTRU LUCRUL CU ȘIRURI / LISTE (5 puncte)")
    print(sub_sep)

    test_list = ("A", "B", "C", "D", "E", "F")
    print("a. GetN(L, n) — extragerea celui de-al n-lea element:")
    print(f"   • Listă L = {test_list}")
    print(f"   • GetN(L, 0)                     = '{GetN(test_list, 0)}' (0-indexed)")
    print(f"   • GetN(L, 2)                     = '{GetN(test_list, 2)}' (0-indexed, al 3-lea)")
    print(f"   • GetN(L, 1, one_indexed=True)   = '{GetN(test_list, 1, one_indexed=True)}' (1-indexed)")
    print(f"   • GetN(L, 4, one_indexed=True)   = '{GetN(test_list, 4, one_indexed=True)}' (1-indexed)")

    dup_list = (1, 2, 3, 2, 4, 1, 5, 3, 6, 2)
    nested_list = (1, (2, 3), 2, (3, (4, 1)))
    print("\nb. Set(L) — lista cu o singură apariție pentru fiecare atom:")
    print(f"   • Listă inițială: {dup_list}")
    print(f"   • Set(L)                    = {Set(dup_list)}")
    print(f"   • set_recursive(L)          = {set_recursive(dup_list)}")
    print(f"   • Listă imbricată de atomi:   {nested_list}")
    print(f"   • Set(L, deep=True)         = {Set(nested_list, deep=True)}")

    oe_list = (10, 20, 30, 40, 50, 60, 70)
    oe_values = (1, 2, 4, 3, 5, 6, 8, 7)
    print("\nc. OddEven(L) — inversarea elementelor vecine:")
    print(f"   • Listă poziții: {oe_list}")
    print(f"   • OddEven(L, mode='index')  = {OddEven(oe_list, mode='index')}")
    print(f"   • Listă paritate: {oe_values}")
    print(f"   • OddEven(L, mode='value')  = {OddEven(oe_values, mode='value')}")

    l1 = (1, 2, 3, 4, 5)
    l2 = (10, 20, 30, 40, 50)
    print("\nd. FuncList(L1, L2, F) — combinarea atomilor după poziție:")
    print(f"   • L1 = {l1}")
    print(f"   • L2 = {l2}")
    print(f"   • FuncList(L1, L2, F=lambda x, y: x + y)   = {FuncList(l1, l2, lambda x, y: x + y)}")
    print(f"   • FuncList(L1, L2, F=lambda x, y: x * y)   = {FuncList(l1, l2, lambda x, y: x * y)}")
    print(f"   • func_list_recursive(L1, L2, op.sub)      = {func_list_recursive(l1, l2, operator.sub)}")
    print(f"   • func_list_sum(L1, L2, op.mul) (sumă tot) = {func_list_sum(l1, l2, operator.mul)}")

    print(separator)


def main() -> None:
    """Punct principal de intrare: execută demonstrația și rulează suita de teste."""
    if "--test" in sys.argv or "-t" in sys.argv:
        # Rulare strictă a testelor unitare
        sys.argv = [sys.argv[0]]
        unittest.main()
        return

    # Execută demonstrația completă
    run_demonstration()

    # Rulare teste de verificare integrată
    print("\n[RULARE AUTOMATĂ TESTE UNITARE]")
    suite = unittest.defaultTestLoader.loadTestsFromTestCase(TestParadigmaFunctionalaLab1)
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)

    if not result.wasSuccessful():
        sys.exit(1)


if __name__ == "__main__":
    main()
