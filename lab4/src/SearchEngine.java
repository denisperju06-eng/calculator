/**
 * Interfață ce definește contractul pentru un motor de căutare web integrat.
 * Demonstrează principiul de Abstracție din OOP.
 */
public interface SearchEngine {

    /**
     * Returnează numele afișat al motorului de căutare (ex: "DuckDuckGo", "Google").
     */
    String getName();

    /**
     * Construiește URL-ul complet de căutare pe baza interogării primite de la utilizator.
     * @param query termenul sau fraza căutată
     * @return URL-ul formatat pentru căutare
     */
    String buildSearchUrl(String query);

    /**
     * Returnează URL-ul paginii principale a motorului de căutare.
     */
    String getHomePageUrl();

    /**
     * Returnează o scurtă descriere a motorului.
     */
    String getDescription();
}
