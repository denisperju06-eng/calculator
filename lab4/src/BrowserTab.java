import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Reprezintă un tab individual de navigare din browser.
 * Încorporează un JEditorPane pentru randarea paginilor HTML,
 * propriul istoric de navigare (Back/Forward), o bară de progres și un worker asincron.
 */
public class BrowserTab extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JEditorPane editorPane;
    private final JScrollPane scrollPane;
    private final JProgressBar progressBar;
    private final transient NavigationHistory navigationHistory;
    private final transient HistoryManager historyManager;
    private final transient FavoritesManager favoritesManager;
    private transient TabListener tabListener;

    private String currentTitle = "Pagină nouă";
    private transient PageLoaderWorker currentWorker = null;
    private boolean isLoading = false;

    private static final String DEFAULT_HOME_URL = "about:home";
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public BrowserTab(HistoryManager historyManager, FavoritesManager favoritesManager, TabListener tabListener) {
        super(new BorderLayout());
        this.historyManager = historyManager;
        this.favoritesManager = favoritesManager;
        this.tabListener = tabListener;
        this.navigationHistory = new NavigationHistory();

        // Bara de progres subtilă la începutul tab-ului
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setForeground(new Color(33, 150, 243));
        progressBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        add(progressBar, BorderLayout.NORTH);

        // Componenta de randare HTML (JEditorPane)
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");

        // Configurare kit HTML pentru randare mai curată și colorată
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 14px; margin: 16px; color: #202124; background-color: #ffffff; }");
        styleSheet.addRule("a { color: #1a0dab; text-decoration: underline; }");
        styleSheet.addRule("h1 { color: #1a237e; font-size: 22px; margin-bottom: 8px; font-weight: bold; }");
        styleSheet.addRule("h2 { color: #283593; font-size: 18px; margin-top: 16px; margin-bottom: 6px; font-weight: bold; }");
        styleSheet.addRule("h3 { color: #303f9f; font-size: 15px; margin-top: 12px; margin-bottom: 4px; font-weight: bold; }");
        styleSheet.addRule("code { background-color: #f1f3f4; padding: 2px 4px; font-family: monospace; font-size: 13px; color: #c2185b; }");
        styleSheet.addRule("table { border-collapse: collapse; width: 100%; margin: 12px 0; }");
        styleSheet.addRule("th, td { border: 1px solid #dadce0; padding: 8px; text-align: left; }");
        styleSheet.addRule("th { background-color: #f1f3f4; color: #202124; font-weight: bold; }");
        styleSheet.addRule(".result { margin-bottom: 18px; padding-bottom: 8px; border-bottom: 1px solid #e8eaed; }");
        styleSheet.addRule(".result__title { font-size: 18px; margin-bottom: 2px; }");
        styleSheet.addRule(".result__title a { color: #1a0dab; text-decoration: underline; font-weight: bold; }");
        styleSheet.addRule(".result__url { color: #006621; font-size: 12px; margin-bottom: 4px; }");
        styleSheet.addRule(".result__snippet { color: #4d5156; font-size: 13px; line-height: 1.4; }");
        styleSheet.addRule(".infobox { background-color: #f8f9fa; border: 1px solid #a2a9b1; padding: 8px; margin: 10px 0; }");
        editorPane.setEditorKit(kit);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        // Ascultător pentru navigarea pe hyperlink-uri
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String target = (e.getURL() != null) ? e.getURL().toString() : e.getDescription();
                if (target != null && !target.trim().isEmpty()) {
                    target = target.trim();
                    if (target.startsWith("system:")) {
                        try {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(new URI(target.substring(7)));
                            }
                        } catch (Exception ignored) {}
                        return;
                    }
                    if (!target.startsWith("http://") && !target.startsWith("https://") && !target.startsWith("file://") && !target.startsWith("about:")) {
                        target = resolveRelativeUrl(target);
                    }
                    loadUrl(target);
                }
            } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                String target = (e.getURL() != null) ? e.getURL().toString() : e.getDescription();
                if (tabListener != null) {
                    tabListener.onStatusMessage(this, target);
                }
            } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                if (tabListener != null) {
                    tabListener.onStatusMessage(this, "Gata");
                }
            }
        });

        scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Încărcăm pagina de start implicită
        loadUrl(DEFAULT_HOME_URL);
    }

    /**
     * Încarcă un URL specificat.
     */
    public void loadUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        String targetUrl = url.trim();

        // Oprim orice încărcare anterioară în curs
        stop();

        // Tratăm paginile speciale interne
        if (targetUrl.startsWith("about:")) {
            handleInternalPage(targetUrl);
            return;
        }

        // Asigurăm prefixul protocolului dacă este un domeniu valid
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://") && !targetUrl.startsWith("file://")) {
            targetUrl = "https://" + targetUrl;
        }

        // Actualizăm istoricul intern Back/Forward
        navigationHistory.navigateTo(targetUrl);
        notifyNavigationState();

        // Lansăm worker-ul asincron pentru încărcare
        setLoadingState(true);
        if (tabListener != null) {
            tabListener.onUrlChanged(this, targetUrl);
            tabListener.onStatusMessage(this, "Se conectează la " + targetUrl + "...");
        }

        currentWorker = new PageLoaderWorker(targetUrl);
        currentWorker.execute();
    }

    /**
     * Tratează paginile speciale interne precum about:home, about:history, about:help.
     */
    private void handleInternalPage(String internalUrl) {
        navigationHistory.navigateTo(internalUrl);
        notifyNavigationState();
        setLoadingState(false);

        String htmlContent;
        String title;

        switch (internalUrl.toLowerCase()) {
            case "about:home":
            case "about:start":
            case "about:blank":
                title = "Pagină de Start";
                htmlContent = generateHomePageHtml();
                break;
            case "about:history":
                title = "Istoric Navigare";
                htmlContent = generateHistoryPageHtml();
                break;
            case "about:favorites":
            case "about:bookmarks":
                title = "Favorite";
                htmlContent = generateFavoritesPageHtml();
                break;
            case "about:help":
                title = "Ajutor & Informații POO";
                htmlContent = generateHelpPageHtml();
                break;
            default:
                title = "Pagină Necunoscută";
                htmlContent = "<html><body><h1>Pagină internă necunoscută: " + escapeHtml(internalUrl) + "</h1></body></html>";
                break;
        }

        this.currentTitle = title;
        renderHtml(htmlContent, internalUrl);

        if (tabListener != null) {
            tabListener.onUrlChanged(this, internalUrl);
            tabListener.onTitleChanged(this, title);
            tabListener.onStatusMessage(this, "Pagină internă încărcată");
        }
    }

    /**
     * Oprește descărcarea paginii curente dacă aceasta se află în curs de încărcare.
     */
    public void stop() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
            currentWorker = null;
        }
        setLoadingState(false);
        if (tabListener != null) {
            tabListener.onStatusMessage(this, "Încărcare oprită de utilizator.");
        }
    }

    /**
     * Reîncarcă pagina curentă.
     */
    public void reload() {
        String current = navigationHistory.getCurrentUrl();
        if (current != null && !current.isEmpty()) {
            // Nu adăugăm reload-ul ca un nou pas în istoric
            if (current.startsWith("about:")) {
                handleInternalPage(current);
            } else {
                setLoadingState(true);
                if (tabListener != null) {
                    tabListener.onStatusMessage(this, "Reîmprospătare: " + current + "...");
                }
                currentWorker = new PageLoaderWorker(current);
                currentWorker.execute();
            }
        }
    }

    /**
     * Navighează la pagina anterioară din istoric (Back).
     */
    public void goBack() {
        if (navigationHistory.canGoBack()) {
            String prevUrl = navigationHistory.goBack();
            notifyNavigationState();
            loadUrlWithoutHistoryUpdate(prevUrl);
        }
    }

    /**
     * Navighează la pagina următoare din istoric (Forward).
     */
    public void goForward() {
        if (navigationHistory.canGoForward()) {
            String nextUrl = navigationHistory.goForward();
            notifyNavigationState();
            loadUrlWithoutHistoryUpdate(nextUrl);
        }
    }

    private void loadUrlWithoutHistoryUpdate(String targetUrl) {
        stop();
        if (targetUrl.startsWith("about:")) {
            handleInternalPage(targetUrl);
            return;
        }

        setLoadingState(true);
        if (tabListener != null) {
            tabListener.onUrlChanged(this, targetUrl);
            tabListener.onStatusMessage(this, "Navigare la " + targetUrl + "...");
        }

        currentWorker = new PageLoaderWorker(targetUrl);
        currentWorker.execute();
    }

    public boolean canGoBack() {
        return navigationHistory.canGoBack();
    }

    public boolean canGoForward() {
        return navigationHistory.canGoForward();
    }

    public String getCurrentUrl() {
        return navigationHistory.getCurrentUrl();
    }

    public String getTitle() {
        return currentTitle;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setTabListener(TabListener listener) {
        this.tabListener = listener;
    }

    private void setLoadingState(boolean loading) {
        this.isLoading = loading;
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        if (tabListener != null) {
            tabListener.onLoadingStateChanged(this, loading);
        }
    }

    private void notifyNavigationState() {
        if (tabListener != null) {
            tabListener.onNavigationStateChanged(this, canGoBack(), canGoForward());
        }
    }

    private void renderHtml(String html, String baseAddress) {
        try {
            if (html != null) {
                html = html.replaceAll("(?is)<script.*?</script>", "");
                html = html.replaceAll("(?is)<noscript.*?</noscript>", "");
            }
            HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKit();
            HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

            if (baseAddress != null && !baseAddress.startsWith("about:")) {
                try {
                    doc.setBase(new URI(baseAddress).toURL());
                } catch (Exception ignored) {}
            }

            java.io.StringReader reader = new java.io.StringReader(html != null ? html : "");
            kit.read(reader, doc, 0);
            editorPane.setDocument(doc);
            editorPane.setCaretPosition(0);
        } catch (Exception e) {
            editorPane.setText("<html><body><h2>Eroare la randare conținut HTML</h2><pre>" + escapeHtml(e.getMessage()) + "</pre></body></html>");
        }
    }

    private String resolveRelativeUrl(String href) {
        String current = navigationHistory.getCurrentUrl();
        if (current == null || current.startsWith("about:")) {
            return href;
        }
        try {
            URI base = new URI(current);
            URI resolved = base.resolve(href);
            return resolved.toString();
        } catch (Exception e) {
            return href;
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    // ==========================================
    // Generare Pagină HTML de Start (about:home)
    // ==========================================
    private String generateHomePageHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='text-align: center; padding: 20px; font-family: sans-serif;'>");

        sb.append("<div style='margin-top: 30px; margin-bottom: 20px;'>");
        sb.append("<h1 style='color: #1976D2; font-size: 32px;'>🌐 Internet Browser POO</h1>");
        sb.append("<p style='color: #666; font-size: 14px;'>Laborator 4 - Paradigma Orientată pe Obiecte (Java Swing)</p>");
        sb.append("</div>");

        // Caseta de comenzi rapide populare
        sb.append("<div style='margin: 25px auto; max-width: 600px; text-align: left; background-color: #f5f5f5; padding: 15px; border-radius: 8px;'>");
        sb.append("<h3 style='margin-top: 0; color: #333;'>🚀 Scurtături Rapide (Acces direct):</h3>");
        sb.append("<p>");
        sb.append("• <a href='https://html.duckduckgo.com'>DuckDuckGo HTML (Căutare ușoară)</a><br>");
        sb.append("• <a href='https://ro.wikipedia.org'>Wikipedia Română</a> | <a href='https://en.wikipedia.org'>Wikipedia English</a><br>");
        sb.append("• <a href='https://example.com'>Example Domain (Test HTTP simplu)</a><br>");
        sb.append("• <a href='https://docs.oracle.com/en/java/'>Java Official Documentation</a><br>");
        sb.append("• <a href='https://www.google.com'>Google Search</a> | <a href='https://www.bing.com'>Microsoft Bing</a>");
        sb.append("</p>");
        sb.append("</div>");

        // Informații despre funcționalitățile implementate (Cerințele laboratorului)
        sb.append("<div style='margin: 20px auto; max-width: 600px; text-align: left; background-color: #E3F2FD; padding: 15px; border-radius: 8px;'>");
        sb.append("<h3 style='margin-top: 0; color: #0D47A1;'>📋 Funcționalități Implementate conform Baremelor:</h3>");
        sb.append("<ul style='color: #333; line-height: 1.6;'>");
        sb.append("<li><b>a. Interfață generală cu bară de căutare + motor de căutare integrat:</b> bară URL inteligentă cu suport DuckDuckGo, Google, Bing, Wikipedia.</li>");
        sb.append("<li><b>b. Stop + Refresh:</b> butoane dedicate pe bară (🔄 / ⏹) cu scurtături tastatură (F5 / Esc).</li>");
        sb.append("<li><b>c. History:</b> istoric complet de navigare cu filtrare în timp real și persistență în <code>history.txt</code>.</li>");
        sb.append("<li><b>d. Favorites (salvate în fișier):</b> adăugare la favorite (⭐), bară de semne de carte și salvare în <code>favorites.txt</code>.</li>");
        sb.append("<li><b>e. Multe pagini (Tabs):</b> sistem complet multi-tab cu adăugare (+), închidere (✕) și comenzi rapide (Ctrl+T, Ctrl+W).</li>");
        sb.append("<li><b>f. Back + Forward:</b> stive independente de navigare pentru fiecare tab (⬅ / ➡).</li>");
        sb.append("</ul>");
        sb.append("</div>");

        // Link-uri interne
        sb.append("<div style='margin-top: 20px; font-size: 13px; color: #888;'>");
        sb.append("<a href='about:history'>📜 Vezi Istoricul</a> &nbsp;|&nbsp; ");
        sb.append("<a href='about:favorites'>⭐ Vezi Favoritele</a> &nbsp;|&nbsp; ");
        sb.append("<a href='about:help'>ℹ️ Ajutor & Scurtături</a>");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String generateHistoryPageHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='padding: 20px; font-family: sans-serif;'>");
        sb.append("<h1>📜 Istoricul de Navigare</h1>");
        sb.append("<p><a href='about:home'>⬅ Înapoi la Pagina Principală</a></p>");

        if (historyManager == null || historyManager.getHistory().isEmpty()) {
            sb.append("<p><i>Istoricul de navigare este momentan gol.</i></p>");
        } else {
            sb.append("<table>");
            sb.append("<tr><th>Dată & Oră</th><th>Titlu Pagină</th><th>URL</th></tr>");
            for (HistoryItem item : historyManager.getHistory()) {
                sb.append("<tr>");
                sb.append("<td>").append(item.getFormattedTimestamp()).append("</td>");
                sb.append("<td>").append(escapeHtml(item.getTitle())).append("</td>");
                sb.append("<td><a href='").append(escapeHtml(item.getUrl())).append("'>").append(escapeHtml(item.getUrl())).append("</a></td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String generateFavoritesPageHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='padding: 20px; font-family: sans-serif;'>");
        sb.append("<h1>⭐ Pagini Favorite (Bookmarks)</h1>");
        sb.append("<p><a href='about:home'>⬅ Înapoi la Pagina Principală</a></p>");

        if (favoritesManager == null || favoritesManager.getFavorites().isEmpty()) {
            sb.append("<p><i>Nu există pagini salvate la favorite.</i></p>");
        } else {
            sb.append("<table>");
            sb.append("<tr><th>Titlu Semn de Carte</th><th>URL</th><th>Data Adăugării</th></tr>");
            for (FavoriteItem fav : favoritesManager.getFavorites()) {
                sb.append("<tr>");
                sb.append("<td><b>").append(escapeHtml(fav.getTitle())).append("</b></td>");
                sb.append("<td><a href='").append(escapeHtml(fav.getUrl())).append("'>").append(escapeHtml(fav.getUrl())).append("</a></td>");
                sb.append("<td>").append(fav.getFormattedDateAdded()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String generateHelpPageHtml() {
        return "<html><body style='padding: 20px; font-family: sans-serif;'>"
             + "<h1>ℹ️ Ajutor & Scurtături Internet Browser POO</h1>"
             + "<p><a href='about:home'>⬅ Înapoi la Pagina Principală</a></p>"
             + "<h2>Comenzi Rapide din Tastatură:</h2>"
             + "<ul>"
             + "<li><b>Ctrl + T / Cmd + T:</b> Deschide un tab nou</li>"
             + "<li><b>Ctrl + W / Cmd + W:</b> Închide tab-ul curent</li>"
             + "<li><b>Ctrl + L / Cmd + L:</b> Selectează bara de adrese</li>"
             + "<li><b>Ctrl + R / Cmd + R / F5:</b> Reîmprospătează pagina (Refresh)</li>"
             + "<li><b>Escape:</b> Oprește încărcarea paginii (Stop)</li>"
             + "<li><b>Ctrl + D / Cmd + D:</b> Adaugă pagina curentă la Favorite</li>"
             + "<li><b>Ctrl + H / Cmd + H:</b> Deschide fereastra cu Istoricul</li>"
             + "<li><b>Alt + Săgeată Stânga:</b> Navigare Înapoi (Back)</li>"
             + "<li><b>Alt + Săgeată Dreapta:</b> Navigare Înainte (Forward)</li>"
             + "</ul>"
             + "<h2>Arhitectură OOP:</h2>"
             + "<p>Aplicația respectă principiile fundamentale ale POO:</p>"
             + "<ul>"
             + "<li><b>Încapsulare:</b> Clase dedicate (<code>NavigationHistory</code>, <code>HistoryItem</code>, <code>FavoriteItem</code>) cu atribute private și metode specifice.</li>"
             + "<li><b>Abstracție & Polimorfism:</b> Interfețele <code>SearchEngine</code>, <code>TabListener</code> și <code>FavoritesListener</code>.</li>"
             + "<li><b>Separarea Responsabilităților:</b> Managementul de date (<code>FavoritesManager</code>, <code>HistoryManager</code>) separat de interfața grafică (<code>BrowserWindow</code>, <code>BrowserTab</code>).</li>"
             + "</ul>"
             + "</body></html>";
    }

    private String generateErrorHtml(String url, String errorMsg) {
        return "<html><body style='padding: 30px; font-family: sans-serif; text-align: center;'>"
             + "<div style='background-color: #FFEBEE; border: 1px solid #FFCDD2; padding: 25px; border-radius: 8px; max-width: 650px; margin: 0 auto; text-align: left;'>"
             + "<h2 style='color: #C62828; margin-top: 0;'>⚠️ Nu s-a putut încărca pagina</h2>"
             + "<p><b>URL solicitat:</b> <code>" + escapeHtml(url) + "</code></p>"
             + "<p><b>Detalii eroare:</b> <span style='color: #D32F2F;'>" + escapeHtml(errorMsg) + "</span></p>"
             + "<hr style='border: 0; border-top: 1px solid #FFCDD2; margin: 15px 0;'>"
             + "<p><b>Sugestii:</b></p>"
             + "<ul>"
             + "<li>Verificați dacă adresa URL a fost introdusă corect.</li>"
             + "<li>Verificați conexiunea la internet a calculatorului.</li>"
             + "<li>Încercați să reîmprospătați pagina (butonul 🔄 Refresh sau F5).</li>"
             + "<li>Căutați pe <a href='https://html.duckduckgo.com/html/?q=" + escapeHtml(url) + "'>DuckDuckGo</a>.</li>"
             + "</ul>"
             + "<p style='margin-top: 20px;'><a href='about:home'>🏠 Înapoi la Pagina de Start</a></p>"
             + "</div>"
             + "</body></html>";
    }

    // =======================================================
    // SwingWorker pentru descărcare și încărcare asincronă
    // =======================================================
    private class PageLoaderWorker extends SwingWorker<PageLoadResult, Void> {

        private final String urlToLoad;

        public PageLoaderWorker(String urlToLoad) {
            this.urlToLoad = urlToLoad;
        }

        @Override
        protected PageLoadResult doInBackground() {
            try {
                String searchQ = extractSearchQuery(urlToLoad);
                if (searchQ != null && !searchQ.trim().isEmpty()) {
                    return buildGlobalSearchResults(searchQ.trim());
                }

                URL url = new URI(urlToLoad).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(25000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);

                // Setăm anteturi HTTP de browser real pentru a evita răspunsuri 403 Forbidden
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                conn.setRequestProperty("Accept-Language", "ro,en-US;q=0.9,en;q=0.8");
                conn.setRequestProperty("Accept-Encoding", "identity");

                int responseCode = conn.getResponseCode();

                // Tratăm redirect-uri (301, 302, 303, 307, 308)
                if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307 || responseCode == 308) {
                    String newLoc = conn.getHeaderField("Location");
                    if (newLoc != null && !newLoc.trim().isEmpty()) {
                        try {
                            newLoc = url.toURI().resolve(newLoc.trim()).toString();
                        } catch (Exception ignored) {}
                        return new PageLoadResult(newLoc, null, null, true);
                    }
                }

                if (responseCode >= 400) {
                    return new PageLoadResult(urlToLoad, "Eroare HTTP " + responseCode + ": " + conn.getResponseMessage(), false);
                }

                // Citim conținutul paginii cu suport pentru decompresie gzip dacă serverul o forțează
                String encoding = conn.getContentEncoding();
                java.io.InputStream inStream = conn.getInputStream();
                if ("gzip".equalsIgnoreCase(encoding)) {
                    inStream = new java.util.zip.GZIPInputStream(inStream);
                    encoding = "UTF-8";
                }
                if (encoding == null || "identity".equalsIgnoreCase(encoding)) {
                    encoding = "UTF-8";
                }

                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream, encoding))) {
                    String line;
                    while (!isCancelled() && (line = in.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                if (isCancelled()) {
                    return null;
                }

                String html = content.toString();

                // Extragem titlul paginii
                String title = urlToLoad;
                Matcher matcher = TITLE_PATTERN.matcher(html);
                if (matcher.find()) {
                    title = matcher.group(1).replaceAll("<[^>]*>", "").trim();
                }

                return new PageLoadResult(urlToLoad, title, html, false);

            } catch (Exception e) {
                return new PageLoadResult(urlToLoad, e.getMessage(), false);
            }
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                setLoadingState(false);
                return;
            }

            try {
                PageLoadResult result = get();
                if (result == null) {
                    setLoadingState(false);
                    return;
                }

                if (result.isRedirect) {
                    setLoadingState(false);
                    loadUrl(result.url);
                    return;
                }

                if (result.isSuccess) {
                    currentTitle = result.title;
                    renderHtml(result.htmlContent, result.url);

                    // Înregistrăm vizita în istoricul global
                    if (historyManager != null) {
                        historyManager.addEntry(result.url, result.title);
                    }

                    if (tabListener != null) {
                        tabListener.onTitleChanged(BrowserTab.this, currentTitle);
                        tabListener.onStatusMessage(BrowserTab.this, "Pagina s-a încărcat cu succes.");
                    }
                } else {
                    currentTitle = "Eroare la încărcare";
                    renderHtml(generateErrorHtml(result.url, result.errorMessage), result.url);
                    if (tabListener != null) {
                        tabListener.onTitleChanged(BrowserTab.this, currentTitle);
                        tabListener.onStatusMessage(BrowserTab.this, "Eroare: " + result.errorMessage);
                    }
                }
            } catch (Exception e) {
                renderHtml(generateErrorHtml(urlToLoad, e.getMessage()), urlToLoad);
            } finally {
                setLoadingState(false);
                notifyNavigationState();
            }
        }

        private String extractSearchQuery(String url) {
            if (url == null) return null;
            if (url.contains("duckduckgo.com") || url.contains("google.com/search") || url.contains("bing.com/search")) {
                try {
                    URI u = new URI(url);
                    String q = u.getRawQuery();
                    if (q != null) {
                        for (String part : q.split("&")) {
                            if (part.startsWith("q=") || part.startsWith("search=")) {
                                String[] kv = part.split("=", 2);
                                if (kv.length > 1) {
                                    return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
            return null;
        }

        private PageLoadResult buildGlobalSearchResults(String query) {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String heading = "";
            String abstractText = "";
            String abstractUrl = "";
            List<String> relatedLinks = new ArrayList<>();
            List<String> wikiTitles = new ArrayList<>();
            List<String> wikiUrls = new ArrayList<>();

            // 1. DuckDuckGo Instant Answer API
            try {
                URL ddgUrl = new URI("https://api.duckduckgo.com/?q=" + encoded + "&format=json").toURL();
                HttpURLConnection conn = (HttpURLConnection) ddgUrl.openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
                if (conn.getResponseCode() == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String l;
                        while ((l = in.readLine()) != null) sb.append(l);
                        String raw = sb.toString();

                        Matcher mHead = Pattern.compile("\"Heading\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(raw);
                        if (mHead.find()) heading = unescapeJson(mHead.group(1));

                        Matcher mAbs = Pattern.compile("\"AbstractText\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(raw);
                        if (mAbs.find()) abstractText = unescapeJson(mAbs.group(1));

                        Matcher mUrl = Pattern.compile("\"AbstractURL\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(raw);
                        if (mUrl.find()) abstractUrl = unescapeJson(mUrl.group(1));

                        Matcher mRel = Pattern.compile("\"FirstURL\":\\s*\"([^\"]+)\",\\s*\"Result\":\\s*\"[^\"]*\",\\s*\"Text\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(raw);
                        while (mRel.find() && relatedLinks.size() < 6) {
                            relatedLinks.add("<p style='margin: 4px 0;'>• <a href='" + mRel.group(1) + "'>" + escapeHtml(unescapeJson(mRel.group(2))) + "</a></p>");
                        }
                    }
                }
            } catch (Exception ignored) {}

            // 2. Wikipedia OpenSearch API
            try {
                URL wikiUrl = new URI("https://ro.wikipedia.org/w/api.php?action=opensearch&search=" + encoded + "&limit=6&format=json").toURL();
                HttpURLConnection conn = (HttpURLConnection) wikiUrl.openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
                if (conn.getResponseCode() == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String l;
                        while ((l = in.readLine()) != null) sb.append(l);
                        String raw = sb.toString();

                        Matcher mWiki = Pattern.compile("\"(https://[^\"]+wikipedia\\.org/wiki/[^\"]+)\"").matcher(raw);
                        while (mWiki.find() && wikiUrls.size() < 6) {
                            String wUrl = mWiki.group(1);
                            wikiUrls.add(wUrl);
                            String titleFromUrl = wUrl.substring(wUrl.lastIndexOf('/') + 1).replace('_', ' ');
                            try {
                                titleFromUrl = java.net.URLDecoder.decode(titleFromUrl, StandardCharsets.UTF_8);
                            } catch (Exception ignored) {}
                            wikiTitles.add(titleFromUrl);
                        }
                    }
                }
            } catch (Exception ignored) {}

            // Construire pagină HTML structurată cu rezultate reale
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='padding: 20px; font-family: sans-serif; background-color: #ffffff;'>");
            sb.append("<div style='background-color: #f1f3f4; padding: 16px; border-radius: 8px; margin-bottom: 20px;'>");
            sb.append("<h1 style='margin: 0; color: #1a73e8; font-size: 24px;'>🔍 Rezultate Căutare Globală: ").append(escapeHtml(query)).append("</h1>");
            sb.append("<p style='margin: 4px 0 0 0; color: #5f6368; font-size: 13px;'>Interogare web în timp real</p>");
            sb.append("</div>");

            // Card Site Oficial dacă este domeniu popular (ex: youtube, google, wikipedia, github, reddit)
            String lowerQ = query.toLowerCase().trim();
            if (lowerQ.equals("youtube") || lowerQ.equals("youtube.com")) {
                sb.append("<div style='background: #FFF8E1; border: 1px solid #FFE082; border-radius: 8px; padding: 14px; margin-bottom: 18px;'>");
                sb.append("<h2 style='margin: 0 0 6px 0; color: #D32F2F;'>▶️ YouTube (Site Oficial)</h2>");
                sb.append("<p style='margin: 0 0 8px 0; color: #006621; font-weight: bold;'>https://www.youtube.com</p>");
                sb.append("<p style='margin: 0 0 10px 0;'>Cea mai mare platformă video din lume. Pentru redare video HD/4K cu sunet complet:</p>");
                sb.append("<p><a href='system:https://www.youtube.com' style='background-color: #D32F2F; color: white; padding: 6px 14px; text-decoration: none; border-radius: 4px; font-weight: bold;'>🌐 Deschide YouTube în Safari / Chrome (Video complet)</a></p>");
                sb.append("</div>");
            } else if (lowerQ.equals("google") || lowerQ.equals("google.com")) {
                sb.append("<div style='background: #E8F0FE; border: 1px solid #C2E7FF; border-radius: 8px; padding: 14px; margin-bottom: 18px;'>");
                sb.append("<h2 style='margin: 0 0 6px 0; color: #1a73e8;'>🌐 Google (Site Oficial)</h2>");
                sb.append("<p style='margin: 0 0 8px 0; color: #006621; font-weight: bold;'>https://www.google.com</p>");
                sb.append("<p><a href='https://www.google.com'>Accesează Google.com</a> &nbsp;|&nbsp; <a href='system:https://www.google.com'>Deschide în Safari / Chrome</a></p>");
                sb.append("</div>");
            } else if (lowerQ.equals("github") || lowerQ.equals("github.com")) {
                sb.append("<div style='background: #F6F8FA; border: 1px solid #D0D7DE; border-radius: 8px; padding: 14px; margin-bottom: 18px;'>");
                sb.append("<h2 style='margin: 0 0 6px 0; color: #24292F;'>🐙 GitHub (Platformă Dezvoltatori)</h2>");
                sb.append("<p style='margin: 0 0 8px 0; color: #006621; font-weight: bold;'>https://github.com</p>");
                sb.append("<p><a href='https://github.com'>Accesează GitHub</a> &nbsp;|&nbsp; <a href='system:https://github.com'>Deschide în Safari / Chrome</a></p>");
                sb.append("</div>");
            }

            // Card Enciclopedic / Instant Answer
            if (!abstractText.isEmpty()) {
                sb.append("<div style='border: 1px solid #dadce0; border-radius: 8px; padding: 16px; margin-bottom: 20px; background-color: #ffffff;'>");
                sb.append("<h2 style='margin-top: 0; color: #202124;'>").append(escapeHtml(heading.isEmpty() ? query : heading)).append("</h2>");
                sb.append("<p style='line-height: 1.6; color: #3c4043; font-size: 14px;'>").append(escapeHtml(abstractText)).append("</p>");
                if (!abstractUrl.isEmpty()) {
                    sb.append("<p><a href='").append(escapeHtml(abstractUrl)).append("'>📖 Citește articolul complet pe Wikipedia</a></p>");
                }
                sb.append("</div>");
            }

            // Rezultate Articole Wikipedia
            if (!wikiUrls.isEmpty()) {
                sb.append("<h3 style='color: #202124; border-bottom: 2px solid #1a73e8; padding-bottom: 4px;'>Articole Web & Wikipedia Relevante:</h3>");
                for (int i = 0; i < wikiUrls.size(); i++) {
                    String wUrl = wikiUrls.get(i);
                    String wTitle = (i < wikiTitles.size()) ? wikiTitles.get(i) : wUrl;
                    sb.append("<div class='result' style='margin-bottom: 14px;'>");
                    sb.append("<div class='result__title'><a href='").append(escapeHtml(wUrl)).append("'>").append(escapeHtml(wTitle)).append("</a></div>");
                    sb.append("<div class='result__url'>").append(escapeHtml(wUrl)).append("</div>");
                    sb.append("</div>");
                }
            }

            // Teme corelate
            if (!relatedLinks.isEmpty()) {
                sb.append("<h3 style='color: #202124; margin-top: 20px;'>Teme Corelate:</h3>");
                for (String rel : relatedLinks) {
                    sb.append(rel);
                }
            }

            sb.append("<hr style='border: 0; border-top: 1px solid #dadce0; margin: 25px 0;'>");
            sb.append("<p style='font-size: 13px; color: #5f6368;'>");
            sb.append("Opțiuni externe: <a href='system:https://www.google.com/search?q=").append(encoded).append("'>Caută pe Google în Browserul de Sistem</a> | ");
            sb.append("<a href='system:https://www.bing.com/search?q=").append(encoded).append("'>Caută pe Bing</a>");
            sb.append("</p>");
            sb.append("</body></html>");

            return new PageLoadResult(urlToLoad, "Căutare: " + query, sb.toString(), false);
        }

        private String unescapeJson(String text) {
            if (text == null) return "";
            return text.replace("\\\"", "\"")
                       .replace("\\\\", "\\")
                       .replace("\\/", "/")
                       .replace("\\n", " ")
                       .replace("\\t", " ");
        }
    }

    private static class PageLoadResult {
        final String url;
        final String title;
        final String htmlContent;
        final String errorMessage;
        final boolean isSuccess;
        final boolean isRedirect;

        // Succes
        PageLoadResult(String url, String title, String htmlContent, boolean isRedirect) {
            this.url = url;
            this.title = title;
            this.htmlContent = htmlContent;
            this.errorMessage = null;
            this.isSuccess = !isRedirect;
            this.isRedirect = isRedirect;
        }

        // Eroare
        PageLoadResult(String url, String errorMessage, boolean isSuccess) {
            this.url = url;
            this.title = "Eroare";
            this.htmlContent = null;
            this.errorMessage = errorMessage;
            this.isSuccess = false;
            this.isRedirect = false;
        }
    }
}
