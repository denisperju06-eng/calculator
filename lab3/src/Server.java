import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

/**
 * Serverul principal de chat pentru rețeaua locală.
 * Administrează conexiunile socket, camerele de chat, istoricul mesajelor
 * și distribuirea pachetelor de date către clienți.
 */
public class Server {
    public static final int DEFAULT_PORT = 12345;

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    private Thread serverThread;

    // Structuri concurente pentru camere și clienți
    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();
    private final Set<ClientHandler> activeClients = Collections.synchronizedSet(new HashSet<>());
    private final HistoryManager historyManager;

    // Observatori pentru interfața grafică sau consolă
    private final List<ServerEventListener> listeners = new CopyOnWriteArrayList<>();

    public Server(int port) {
        this.port = port;
        // Salvează istoricul în directorul aplicației
        this.historyManager = new HistoryManager(System.getProperty("user.dir"));
        initializeDefaultRooms();
    }

    /**
     * Inițializează camerele de chat implicite și reîncarcă istoricul existent.
     */
    private void initializeDefaultRooms() {
        String[] defaultRoomNames = {"General", "Laborator POO", "Proiecte", "Discuții Libere"};
        for (String name : defaultRoomNames) {
            ChatRoom room = new ChatRoom(name);
            List<Message> savedHistory = historyManager.loadBinaryHistory(name);
            if (!savedHistory.isEmpty()) {
                room.loadHistory(savedHistory);
            }
            rooms.put(name, room);
        }
    }

    /**
     * Pornește serverul pe un fir de execuție separat.
     */
    public synchronized void start() throws IOException {
        if (isRunning) return;

        serverSocket = new ServerSocket(port);
        isRunning = true;

        serverThread = new Thread(() -> {
            log("Serverul a pornit pe portul " + port + ". Aștept conexiuni...");
            notifyStatus(true, port);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    handler.start();
                } catch (IOException e) {
                    if (!isRunning) {
                        break; // Serverul a fost oprit intenționat
                    }
                    log("Eroare la acceptarea conexiunii: " + e.getMessage());
                }
            }

            log("Serverul s-a oprit.");
            notifyStatus(false, port);
        }, "Server-Accept-Thread");

        serverThread.start();
    }

    /**
     * Oprește serverul și închide toate conexiunile active.
     */
    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;

        log("Oprire server în curs...");

        // Închide toate conexiunile clienților activi
        List<ClientHandler> clientsToClose;
        synchronized (activeClients) {
            clientsToClose = new ArrayList<>(activeClients);
            activeClients.clear();
        }
        for (ClientHandler client : clientsToClose) {
            client.sendMessage(Message.createSystemMessage(null, "Serverul se închide. La revedere!"));
            client.close();
        }

        // Închide ServerSocket pentru a debloca accept()
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Eroare la închiderea socket-ului de server: " + e.getMessage());
        }

        notifyStatus(false, port);
    }

    /**
     * Înregistrează un client la conectare.
     */
    public void registerClient(ClientHandler client) {
        activeClients.add(client);
        notifyClientConnected(client.getUsername(), client.getSocket().getRemoteSocketAddress().toString());
    }

    /**
     * Dezînregistrează un client la deconectare și curăță apartenența la cameră.
     */
    public void unregisterClient(ClientHandler client) {
        activeClients.remove(client);
        String oldRoom = client.getCurrentRoom();
        if (oldRoom != null && rooms.containsKey(oldRoom)) {
            ChatRoom room = rooms.get(oldRoom);
            room.removeParticipant(client);
            broadcastToRoom(oldRoom, Message.createSystemMessage(oldRoom, client.getUsername() + " a părăsit camera."));
            broadcastUserList(oldRoom);
        }
        notifyClientDisconnected(client.getUsername());
    }

    /**
     * Creează o nouă cameră de chat dacă nu există deja.
     */
    public synchronized boolean createRoom(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) return false;
        String trimmed = roomName.trim();
        if (!rooms.containsKey(trimmed)) {
            ChatRoom newRoom = new ChatRoom(trimmed);
            rooms.put(trimmed, newRoom);
            log("A fost creată camera nouă: " + trimmed);
            broadcastRoomList();
            notifyRoomCreated(trimmed);
            return true;
        }
        return false;
    }

    /**
     * Gestionează mutarea sau alăturarea unui client la o cameră.
     */
    public void joinRoom(ClientHandler client, String targetRoomName) {
        if (targetRoomName == null || targetRoomName.trim().isEmpty()) return;
        targetRoomName = targetRoomName.trim();

        // Dacă nu există camera, o creăm automat
        if (!rooms.containsKey(targetRoomName)) {
            createRoom(targetRoomName);
        }

        String oldRoomName = client.getCurrentRoom();
        if (oldRoomName != null && rooms.containsKey(oldRoomName)) {
            ChatRoom oldRoom = rooms.get(oldRoomName);
            oldRoom.removeParticipant(client);
            broadcastToRoom(oldRoomName, Message.createSystemMessage(oldRoomName, client.getUsername() + " a părăsit camera."));
            broadcastUserList(oldRoomName);
        }

        // Adăugăm în noua cameră
        ChatRoom newRoom = rooms.get(targetRoomName);
        newRoom.addParticipant(client);
        client.setCurrentRoom(targetRoomName);

        // Notificăm clientul cu lista de camere actuală și cu istoricul camerei noi
        client.sendMessage(Message.createRoomListMessage(getRoomNames()));
        client.sendMessage(Message.createHistoryResponseMessage(targetRoomName, newRoom.getHistory()));

        // Notificăm participanții noii camere
        broadcastToRoom(targetRoomName, Message.createSystemMessage(targetRoomName, client.getUsername() + " s-a alăturat camerei."));
        broadcastUserList(targetRoomName);
    }

    /**
     * Transmite un mesaj tuturor participanților dintr-o cameră specifică
     * și îl salvează în istoric.
     */
    public void broadcastToRoom(String roomName, Message message) {
        if (roomName == null || message == null) return;
        ChatRoom room = rooms.get(roomName);
        if (room != null) {
            // Salvează mesajul în istoricul din memorie și pe disc
            room.addMessage(message);
            historyManager.appendMessage(message);

            // Transmite mesajul tuturor participanților din cameră
            for (ClientHandler participant : room.getParticipants()) {
                participant.sendMessage(message);
            }
        }
    }

    /**
     * Trimite lista de camere actualizată către toți clienții conectați la server.
     */
    public void broadcastRoomList() {
        Message roomListMsg = Message.createRoomListMessage(getRoomNames());
        synchronized (activeClients) {
            for (ClientHandler client : activeClients) {
                client.sendMessage(roomListMsg);
            }
        }
    }

    /**
     * Trimite lista de utilizatori dintr-o cameră către toți membrii camerei respective.
     */
    public void broadcastUserList(String roomName) {
        ChatRoom room = rooms.get(roomName);
        if (room != null) {
            Message userListMsg = Message.createUserListMessage(roomName, room.getParticipantUsernames());
            for (ClientHandler participant : room.getParticipants()) {
                participant.sendMessage(userListMsg);
            }
        }
    }

    public List<String> getRoomNames() {
        List<String> list = new ArrayList<>(rooms.keySet());
        Collections.sort(list);
        return list;
    }

    public List<Message> getRoomHistory(String roomName) {
        ChatRoom room = rooms.get(roomName);
        return (room != null) ? room.getHistory() : new ArrayList<>();
    }

    public Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    public Set<ClientHandler> getActiveClients() {
        synchronized (activeClients) {
            return new HashSet<>(activeClients);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    // ==========================================
    // NOTIFICĂRI OBSERVER / LOGGING
    // ==========================================

    public void addEventListener(ServerEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(ServerEventListener listener) {
        listeners.remove(listener);
    }

    public void log(String message) {
        System.out.println("[SERVER] " + message);
        for (ServerEventListener l : listeners) {
            l.onLog(message);
        }
    }

    private void notifyClientConnected(String username, String address) {
        for (ServerEventListener l : listeners) {
            l.onClientConnected(username, address);
        }
    }

    private void notifyClientDisconnected(String username) {
        for (ServerEventListener l : listeners) {
            l.onClientDisconnected(username);
        }
    }

    private void notifyRoomCreated(String roomName) {
        for (ServerEventListener l : listeners) {
            l.onRoomCreated(roomName);
        }
    }

    private void notifyStatus(boolean running, int port) {
        for (ServerEventListener l : listeners) {
            l.onServerStatusChanged(running, port);
        }
    }

    // ==========================================
    // PUNCT DE INTRARE (MAIN)
    // ==========================================

    public static void main(String[] args) {
        boolean cliMode = false;
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            if ("--cli".equalsIgnoreCase(args[i]) || "-c".equalsIgnoreCase(args[i]) || "--headless".equalsIgnoreCase(args[i])) {
                cliMode = true;
            } else if ("--port".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (cliMode) {
            System.out.println("Pornire Server în mod Consolă (CLI)...");
            Server server = new Server(port);
            try {
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            } catch (IOException e) {
                System.err.println("Eroare la pornirea serverului: " + e.getMessage());
            }
        } else {
            // Mod Interfață Grafică Swing (implicit)
            SwingUtilities.invokeLater(() -> {
                ServerGUI gui = new ServerGUI();
                gui.setVisible(true);
            });
        }
    }
}
