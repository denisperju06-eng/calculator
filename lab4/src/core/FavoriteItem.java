package core;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Reprezintă o pagină salvată la Favorite (Bookmark).
 * Încapsulează titlul paginii, URL-ul și data la care a fost adăugată.
 */
public class FavoriteItem {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String title;
    private final String url;
    private final LocalDateTime dateAdded;

    public FavoriteItem(String title, String url) {
        this(title, url, LocalDateTime.now());
    }

    public FavoriteItem(String title, String url, LocalDateTime dateAdded) {
        this.title = (title != null && !title.trim().isEmpty()) ? title.trim() : url;
        this.url = (url != null) ? url.trim() : "";
        this.dateAdded = (dateAdded != null) ? dateAdded : LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public String getFormattedDateAdded() {
        return dateAdded.format(FORMATTER);
    }

    /**
     * Serializare într-o linie de fișier (Format TSV: Titlu\tURL\tDataAdaugarii).
     */
    public String toFileLine() {
        String safeTitle = title.replace("\t", " ").replace("\n", " ");
        String safeUrl = url.replace("\t", " ").replace("\n", " ");
        return safeTitle + "\t" + safeUrl + "\t" + getFormattedDateAdded();
    }

    /**
     * Parsare dintr-o linie de fișier.
     */
    public static FavoriteItem fromFileLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\t");
        if (parts.length >= 3) {
            String title = parts[0];
            String url = parts[1];
            try {
                LocalDateTime date = LocalDateTime.parse(parts[2], FORMATTER);
                return new FavoriteItem(title, url, date);
            } catch (Exception e) {
                return new FavoriteItem(title, url, LocalDateTime.now());
            }
        } else if (parts.length == 2) {
            return new FavoriteItem(parts[0], parts[1]);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteItem that = (FavoriteItem) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return title + " (" + url + ")";
    }
}
