import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Componentă personalizată pentru antetul unui tab din JTabbedPane.
 * Include iconiță, titlu dinamic și buton de închidere ('✕') cu efect vizual.
 */
public class TabHeaderComponent extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabbedPane;
    private final transient BrowserTab browserTab;
    private final JLabel titleLabel;
    private final JButton closeButton;

    public TabHeaderComponent(JTabbedPane tabbedPane, BrowserTab browserTab, Runnable onCloseAction) {
        super(new FlowLayout(FlowLayout.LEFT, 5, 2));
        this.tabbedPane = tabbedPane;
        this.browserTab = browserTab;
        setOpaque(false);

        // Iconiță tab
        JLabel iconLabel = new JLabel("🌐");
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        add(iconLabel);

        // Etichetă titlu
        titleLabel = new JLabel(formatTitle(browserTab.getTitle()));
        titleLabel.setToolTipText(browserTab.getTitle());
        titleLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        add(titleLabel);

        // Buton de închidere tab '✕'
        closeButton = new JButton("✕");
        closeButton.setPreferredSize(new Dimension(18, 18));
        closeButton.setFont(new Font("Dialog", Font.BOLD, 10));
        closeButton.setToolTipText("Închide tab-ul (Ctrl+W)");
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setForeground(Color.GRAY);

        // Efect de hover pentru butonul de închidere
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(Color.GRAY);
            }
        });

        closeButton.addActionListener(e -> {
            if (onCloseAction != null) {
                onCloseAction.run();
            }
        });

        add(closeButton);

        // Click pe antet pentru a selecta tab-ul corespunzător
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tabbedPane.indexOfComponent(browserTab);
                if (index != -1) {
                    tabbedPane.setSelectedIndex(index);
                }
            }
        });
    }

    public void updateTitle(String fullTitle) {
        titleLabel.setText(formatTitle(fullTitle));
        titleLabel.setToolTipText(fullTitle);
        revalidate();
        repaint();
    }

    private String formatTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Tab Nou";
        }
        String clean = title.trim();
        if (clean.length() > 18) {
            return clean.substring(0, 15) + "...";
        }
        return clean;
    }

    public BrowserTab getBrowserTab() {
        return browserTab;
    }
}
