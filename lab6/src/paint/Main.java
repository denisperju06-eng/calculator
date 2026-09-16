package paint;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punctul de pornire (Entry Point) al aplicației Redactor Grafic.
 * Configurează aspectul nativ al sistemului (Look and Feel) și pornește interfața grafică Swing.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Încercăm să setăm Look and Feel nativ al sistemului de operare (macOS / Windows / Linux)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Dacă eșuează, va fi folosit Look and Feel implicit Swing (Metal/Nimbus)
            }

            PaintApp app = new PaintApp();
            app.setVisible(true);
        });
    }
}
