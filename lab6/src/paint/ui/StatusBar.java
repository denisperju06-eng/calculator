package paint.ui;

import paint.tools.ToolType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Bara de stare (Status Bar) plasată în partea inferioară a aplicației.
 * Afișează coordonatele cursorului pe pânză, dimensiunea pânzei și instrucțiuni utile pentru instrumentul activ.
 */
public class StatusBar extends JPanel {

    private final JLabel coordinatesLabel;
    private final JLabel canvasSizeLabel;
    private final JLabel messageLabel;

    public StatusBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        setPreferredSize(new Dimension(800, 24));

        Font font = new Font("SansSerif", Font.PLAIN, 11);

        messageLabel = new JLabel("Gata de desenat.");
        messageLabel.setFont(font);

        coordinatesLabel = new JLabel("X: 0, Y: 0 px");
        coordinatesLabel.setFont(font);
        coordinatesLabel.setPreferredSize(new Dimension(130, 18));
        coordinatesLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        canvasSizeLabel = new JLabel("Dimensiune: 800 x 600 px");
        canvasSizeLabel.setFont(font);
        canvasSizeLabel.setPreferredSize(new Dimension(180, 18));
        canvasSizeLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        add(messageLabel);
        add(Box.createHorizontalGlue());
        add(coordinatesLabel);
        add(Box.createHorizontalStrut(10));
        add(canvasSizeLabel);
    }

    public void setCoordinates(int x, int y) {
        if (x < 0 || y < 0) {
            coordinatesLabel.setText("X: -, Y: - px");
        } else {
            coordinatesLabel.setText(String.format("X: %d, Y: %d px", x, y));
        }
    }

    public void setCanvasSize(int width, int height) {
        canvasSizeLabel.setText(String.format("Dimensiune: %d x %d px", width, height));
    }

    public void setMessage(String message) {
        messageLabel.setText(message != null ? message : "");
    }

    public void setToolHint(ToolType tool) {
        if (tool == null) {
            setMessage("Selectați un instrument din bara de unelte.");
            return;
        }

        switch (tool) {
            case PENCIL:
                setMessage("Creion: Trageți cu mouse-ul pentru desenare liberă.");
                break;
            case LINE:
                setMessage("Linie: Apăsați și trageți pentru a trasa un segment de dreaptă.");
                break;
            case RECTANGLE:
                setMessage("Dreptunghi: Apăsați și trageți pentru a desena un dreptunghi.");
                break;
            case OVAL:
                setMessage("Oval: Apăsați și trageți pentru a desena o elipsă sau un cerc.");
                break;
            case BEZIER_QUAD:
                setMessage("Bezier Pătratic: 1. Trageți linia de bază (Start->End) | 2. Deplasați și faceți clic pentru punctul de control.");
                break;
            case BEZIER_CUBIC:
                setMessage("Bezier Cubic: 1. Trageți linia de bază | 2. Clic pt Punct Control 1 | 3. Clic pt Punct Control 2.");
                break;
            case BUCKET_FILL:
                setMessage("Găleată: Faceți clic pe o zonă închisă pentru a o colora cu Culoarea 1.");
                break;
            case ERASER:
                setMessage("Radieră: Trageți pentru a șterge porțiuni (desenează cu Culoarea 2).");
                break;
            case COLOR_PICKER:
                setMessage("Pipetă: Clic stânga pentru Culoare 1, Clic dreapta pentru Culoare 2.");
                break;
        }
    }
}
