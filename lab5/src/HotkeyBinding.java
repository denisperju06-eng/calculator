import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 * În capsulează asocierea dintre o combinație de taste (KeyStroke),
 * denumirea acesteia și acțiunea executată.
 * 
 * Cerința e: Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident.
 */
public class HotkeyBinding {

    private final String id;
    private final String name;
    private final String description;
    private KeyStroke keyStroke;
    private final HotkeyAction action;

    public HotkeyBinding(String id, String name, String description, KeyStroke defaultKeyStroke, HotkeyAction action) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.keyStroke = defaultKeyStroke;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public synchronized void setKeyStroke(KeyStroke newKeyStroke) {
        if (newKeyStroke != null) {
            this.keyStroke = newKeyStroke;
        }
    }

    public HotkeyAction getAction() {
        return action;
    }

    /**
     * Verifică dacă un eveniment de tastatură KeyEvent corespunde combinației înregistrate.
     */
    public boolean matches(KeyEvent event) {
        if (event == null || keyStroke == null) {
            return false;
        }
        if (event.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }
        int keyCode = event.getKeyCode();
        int modifiers = event.getModifiersEx();

        // Mască pentru ignorarea stărilor neesențiale (ex: CapsLock, NumLock)
        int cleanModifiers = modifiers & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | 
                                          KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);

        int targetModifiers = keyStroke.getModifiers() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | 
                                                          KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);

        return keyCode == keyStroke.getKeyCode() && cleanModifiers == targetModifiers;
    }

    /**
     * Reprezentare text prietenoasă a combinației (ex: "Ctrl+Shift+R" sau "Cmd+Shift+R" pe Mac).
     */
    public String getDisplayText() {
        if (keyStroke == null) {
            return "Nicio tastă";
        }
        StringBuilder sb = new StringBuilder();
        int mod = keyStroke.getModifiers();
        boolean isMac = System.getProperty("os.name", "").toLowerCase().contains("mac");

        if ((mod & KeyEvent.META_DOWN_MASK) != 0) {
            sb.append(isMac ? "Cmd+" : "Meta+");
        }
        if ((mod & KeyEvent.CTRL_DOWN_MASK) != 0) {
            sb.append("Ctrl+");
        }
        if ((mod & KeyEvent.ALT_DOWN_MASK) != 0) {
            sb.append("Alt+");
        }
        if ((mod & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            sb.append("Shift+");
        }

        String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
        sb.append(keyText);
        return sb.toString();
    }

    @Override
    public String toString() {
        return name + " [" + getDisplayText() + "] - " + description;
    }
}
