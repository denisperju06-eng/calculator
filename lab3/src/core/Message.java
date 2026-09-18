package core;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Clasa ce reprezintă un mesaj transmis prin rețeaua locală de chat.
 * Implementează Serializable pentru transmitere binară prin Object Streams pe Sockets.
 * Include suport complet pentru text, fișiere atașate, reply (citare mesaj anterior),
 * chat-rooms, istoric și mesaje de sistem.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final MessageType type;
    private final String sender;
    private final String room;
    private final String content;
    private final long timestamp;

    // Câmpuri specifice pentru mecanismul de Răspuns (Reply)
    private final String replyToId;
    private final String replyToSender;
    private final String replyToContent;

    // Câmp opțional pentru transfer de fișiere
    private final FileAttachment fileAttachment;

    // Liste opționale pentru sincronizare stări între Server și Client
    private List<String> roomList;
    private List<String> userList;
    private List<Message> historyList;

    /**
     * Constructor general privat. Instanțierea se face prin Factory Methods.
     */
    public Message(String id, MessageType type, String sender, String room, String content,
                   long timestamp, String replyToId, String replyToSender, String replyToContent,
                   FileAttachment fileAttachment) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.type = type;
        this.sender = sender;
        this.room = room;
        this.content = content;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.replyToId = replyToId;
        this.replyToSender = replyToSender;
        this.replyToContent = replyToContent;
        this.fileAttachment = fileAttachment;
    }

    // ==========================================
    // METODE FACTORY PENTRU CONSTRUCȚIA MESAJELOR
    // ==========================================

    /**
     * Creează un mesaj text standard sau cu reply opțional.
     */
    public static Message createTextMessage(String sender, String room, String content, Message replyTo) {
        MessageType type = (replyTo != null) ? MessageType.REPLY : MessageType.TEXT;
        String repId = (replyTo != null) ? replyTo.getId() : null;
        String repSender = (replyTo != null) ? replyTo.getSender() : null;
        String repContent = null;
        if (replyTo != null) {
            repContent = replyTo.isFile() ? "[Fișier: " + replyTo.getFileAttachment().getFileName() + "]" : replyTo.getContent();
            if (repContent != null && repContent.length() > 60) {
                repContent = repContent.substring(0, 57) + "...";
            }
        }
        return new Message(UUID.randomUUID().toString(), type, sender, room, content,
                System.currentTimeMillis(), repId, repSender, repContent, null);
    }

    /**
     * Creează un mesaj cu fișier atașat și eventual un reply.
     */
    public static Message createFileMessage(String sender, String room, FileAttachment fileAttachment, Message replyTo) {
        String repId = (replyTo != null) ? replyTo.getId() : null;
        String repSender = (replyTo != null) ? replyTo.getSender() : null;
        String repContent = null;
        if (replyTo != null) {
            repContent = replyTo.isFile() ? "[Fișier: " + replyTo.getFileAttachment().getFileName() + "]" : replyTo.getContent();
            if (repContent != null && repContent.length() > 60) {
                repContent = repContent.substring(0, 57) + "...";
            }
        }
        return new Message(UUID.randomUUID().toString(), MessageType.FILE, sender, room,
                fileAttachment.getFileName(), System.currentTimeMillis(), repId, repSender, repContent, fileAttachment);
    }

    /**
     * Creează un mesaj de sistem (notificări, alerte, intrări/ieșiri din cameră).
     */
    public static Message createSystemMessage(String room, String content) {
        return new Message(UUID.randomUUID().toString(), MessageType.SYSTEM, "SISTEM", room, content,
                System.currentTimeMillis(), null, null, null, null);
    }

    /**
     * Creează mesajul de autentificare / handshake inițial.
     */
    public static Message createConnectMessage(String username) {
        return new Message(UUID.randomUUID().toString(), MessageType.CONNECT, username, null, "Connect",
                System.currentTimeMillis(), null, null, null, null);
    }

    /**
     * Creează mesajul de deconectare a clientului.
     */
    public static Message createDisconnectMessage(String username, String room) {
        return new Message(UUID.randomUUID().toString(), MessageType.DISCONNECT, username, room, "Disconnect",
                System.currentTimeMillis(), null, null, null, null);
    }

    /**
     * Creează o cerere de alăturare la o cameră (JOIN_ROOM).
     */
    public static Message createJoinRoomMessage(String sender, String targetRoom) {
        return new Message(UUID.randomUUID().toString(), MessageType.JOIN_ROOM, sender, targetRoom, "Join " + targetRoom,
                System.currentTimeMillis(), null, null, null, null);
    }

    /**
     * Creează o cerere de creare a unei noi camere de chat (CREATE_ROOM).
     */
    public static Message createCreateRoomMessage(String sender, String newRoomName) {
        return new Message(UUID.randomUUID().toString(), MessageType.CREATE_ROOM, sender, newRoomName, "Create " + newRoomName,
                System.currentTimeMillis(), null, null, null, null);
    }

    /**
     * Creează mesajul de distribuire a listei de camere (ROOM_LIST).
     */
    public static Message createRoomListMessage(List<String> rooms) {
        Message msg = new Message(UUID.randomUUID().toString(), MessageType.ROOM_LIST, "SISTEM", null, null,
                System.currentTimeMillis(), null, null, null, null);
        msg.setRoomList(new ArrayList<>(rooms));
        return msg;
    }

    /**
     * Creează mesajul de distribuire a listei de utilizatori dintr-o cameră (USER_LIST).
     */
    public static Message createUserListMessage(String room, List<String> users) {
        Message msg = new Message(UUID.randomUUID().toString(), MessageType.USER_LIST, "SISTEM", room, null,
                System.currentTimeMillis(), null, null, null, null);
        msg.setUserList(new ArrayList<>(users));
        return msg;
    }

    /**
     * Creează cerere pentru istoricul mesajelor din cameră.
     */
    public static Message createHistoryRequestMessage(String sender, String room) {
        return new Message(UUID.randomUUID().toString(), MessageType.HISTORY_REQUEST, sender, room, "Request History",
                System.currentTimeMillis(), null, null, null, null);
    }

    /**
     * Creează răspunsul cu istoricul mesajelor dintr-o cameră (HISTORY_RESPONSE).
     */
    public static Message createHistoryResponseMessage(String room, List<Message> history) {
        Message msg = new Message(UUID.randomUUID().toString(), MessageType.HISTORY_RESPONSE, "SISTEM", room, null,
                System.currentTimeMillis(), null, null, null, null);
        msg.setHistoryList(new ArrayList<>(history));
        return msg;
    }

    // ==========================================
    // GETTERE ȘI METODE AJUTĂTOARE
    // ==========================================

    public String getId() {
        return id;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getRoom() {
        return room;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public String getReplyToSender() {
        return replyToSender;
    }

    public String getReplyToContent() {
        return replyToContent;
    }

    public FileAttachment getFileAttachment() {
        return fileAttachment;
    }

    public boolean isReply() {
        return replyToId != null && !replyToId.trim().isEmpty();
    }

    public boolean isFile() {
        return type == MessageType.FILE && fileAttachment != null;
    }

    public List<String> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<String> roomList) {
        this.roomList = roomList;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public List<Message> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<Message> historyList) {
        this.historyList = historyList;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return String.format("[%s] [%s] %s: %s%s",
                getFormattedTime(),
                room != null ? room : "Global",
                sender,
                isFile() ? "[Fișier: " + fileAttachment.getFileName() + " (" + fileAttachment.getFormattedSize() + ")]" : content,
                isReply() ? " (Răspuns către @" + replyToSender + ")" : "");
    }
}
