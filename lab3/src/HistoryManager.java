import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionează persistența și exportul istoricului de mesaje pe disc.
 * Salvează mesajele în format binar (pentru reîncărcare completă cu obiecte Message)
 * și opțional în format text lizibil (pentru vizualizare și export).
 */
public class HistoryManager {
    private final File storageDir;

    public HistoryManager(String baseDirPath) {
        this.storageDir = new File(baseDirPath, "server_history");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    /**
     * Salvează un mesaj în fișierele de istoric ale camerei (binar și jurnal text).
     */
    public synchronized void appendMessage(Message message) {
        if (message == null || message.getRoom() == null) return;

        // Nu salvăm handshake-urile de conectare/deconectare ca mesaje normale de chat
        if (message.getType() == MessageType.CONNECT ||
            message.getType() == MessageType.DISCONNECT ||
            message.getType() == MessageType.ROOM_LIST ||
            message.getType() == MessageType.USER_LIST ||
            message.getType() == MessageType.HISTORY_REQUEST ||
            message.getType() == MessageType.HISTORY_RESPONSE) {
            return;
        }

        String safeRoomName = message.getRoom().replaceAll("[^a-zA-Z0-9._-]", "_");

        // 1. Salvare în fișier jurnal text lizibil
        File textLogFile = new File(storageDir, "chat_" + safeRoomName + ".log");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(textLogFile, StandardCharsets.UTF_8, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(message.getFormattedDateTime()).append("] ");
            sb.append("<").append(message.getSender()).append("> ");
            if (message.isReply()) {
                sb.append("[Răspuns către @").append(message.getReplyToSender()).append(": \"")
                  .append(message.getReplyToContent()).append("\"] ");
            }
            if (message.isFile()) {
                sb.append("[FIȘIER: ").append(message.getFileAttachment().getFileName())
                  .append(" (").append(message.getFileAttachment().getFormattedSize()).append(")]");
            } else {
                sb.append(message.getContent());
            }
            sb.append("\n");
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("[HistoryManager] Eroare la scrierea logului text: " + e.getMessage());
        }

        // 2. Salvare binară pentru reîncărcare la repornirea serverului
        File binaryFile = new File(storageDir, "chat_" + safeRoomName + ".dat");
        List<Message> existing = loadBinaryHistory(safeRoomName);
        existing.add(message);
        if (existing.size() > 500) {
            existing.remove(0); // păstrăm ultimele 500 mesaje pe disc
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(binaryFile))) {
            oos.writeObject(existing);
        } catch (IOException e) {
            System.err.println("[HistoryManager] Eroare la salvarea binară a istoricului: " + e.getMessage());
        }
    }

    /**
     * Încarcă istoricul salvat pentru o cameră specificată.
     */
    @SuppressWarnings("unchecked")
    public synchronized List<Message> loadBinaryHistory(String roomName) {
        String safeRoomName = roomName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File binaryFile = new File(storageDir, "chat_" + safeRoomName + ".dat");
        if (!binaryFile.exists() || binaryFile.length() == 0) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(binaryFile))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<Message>) obj;
            }
        } catch (Exception e) {
            System.err.println("[HistoryManager] Nu s-a putut citi istoricul binar pentru " + roomName + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Exportă o listă de mesaje într-un fișier text specificat de utilizator.
     */
    public static void exportToTextFile(List<Message> messages, File targetFile, String roomName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write("====================================================\n");
            writer.write("           ISTORIC CHAT - CAMERA: " + (roomName != null ? roomName : "Global") + "\n");
            writer.write("           Exportat la: " + new java.util.Date() + "\n");
            writer.write("           Număr total mesaje: " + messages.size() + "\n");
            writer.write("====================================================\n\n");

            for (Message m : messages) {
                StringBuilder line = new StringBuilder();
                line.append("[").append(m.getFormattedDateTime()).append("] ");
                line.append(m.getSender()).append(": ");
                if (m.isReply()) {
                    line.append(" (Răspuns către @").append(m.getReplyToSender())
                        .append(": \"").append(m.getReplyToContent()).append("\") ");
                }
                if (m.isFile()) {
                    line.append("[FIȘIER ATAȘAT: ")
                        .append(m.getFileAttachment().getFileName())
                        .append(" (").append(m.getFileAttachment().getFormattedSize()).append(")]");
                } else {
                    line.append(m.getContent());
                }
                line.append("\n");
                writer.write(line.toString());
            }
        }
    }

    public File getStorageDir() {
        return storageDir;
    }
}
