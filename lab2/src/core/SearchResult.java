package core;
/**
 * Clasă imutabilă ce încapsulează rezultatul unei operații de căutare sau înlocuire.
 * Respectă principiul încapsulării din Paradigma Orientată pe Obiecte.
 */
public class SearchResult {
    private final boolean found;
    private final int startIndex;
    private final int length;
    private final String message;

    public SearchResult(boolean found, int startIndex, int length, String message) {
        this.found = found;
        this.startIndex = startIndex;
        this.length = length;
        this.message = message != null ? message : "";
    }

    public static SearchResult notFound(String message) {
        return new SearchResult(false, -1, 0, message);
    }

    public static SearchResult success(int startIndex, int length, String message) {
        return new SearchResult(true, startIndex, length, message);
    }

    public boolean isFound() {
        return found;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getLength() {
        return length;
    }

    public int getEndIndex() {
        return startIndex + length;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "found=" + found +
                ", startIndex=" + startIndex +
                ", length=" + length +
                ", message='" + message + '\'' +
                '}';
    }
}
