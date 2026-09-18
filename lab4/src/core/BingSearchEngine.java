package core;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementare a motorului de căutare Microsoft Bing.
 */
public class BingSearchEngine implements SearchEngine {

    @Override
    public String getName() {
        return "Bing";
    }

    @Override
    public String buildSearchUrl(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return "https://www.bing.com/search?q=" + encoded;
    }

    @Override
    public String getHomePageUrl() {
        return "https://www.bing.com";
    }

    @Override
    public String getDescription() {
        return "Motorul de căutare dezvoltat de Microsoft";
    }

    @Override
    public String toString() {
        return getName();
    }
}
