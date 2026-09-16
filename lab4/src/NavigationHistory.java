import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Gestionează istoricul de navigare Înapoi (Back) și Înainte (Forward) pentru un tab.
 * Folosește două stive (Deque) conform modelului clasic de browser web.
 * Respectă principiul de încapsulare.
 */
public class NavigationHistory {

    private final Deque<String> backStack;
    private final Deque<String> forwardStack;
    private String currentUrl;

    public NavigationHistory() {
        this.backStack = new ArrayDeque<>();
        this.forwardStack = new ArrayDeque<>();
        this.currentUrl = null;
    }

    /**
     * Navighează la un URL nou.
     * Salvează URL-ul curent în stiva Înapoi și curăță stiva Înainte.
     */
    public synchronized void navigateTo(String newUrl) {
        if (newUrl == null || newUrl.trim().isEmpty()) {
            return;
        }

        if (currentUrl != null && !currentUrl.equals(newUrl)) {
            backStack.push(currentUrl);
        }
        this.currentUrl = newUrl;
        forwardStack.clear();
    }

    /**
     * Verifică dacă există pagini anterioare în istoric.
     */
    public synchronized boolean canGoBack() {
        return !backStack.isEmpty();
    }

    /**
     * Verifică dacă există pagini ulterioare în istoric (după acțiuni de Back).
     */
    public synchronized boolean canGoForward() {
        return !forwardStack.isEmpty();
    }

    /**
     * Efectuează navigarea Înapoi (Back).
     * @return noul URL curent, sau URL-ul curent dacă stiva este goală
     */
    public synchronized String goBack() {
        if (!canGoBack()) {
            return currentUrl;
        }

        if (currentUrl != null) {
            forwardStack.push(currentUrl);
        }
        currentUrl = backStack.pop();
        return currentUrl;
    }

    /**
     * Efectuează navigarea Înainte (Forward).
     * @return noul URL curent, sau URL-ul curent dacă stiva este goală
     */
    public synchronized String goForward() {
        if (!canGoForward()) {
            return currentUrl;
        }

        if (currentUrl != null) {
            backStack.push(currentUrl);
        }
        currentUrl = forwardStack.pop();
        return currentUrl;
    }

    public synchronized String getCurrentUrl() {
        return currentUrl;
    }

    public synchronized void setCurrentUrlWithoutHistory(String url) {
        this.currentUrl = url;
    }

    public synchronized void clear() {
        backStack.clear();
        forwardStack.clear();
        currentUrl = null;
    }

    public synchronized List<String> getBackHistory() {
        return new ArrayList<>(backStack);
    }

    public synchronized List<String> getForwardHistory() {
        return new ArrayList<>(forwardStack);
    }
}
