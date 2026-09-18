package core;
import javax.swing.JTextPane;
import javax.swing.text.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviciu dedicat pentru operațiile de căutare, înlocuire și evidențiere a subșirurilor.
 * Respectă principiul Responsabilității Unice (SRP) și încapsulează algoritmii de căutare.
 */
public class SearchService {

    public enum Direction {
        FORWARD,
        BACKWARD
    }

    // Painter special pentru evidențierea tuturor aparițiilor (Highlight All)
    private final Highlighter.HighlightPainter highlightPainter;
    private final List<Object> currentHighlightTags;

    public SearchService() {
        this.highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 235, 59, 180));
        this.currentHighlightTags = new ArrayList<>();
    }

    /**
     * Căutarea următorului subșir în funcție de direcție, sensibilitate la majuscule și reluare de la capăt.
     */
    public SearchResult findNext(JTextPane textPane, String query, Direction direction, boolean matchCase, boolean wrapAround) {
        if (textPane == null || query == null || query.isEmpty()) {
            return SearchResult.notFound("Introduceți un text pentru căutare.");
        }

        Document doc = textPane.getDocument();
        int docLength = doc.getLength();
        if (docLength == 0) {
            return SearchResult.notFound("Documentul este gol.");
        }

        try {
            String fullText = doc.getText(0, docLength);
            String searchContent = matchCase ? fullText : fullText.toLowerCase();
            String searchQuery = matchCase ? query : query.toLowerCase();

            int selectionStart = textPane.getSelectionStart();
            int selectionEnd = textPane.getSelectionEnd();
            int matchIndex = -1;

            if (direction == Direction.FORWARD) {
                // Căutare înainte (în jos) pornind de la finalul selecției curente
                int searchStart = selectionEnd;
                if (searchStart < docLength) {
                    matchIndex = searchContent.indexOf(searchQuery, searchStart);
                }

                // Dacă nu s-a găsit și este activată reluarea de la început
                if (matchIndex == -1 && wrapAround) {
                    matchIndex = searchContent.indexOf(searchQuery, 0);
                    if (matchIndex != -1 && matchIndex >= searchStart) {
                        // S-a găsit din nou la aceeași poziție, deci nu există alt rezultat
                    }
                }
            } else {
                // Căutare înapoi (în sus) pornind de la începutul selecției curente
                int searchStart = selectionStart - 1;
                if (searchStart >= 0) {
                    matchIndex = searchContent.lastIndexOf(searchQuery, searchStart);
                }

                // Dacă nu s-a găsit și este activată reluarea de la sfârșit
                if (matchIndex == -1 && wrapAround) {
                    matchIndex = searchContent.lastIndexOf(searchQuery, docLength - 1);
                }
            }

            if (matchIndex != -1) {
                // Selectăm textul găsit și derulăm vizualizarea
                textPane.setCaretPosition(matchIndex);
                textPane.moveCaretPosition(matchIndex + query.length());
                scrollCaretToVisible(textPane, matchIndex, query.length());
                return SearchResult.success(matchIndex, query.length(),
                        "Găsit la poziția " + matchIndex + ".");
            } else {
                return SearchResult.notFound("Subșirul '" + query + "' nu a fost găsit.");
            }

        } catch (BadLocationException e) {
            return SearchResult.notFound("Eroare la accesarea textului: " + e.getMessage());
        }
    }

    /**
     * Înlocuiește apariția curentă dacă este selectată, sau caută următoarea apariție.
     */
    public SearchResult replace(JTextPane textPane, String query, String replacement,
                                Direction direction, boolean matchCase, boolean wrapAround) {
        if (textPane == null || query == null || query.isEmpty()) {
            return SearchResult.notFound("Introduceți un text pentru căutare.");
        }
        if (replacement == null) {
            replacement = "";
        }

        StyledDocument doc = textPane.getStyledDocument();
        int selStart = textPane.getSelectionStart();
        int selEnd = textPane.getSelectionEnd();
        int selLength = selEnd - selStart;

        boolean currentSelectedMatches = false;
        if (selLength == query.length()) {
            try {
                String selectedText = doc.getText(selStart, selLength);
                currentSelectedMatches = matchCase ? selectedText.equals(query) : selectedText.equalsIgnoreCase(query);
            } catch (BadLocationException ignored) {
            }
        }

        if (currentSelectedMatches) {
            try {
                // Păstrăm atributele primului caracter din selecție pentru a menține stilizarea
                AttributeSet currentAttr = doc.getCharacterElement(selStart).getAttributes();
                SimpleAttributeSet preservedAttr = new SimpleAttributeSet(currentAttr);

                doc.remove(selStart, selLength);
                doc.insertString(selStart, replacement, preservedAttr);

                // Repoziționăm cursorul după textul înlocuit
                int newCaret = selStart + replacement.length();
                textPane.setCaretPosition(newCaret);

                // Căutăm automat următoarea apariție
                SearchResult nextResult = findNext(textPane, query, direction, matchCase, wrapAround);
                if (nextResult.isFound()) {
                    return SearchResult.success(selStart, replacement.length(),
                            "Înlocuit cu succes. " + nextResult.getMessage());
                } else {
                    return SearchResult.success(selStart, replacement.length(),
                            "Înlocuit cu succes. Nu mai există alte apariții.");
                }

            } catch (BadLocationException e) {
                return SearchResult.notFound("Eroare la înlocuire: " + e.getMessage());
            }
        } else {
            // Dacă nu e selectată o potrivire, căutăm întâi următoarea
            SearchResult findRes = findNext(textPane, query, direction, matchCase, wrapAround);
            if (findRes.isFound()) {
                return SearchResult.success(findRes.getStartIndex(), findRes.getLength(),
                        "Potrivire găsită. Apăsați din nou 'Înlocuiește' pentru a o substitui.");
            } else {
                return findRes;
            }
        }
    }

    /**
     * Înlocuiește toate aparițiile subșirului căutat în întregul document.
     * Căutarea se face de la coadă la cap pentru ca indicii să nu se decaleze.
     * Păstrează stilurile caracterelor înlocuite.
     */
    public int replaceAll(JTextPane textPane, String query, String replacement, boolean matchCase) {
        if (textPane == null || query == null || query.isEmpty()) {
            return 0;
        }
        if (replacement == null) {
            replacement = "";
        }

        StyledDocument doc = textPane.getStyledDocument();
        int docLength = doc.getLength();
        if (docLength == 0) {
            return 0;
        }

        try {
            String fullText = doc.getText(0, docLength);
            String searchContent = matchCase ? fullText : fullText.toLowerCase();
            String searchQuery = matchCase ? query : query.toLowerCase();

            List<Integer> matchIndices = new ArrayList<>();
            int idx = 0;
            while ((idx = searchContent.indexOf(searchQuery, idx)) != -1) {
                matchIndices.add(idx);
                idx += searchQuery.length();
            }

            if (matchIndices.isEmpty()) {
                return 0;
            }

            // Înlocuim în ordine inversă (de la sfârșit la început)
            for (int i = matchIndices.size() - 1; i >= 0; i--) {
                int pos = matchIndices.get(i);
                AttributeSet attr = doc.getCharacterElement(pos).getAttributes();
                SimpleAttributeSet preservedAttr = new SimpleAttributeSet(attr);

                doc.remove(pos, query.length());
                doc.insertString(pos, replacement, preservedAttr);
            }

            return matchIndices.size();

        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Selectează / evidențiază TOATE aparițiile subșirului căutat în document (Select All matches).
     * Folosește Highlighter-ul Swing pentru a marca simultan toate pozițiile.
     */
    public int highlightAll(JTextPane textPane, String query, boolean matchCase) {
        clearHighlights(textPane);
        if (textPane == null || query == null || query.isEmpty()) {
            return 0;
        }

        Document doc = textPane.getDocument();
        int docLength = doc.getLength();
        if (docLength == 0) {
            return 0;
        }

        try {
            String fullText = doc.getText(0, docLength);
            String searchContent = matchCase ? fullText : fullText.toLowerCase();
            String searchQuery = matchCase ? query : query.toLowerCase();

            Highlighter highlighter = textPane.getHighlighter();
            int idx = 0;
            int count = 0;
            int firstMatch = -1;

            while ((idx = searchContent.indexOf(searchQuery, idx)) != -1) {
                int end = idx + query.length();
                Object tag = highlighter.addHighlight(idx, end, highlightPainter);
                currentHighlightTags.add(tag);
                if (firstMatch == -1) {
                    firstMatch = idx;
                }
                count++;
                idx = end;
            }

            if (firstMatch != -1) {
                // Selectăm prima apariție și scroll-ăm către ea
                textPane.setCaretPosition(firstMatch);
                textPane.moveCaretPosition(firstMatch + query.length());
                scrollCaretToVisible(textPane, firstMatch, query.length());
            }

            return count;

        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Șterge evidențierile active de căutare din editor.
     */
    public void clearHighlights(JTextPane textPane) {
        if (textPane == null) return;
        Highlighter highlighter = textPane.getHighlighter();
        for (Object tag : currentHighlightTags) {
            highlighter.removeHighlight(tag);
        }
        currentHighlightTags.clear();
    }

    /**
     * Asigură vizibilitatea selecției în JScrollPane prin derulare automată.
     */
    private void scrollCaretToVisible(JTextPane textPane, int start, int length) {
        try {
            Rectangle rect = textPane.modelToView2D(start) != null ?
                    textPane.modelToView2D(start).getBounds() : null;
            if (rect != null) {
                textPane.scrollRectToVisible(rect);
            }
        } catch (BadLocationException ignored) {
        }
    }
}
