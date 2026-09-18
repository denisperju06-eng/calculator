package core;
import java.io.Serializable;

/**
 * Clasa care încapsulează un fișier transmis prin rețeaua de chat.
 * Respectă principiile POO (încapsulare, serializare pentru transfer binar prin Sockets).
 */
public class FileAttachment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final long fileSize;
    private final byte[] data;

    public FileAttachment(String fileName, long fileSize, byte[] data) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Returnează dimensiunea fișierului formatată lizibil (B, KB, MB).
     */
    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "FileAttachment{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + getFormattedSize() +
                '}';
    }
}
