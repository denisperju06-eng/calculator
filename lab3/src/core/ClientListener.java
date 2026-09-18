package core;
import java.util.List;

/**
 * Interfață Observer (Listener) pentru recepționarea evenimentelor rețelei de către ClientGUI.
 * Permite decuplarea completă a logicii de rețea (Sockets/Threads) de interfața utilizator (Swing).
 */
public interface ClientListener {
    void onConnected(String host, int port, String username);
    void onDisconnected(String reason);
    void onMessageReceived(Message message);
    void onHistoryReceived(String room, List<Message> history);
    void onRoomListUpdated(List<String> rooms);
    void onUserListUpdated(String room, List<String> users);
    void onError(String errorMessage);
}
