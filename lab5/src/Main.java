import core.*;
import java.awt.SystemTray;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punctul de intrare (Main) pentru aplicația rezidentă POO Lab 5.
 * Configurează aspectul vizual (Look and Feel) și inițializează controlerul aplicației rezidente.
 * 
 * Îndeplinește cerințele:
 * a. Conectarea la un serviciu Internet pentru obținerea informației (vreme / curs valutar)
 * b. Ascunderea aplicației de pe bara Start cu afișarea în System tray
 * c. Setarea perioadei de recitire a informației din Internet și citirea repetată a informației
 * d. Modificarea pictogramei aplicației în dependență de starea aplicației sau datelor citite
 * e. Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident
 */
public class Main {

    public static void main(String[] args) {
        // Configurăm Look and Feel-ul nativ al sistemului de operare pentru o experiență modernă
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Afișăm antetul în consolă
        printBanner();

        // Inițializăm și pornim aplicația pe firul de execuție grafic Swing (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            ResidentApp app = new ResidentApp();
            app.start();
        });
    }

    private static void printBanner() {
        System.out.println("=================================================================");
        System.out.println("  POO - Laboratorul 5: Rețeaua Internet + Aplicație Rezident    ");
        System.out.println("=================================================================");
        System.out.println("  [a] Conectare la servicii Internet HTTP (Open-Meteo & Valută)  ");
        System.out.println("  [b] Rulare rezidentă în System Tray & ascundere de pe Start    ");
        System.out.println("  [c] Recitire automată repetată cu java.util.Timer configurabil ");
        System.out.println("  [d] Pictogramă dinamică în System Tray adaptată datelor/stării ");
        System.out.println("  [e] Combinatii active de taste (Hotkeys) & comenzi rapide      ");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("  SystemTray suportat: " + SystemTray.isSupported());
        System.out.println("  Platformă: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        System.out.println("=================================================================\n");
    }
}
