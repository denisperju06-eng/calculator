"""
=============================================================================
PARADIGMA FUNCȚIONALĂ - LABORATORUL 3
Parser XML și Prelucrarea Funcțională a Arborelui de Etichete
=============================================================================

Cerințe acoperite (10 puncte conform conditii.md):
  1. Elaborarea unui parser pentru documente XML (3 puncte)
  2. Generarea arborelui de etichete pe baza prelucrării documentului XML (1 punct)
  3. Căutarea unei etichete specifice în arborele de etichete (1 punct)
  4. Generarea unei liste pe baza rezultatului căutării etichetei (1 punct)
  5. Obținerea adâncimii arborelui de etichete rezultat (1 punct)
  6. Obținerea lățimii arborelui de etichete rezultat (1 punct)
  7. Obținerea numărului de noduri de pe un anumit nivel, pornind de la
     un nod dat al arborelui (2 puncte)

Principii funcționale respectate cu strictețe:
  - Imutabilitate completă (structuri de tip NamedTuple și tuple).
  - Funcții pure (fără efecte laterale, fără variabile globale mutabile).
  - Recursivitate structurală în locul buclelor iterative (fără for/while în algoritmi).
  - Funcții de ordin superior (map, filter, reduce, fold_tree).
  - Transparență referențială.
=============================================================================
"""

from __future__ import annotations
import re
import html
import sys
from typing import NamedTuple, Tuple, Optional, Callable, Any, Dict, List, Union
import xml.etree.ElementTree as ET


# =============================================================================
# 1. STRUCTURI DE DATE IMUTABILE
# =============================================================================

class OpenTag(NamedTuple):
    """Token pentru o etichetă de deschidere: <tag attr="val">"""
    tag: str
    attributes: Tuple[Tuple[str, str], ...]


class CloseTag(NamedTuple):
    """Token pentru o etichetă de închidere: </tag>"""
    tag: str


class SelfClosingTag(NamedTuple):
    """Token pentru o etichetă auto-închisă: <tag attr="val" />"""
    tag: str
    attributes: Tuple[Tuple[str, str], ...]


class TextToken(NamedTuple):
    """Token pentru conținutul text dintre etichete."""
    text: str


Token = Union[OpenTag, CloseTag, SelfClosingTag, TextToken]


class Node(NamedTuple):
    """
    Reprezentarea unui nod din arborele XML (complet imutabilă).
    - tag: numele etichetei XML
    - attributes: tuplu imutabil de perechi (cheie, valoare)
    - text: conținutul text asociat nodului
    - children: tuplu imutabil de noduri copil (arborescență recursivă)
    """
    tag: str
    attributes: Tuple[Tuple[str, str], ...]
    text: str
    children: Tuple[Node, ...]

    def get_attr(self, name: str, default: Optional[str] = None) -> Optional[str]:
        """Extrage funcțional valoarea unui atribut după cheie."""
        matches = tuple(filter(lambda pair: pair[0] == name, self.attributes))
        return matches[0][1] if matches else default

    @property
    def attr_dict(self) -> Dict[str, str]:
        """Conversie imutabilă în dicționar pentru afișare/inspecție comodă."""
        return dict(self.attributes)

    def __repr__(self) -> str:
        attrs_str = f", attrs={self.attributes}" if self.attributes else ""
        text_str = f", text='{self.text}'" if self.text else ""
        ch_str = f", children_count={len(self.children)}" if self.children else ""
        return f"Node({self.tag}{attrs_str}{text_str}{ch_str})"


# =============================================================================
# 2. TOKENIZER PUR FUNCȚIONAL PENTRU DOCUMENTE XML
# =============================================================================

ATTR_REGEX = re.compile(r'([a-zA-Z0-9_:-]+)\s*=\s*(?:"([^"]*)"|\'([^\']*)\')')

TOKEN_REGEX = re.compile(
    r'(?P<comment><!--[\s\S]*?-->)|'
    r'(?P<cdata><!\[CDATA\[(?P<cdata_val>[\s\S]*?)\]\]>)|'
    r'(?P<pi><\?[\s\S]*?\?>)|'
    r'(?P<doctype><!DOCTYPE[\s\S]*?>)|'
    r'(?P<close></\s*(?P<close_name>[a-zA-Z0-9_:-]+)\s*>)|'
    r'(?P<self_close><\s*(?P<sc_name>[a-zA-Z0-9_:-]+)(?P<sc_attrs>[^>]*?)/\s*>)|'
    r'(?P<open><\s*(?P<open_name>[a-zA-Z0-9_:-]+)(?P<open_attrs>[^>]*?)>)|'
    r'(?P<text>[^<]+)'
)


def parse_attributes(attr_str: str) -> Tuple[Tuple[str, str], ...]:
    """
    Extrage atributele dintr-un șir de caractere în mod pur funcțional.
    Folosește map și unescape pentru a returna un tuplu imutabil (nume, valoare).
    """
    matches = ATTR_REGEX.findall(attr_str)
    return tuple(
        map(
            lambda m: (m[0], html.unescape(m[1] if m[1] != "" else m[2])),
            matches
        )
    )


def match_to_token(match: re.Match) -> Optional[Token]:
    """
    Funcție pură de conversie: transformă un Regex Match într-un obiect Token.
    Comentariile, declarațiile XML și doctype-urile sunt ignorate (returnează None).
    """
    if match.group('comment') or match.group('pi') or match.group('doctype'):
        return None
    if match.group('cdata'):
        clean_cdata = match.group('cdata_val').strip()
        return TextToken(clean_cdata) if clean_cdata else None
    if match.group('close'):
        return CloseTag(match.group('close_name'))
    if match.group('self_close'):
        return SelfClosingTag(
            match.group('sc_name'),
            parse_attributes(match.group('sc_attrs'))
        )
    if match.group('open'):
        return OpenTag(
            match.group('open_name'),
            parse_attributes(match.group('open_attrs'))
        )
    raw_text = match.group('text').strip()
    return TextToken(html.unescape(raw_text)) if raw_text else None


def tokenize(xml_str: str) -> Tuple[Token, ...]:
    """
    Tokenizează un șir XML într-un tuplu imutabil de tokeni.
    Implementat funcțional folosind pipeline-ul map -> filter.
    """
    return tuple(filter(None, map(match_to_token, TOKEN_REGEX.finditer(xml_str))))


# =============================================================================
# 3. PARSER XML PROPRIU RECURSIV (Punctul 1: 3 puncte)
# =============================================================================

def parse_body(
    tokens: Tuple[Token, ...],
    parent_tag: str,
    acc_text: Tuple[str, ...],
    acc_children: Tuple[Node, ...]
) -> Tuple[Tuple[str, ...], Tuple[Node, ...], Tuple[Token, ...]]:
    """
    Funcție recursivă pură care parsează conținutul intern (text și copii)
    al unei etichete până la găsirea etichetei de închidere corespunzătoare.
    
    Parametri:
      - tokens: fluxul imutabil de tokeni rămași
      - parent_tag: numele etichetei părinte care se dorește a fi închisă
      - acc_text: acumulator imutabil pentru fragmentele de text
      - acc_children: acumulator imutabil pentru nodurile copil construite
      
    Returnează:
      (texte_acumulate, copii_acumulați, tokeni_ramasi)
    """
    if not tokens:
        raise ValueError(f"Eroare XML: Eticheta <{parent_tag}> nu este închisă (sfârșit de fișier).")

    cur_tok = tokens[0]
    rest_tok = tokens[1:]

    # Cazul 1: Etichetă de închidere
    if isinstance(cur_tok, CloseTag):
        if cur_tok.tag != parent_tag:
            raise ValueError(
                f"Eroare XML: Neconcordanță etichete! Așteptat </{parent_tag}>, primit </{cur_tok.tag}>."
            )
        return acc_text, acc_children, rest_tok

    # Cazul 2: Fragment de text
    if isinstance(cur_tok, TextToken):
        return parse_body(rest_tok, parent_tag, acc_text + (cur_tok.text,), acc_children)

    # Cazul 3: Copil (element deschis sau auto-închis)
    if isinstance(cur_tok, (OpenTag, SelfClosingTag)):
        child_node, remaining_after_child = parse_element(tokens)
        return parse_body(remaining_after_child, parent_tag, acc_text, acc_children + (child_node,))

    # Oricare alt token ignorat
    return parse_body(rest_tok, parent_tag, acc_text, acc_children)


def parse_element(tokens: Tuple[Token, ...]) -> Tuple[Node, Tuple[Token, ...]]:
    """
    Funcție recursivă pură care consumă tokeni și construiește un singur Node XML.
    
    Returnează:
      (nodul_construit, tokeni_ramasi)
    """
    if not tokens:
        raise ValueError("Eroare XML: Fluxul de tokeni este gol.")

    first = tokens[0]
    rest = tokens[1:]

    # Nod auto-închis: <tag attr="val" />
    if isinstance(first, SelfClosingTag):
        node = Node(
            tag=first.tag,
            attributes=first.attributes,
            text="",
            children=()
        )
        return node, rest

    # Nod cu conținut: <tag attr="val"> ... </tag>
    if isinstance(first, OpenTag):
        texts, children, remaining = parse_body(rest, first.tag, (), ())
        node_text = " ".join(texts).strip() if texts else ""
        node = Node(
            tag=first.tag,
            attributes=first.attributes,
            text=node_text,
            children=children
        )
        return node, remaining

    raise ValueError(f"Eroare XML: Token neașteptat la începutul nodului: {first}")


def parse_xml(xml_content: str) -> Node:
    """
    Parser XML principal (elaborat de la zero în mod pur funcțional):
    Transformă conținutul text XML într-un arbore imutabil de tip Node.
    """
    tokens = tokenize(xml_content)
    if not tokens:
        raise ValueError("Eroare XML: Nu s-au identificat etichete XML valide în document.")
    root, _ = parse_element(tokens)
    return root


def parse_xml_file(filepath: str) -> Node:
    """Încarcă un fișier XML și îl parsează pur funcțional."""
    with open(filepath, "r", encoding="utf-8") as f:
        return parse_xml(f.read())


# =============================================================================
# 4. INTEROPERABILITATE CU ELEMENTTREE (Conversie recursivă pur funcțională)
# =============================================================================

def from_element_tree(elem: ET.Element) -> Node:
    """
    Conversie recursivă pur funcțională dintr-un arbore xml.etree.ElementTree
    într-un arbore imutabil Node definit de noi.
    Respectă cerința opțională din enunț:
      '(sau folosind elementtree dar functiile de extragere sa fie recursive pur functionale)'
    """
    return Node(
        tag=elem.tag,
        attributes=tuple(elem.attrib.items()),
        text=(elem.text or "").strip(),
        children=tuple(map(from_element_tree, elem))
    )


# =============================================================================
# 5. GENERAREA ARBORELUI DE ETICHETE (Punctul 2: 1 punct)
# =============================================================================

def generate_tag_tree(xml_source: Union[str, ET.Element]) -> Node:
    """
    Generează arborele de etichete pe baza prelucrării documentului XML.
    Acceptă fie un șir XML (utilizând parserul propriu), fie un nod ElementTree.
    """
    if isinstance(xml_source, ET.Element):
        return from_element_tree(xml_source)
    return parse_xml(xml_source)


def tree_to_string(node: Optional[Node], indent: int = 0, step: int = 2) -> str:
    """
    Afișare ierarhică vizuală a arborelui de etichete generat (recursivă pur funcțională).
    """
    if node is None:
        return ""
    spaces = " " * indent
    attrs_str = " " + " ".join(f'{k}="{v}"' for k, v in node.attributes) if node.attributes else ""
    text_str = f" => \"{node.text}\"" if node.text else ""
    header = f"{spaces}<{node.tag}{attrs_str}>{text_str}"
    if not node.children:
        return header
    children_str = "\n".join(tree_to_string(child, indent + step, step) for child in node.children)
    return f"{header}\n{children_str}"


# =============================================================================
# 6. CĂUTAREA UNEI ETICHETE SPECIFICE ÎN ARBORE (Punctul 3: 1 punct)
# =============================================================================

def find_all_by_tag(node: Optional[Node], target_tag: str) -> Tuple[Node, ...]:
    """
    Caută recursiv și pur funcțional TOATE aparițiile etichetei 'target_tag' în arbore.
    Returnează un tuplu imutabil cu toate nodurile găsite.
    """
    if node is None:
        return ()
    # Verificare nod curent
    current = (node,) if node.tag == target_tag else ()
    # Recursie pe fiecare copil folosind map și sum funcțional
    children_results = sum(map(lambda ch: find_all_by_tag(ch, target_tag), node.children), ())
    return current + children_results


def find_first_by_tag(node: Optional[Node], target_tag: str) -> Optional[Node]:
    """
    Caută recursiv prima apariție a unei etichete specifice în arbore (short-circuit pur funcțional).
    Returnează nodul găsit sau None.
    """
    if node is None:
        return None
    if node.tag == target_tag:
        return node
    
    def search_children(children: Tuple[Node, ...]) -> Optional[Node]:
        if not children:
            return None
        res = find_first_by_tag(children[0], target_tag)
        return res if res is not None else search_children(children[1:])

    return search_children(node.children)


def find_by_predicate(node: Optional[Node], predicate: Callable[[Node], bool]) -> Tuple[Node, ...]:
    """
    Funcție de ordin superior (Higher-Order Function):
    Caută toate nodurile care satisfac predicatul dat (ex: filtrare după atribut, text etc.).
    """
    if node is None:
        return ()
    current = (node,) if predicate(node) else ()
    children_results = sum(map(lambda ch: find_by_predicate(ch, predicate), node.children), ())
    return current + children_results


# =============================================================================
# 7. GENERAREA UNEI LISTE PE BAZA CĂUTĂRII (Punctul 4: 1 punct)
# =============================================================================

def flatten_tree_to_list(node: Optional[Node]) -> Tuple[Node, ...]:
    """
    Aplatizează arborele de noduri într-o listă (tuplu) conform parcurgerii în preordine.
    Implementat recursiv pur funcțional.
    """
    if node is None:
        return ()
    return (node,) + sum(map(flatten_tree_to_list, node.children), ())


def nodes_to_list(nodes: Tuple[Node, ...]) -> Tuple[Dict[str, Any], ...]:
    """
    Transformă o colecție de noduri rezultată dintr-o căutare într-o listă structurată
    de dicționare informative (tag, atribute, text, număr copii).
    """
    return tuple(
        map(
            lambda n: {
                "tag": n.tag,
                "attributes": n.attr_dict,
                "text": n.text,
                "children_count": len(n.children),
            },
            nodes
        )
    )


def nodes_to_text_list(nodes: Tuple[Node, ...]) -> Tuple[str, ...]:
    """
    Extrage doar textele din nodurile rezultate în urma căutării.
    """
    return tuple(map(lambda n: n.text, nodes))


def nodes_to_tag_list(nodes: Tuple[Node, ...]) -> Tuple[str, ...]:
    """
    Extrage doar etichetele din nodurile rezultate.
    """
    return tuple(map(lambda n: n.tag, nodes))


def tag_search_to_list(node: Optional[Node], target_tag: str) -> Tuple[Node, ...]:
    """
    Căutare completă urmată de generarea listei de noduri corespunzătoare etichetei.
    """
    return find_all_by_tag(node, target_tag)


def search_and_extract(
    node: Optional[Node],
    target_tag: str,
    extractor: Callable[[Node], Any]
) -> Tuple[Any, ...]:
    """
    Funcție de ordin superior: caută o etichetă și aplică funcția de extracție
    pe fiecare nod găsit prin map funcțional.
    """
    return tuple(map(extractor, find_all_by_tag(node, target_tag)))


# =============================================================================
# 8. ADÂNCIMEA ARBORELUI DE ETICHETE REZULTAT (Punctul 5: 1 punct)
# =============================================================================

def get_tree_depth(node: Optional[Node]) -> int:
    """
    Calculează adâncimea (înălțimea) arborelui în mod recursiv pur funcțional.
    Cazuri:
      - None -> 0
      - Nod frunză (fără copii) -> 1
      - Nod intern -> 1 + max(adâncimile copiilor)
    """
    if node is None:
        return 0
    if not node.children:
        return 1
    return 1 + max(map(get_tree_depth, node.children))


# =============================================================================
# 9. LĂȚIMEA ARBORELUI DE ETICHETE REZULTAT (Punctul 6: 1 punct)
# =============================================================================

def get_tree_width(node: Optional[Node]) -> int:
    """
    Obține lățimea arborelui de etichete (definiția canonică în algoritmică):
    Numărul maxim de noduri aflate pe un singur nivel al arborelui.
    Implementat funcțional folosind max și map peste nivelurile 1..adâncime.
    """
    if node is None:
        return 0
    depth = get_tree_depth(node)
    return max(map(lambda lvl: count_nodes_at_level(node, lvl), range(1, depth + 1)))


def get_tree_leaf_count(node: Optional[Node]) -> int:
    """
    Definiție complementară a lățimii (numărul total de noduri frunză).
    Implementat recursiv pur funcțional.
    """
    if node is None:
        return 0
    if not node.children:
        return 1
    return sum(map(get_tree_leaf_count, node.children))


def get_max_branching_factor(node: Optional[Node]) -> int:
    """
    Gradul maxim de ramificare (numărul maxim de fii direcți ai unui nod).
    """
    if node is None:
        return 0
    direct = len(node.children)
    children_max = max(map(get_max_branching_factor, node.children)) if node.children else 0
    return max(direct, children_max)


# =============================================================================
# 10. NODURI PE UN ANUMIT NIVEL PORNIND DE LA UN NOD DAT (Punctul 7: 2 puncte)
# =============================================================================

def get_nodes_at_level(node: Optional[Node], target_level: int) -> Tuple[Node, ...]:
    """
    Returnează tuplul de noduri aflate la nivelul 'target_level',
    pornind de la nodul dat 'node' considerat ca fiind pe nivelul 1.
    
    Implementat prin recursivitate pură:
      - target_level < 1 sau node is None -> ()
      - target_level == 1 -> (node,)
      - target_level > 1 -> reuniunea nodurilor de pe nivelul (target_level - 1) din fiecare copil
    """
    if node is None or target_level < 1:
        return ()
    if target_level == 1:
        return (node,)
    return sum(map(lambda ch: get_nodes_at_level(ch, target_level - 1), node.children), ())


def count_nodes_at_level(node: Optional[Node], target_level: int) -> int:
    """
    Obține numărul de noduri de pe un anumit nivel, pornind de la un nod dat al arborelui.
    Implementat prin calcul direct recursiv fără alocare intermediară de liste:
      - target_level < 1 sau node is None -> 0
      - target_level == 1 -> 1
      - target_level > 1 -> suma apelurilor recursive pe copii pentru (target_level - 1)
    """
    if node is None or target_level < 1:
        return 0
    if target_level == 1:
        return 1
    return sum(map(lambda ch: count_nodes_at_level(ch, target_level - 1), node.children))


def count_all_levels(node: Optional[Node]) -> Tuple[Tuple[int, int], ...]:
    """
    Generează o statistică completă a distribuției nodurilor pe toate nivelurile arborelui:
    ((nivel_1, nr_noduri_1), (nivel_2, nr_noduri_2), ...)
    """
    if node is None:
        return ()
    depth = get_tree_depth(node)
    return tuple(map(lambda lvl: (lvl, count_nodes_at_level(node, lvl)), range(1, depth + 1)))


# =============================================================================
# 11. CATAMORFISM PE ARBORE (Tree Fold - Funcție de ordin superior generică)
# =============================================================================

def fold_tree(node: Optional[Node], fn: Callable[[Node, Tuple[Any, ...]], Any]) -> Any:
    """
    Catamorfism structural pentru arbore (fold_tree).
    Combină recursiv rezultatele din subarbori fără bucle și fără mutație.
    
    Exemple de utilizare:
      - Calcul adâncime: fold_tree(root, lambda n, ch: 1 + (max(ch) if ch else 0))
      - Număr total noduri: fold_tree(root, lambda n, ch: 1 + sum(ch))
    """
    if node is None:
        return None
    folded_children = tuple(map(lambda ch: fold_tree(ch, fn), node.children))
    return fn(node, folded_children)


# =============================================================================
# 12. DEMONSTRAȚIE ȘI TESTARE AUTOMATĂ
# =============================================================================

SAMPLE_XML = """<?xml version="1.0" encoding="UTF-8"?>
<!-- Catalogul Bibliotecii Tehnice - Document Demonstrativ -->
<catalog name="Biblioteca Tehnica" campus="Central">
    <section id="s1" domain="Informatica">
        <book id="b1" format="hardcover">
            <title>Structure and Interpretation of Computer Programs</title>
            <author country="USA">Harold Abelson</author>
            <author country="USA">Gerald Jay Sussman</author>
            <year>1996</year>
            <topics>
                <topic>Functional Programming</topic>
                <topic>Lisp</topic>
                <topic>Recursion</topic>
            </topics>
            <price currency="USD">65.00</price>
            <available status="yes" />
        </book>
        <book id="b2" format="paperback">
            <title>The C Programming Language</title>
            <author country="USA">Brian W. Kernighan</author>
            <author country="USA">Dennis M. Ritchie</author>
            <year>1988</year>
            <topics>
                <topic>Systems Programming</topic>
                <topic>Pointers</topic>
            </topics>
            <price currency="USD">45.50</price>
            <available status="yes" />
        </book>
    </section>
    <section id="s2" domain="Matematica">
        <book id="b3" format="hardcover">
            <title>Calculus and Analytic Geometry</title>
            <author country="Romania">Gheorghe Titeica</author>
            <year>1965</year>
            <topics>
                <topic>Analiza Matematica</topic>
                <topic>Geometrie</topic>
            </topics>
            <price currency="RON">55.00</price>
            <available status="no" />
        </book>
    </section>
</catalog>
"""


def run_unit_tests(tree: Node) -> None:
    """Verifică corectitudinea prin aserțiuni automate."""
    # 1. Verificare etichetă rădăcină
    assert tree.tag == "catalog", f"Așteptat root 'catalog', obținut '{tree.tag}'"
    assert tree.get_attr("name") == "Biblioteca Tehnica"

    # 2. Verificare căutare etichete
    books = find_all_by_tag(tree, "book")
    assert len(books) == 3, f"Așteptat 3 cărți, găsit {len(books)}"

    authors = find_all_by_tag(tree, "author")
    assert len(authors) == 5, f"Așteptat 5 autori, găsit {len(authors)}"

    first_author = find_first_by_tag(tree, "author")
    assert first_author is not None and first_author.text == "Harold Abelson"

    # 3. Verificare generare liste
    book_titles = tuple(map(lambda b: find_first_by_tag(b, "title").text, books))
    assert "Structure and Interpretation of Computer Programs" in book_titles
    assert len(book_titles) == 3

    # 4. Verificare adâncime
    depth = get_tree_depth(tree)
    # Ierarhie: catalog(1) -> section(2) -> book(3) -> topics(4) -> topic(5) => depth = 5
    assert depth == 5, f"Așteptat depth=5, obținut {depth}"

    # Verificare adâncime prin fold_tree
    depth_fold = fold_tree(tree, lambda n, ch: 1 + (max(ch) if ch else 0))
    assert depth == depth_fold

    # 5. Verificare număr noduri pe nivel pornind de la rădăcină
    assert count_nodes_at_level(tree, 1) == 1   # catalog
    assert count_nodes_at_level(tree, 2) == 2   # 2 section
    assert count_nodes_at_level(tree, 3) == 3   # 3 book
    # Nivel 4 conține fiii direcți ai cărților:
    #   book1 are: title, author, author, year, topics, price, available (7)
    #   book2 are: title, author, author, year, topics, price, available (7)
    #   book3 are: title, author, year, topics, price, available (6)
    #   Total nivel 4 = 7 + 7 + 6 = 20
    assert count_nodes_at_level(tree, 4) == 20
    # Nivel 5 conține copiii nodurilor 'topics':
    #   book1.topics: 3 topic
    #   book2.topics: 2 topic
    #   book3.topics: 2 topic
    #   Total nivel 5 = 3 + 2 + 2 = 7
    assert count_nodes_at_level(tree, 5) == 7
    assert count_nodes_at_level(tree, 6) == 0

    # 6. Verificare pornind de la un alt nod dat (ex: primul nod book)
    first_book = books[0]
    assert count_nodes_at_level(first_book, 1) == 1  # book1 însuși
    assert count_nodes_at_level(first_book, 2) == 7  # copiii săi direcți
    assert count_nodes_at_level(first_book, 3) == 3  # cei 3 copii din topics
    assert count_nodes_at_level(first_book, 4) == 0
    assert get_tree_depth(first_book) == 3

    # 7. Verificare lățime
    width = get_tree_width(tree)
    assert width == 20, f"Lățimea maximă așteptată la nivelul 4 este 20, obținut {width}"

    # 8. Verificare conversie ElementTree
    et_root = ET.fromstring(SAMPLE_XML)
    et_node = from_element_tree(et_root)
    assert get_tree_depth(et_node) == get_tree_depth(tree)
    assert len(find_all_by_tag(et_node, "book")) == len(books)


def main() -> None:
    print("=" * 80)
    print(" LABORATORUL 3: PARADIGMA FUNCȚIONALĂ - PARSER ȘI ARBORE XML")
    print("=" * 80)

    # Determinăm sursa XML (argument din linia de comandă sau conținutul demonstrativ)
    if len(sys.argv) > 1:
        filepath = sys.argv[1]
        print(f"[*] Se încarcă fișierul specificat: {filepath}")
        root = parse_xml_file(filepath)
    else:
        print("[*] Se procesează documentul XML demonstrativ intern...")
        root = parse_xml(SAMPLE_XML)

    print("\n" + "-" * 80)
    print("PUNCTUL 1 & 2: Elaborarea Parserului și Generarea Arborelui de Etichete")
    print("-" * 80)
    print(f"Rădăcina arborelui: <{root.tag}> cu atributele {root.attr_dict}")
    print(f"Număr de copii direcți ai rădăcinii: {len(root.children)}")
    print("\nStructura ierarhică a arborelui de etichete (primele 3 niveluri detaliate):")
    # Afișăm o porțiune din arbore
    for section in root.children:
        print(f"  <{section.tag} id=\"{section.get_attr('id')}\" domain=\"{section.get_attr('domain')}\">")
        for b in section.children:
            print(f"    <{b.tag} id=\"{b.get_attr('id')}\">")
            for prop in b.children:
                text_info = f" -> {prop.text}" if prop.text else ""
                attrs_info = f" {prop.attr_dict}" if prop.attributes else ""
                print(f"      <{prop.tag}{attrs_info}>{text_info}")

    print("\n" + "-" * 80)
    print("PUNCTUL 3: Căutarea unei etichete specifice în arborele de etichete")
    print("-" * 80)
    target_tag = "author"
    found_authors = find_all_by_tag(root, target_tag)
    print(f"Căutare după eticheta '{target_tag}':")
    print(f"  S-au găsit {len(found_authors)} apariții ale etichetei <{target_tag}>.")
    for i, a in enumerate(found_authors, 1):
        print(f"    [{i}] Autor: {a.text:35} (Țară: {a.get_attr('country')})")

    first_book = find_first_by_tag(root, "book")
    print(f"\nPrima apariție a etichetei 'book':")
    print(f"  ID: {first_book.get_attr('id')}, Format: {first_book.get_attr('format')}")

    print("\n" + "-" * 80)
    print("PUNCTUL 4: Generarea unei liste pe baza rezultatului căutării etichetei")
    print("-" * 80)
    # Generăm lista structurată
    structured_list = nodes_to_list(found_authors)
    print(f"Lista de dicționare generată pentru eticheta '{target_tag}':")
    for item in structured_list:
        print(f"  - {item}")

    # Generăm lista de conținuturi text
    texts_list = nodes_to_text_list(find_all_by_tag(root, "title"))
    print(f"\nLista titlurilor de cărți extrase (nodes_to_text_list):")
    for t in texts_list:
        print(f"  • \"{t}\"")

    # Generăm lista tuturor temelor (topics)
    topic_nodes = find_all_by_tag(root, "topic")
    topics_list = tuple(map(lambda n: n.text, topic_nodes))
    print(f"\nLista tuturor etichetelor <topic> ({len(topics_list)} elemente):")
    print(f"  {list(topics_list)}")

    print("\n" + "-" * 80)
    print("PUNCTUL 5: Obținerea adâncimii arborelui de etichete rezultat")
    print("-" * 80)
    total_depth = get_tree_depth(root)
    print(f"Adâncimea întregului arbore de etichete (de la <catalog>): {total_depth}")

    if first_book:
        book_depth = get_tree_depth(first_book)
        print(f"Adâncimea subarborelui extras pentru prima carte:      {book_depth}")

    # Calcul adâncime prin catamorfism (fold_tree)
    fold_depth = fold_tree(root, lambda n, ch: 1 + (max(ch) if ch else 0))
    print(f"Adâncime validată prin catamorfism funcțional (fold_tree): {fold_depth}")

    print("\n" + "-" * 80)
    print("PUNCTUL 6: Obținerea lățimii arborelui de etichete rezultat")
    print("-" * 80)
    tree_width = get_tree_width(root)
    leaf_count = get_tree_leaf_count(root)
    max_branch = get_max_branching_factor(root)
    print(f"Lățimea arborelui (numărul maxim de noduri pe un nivel):  {tree_width}")
    print(f"Numărul de noduri frunză (definiție alternativă lățime):  {leaf_count}")
    print(f"Gradul maxim de ramificare (număr maxim de fii direcți):  {max_branch}")

    if first_book:
        print(f"Lățimea subarborelui primei cărți:                        {get_tree_width(first_book)}")

    print("\n" + "-" * 80)
    print("PUNCTUL 7: Numărul de noduri de pe un anumit nivel, pornind de la un nod dat")
    print("-" * 80)
    print("Distribuția nodurilor pe niveluri pornind de la nodul RĂDĂCINĂ (<catalog>):")
    level_stats = count_all_levels(root)
    for lvl, count in level_stats:
        nodes = get_nodes_at_level(root, lvl)
        tags_sample = ", ".join(tuple(map(lambda n: n.tag, nodes))[:5])
        suffix = "..." if len(nodes) > 5 else ""
        print(f"  Nivelul {lvl}: {count:2} noduri [etichete: {tags_sample}{suffix}]")

    print("\nCalcul pornind de la un NOD DAT diferit de rădăcină (ex: secțiunea 'Informatica'):")
    section_info = find_first_by_tag(root, "section")
    if section_info:
        sec_depth = get_tree_depth(section_info)
        print(f"  Nod de start: <{section_info.tag} id=\"{section_info.get_attr('id')}\">")
        for lvl in range(1, sec_depth + 1):
            cnt = count_nodes_at_level(section_info, lvl)
            noduri = get_nodes_at_level(section_info, lvl)
            tags_repr = ", ".join(tuple(map(lambda n: n.tag, noduri)))
            print(f"    Nivelul {lvl} relativ: {cnt:2} noduri -> ({tags_repr})")

    print("\n" + "-" * 80)
    print("VERIFICARE TESTE AUTOMATE (ASSERTIONS)")
    print("-" * 80)
    run_unit_tests(root)
    print(">>> Toate testele și aserțiunile au trecut cu succes! Implementare validă 100%.")
    print("=" * 80)


if __name__ == "__main__":
    main()
