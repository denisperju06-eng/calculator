import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementare a motorului de căutare DuckDuckGo.
 * Folosește versiunea HTML ușoară a DuckDuckGo, optimă pentru randare în JEditorPane.
 */
public class DuckDuckGoSearchEngine implements SearchEngine {

    @Override
    public String getName() {
        return "DuckDuckGo";
    }

    @Override
    public String buildSearchUrl(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        // Versiunea HTML este simplificată și compatibilă cu componentele HTML3/Swing
        return "https://html.duckduckgo.com/html/?q=" + encoded;
    }

    @Override
    public String getHomePageUrl() {
        return "https://duckduckgo.com";
    }

    @Override
    public String getDescription() {
        return "Motor de căutare axat pe confidențialitate (HTML ușor)";
    }

    @Override
    public String toString() {
        return getName();
    }
}
