import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Fir de execuție (Thread) responsabil de comunicarea bidirecțională prin Socket
 * cu un singur client conectat.
 * Gestionează citirea mesajelor primite, delegarea acțiunilor către Server
 * și transmiterea răspunsurilor.
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String currentRoom;
    private volatile boolean isRunning = true;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Inițializăm fluxurile I/O.
            // IMPORTANT: ObjectOutputStream trebuie creat și curățat înaintea lui ObjectInputStream
            // pentru a evita blocarea protocolului de handshake Java Object Stream.
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Așteptăm mesajul inițial de autentificare / conectare
            Object initialObj = in.readObject();
            if (initialObj instanceof Message) {
                Message connectMsg = (Message) initialObj;
                this.username = (connectMsg.getSender() != null && !connectMsg.getSender().trim().isEmpty())
                        ? connectMsg.getSender().trim()
                        : "Utilizator_" + socket.getPort();
            } else {
                this.username = "Utilizator_" + socket.getPort();
            }

            // Înregistrăm clientul pe server și îl adăugăm în camera implicită ("General")
            server.registerClient(this);
            server.log("Client conectat: " + username + " de la " + socket.getRemoteSocketAddress());

            // Trimitem lista curentă de camere către noul client
            sendMessage(Message.createRoomListMessage(server.getRoomNames()));

            // Alăturăm clientul la camera de pornire "General"
            server.joinRoom(this, "General");

            // Bucla principală de recepție mesaje
            while (isRunning) {
                try {
                    Object obj = in.readObject();
                    if (obj == null) break;

                    if (obj instanceof Message) {
                        Message msg = (Message) obj;
                        handleIncomingMessage(msg);
                    }
                } catch (ClassNotFoundException e) {
                    server.log("Format mesaj necunoscut primit de la " + username + ": " + e.getMessage());
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            // Conexiune întreruptă normal sau brusc
            server.log("Clientul " + (username != null ? username : socket.getRemoteSocketAddress()) + " s-a deconectat.");
        } finally {
            close();
        }
    }

    /**
     * Procesează mesajele primite de la client în funcție de tipul lor.
     */
    private void handleIncomingMessage(Message msg) {
        if (msg == null) return;

        switch (msg.getType()) {
            case TEXT:
            case REPLY:
                server.log(String.format("[%s] %s: %s%s",
                        msg.getRoom(), msg.getSender(), msg.getContent(),
                        msg.isReply() ? " (Răspuns către @" + msg.getReplyToSender() + ")" : ""));
                server.broadcastToRoom(msg.getRoom(), msg);
                break;

            case FILE:
                server.log(String.format("[%s] %s a transmis fișierul: %s (%s)",
                        msg.getRoom(), msg.getSender(),
                        msg.getFileAttachment().getFileName(),
                        msg.getFileAttachment().getFormattedSize()));
                server.broadcastToRoom(msg.getRoom(), msg);
                break;

            case JOIN_ROOM:
                String targetRoom = msg.getContent() != null ? msg.getContent().replace("Join ", "").trim() : msg.getRoom();
                if (targetRoom != null && !targetRoom.isEmpty()) {
                    server.joinRoom(this, targetRoom);
                }
                break;

            case CREATE_ROOM:
                String newRoom = msg.getContent() != null ? msg.getContent().replace("Create ", "").trim() : null;
                if (newRoom != null && !newRoom.trim().isEmpty()) {
                    server.createRoom(newRoom.trim());
                    // Alăturăm direct utilizatorul creator la noua cameră
                    server.joinRoom(this, newRoom.trim());
                }
                break;

            case HISTORY_REQUEST:
                String roomForHistory = msg.getRoom() != null ? msg.getRoom() : currentRoom;
                List<Message> history = server.getRoomHistory(roomForHistory);
                sendMessage(Message.createHistoryResponseMessage(roomForHistory, history));
                break;

            case DISCONNECT:
                isRunning = false;
                close();
                break;

            default:
                server.log("Mesaj neprocesat de tip: " + msg.getType());
                break;
        }
    }

    /**
     * Trimite un mesaj către client prin Socket în mod sincronizat și sigur.
     */
    public synchronized boolean sendMessage(Message message) {
        if (!isRunning || out == null) return false;
        try {
            out.writeObject(message);
            out.flush();
            out.reset(); // Curăță cache-ul ObjectOutputStream pentru a preveni referințe învechite
            return true;
        } catch (IOException e) {
            server.log("Eroare la trimiterea mesajului către " + username + ": " + e.getMessage());
            close();
            return false;
        }
    }

    /**
     * Închide resursele de rețea asociate acestui client.
     */
    public synchronized void close() {
        if (!isRunning && socket.isClosed()) return;
        isRunning = false;

        server.unregisterClient(this);

        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}

        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}

        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public String getUsername() {
        return username;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String room) {
        this.currentRoom = room;
    }

    public Socket getSocket() {
        return socket;
    }
}
