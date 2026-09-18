package core;
import javax.swing.JTextPane;
import javax.swing.text.*;
import java.awt.Color;

/**
 * Serviciu care gestionează stilizarea textului (Rich Text) respectând cerința:
 * "Stilurile trebuie să se suprapună."
 * 
 * Aplicarea unui stil (ex: Italic) nu elimină stilurile existente (Bold, Underline, Culoare, Font).
 * Folosește StyledDocument.setCharacterAttributes cu parametrul replace = false.
 */
public class StyleService {

    /**
     * Comută starea Bold (aldin) pentru selecție sau pentru poziția curentă a cursorului.
     * Păstrează toate celelalte atribute existente (italic, underline, culoare, mărime etc.).
     * @return noua stare de bold (true dacă este activat, false dacă este dezactivat)
     */
    public boolean toggleBold(JTextPane textPane) {
        if (textPane == null) return false;

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();

        boolean newState;
        if (start != end) {
            // Dacă tot textul selectat este deja bold, îl dezactivăm; altfel îl activăm
            boolean allBold = true;
            for (int i = start; i < end; i++) {
                AttributeSet attr = doc.getCharacterElement(i).getAttributes();
                if (!StyleConstants.isBold(attr)) {
                    allBold = false;
                    break;
                }
            }
            newState = !allBold;

            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setBold(sas, newState);
            // replace = false asigură suprapunerea stilurilor existente!
            doc.setCharacterAttributes(start, end - start, sas, false);
        } else {
            MutableAttributeSet inputAttr = textPane.getInputAttributes();
            newState = !StyleConstants.isBold(inputAttr);
            StyleConstants.setBold(inputAttr, newState);
        }
        return newState;
    }

    /**
     * Comută starea Italic (cursiv) pentru selecție sau pentru poziția curentă a cursorului.
     * Păstrează celelalte atribute fără a le suprascrie.
     * @return noua stare de italic
     */
    public boolean toggleItalic(JTextPane textPane) {
        if (textPane == null) return false;

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();

        boolean newState;
        if (start != end) {
            boolean allItalic = true;
            for (int i = start; i < end; i++) {
                AttributeSet attr = doc.getCharacterElement(i).getAttributes();
                if (!StyleConstants.isItalic(attr)) {
                    allItalic = false;
                    break;
                }
            }
            newState = !allItalic;

            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setItalic(sas, newState);
            doc.setCharacterAttributes(start, end - start, sas, false);
        } else {
            MutableAttributeSet inputAttr = textPane.getInputAttributes();
            newState = !StyleConstants.isItalic(inputAttr);
            StyleConstants.setItalic(inputAttr, newState);
        }
        return newState;
    }

    /**
     * Comută starea Underline (subliniat) pentru selecție sau cursor.
     * Păstrează celelalte atribute intacte.
     * @return noua stare de subliniere
     */
    public boolean toggleUnderline(JTextPane textPane) {
        if (textPane == null) return false;

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();

        boolean newState;
        if (start != end) {
            boolean allUnderline = true;
            for (int i = start; i < end; i++) {
                AttributeSet attr = doc.getCharacterElement(i).getAttributes();
                if (!StyleConstants.isUnderline(attr)) {
                    allUnderline = false;
                    break;
                }
            }
            newState = !allUnderline;

            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setUnderline(sas, newState);
            doc.setCharacterAttributes(start, end - start, sas, false);
        } else {
            MutableAttributeSet inputAttr = textPane.getInputAttributes();
            newState = !StyleConstants.isUnderline(inputAttr);
            StyleConstants.setUnderline(inputAttr, newState);
        }
        return newState;
    }

    /**
     * Modifică familia fontului (ex: Arial, Times New Roman etc.) pe subșirul selectat.
     */
    public void setFontFamily(JTextPane textPane, String fontFamily) {
        if (textPane == null || fontFamily == null) return;

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setFontFamily(sas, fontFamily);

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, sas, false);
        }
        StyleConstants.setFontFamily(textPane.getInputAttributes(), fontFamily);
    }

    /**
     * Modifică mărimea fontului pe subșirul selectat.
     */
    public void setFontSize(JTextPane textPane, int fontSize) {
        if (textPane == null || fontSize <= 0) return;

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setFontSize(sas, fontSize);

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, sas, false);
        }
        StyleConstants.setFontSize(textPane.getInputAttributes(), fontSize);
    }

    /**
     * Modifică culoarea textului pe subșirul selectat (sau viitorul text introdus).
     */
    public void setForegroundColor(JTextPane textPane, Color color) {
        if (textPane == null || color == null) return;

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, color);

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, sas, false);
        }
        StyleConstants.setForeground(textPane.getInputAttributes(), color);
    }

    /**
     * Modifică culoarea de fundal (evidențiere / highlight) pe subșirul selectat.
     */
    public void setBackgroundColor(JTextPane textPane, Color color) {
        if (textPane == null) return;

        SimpleAttributeSet sas = new SimpleAttributeSet();
        if (color != null) {
            StyleConstants.setBackground(sas, color);
        } else {
            sas.removeAttribute(StyleConstants.Background);
        }

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, sas, false);
        }
        if (color != null) {
            StyleConstants.setBackground(textPane.getInputAttributes(), color);
        }
    }

    /**
     * Setează alinierea paragrafului curent (Left, Center, Right, Justify).
     */
    public void setAlignment(JTextPane textPane, int alignment) {
        if (textPane == null) return;

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setAlignment(sas, alignment);

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        textPane.getStyledDocument().setParagraphAttributes(start, end - start, sas, false);
    }

    /**
     * Elimină stilizarea specifică (revine la text simplu implicit) pe selecție.
     */
    public void clearFormatting(JTextPane textPane) {
        if (textPane == null) return;

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
            StyleConstants.setFontFamily(defaultAttr, "Arial");
            StyleConstants.setFontSize(defaultAttr, 14);
            StyleConstants.setForeground(defaultAttr, Color.BLACK);
            StyleConstants.setBold(defaultAttr, false);
            StyleConstants.setItalic(defaultAttr, false);
            StyleConstants.setUnderline(defaultAttr, false);
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, defaultAttr, true);
        }
    }

    /**
     * Obține atributele active de la poziția curentă a cursorului sau selecției.
     */
    public AttributeSet getActiveAttributes(JTextPane textPane) {
        return getAttributesAtCaretOrSelection(textPane);
    }

    /**
     * Obține atributele caracterului de la poziția curentă a cursorului sau selecției.
     */
    public AttributeSet getAttributesAtCaretOrSelection(JTextPane textPane) {
        if (textPane == null) return new SimpleAttributeSet();

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            return textPane.getStyledDocument().getCharacterElement(start).getAttributes();
        } else {
            int pos = textPane.getCaretPosition();
            if (pos > 0) {
                return textPane.getStyledDocument().getCharacterElement(pos - 1).getAttributes();
            }
            return textPane.getInputAttributes();
        }
    }
}
