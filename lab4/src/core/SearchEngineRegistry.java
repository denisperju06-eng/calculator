package core;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registru pentru gestionarea motoarelor de căutare disponibile în browser.
 * Oferă punct centralizat de configurare și selecție a motorului activ.
 */
public class SearchEngineRegistry {

    private final List<SearchEngine> engines;
    private SearchEngine defaultEngine;

    public SearchEngineRegistry() {
        engines = new ArrayList<>();

        SearchEngine duckDuckGo = new DuckDuckGoSearchEngine();
        SearchEngine google = new GoogleSearchEngine();
        SearchEngine bing = new BingSearchEngine();
        SearchEngine wikipedia = new WikipediaSearchEngine();

        engines.add(duckDuckGo);
        engines.add(google);
        engines.add(bing);
        engines.add(wikipedia);

        // DuckDuckGo este implicit deoarece versiunea sa HTML este ușor de randat în JEditorPane
        defaultEngine = duckDuckGo;
    }

    public List<SearchEngine> getEngines() {
        return Collections.unmodifiableList(engines);
    }

    public void addEngine(SearchEngine engine) {
        if (engine != null && !engines.contains(engine)) {
            engines.add(engine);
        }
    }

    public SearchEngine getDefaultEngine() {
        return defaultEngine;
    }

    public void setDefaultEngine(SearchEngine engine) {
        if (engine != null && engines.contains(engine)) {
            this.defaultEngine = engine;
        }
    }

    public SearchEngine getEngineByName(String name) {
        for (SearchEngine engine : engines) {
            if (engine.getName().equalsIgnoreCase(name)) {
                return engine;
            }
        }
        return defaultEngine;
    }
}
