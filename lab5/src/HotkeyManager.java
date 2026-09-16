import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Manager centralizat pentru combinatiile de taste (Hotkeys) ale aplicației rezidente.
 * Permite înregistrarea, interceptarea globală în cadrul JVM și reconfigurarea dinamică a tastelor.
 * 
 * Cerința e: Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident.
 */
public class HotkeyManager implements KeyEventDispatcher {

    private final Map<String, HotkeyBinding> bindings = new LinkedHashMap<>();
    private boolean enabled = true;
    private Thread consoleListenerThread;

    public HotkeyManager() {
        // Înregistrăm acest manager ca dispecer global de evenimente de tastatură în JVM
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        startConsoleHotkeyListener();
    }

    /**
     * Înregistrează o combinație nouă de taste.
     */
    public synchronized void registerHotkey(String id, String name, String description, 
                                            KeyStroke defaultKeyStroke, HotkeyAction action) {
        HotkeyBinding binding = new HotkeyBinding(id, name, description, defaultKeyStroke, action);
        bindings.put(id, binding);
    }

    /**
     * Reconfigurează combinația de taste pentru o acțiune existentă.
     */
    public synchronized boolean rebindHotkey(String id, KeyStroke newKeyStroke) {
        HotkeyBinding binding = bindings.get(id);
        if (binding != null) {
            binding.setKeyStroke(newKeyStroke);
            return true;
        }
        return false;
    }

    public synchronized HotkeyBinding getBinding(String id) {
        return bindings.get(id);
    }

    public synchronized List<HotkeyBinding> getAllBindings() {
        return Collections.unmodifiableList(new ArrayList<>(bindings.values()));
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (!enabled || e.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }

        HotkeyBinding matchedBinding = null;
        synchronized (this) {
            for (HotkeyBinding binding : bindings.values()) {
                if (binding.matches(e)) {
                    matchedBinding = binding;
                    break;
                }
            }
        }

        if (matchedBinding != null) {
            final HotkeyBinding target = matchedBinding;
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("[Hotkey] Declanșat: " + target.getName() + " (" + target.getDisplayText() + ")");
                    target.getAction().execute();
                } catch (Exception ex) {
                    System.err.println("[Hotkey] Eroare la execuția acțiunii: " + ex.getMessage());
                }
            });
            // Marcăm evenimentul ca fiind consumat
            e.consume();
            return true;
        }

        return false;
    }

    /**
     * Pornește un ascultător în consolă (terminal) pentru a permite comenzi rapide
     * chiar și atunci când aplicația rulează exclusiv în fundal / System Tray.
     */
    private void startConsoleHotkeyListener() {
        consoleListenerThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.isEmpty()) continue;

                    switch (line) {
                        case "r":
                        case "refresh":
                            triggerById("REFRESH");
                            break;
                        case "d":
                        case "dash":
                        case "gui":
                            triggerById("TOGGLE_WINDOW");
                            break;
                        case "m":
                        case "mode":
                            triggerById("TOGGLE_MODE");
                            break;
                        case "q":
                        case "quit":
                        case "exit":
                            triggerById("QUIT");
                            break;
                        case "h":
                        case "help":
                            printHotkeysHelp();
                            break;
                        default:
                            System.out.println("[Console Help] Taste rapide: 'r' = Refresh, 'd' = Dashboard, 'm' = Mod (Vreme/Valută), 'q' = Quit, 'h' = Help");
                            break;
                    }
                }
            } catch (Exception ignored) {
                // Închiderea stream-ului stdin la ieșirea din aplicație
            }
        }, "ConsoleHotkeyDaemon");
        consoleListenerThread.setDaemon(true);
        consoleListenerThread.start();
    }

    private void triggerById(String id) {
        HotkeyBinding b;
        synchronized (this) {
            b = bindings.get(id);
        }
        if (b != null) {
            SwingUtilities.invokeLater(b.getAction()::execute);
        }
    }

    public void printHotkeysHelp() {
        System.out.println("\n=== Combinații de taste active (Hotkeys) ===");
        for (HotkeyBinding b : getAllBindings()) {
            System.out.println("  • " + b.getDisplayText() + " -> " + b.getName() + " (" + b.getDescription() + ")");
        }
        System.out.println("Comenzi scurte în consolă: r (refresh), d (dashboard), m (schimbă modul), q (ieșire)\n");
    }

    public void shutdown() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        if (consoleListenerThread != null) {
            consoleListenerThread.interrupt();
        }
    }
}
