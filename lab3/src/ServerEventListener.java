/**
 * Interfață Observer pentru monitorizarea evenimentelor produse de Server.
 * Permite decuplarea logicii de rețea de interfața grafică ServerGUI.
 */
public interface ServerEventListener {
    void onLog(String logMessage);
    void onClientConnected(String username, String address);
    void onClientDisconnected(String username);
    void onRoomCreated(String roomName);
    void onServerStatusChanged(boolean isRunning, int port);
}
