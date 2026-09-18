package core;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementare a motorului de căutare Google.
 */
public class GoogleSearchEngine implements SearchEngine {

    @Override
    public String getName() {
        return "Google";
    }

    @Override
    public String buildSearchUrl(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return "https://www.google.com/search?q=" + encoded + "&gbv=1";
    }

    @Override
    public String getHomePageUrl() {
        return "https://www.google.com";
    }

    @Override
    public String getDescription() {
        return "Cel mai popular motor de căutare web";
    }

    @Override
    public String toString() {
        return getName();
    }
}
