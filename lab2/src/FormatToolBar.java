import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Bară de instrumente pentru formatarea textului bogat (Rich Text Formatting Toolbar).
 * Permite setarea fontului, mărimii, stilurilor (Bold, Italic, Underline), culorii și alinierii.
 * Respectă cerința suprapunerii stilurilor.
 */
public class FormatToolBar extends JToolBar implements CaretListener {

    private final StyleService styleService;
    private TextEditorTab activeTab;
    private boolean isSyncingFromEditor = false;

    private JComboBox<String> fontBox;
    private JComboBox<Integer> sizeBox;
    private JToggleButton boldBtn;
    private JToggleButton italicBtn;
    private JToggleButton underlineBtn;
    private JButton colorBtn;
    private JButton highlightBtn;
    private JToggleButton alignLeftBtn;
    private JToggleButton alignCenterBtn;
    private JToggleButton alignRightBtn;
    private JToggleButton alignJustifyBtn;

    private Color currentColor = Color.BLACK;
    private Color currentHighlightColor = null;

    public FormatToolBar(StyleService styleService) {
        super("Formatare");
        this.styleService = styleService;
        setFloatable(false);
        setRollover(true);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(215, 220, 225)));

        initUI();
    }

    private void initUI() {
        // 1. Selector Familie Font
        String[] popularFonts = {
                "Arial", "Calibri", "Comic Sans MS", "Courier New", "Georgia",
                "Helvetica", "Lucida Console", "Segoe UI", "Tahoma", "Times New Roman",
                "Trebuchet MS", "Verdana", "Monospaced", "Serif", "SansSerif"
        };
        // Obținem fonturile disponibile pe sistem
        String[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        java.util.LinkedHashSet<String> combinedFonts = new java.util.LinkedHashSet<>(Arrays.asList(popularFonts));
        combinedFonts.addAll(Arrays.asList(systemFonts));

        fontBox = new JComboBox<>(combinedFonts.toArray(new String[0]));
        fontBox.setMaximumSize(new Dimension(160, 28));
        fontBox.setPreferredSize(new Dimension(140, 28));
        fontBox.setSelectedItem("Arial");
        fontBox.setToolTipText("Familia fontului");
        fontBox.addActionListener(e -> {
            if (!isSyncingFromEditor && activeTab != null) {
                String family = (String) fontBox.getSelectedItem();
                styleService.setFontFamily(activeTab.getTextPane(), family);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        add(fontBox);
        addSeparator(new Dimension(6, 28));

        // 2. Selector Mărime Font
        Integer[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36, 48, 72};
        sizeBox = new JComboBox<>(fontSizes);
        sizeBox.setMaximumSize(new Dimension(65, 28));
        sizeBox.setPreferredSize(new Dimension(60, 28));
        sizeBox.setSelectedItem(14);
        sizeBox.setToolTipText("Mărimea fontului");
        sizeBox.addActionListener(e -> {
            if (!isSyncingFromEditor && activeTab != null) {
                Integer size = (Integer) sizeBox.getSelectedItem();
                if (size != null) {
                    styleService.setFontSize(activeTab.getTextPane(), size);
                    activeTab.getTextPane().requestFocusInWindow();
                }
            }
        });
        add(sizeBox);
        addSeparator(new Dimension(6, 28));

        // 3. Buton Bold
        boldBtn = new JToggleButton("B");
        boldBtn.setFont(new Font("Serif", Font.BOLD, 14));
        boldBtn.setToolTipText("Aldin (Bold) - Ctrl+B");
        boldBtn.setPreferredSize(new Dimension(32, 28));
        boldBtn.setFocusable(false);
        boldBtn.addActionListener(e -> {
            if (!isSyncingFromEditor && activeTab != null) {
                boolean newState = styleService.toggleBold(activeTab.getTextPane());
                boldBtn.setSelected(newState);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        add(boldBtn);

        // 4. Buton Italic
        italicBtn = new JToggleButton("I");
        italicBtn.setFont(new Font("Serif", Font.ITALIC, 14));
        italicBtn.setToolTipText("Cursiv (Italic) - Ctrl+I");
        italicBtn.setPreferredSize(new Dimension(32, 28));
        italicBtn.setFocusable(false);
        italicBtn.addActionListener(e -> {
            if (!isSyncingFromEditor && activeTab != null) {
                boolean newState = styleService.toggleItalic(activeTab.getTextPane());
                italicBtn.setSelected(newState);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        add(italicBtn);

        // 5. Buton Underline
        underlineBtn = new JToggleButton("U");
        underlineBtn.setFont(new Font("Serif", Font.PLAIN, 14));
        underlineBtn.setToolTipText("Subliniat (Underline) - Ctrl+U");
        underlineBtn.setPreferredSize(new Dimension(32, 28));
        underlineBtn.setFocusable(false);
        underlineBtn.addActionListener(e -> {
            if (!isSyncingFromEditor && activeTab != null) {
                boolean newState = styleService.toggleUnderline(activeTab.getTextPane());
                underlineBtn.setSelected(newState);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        add(underlineBtn);
        addSeparator(new Dimension(6, 28));

        // 6. Culoare Text
        colorBtn = new JButton("A");
        colorBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        colorBtn.setToolTipText("Culoare text");
        colorBtn.setPreferredSize(new Dimension(34, 28));
        colorBtn.setFocusable(false);
        updateColorIcon(colorBtn, currentColor);
        colorBtn.addActionListener(e -> {
            if (activeTab != null) {
                Color chosen = JColorChooser.showDialog(this, "Selectați culoarea textului", currentColor);
                if (chosen != null) {
                    currentColor = chosen;
                    updateColorIcon(colorBtn, currentColor);
                    styleService.setForegroundColor(activeTab.getTextPane(), currentColor);
                    activeTab.getTextPane().requestFocusInWindow();
                }
            }
        });
        add(colorBtn);

        // 7. Culoare Evidențiere (Highlight)
        highlightBtn = new JButton("🖊");
        highlightBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        highlightBtn.setToolTipText("Culoare evidențiere fundal");
        highlightBtn.setPreferredSize(new Dimension(34, 28));
        highlightBtn.setFocusable(false);
        highlightBtn.addActionListener(e -> {
            if (activeTab != null) {
                Color chosen = JColorChooser.showDialog(this, "Selectați culoarea de evidențiere", currentHighlightColor != null ? currentHighlightColor : Color.YELLOW);
                if (chosen != null) {
                    currentHighlightColor = chosen;
                    styleService.setBackgroundColor(activeTab.getTextPane(), currentHighlightColor);
                    activeTab.getTextPane().requestFocusInWindow();
                }
            }
        });
        add(highlightBtn);
        addSeparator(new Dimension(6, 28));

        // 8. Aliniere text
        ButtonGroup alignGroup = new ButtonGroup();
        alignLeftBtn = new JToggleButton("⯇");
        alignLeftBtn.setToolTipText("Aliniere stânga");
        alignLeftBtn.setPreferredSize(new Dimension(30, 28));
        alignLeftBtn.setFocusable(false);
        alignLeftBtn.setSelected(true);
        alignLeftBtn.addActionListener(e -> {
            if (activeTab != null) {
                styleService.setAlignment(activeTab.getTextPane(), StyleConstants.ALIGN_LEFT);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        alignGroup.add(alignLeftBtn);
        add(alignLeftBtn);

        alignCenterBtn = new JToggleButton("≡");
        alignCenterBtn.setToolTipText("Centrare");
        alignCenterBtn.setPreferredSize(new Dimension(30, 28));
        alignCenterBtn.setFocusable(false);
        alignCenterBtn.addActionListener(e -> {
            if (activeTab != null) {
                styleService.setAlignment(activeTab.getTextPane(), StyleConstants.ALIGN_CENTER);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        alignGroup.add(alignCenterBtn);
        add(alignCenterBtn);

        alignRightBtn = new JToggleButton("⯈");
        alignRightBtn.setToolTipText("Aliniere dreapta");
        alignRightBtn.setPreferredSize(new Dimension(30, 28));
        alignRightBtn.setFocusable(false);
        alignRightBtn.addActionListener(e -> {
            if (activeTab != null) {
                styleService.setAlignment(activeTab.getTextPane(), StyleConstants.ALIGN_RIGHT);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        alignGroup.add(alignRightBtn);
        add(alignRightBtn);

        alignJustifyBtn = new JToggleButton("≣");
        alignJustifyBtn.setToolTipText("Aliniere stânga-dreapta (Justify)");
        alignJustifyBtn.setPreferredSize(new Dimension(30, 28));
        alignJustifyBtn.setFocusable(false);
        alignJustifyBtn.addActionListener(e -> {
            if (activeTab != null) {
                styleService.setAlignment(activeTab.getTextPane(), StyleConstants.ALIGN_JUSTIFIED);
                activeTab.getTextPane().requestFocusInWindow();
            }
        });
        alignGroup.add(alignJustifyBtn);
        add(alignJustifyBtn);
    }

    private void updateColorIcon(JButton button, Color color) {
        int w = 16;
        int h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0, 0, w - 1, h - 1);
        g2.dispose();
        button.setIcon(new ImageIcon(img));
    }

    public void setActiveTab(TextEditorTab tab) {
        if (this.activeTab != null) {
            this.activeTab.getTextPane().removeCaretListener(this);
        }

        this.activeTab = tab;

        if (this.activeTab != null) {
            this.activeTab.getTextPane().addCaretListener(this);
            syncWithEditor();
        }
    }

    private void syncWithEditor() {
        if (activeTab == null) return;

        isSyncingFromEditor = true;
        try {
            AttributeSet attrs = styleService.getActiveAttributes(activeTab.getTextPane());

            boolean isBold = StyleConstants.isBold(attrs);
            boolean isItalic = StyleConstants.isItalic(attrs);
            boolean isUnderline = StyleConstants.isUnderline(attrs);
            String family = StyleConstants.getFontFamily(attrs);
            int size = StyleConstants.getFontSize(attrs);
            Color fg = StyleConstants.getForeground(attrs);

            boldBtn.setSelected(isBold);
            italicBtn.setSelected(isItalic);
            underlineBtn.setSelected(isUnderline);

            if (family != null) {
                fontBox.setSelectedItem(family);
            }
            if (size > 0) {
                sizeBox.setSelectedItem(size);
            }
            if (fg != null) {
                currentColor = fg;
                updateColorIcon(colorBtn, currentColor);
            }
        } finally {
            isSyncingFromEditor = false;
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        syncWithEditor();
    }
}
