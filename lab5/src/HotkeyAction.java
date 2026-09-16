/**
 * Interfață funcțională ce definește acțiunea executată la declanșarea unei combinații de taste.
 * Aplică Command Pattern din Paradigma Orientată pe Obiecte.
 * 
 * Cerința e: Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident.
 */
@FunctionalInterface
public interface HotkeyAction {
    /**
     * Execută comanda asociată scurtăturii de tastatură.
     */
    void execute();
}
