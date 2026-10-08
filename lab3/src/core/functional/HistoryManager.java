package core.functional;
import core.vizual.*;
import core.functional.*;
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
        File baseDir = new File(baseDirPath != null ? baseDirPath : System.getProperty("user.dir"));
        // Căutăm directorul server_history în folderul curent sau în directorul rădăcină (dacă suntem în src)
        File candidateCurrent = new File(baseDir, "server_history");
        File candidateParent = (baseDir.getName().equals("src") && baseDir.getParentFile() != null)
                ? new File(baseDir.getParentFile(), "server_history")
                : null;

        if (candidateCurrent.exists()) {
            this.storageDir = candidateCurrent;
        } else if (candidateParent != null && candidateParent.exists()) {
            this.storageDir = candidateParent;
        } else {
            this.storageDir = candidateCurrent;
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    /**
     * Flux ObjectInputStream compatibil ce mapează transparent denumirile vechi de clase
     * (core.Message, Message etc.) către noul pachet core.functional.Message.
     */
    private static class CompatibleObjectInputStream extends ObjectInputStream {
        public CompatibleObjectInputStream(java.io.InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(java.io.ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            if ("core.Message".equals(name) || "Message".equals(name)) {
                return core.functional.Message.class;
            }
            if ("core.MessageType".equals(name) || "MessageType".equals(name)) {
                return core.functional.MessageType.class;
            }
            if ("core.FileAttachment".equals(name) || "FileAttachment".equals(name)) {
                return core.functional.FileAttachment.class;
            }
            return super.resolveClass(desc);
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
        List<Message> existing = loadBinaryHistory(message.getRoom());
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
     * Încarcă istoricul salvat pentru o cameră specificată prin numele său.
     */
    public synchronized List<Message> loadBinaryHistory(String roomName) {
        String safeRoomName = roomName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File binaryFile = new File(storageDir, "chat_" + safeRoomName + ".dat");
        return loadBinaryHistoryFromFile(binaryFile);
    }

    /**
     * Încarcă istoricul dintr-un fișier .dat concret cu filtrare și mapare de siguranță.
     */
    public synchronized List<Message> loadBinaryHistoryFromFile(File binaryFile) {
        if (binaryFile == null || !binaryFile.exists() || binaryFile.length() == 0) {
            return new ArrayList<>();
        }
        try (CompatibleObjectInputStream ois = new CompatibleObjectInputStream(new FileInputStream(binaryFile))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                List<?> rawList = (List<?>) obj;
                List<Message> cleanList = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Message) {
                        cleanList.add((Message) item);
                    }
                }
                return cleanList;
            }
        } catch (Exception e) {
            System.err.println("[HistoryManager] Nu s-a putut citi istoricul binar din " + binaryFile.getName() + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Descoperă toate camerele existente pe disc analizând fișierele de istoric.
     */
    public synchronized List<String> discoverRoomsOnDisk() {
        List<String> discoveredRooms = new ArrayList<>();
        File[] datFiles = storageDir.listFiles((dir, name) -> name.startsWith("chat_") && name.endsWith(".dat"));
        if (datFiles != null) {
            for (File file : datFiles) {
                List<Message> history = loadBinaryHistoryFromFile(file);
                if (!history.isEmpty()) {
                    String roomName = history.get(0).getRoom();
                    if (roomName != null && !roomName.trim().isEmpty() && !discoveredRooms.contains(roomName.trim())) {
                        discoveredRooms.add(roomName.trim());
                    }
                } else {
                    String fn = file.getName();
                    String fallbackName = fn.substring("chat_".length(), fn.length() - ".dat".length()).replace('_', ' ');
                    if (!discoveredRooms.contains(fallbackName)) {
                        discoveredRooms.add(fallbackName);
                    }
                }
            }
        }
        return discoveredRooms;
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
