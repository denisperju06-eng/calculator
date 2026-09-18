package core;
import java.io.File;
import core.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

/**
 * Clientul de rețea pentru aplicația de Chat.
 * Gestionează conexiunea Socket TCP, fluxurile de date serializate,
 * firul de execuție de fundal pentru ascultarea mesajelor sosite
 * și API-ul de transmitere text, fișiere, comenzi de cameră și răspunsuri.
 */
public class Client {
    private String host;
    private int port;
    private String username;
    private String currentRoom = "General";

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread receiverThread;
    private volatile boolean isConnected = false;

    private final List<ClientListener> listeners = new CopyOnWriteArrayList<>();

    public Client() {
    }

    /**
     * Inițiază conexiunea la serverul de chat pe un fir de execuție separat.
     */
    public void connect(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = (username != null && !username.trim().isEmpty()) ? username.trim() : "Utilizator";

        new Thread(() -> {
            try {
                socket = new Socket(this.host, this.port);

                // Inițializare fluxuri: ObjectOutputStream mai întâi
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                isConnected = true;

                // Transmitere handshake inițial cu numele utilizatorului
                sendMessage(Message.createConnectMessage(this.username));

                // Pornire fir de execuție pentru recepție continuă
                startReceiverThread();

                notifyConnected(this.host, this.port, this.username);

            } catch (IOException e) {
                isConnected = false;
                notifyError("Nu s-a putut conecta la server (" + host + ":" + port + "): " + e.getMessage());
                notifyDisconnected("Conexiune eșuată.");
            }
        }, "Client-Connect-Thread").start();
    }

    /**
     * Bucla de ascultare a mesajelor primite de la server.
     */
    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            while (isConnected) {
                try {
                    Object obj = in.readObject();
                    if (obj == null) break;

                    if (obj instanceof Message) {
                        Message msg = (Message) obj;
                        dispatchMessage(msg);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (isConnected) {
                        disconnect("Conexiunea cu serverul a fost pierdută.");
                    }
                    break;
                }
            }
        }, "Client-Receiver-Thread");

        receiverThread.start();
    }

    /**
     * Distribuie mesajul sosit către ascultătorii înregistrați.
     */
    private void dispatchMessage(Message msg) {
        switch (msg.getType()) {
            case TEXT:
            case REPLY:
            case FILE:
            case SYSTEM:
                notifyMessageReceived(msg);
                break;

            case ROOM_LIST:
                if (msg.getRoomList() != null) {
                    notifyRoomListUpdated(msg.getRoomList());
                }
                break;

            case USER_LIST:
                if (msg.getUserList() != null) {
                    notifyUserListUpdated(msg.getRoom(), msg.getUserList());
                }
                break;

            case HISTORY_RESPONSE:
                if (msg.getHistoryList() != null) {
                    notifyHistoryReceived(msg.getRoom(), msg.getHistoryList());
                }
                break;

            default:
                break;
        }
    }

    /**
     * Trimite un mesaj text (sau răspuns la alt mesaj).
     */
    public boolean sendTextMessage(String text, Message replyTo) {
        if (!isConnected || text == null || text.trim().isEmpty()) return false;
        Message msg = Message.createTextMessage(username, currentRoom, text.trim(), replyTo);
        return sendMessage(msg);
    }

    /**
     * Trimite un fișier către camera curentă.
     */
    public boolean sendFile(File file, Message replyTo) throws IOException {
        if (!isConnected || file == null || !file.exists()) return false;

        // Verificăm dimensiunea (limită rezonabilă de siguranță: 30MB)
        long fileSize = file.length();
        if (fileSize > 30 * 1024 * 1024) {
            throw new IOException("Fișierul depășește limita maximă de 30 MB!");
        }

        byte[] fileData = new byte[(int) fileSize];
        try (FileInputStream fis = new FileInputStream(file)) {
            int readBytes = 0;
            while (readBytes < fileSize) {
                int read = fis.read(fileData, readBytes, (int) (fileSize - readBytes));
                if (read == -1) break;
                readBytes += read;
            }
        }

        FileAttachment attachment = new FileAttachment(file.getName(), fileSize, fileData);
        Message msg = Message.createFileMessage(username, currentRoom, attachment, replyTo);
        return sendMessage(msg);
    }

    /**
     * Schimbă camera curentă sau se alătură uneia noi.
     */
    public void joinRoom(String roomName) {
        if (!isConnected || roomName == null || roomName.trim().isEmpty()) return;
        this.currentRoom = roomName.trim();
        sendMessage(Message.createJoinRoomMessage(username, currentRoom));
    }

    /**
     * Solicită serverului crearea unei noi camere de chat.
     */
    public void createRoom(String roomName) {
        if (!isConnected || roomName == null || roomName.trim().isEmpty()) return;
        sendMessage(Message.createCreateRoomMessage(username, roomName.trim()));
    }

    /**
     * Solicită istoricul camerei curente de la server.
     */
    public void requestHistory() {
        if (!isConnected) return;
        sendMessage(Message.createHistoryRequestMessage(username, currentRoom));
    }

    /**
     * Trimite un obiect Message pe socket. Sincronizat pentru siguranță concurentă.
     */
    private synchronized boolean sendMessage(Message msg) {
        if (!isConnected || out == null) return false;
        try {
            out.writeObject(msg);
            out.flush();
            out.reset(); // Previne referințele cache-uite în ObjectOutputStream
            return true;
        } catch (IOException e) {
            notifyError("Eroare la trimiterea datelor: " + e.getMessage());
            disconnect("Eroare de comunicare.");
            return false;
        }
    }

    /**
     * Închide conexiunea cu serverul și eliberează resursele.
     */
    public synchronized void disconnect(String reason) {
        if (!isConnected) return;
        isConnected = false;

        try {
            if (out != null) {
                out.writeObject(Message.createDisconnectMessage(username, currentRoom));
                out.flush();
            }
        } catch (Exception ignored) {}

        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}

        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}

        notifyDisconnected(reason != null ? reason : "Deconectat.");
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getUsername() {
        return username;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    // ==========================================
    // NOTIFICĂRI CĂTRE LISTENERS
    // ==========================================

    public void addListener(ClientListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ClientListener listener) {
        listeners.remove(listener);
    }

    private void notifyConnected(String host, int port, String username) {
        for (ClientListener l : listeners) {
            l.onConnected(host, port, username);
        }
    }

    private void notifyDisconnected(String reason) {
        for (ClientListener l : listeners) {
            l.onDisconnected(reason);
        }
    }

    private void notifyMessageReceived(Message message) {
        for (ClientListener l : listeners) {
            l.onMessageReceived(message);
        }
    }

    private void notifyHistoryReceived(String room, List<Message> history) {
        for (ClientListener l : listeners) {
            l.onHistoryReceived(room, history);
        }
    }

    private void notifyRoomListUpdated(List<String> rooms) {
        for (ClientListener l : listeners) {
            l.onRoomListUpdated(rooms);
        }
    }

    private void notifyUserListUpdated(String room, List<String> users) {
        for (ClientListener l : listeners) {
            l.onUserListUpdated(room, users);
        }
    }

    private void notifyError(String error) {
        for (ClientListener l : listeners) {
            l.onError(error);
        }
    }

    // ==========================================
    // PUNCT DE INTRARE (MAIN)
    // ==========================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
        });
    }
}
