/**
 * Interfață ascultător pentru evenimentele generate de un tab de navigare.
 * Permite comunicarea decuplată între BrowserTab și fereastra principală BrowserWindow.
 */
public interface TabListener {

    /**
     * Apelat când URL-ul curent al tab-ului s-a schimbat.
     */
    void onUrlChanged(BrowserTab tab, String newUrl);

    /**
     * Apelat când titlul paginii s-a actualizat.
     */
    void onTitleChanged(BrowserTab tab, String newTitle);

    /**
     * Apelat când starea de încărcare a paginii s-a schimbat (a început sau s-a terminat).
     */
    void onLoadingStateChanged(BrowserTab tab, boolean isLoading);

    /**
     * Apelat pentru actualizarea mesajului de stare (ex: la trecerea mouse-ului peste link-uri).
     */
    void onStatusMessage(BrowserTab tab, String message);

    /**
     * Apelat când starea butoanelor Back/Forward s-a schimbat pentru tab.
     */
    void onNavigationStateChanged(BrowserTab tab, boolean canGoBack, boolean canGoForward);

    /**
     * Solicitare din tab de a deschide un nou URL într-un tab nou.
     */
    void onRequestNewTab(String url);
}
