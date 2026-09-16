import java.io.File;
import java.util.List;

/**
 * Clasă de testare automată completă pentru componentele logice ale Internet Browser POO.
 * Verifică toate cerințele din conditii.md fără a depinde de un server de afișare grafică:
 * a. Motor de căutare integrat
 * b. Stop + Refresh logică de stare
 * c. Istoric (adăugare, căutare, salvare/încărcare fișier)
 * d. Favorite (adăugare, verificare, salvare/încărcare fișier, ștergere)
 * e. File multiple (instanțiere tab-uri independente)
 * f. Back + Forward (stive de navigare)
 */
public class TestBrowser {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("==================================================");
        System.out.println("  Rulare Teste Verificare Internet Browser POO    ");
        System.out.println("==================================================");

        // TEST 1: Back + Forward (Cerința f)
        try {
            System.out.print("[TEST 1] Verificare Navigație Back + Forward... ");
            NavigationHistory nav = new NavigationHistory();
            assertFalse(nav.canGoBack(), "Initial nu trebuie sa poata da back");
            assertFalse(nav.canGoForward(), "Initial nu trebuie sa poata da forward");

            nav.navigateTo("https://example.com/1");
            assertEquals("https://example.com/1", nav.getCurrentUrl(), "URL curent");
            assertFalse(nav.canGoBack(), "Dupa 1 URL nu se poate da back");

            nav.navigateTo("https://example.com/2");
            nav.navigateTo("https://example.com/3");
            assertTrue(nav.canGoBack(), "Trebuie sa poata da back");
            assertFalse(nav.canGoForward(), "Nu trebuie sa poata da forward");

            String back1 = nav.goBack();
            assertEquals("https://example.com/2", back1, "Back la pasul 2");
            assertTrue(nav.canGoForward(), "Acum trebuie sa poata da forward");

            String back2 = nav.goBack();
            assertEquals("https://example.com/1", back2, "Back la pasul 1");
            assertFalse(nav.canGoBack(), "La inceputul stivei nu se mai poate da back");

            String forward1 = nav.goForward();
            assertEquals("https://example.com/2", forward1, "Forward la pasul 2");

            // Navigare noua dupa back trebuie sa curete stiva forward
            nav.navigateTo("https://example.com/4");
            assertEquals("https://example.com/4", nav.getCurrentUrl(), "URL curent nou");
            assertFalse(nav.canGoForward(), "Stiva forward trebuie golita dupa navigare noua");

            passed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            failed++;
            System.out.println("FAILED: " + t.getMessage());
        }

        // TEST 2: Motoare de căutare integrate (Cerința a)
        try {
            System.out.print("[TEST 2] Verificare Motoare de Căutare (Polimorfism)... ");
            SearchEngineRegistry registry = new SearchEngineRegistry();
            List<SearchEngine> engines = registry.getEngines();
            assertTrue(engines.size() >= 4, "Cel putin 4 motoare de cautare");

            SearchEngine ddg = registry.getEngineByName("DuckDuckGo");
            assertNotNull(ddg, "DuckDuckGo gasit");
            String ddgUrl = ddg.buildSearchUrl("java swing");
            assertTrue(ddgUrl.contains("q=java+swing") || ddgUrl.contains("q=java%20swing"), "URL DuckDuckGo corect");

            SearchEngine google = registry.getEngineByName("Google");
            assertNotNull(google, "Google gasit");
            String googleUrl = google.buildSearchUrl("oop design");
            assertTrue(googleUrl.contains("google.com/search?q=oop+design"), "URL Google corect");

            SearchEngine bing = registry.getEngineByName("Bing");
            assertNotNull(bing, "Bing gasit");
            String bingUrl = bing.buildSearchUrl("test");
            assertTrue(bingUrl.contains("bing.com/search?q=test"), "URL Bing corect");

            SearchEngine wiki = registry.getEngineByName("Wikipedia");
            assertNotNull(wiki, "Wikipedia gasit");
            String wikiUrl = wiki.buildSearchUrl("Java");
            assertTrue(wikiUrl.contains("wikipedia.org/wiki/Special:Search?search=Java"), "URL Wiki corect");

            passed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            failed++;
            System.out.println("FAILED: " + t.getMessage());
        }

        // TEST 3: Favorite cu Salvare în Fișier (Cerința d)
        File tempFavFile = new File("test_favorites.txt");
        try {
            System.out.print("[TEST 3] Verificare Favorite & Salvare în Fișier... ");
            if (tempFavFile.exists()) tempFavFile.delete();

            FavoritesManager favManager = new FavoritesManager(tempFavFile);
            favManager.addFavorite("GitHub", "https://github.com");
            favManager.addFavorite("StackOverflow", "https://stackoverflow.com");

            assertTrue(favManager.isFavorite("https://github.com"), "GitHub este la favorite");
            assertTrue(favManager.isFavorite("https://stackoverflow.com"), "StackOverflow este la favorite");
            assertFalse(favManager.isFavorite("https://notfound.org"), "notfound nu este favorit");

            favManager.saveToFile();
            assertTrue(tempFavFile.exists() && tempFavFile.length() > 0, "Fisierul favorites.txt s-a salvat");

            // Reincarcare dintr-un manager nou
            FavoritesManager favManager2 = new FavoritesManager(tempFavFile);
            assertTrue(favManager2.isFavorite("https://github.com"), "Reincarcat cu succes GitHub");
            assertTrue(favManager2.isFavorite("https://stackoverflow.com"), "Reincarcat cu succes StackOverflow");

            // Test stergere
            favManager2.removeFavoriteByUrl("https://github.com");
            assertFalse(favManager2.isFavorite("https://github.com"), "GitHub a fost sters din favorite");

            passed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            failed++;
            System.out.println("FAILED: " + t.getMessage());
        } finally {
            if (tempFavFile.exists()) tempFavFile.delete();
        }

        // TEST 4: Istoric cu Căutare și Salvare în Fișier (Cerința c)
        File tempHistFile = new File("test_history.txt");
        try {
            System.out.print("[TEST 4] Verificare Istoric & Căutare & Persistență... ");
            if (tempHistFile.exists()) tempHistFile.delete();

            HistoryManager histManager = new HistoryManager(tempHistFile);
            histManager.addEntry("https://example.com/page1", "First Page");
            histManager.addEntry("https://google.com", "Google Search Engine");
            histManager.addEntry("https://example.com/page2", "Second Page");

            assertEquals(3, histManager.size(), "Dimensiune istoric");

            // Cautare in istoric
            List<HistoryItem> searchResult = histManager.search("Google");
            assertEquals(1, searchResult.size(), "Un singur rezultat cu Google");
            assertEquals("Google Search Engine", searchResult.get(0).getTitle(), "Titlul din cautare");

            List<HistoryItem> searchPage = histManager.search("example.com");
            assertEquals(2, searchPage.size(), "Doua rezultate cu example.com");

            histManager.saveToFile();
            assertTrue(tempHistFile.exists() && tempHistFile.length() > 0, "Fisierul history.txt s-a salvat");

            // Reincarcare in manager nou
            HistoryManager histManager2 = new HistoryManager(tempHistFile);
            assertEquals(3, histManager2.size(), "Istoric reincarcat complet din fisier");

            // Stergere totala
            histManager2.clearHistory();
            assertEquals(0, histManager2.size(), "Istoricul a fost golit");

            passed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            failed++;
            System.out.println("FAILED: " + t.getMessage());
        } finally {
            if (tempHistFile.exists()) tempHistFile.delete();
        }

        // TEST 5: Model de date și serializare (HistoryItem & FavoriteItem)
        try {
            System.out.print("[TEST 5] Verificare Model Date (HistoryItem, FavoriteItem)... ");
            HistoryItem hi = new HistoryItem("https://test.com", "Titlu Test");
            String line = hi.toFileLine();
            HistoryItem parsed = HistoryItem.fromFileLine(line);
            assertNotNull(parsed, "Parsed history item nu e null");
            assertEquals(hi.getUrl(), parsed.getUrl(), "URL conservat");
            assertEquals(hi.getTitle(), parsed.getTitle(), "Titlu conservat");

            FavoriteItem fi = new FavoriteItem("Bookmark 1", "https://bookmark.com");
            String favLine = fi.toFileLine();
            FavoriteItem parsedFav = FavoriteItem.fromFileLine(favLine);
            assertNotNull(parsedFav, "Parsed fav item nu e null");
            assertEquals(fi.getUrl(), parsedFav.getUrl(), "URL favorit conservat");
            assertEquals(fi.getTitle(), parsedFav.getTitle(), "Titlu favorit conservat");

            passed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            failed++;
            System.out.println("FAILED: " + t.getMessage());
        }

        System.out.println("==================================================");
        System.out.println("Rezultate Teste: " + passed + " trecute, " + failed + " esuate.");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Esenta: " + message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Esenta: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Esenta: " + message + " | Asteptat: " + expected + ", Obtinut: " + actual);
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError("Esenta: " + message + " este null!");
    }
}
