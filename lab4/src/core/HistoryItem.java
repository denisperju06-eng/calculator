package core;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Reprezintă o intrare individuală în istoricul de navigare.
 * Încapsulează informații despre URL, titlu, data și ora accesării.
 */
public class HistoryItem {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String url;
    private String title;
    private final LocalDateTime timestamp;

    public HistoryItem(String url, String title) {
        this(url, title, LocalDateTime.now());
    }

    public HistoryItem(String url, String title, LocalDateTime timestamp) {
        this.url = (url != null) ? url.trim() : "";
        this.title = (title != null && !title.trim().isEmpty()) ? title.trim() : this.url;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    /**
     * Serializare într-o linie de text pentru salvare în fișier.
     * Format: Titlu\tURL\tTimestamp
     */
    public String toFileLine() {
        // Înlocuim tab-urile și newline-urile pentru a păstra integritatea formatului TSV
        String safeTitle = title.replace("\t", " ").replace("\n", " ");
        String safeUrl = url.replace("\t", " ").replace("\n", " ");
        return safeTitle + "\t" + safeUrl + "\t" + getFormattedTimestamp();
    }

    /**
     * Parsare dintr-o linie de text citită din fișier.
     */
    public static HistoryItem fromFileLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\t");
        if (parts.length >= 3) {
            String title = parts[0];
            String url = parts[1];
            try {
                LocalDateTime time = LocalDateTime.parse(parts[2], FORMATTER);
                return new HistoryItem(url, title, time);
            } catch (Exception e) {
                return new HistoryItem(url, title, LocalDateTime.now());
            }
        } else if (parts.length == 2) {
            return new HistoryItem(parts[1], parts[0]);
        } else if (parts.length == 1) {
            return new HistoryItem(parts[0], parts[0]);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryItem that = (HistoryItem) o;
        return Objects.equals(url, that.url) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, timestamp);
    }

    @Override
    public String toString() {
        return "[" + getFormattedTimestamp() + "] " + title + " (" + url + ")";
    }
}
