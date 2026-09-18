import core.*;
import javax.swing.*;

/**
 * Punctul de intrare (Entry Point) în aplicația Redactor Text.
 * Configurează Look and Feel-ul sistemului și lansează interfața grafică pe firul de execuție Swing (EDT).
 */
public class Main {
    public static void main(String[] args) {
        // Activare anti-aliasing pentru text în Java Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Setare Look & Feel conform sistemului de operare (macOS / Windows / Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Dacă L&F-ul nativ nu este disponibil, se folosește cel implicit
        }

        // Pornire GUI pe Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
