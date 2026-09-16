import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementare a motorului de căutare Wikipedia.
 */
public class WikipediaSearchEngine implements SearchEngine {

    @Override
    public String getName() {
        return "Wikipedia";
    }

    @Override
    public String buildSearchUrl(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return "https://en.wikipedia.org/wiki/Special:Search?search=" + encoded;
    }

    @Override
    public String getHomePageUrl() {
        return "https://en.wikipedia.org";
    }

    @Override
    public String getDescription() {
        return "Căutare în enciclopedia liberă Wikipedia";
    }

    @Override
    public String toString() {
        return getName();
    }
}
