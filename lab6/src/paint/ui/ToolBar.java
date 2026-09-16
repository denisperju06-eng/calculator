package paint.ui;

import paint.model.BezierCurve;
import paint.tools.ToolType;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;

/**
 * Bara de instrumente (Toolbar) conform cerinței b:
 * "Crearea barei de instrumente".
 *
 * Conține butoane pentru selecția instrumentelor, reglarea grosimii liniei,
 * opțiunea de umplere a figurilor, comutatorul pentru motorul curbelor Bezier,
 * precum și butoane pentru Undo, Redo și Curățare Pânză.
 */
public class ToolBar extends JToolBar {

    private final PaintCanvas canvas;
    private final ButtonGroup toolGroup = new ButtonGroup();
    private final Map<ToolType, JToggleButton> toolButtons = new EnumMap<>(ToolType.class);

    private JSpinner strokeSpinner;
    private JCheckBox fillCheckBox;
    private JComboBox<String> bezierEngineCombo;

    public ToolBar(PaintCanvas canvas) {
        super("Instrumente de desenare");
        this.canvas = canvas;
        setFloatable(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        initTools();
        addSeparator(new Dimension(10, 24));
        initStrokeControl();
        addSeparator(new Dimension(10, 24));
        initFillControl();
        addSeparator(new Dimension(10, 24));
        initBezierEngineControl();
        addSeparator(new Dimension(10, 24));
        initActionButtons();
    }

    private void initTools() {
        // Instrumente uzuale
        addToolButton(ToolType.PENCIL, "Creion", "Desenare liberă (P)");
        addToolButton(ToolType.LINE, "Linie", "Trasează o linie dreaptă (L)");
        addToolButton(ToolType.RECTANGLE, "Dreptunghi", "Trasează un dreptunghi (R)");
        addToolButton(ToolType.OVAL, "Oval", "Trasează un cerc sau o elipsă (O)");
        addToolButton(ToolType.BEZIER_QUAD, "Bezier 3P", "Curbă Bezier Pătratică (1 punct control)");
        addToolButton(ToolType.BEZIER_CUBIC, "Bezier 4P", "Curbă Bezier Cubică (2 puncte control)");
        addToolButton(ToolType.BUCKET_FILL, "Găleată", "Umple o zonă închisă cu culoare (B)");
        addToolButton(ToolType.ERASER, "Radieră", "Șterge cu culoarea de fundal (E)");
        addToolButton(ToolType.COLOR_PICKER, "Pipetă", "Alege culoarea de pe pânză (I)");

        // Selectăm creionul implicit
        JToggleButton pencilBtn = toolButtons.get(ToolType.PENCIL);
        if (pencilBtn != null) {
            pencilBtn.setSelected(true);
        }
    }

    private void addToolButton(ToolType toolType, String text, String tooltip) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);

        btn.addActionListener(e -> {
            canvas.setCurrentTool(toolType);
        });

        toolGroup.add(btn);
        toolButtons.put(toolType, btn);
        add(btn);
    }

    private void initStrokeControl() {
        JLabel strokeLabel = new JLabel("Grosime: ");
        strokeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(strokeLabel);

        SpinnerNumberModel model = new SpinnerNumberModel(3, 1, 64, 1);
        strokeSpinner = new JSpinner(model);
        strokeSpinner.setPreferredSize(new Dimension(50, 24));
        strokeSpinner.setMaximumSize(new Dimension(50, 24));
        strokeSpinner.setFont(new Font("SansSerif", Font.PLAIN, 11));
        strokeSpinner.setToolTipText("Grosimea liniei de contur (1 - 64 px)");

        strokeSpinner.addChangeListener(e -> {
            int val = (Integer) strokeSpinner.getValue();
            canvas.setStrokeWidth((float) val);
        });

        add(strokeSpinner);
        JLabel pxLabel = new JLabel(" px");
        pxLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(pxLabel);
    }

    private void initFillControl() {
        fillCheckBox = new JCheckBox("Umplere figură");
        fillCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 11));
        fillCheckBox.setToolTipText("Bifați pentru a umple interiorul figurilor cu Culoarea 2 (Fundal)");
        fillCheckBox.setFocusable(false);

        fillCheckBox.addActionListener(e -> {
            canvas.setFilled(fillCheckBox.isSelected());
        });

        add(fillCheckBox);
    }

    private void initBezierEngineControl() {
        JLabel bezierLabel = new JLabel("Motor Bezier: ");
        bezierLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(bezierLabel);

        String[] engines = {"Java2D (Geometrie)", "Matematic (Bernstein)"};
        bezierEngineCombo = new JComboBox<>(engines);
        bezierEngineCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bezierEngineCombo.setPreferredSize(new Dimension(155, 24));
        bezierEngineCombo.setMaximumSize(new Dimension(155, 24));
        bezierEngineCombo.setToolTipText("Selectați motorul de randare pentru curbele Bezier conform cerinței");
        bezierEngineCombo.setFocusable(false);

        bezierEngineCombo.addActionListener(e -> {
            int idx = bezierEngineCombo.getSelectedIndex();
            if (idx == 0) {
                canvas.setBezierEngine(BezierCurve.RenderEngine.JAVA2D);
            } else {
                canvas.setBezierEngine(BezierCurve.RenderEngine.MATH);
            }
        });

        add(bezierEngineCombo);
    }

    private void initActionButtons() {
        JButton btnUndo = new JButton("↶ Undo");
        btnUndo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnUndo.setToolTipText("Anulează ultima operațiune (Ctrl+Z)");
        btnUndo.setFocusable(false);
        btnUndo.addActionListener(e -> canvas.undo());
        add(btnUndo);

        JButton btnRedo = new JButton("↷ Redo");
        btnRedo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnRedo.setToolTipText("Reface operațiunea anulată (Ctrl+Y)");
        btnRedo.setFocusable(false);
        btnRedo.addActionListener(e -> canvas.redo());
        add(btnRedo);

        JButton btnClear = new JButton("🗑 Curăță");
        btnClear.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnClear.setToolTipText("Curăță întreaga pânză cu culoarea de fundal");
        btnClear.setFocusable(false);
        btnClear.addActionListener(e -> canvas.clearCanvas(canvas.getSecondaryColor()));
        add(btnClear);
    }

    public void selectTool(ToolType type) {
        JToggleButton btn = toolButtons.get(type);
        if (btn != null) {
            btn.setSelected(true);
            canvas.setCurrentTool(type);
        }
    }
}
