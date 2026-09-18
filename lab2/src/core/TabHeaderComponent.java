package core;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Componentă personalizată pentru antetul unei file (Tab) din JTabbedPane.
 * Afișează titlul, indicatorul de modificare ('*') și un buton de închidere ('×').
 */
public class TabHeaderComponent extends JPanel {

    public interface TabCloseListener {
        void onCloseTab(TextEditorTab tab);
    }

    private final TextEditorTab editorTab;
    private final JTabbedPane tabbedPane;
    private final TabCloseListener closeListener;
    private final JLabel titleLabel;
    private final JButton closeButton;

    public TabHeaderComponent(TextEditorTab editorTab, JTabbedPane tabbedPane, TabCloseListener closeListener) {
        super(new FlowLayout(FlowLayout.LEFT, 5, 2));
        this.editorTab = editorTab;
        this.tabbedPane = tabbedPane;
        this.closeListener = closeListener;

        setOpaque(false);

        this.titleLabel = new JLabel(formatTitle());
        this.titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(titleLabel);

        this.closeButton = createCloseButton();
        add(closeButton);

        // Click pe antet selectează fila corespunzătoare
        MouseAdapter selectTabAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tabbedPane.indexOfComponent(editorTab);
                if (index != -1) {
                    tabbedPane.setSelectedIndex(index);
                }
            }
        };
        addMouseListener(selectTabAdapter);
        titleLabel.addMouseListener(selectTabAdapter);

        // Ascultă modificările tab-ului pentru a actualiza indicatorul '*'
        editorTab.addModificationListener((tab, isModified) -> updateTitle());
    }

    private String formatTitle() {
        return editorTab.getTitle() + (editorTab.isModified() ? " *" : "");
    }

    public void updateTitle() {
        titleLabel.setText(formatTitle());
        if (editorTab.getCurrentFile() != null) {
            setToolTipText(editorTab.getCurrentFile().getAbsolutePath());
        } else {
            setToolTipText("Fișier nesalvat");
        }
    }

    private JButton createCloseButton() {
        JButton btn = new JButton("×") {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(17, 17);
            }
        };

        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setToolTipText("Închide fila");
        btn.setForeground(new Color(130, 130, 130));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(new Color(220, 50, 50));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(new Color(130, 130, 130));
            }
        });

        btn.addActionListener(e -> {
            if (closeListener != null) {
                closeListener.onCloseTab(editorTab);
            }
        });

        return btn;
    }
}
