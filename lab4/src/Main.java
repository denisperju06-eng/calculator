import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punctul de intrare principal în aplicația Internet Browser POO.
 * Inițializează serviciile necesare, configurează aspectul grafic (Look & Feel)
 * și lansează fereastra principală pe firul de execuție grafic (Event Dispatch Thread).
 */
public class Main {

    public static void main(String[] args) {
        // Setăm User-Agent nativ global pentru ca componentele de rețea și ImageView Swing să nu fie blocate
        System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        // Setăm aspectul vizual nativ al sistemului de operare (macOS, Windows sau Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se continuă cu aspectul implicit Java Swing în caz de incompatibilitate
        }

        SwingUtilities.invokeLater(() -> {
            // Inițializarea serviciilor de persistență și căutare
            File favoritesFile = new File("favorites.txt");
            File historyFile = new File("history.txt");

            FavoritesManager favoritesManager = new FavoritesManager(favoritesFile);
            HistoryManager historyManager = new HistoryManager(historyFile);
            SearchEngineRegistry searchEngineRegistry = new SearchEngineRegistry();

            // Înregistrare hook de salvare la închiderea aplicației (Shutdown Hook)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                favoritesManager.saveToFile();
                historyManager.saveToFile();
            }));

            // Crearea și afișarea ferestrei principale a browser-ului
            BrowserWindow browserWindow = new BrowserWindow(historyManager, favoritesManager, searchEngineRegistry);
            browserWindow.setVisible(true);
        });
    }
}
