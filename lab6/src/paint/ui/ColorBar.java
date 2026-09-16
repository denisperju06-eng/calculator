package paint.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Bara de culori conform cerinței c:
 * "Crearea barei de culori (cu selectarea culorii de desen și a culorii de fon/fundal)".
 *
 * Oferă:
 * - Culoare 1: Culoare de desen (Foreground)
 * - Culoare 2: Culoare de fundal / umplere (Background)
 * - Buton de inversare culori (Swap)
 * - Paletă rapidă cu 28 de culori prestabilite (Click stânga -> Culoare 1, Click dreapta -> Culoare 2)
 * - Buton "Editează culori..." cu JColorChooser complet
 */
public class ColorBar extends JPanel {

    public interface ColorChangeListener {
        void onPrimaryColorChanged(Color newColor);
        void onSecondaryColorChanged(Color newColor);
    }

    private Color primaryColor = Color.BLACK;      // Culoare de desen (Color 1)
    private Color secondaryColor = Color.WHITE;    // Culoare de fon/fundal (Color 2)

    private final JPanel primaryColorBox;
    private final JPanel secondaryColorBox;
    private final List<ColorChangeListener> listeners = new ArrayList<>();

    // Paleta de 28 de culori (2 rânduri de 14 culori)
    private static final Color[] PALETTE_ROW_1 = {
            new Color(0, 0, 0),        // Negru
            new Color(128, 128, 128),  // Gri închis
            new Color(128, 0, 0),      // Vișiniu / Maro
            new Color(255, 0, 0),      // Roșu
            new Color(255, 128, 0),    // Portocaliu
            new Color(255, 255, 0),    // Galben
            new Color(0, 128, 0),      // Verde închis
            new Color(0, 200, 0),      // Verde deschis
            new Color(0, 255, 255),    // Cyan
            new Color(0, 128, 255),    // Azur
            new Color(0, 0, 255),      // Albastru
            new Color(128, 0, 255),    // Violet
            new Color(255, 0, 255),    // Magenta
            new Color(139, 69, 19)     // Maro clasic
    };

    private static final Color[] PALETTE_ROW_2 = {
            new Color(255, 255, 255),  // Alb
            new Color(192, 192, 192),  // Gri deschis
            new Color(180, 50, 50),    // Cărămiziu
            new Color(255, 150, 150),  // Roz deschis
            new Color(255, 200, 120),  // Piersică
            new Color(255, 255, 180),  // Galben pal
            new Color(144, 238, 144),  // Verde pastel
            new Color(175, 255, 175),  // Menta
            new Color(200, 255, 255),  // Bleu pal
            new Color(173, 216, 230),  // Bleu ciel
            new Color(150, 150, 255),  // Albastru pastel
            new Color(216, 191, 216),  // Lavandă
            new Color(255, 182, 193),  // Roz bombon
            new Color(245, 222, 179)   // Bej / Grâu
    };

    public ColorBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        // 1. Panou Indicator Culoare 1 & Culoare 2
        JPanel indicatorsPanel = new JPanel();
        indicatorsPanel.setLayout(new BoxLayout(indicatorsPanel, BoxLayout.X_AXIS));
        indicatorsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Culori active", 0, 0, new Font("SansSerif", Font.PLAIN, 10)
        ));

        // Culoare 1 (Desen)
        JPanel col1Wrapper = new JPanel();
        col1Wrapper.setLayout(new BoxLayout(col1Wrapper, BoxLayout.Y_AXIS));
        JLabel lbl1 = new JLabel("Desen (1)", SwingConstants.CENTER);
        lbl1.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl1.setAlignmentX(CENTER_ALIGNMENT);

        primaryColorBox = new JPanel();
        primaryColorBox.setPreferredSize(new Dimension(32, 32));
        primaryColorBox.setMaximumSize(new Dimension(32, 32));
        primaryColorBox.setBackground(primaryColor);
        primaryColorBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        ));
        primaryColorBox.setToolTipText("Culoare de desen (Click stânga pe paletă sau dublu-click pentru selector)");
        primaryColorBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        primaryColorBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseCustomColor(true);
            }
        });

        col1Wrapper.add(lbl1);
        col1Wrapper.add(Box.createVerticalStrut(2));
        col1Wrapper.add(primaryColorBox);

        // Buton Swap (Inversare culori)
        JButton btnSwap = new JButton("⇄");
        btnSwap.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSwap.setToolTipText("Inversează Culoarea 1 cu Culoarea 2 (X)");
        btnSwap.setMargin(new java.awt.Insets(1, 4, 1, 4));
        btnSwap.setFocusable(false);
        btnSwap.addActionListener(e -> swapColors());

        // Culoare 2 (Fundal)
        JPanel col2Wrapper = new JPanel();
        col2Wrapper.setLayout(new BoxLayout(col2Wrapper, BoxLayout.Y_AXIS));
        JLabel lbl2 = new JLabel("Fundal (2)", SwingConstants.CENTER);
        lbl2.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl2.setAlignmentX(CENTER_ALIGNMENT);

        secondaryColorBox = new JPanel();
        secondaryColorBox.setPreferredSize(new Dimension(32, 32));
        secondaryColorBox.setMaximumSize(new Dimension(32, 32));
        secondaryColorBox.setBackground(secondaryColor);
        secondaryColorBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        ));
        secondaryColorBox.setToolTipText("Culoare de fundal/umplere (Click dreapta pe paletă sau dublu-click pentru selector)");
        secondaryColorBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        secondaryColorBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseCustomColor(false);
            }
        });

        col2Wrapper.add(lbl2);
        col2Wrapper.add(Box.createVerticalStrut(2));
        col2Wrapper.add(secondaryColorBox);

        indicatorsPanel.add(Box.createHorizontalStrut(4));
        indicatorsPanel.add(col1Wrapper);
        indicatorsPanel.add(Box.createHorizontalStrut(6));
        indicatorsPanel.add(btnSwap);
        indicatorsPanel.add(Box.createHorizontalStrut(6));
        indicatorsPanel.add(col2Wrapper);
        indicatorsPanel.add(Box.createHorizontalStrut(4));

        add(indicatorsPanel);

        // 2. Grilă Paletă de Culori (2x14)
        JPanel palettePanel = new JPanel(new GridLayout(2, 14, 2, 2));
        palettePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Paletă culori (Stânga=Desen, Dreapta=Fundal)", 0, 0, new Font("SansSerif", Font.PLAIN, 10)
        ));

        for (Color c : PALETTE_ROW_1) {
            palettePanel.add(createSwatchButton(c));
        }
        for (Color c : PALETTE_ROW_2) {
            palettePanel.add(createSwatchButton(c));
        }

        add(palettePanel);

        // 3. Buton Selector Culoare Avansat (JColorChooser)
        JButton btnMoreColors = new JButton("Editează culori...");
        btnMoreColors.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnMoreColors.setToolTipText("Deschide paleta avansată de culori");
        btnMoreColors.setFocusable(false);
        btnMoreColors.addActionListener(e -> chooseCustomColor(true));

        add(btnMoreColors);
    }

    private JPanel createSwatchButton(Color color) {
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(18, 18));
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        swatch.setToolTipText(String.format("RGB(%d, %d, %d) - Stânga: Culoare 1 | Dreapta: Culoare 2",
                color.getRed(), color.getGreen(), color.getBlue()));

        swatch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    setPrimaryColor(color);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    setSecondaryColor(color);
                }
            }
        });

        return swatch;
    }

    private void chooseCustomColor(boolean forPrimary) {
        String title = forPrimary ? "Alege Culoarea 1 (Desen)" : "Alege Culoarea 2 (Fundal)";
        Color initial = forPrimary ? primaryColor : secondaryColor;
        Color chosen = JColorChooser.showDialog(this, title, initial);
        if (chosen != null) {
            if (forPrimary) {
                setPrimaryColor(chosen);
            } else {
                setSecondaryColor(chosen);
            }
        }
    }

    public void swapColors() {
        Color temp = primaryColor;
        primaryColor = secondaryColor;
        secondaryColor = temp;
        primaryColorBox.setBackground(primaryColor);
        secondaryColorBox.setBackground(secondaryColor);
        notifyListeners();
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color color) {
        if (color == null) return;
        this.primaryColor = color;
        primaryColorBox.setBackground(color);
        for (ColorChangeListener l : listeners) {
            l.onPrimaryColorChanged(color);
        }
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color color) {
        if (color == null) return;
        this.secondaryColor = color;
        secondaryColorBox.setBackground(color);
        for (ColorChangeListener l : listeners) {
            l.onSecondaryColorChanged(color);
        }
    }

    public void addColorChangeListener(ColorChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (ColorChangeListener l : listeners) {
            l.onPrimaryColorChanged(primaryColor);
            l.onSecondaryColorChanged(secondaryColor);
        }
    }
}
