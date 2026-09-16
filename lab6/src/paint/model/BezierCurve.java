package paint.model;

import paint.tools.BezierMath;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;

/**
 * Clasa derivată 'BezierCurve' pentru desenarea curbelor Bezier.
 * Moștenește clasa de bază Shape.
 *
 * Suportă:
 * 1. Curbe Bezier pătratice (QuadCurve2D - 1 punct de control).
 * 2. Curbe Bezier cubice (CubicCurve2D - 2 puncte de control).
 * 3. Randare fie prin clasele native Java2D (QuadCurve2D/CubicCurve2D),
 *    fie prin evaluarea formulei matematice explicite (BezierMath).
 * 4. Afișarea reperelor vizuale (puncte de control și linii tangente ghidaj conform https://bezier.method.ac).
 */
public class BezierCurve extends Shape {
    private static final long serialVersionUID = 1L;

    public enum CurveType {
        QUADRATIC, // 3 puncte: Start (P0), Control (P1), End (P2)
        CUBIC      // 4 puncte: Start (P0), Control 1 (P1), Control 2 (P2), End (P3)
    }

    public enum RenderEngine {
        JAVA2D,    // java.awt.geom.QuadCurve2D / CubicCurve2D
        MATH       // Formule matematice Bernstein polinomiale din BezierMath
    }

    private CurveType curveType;
    private RenderEngine renderEngine = RenderEngine.JAVA2D;

    // Puncte de control
    private int ctrlX1, ctrlY1; // P1 (pentru pătratică și cubică)
    private int ctrlX2, ctrlY2; // P2 (doar pentru cubică)

    /**
     * Constructor pentru curbă pătratică (Quad Bezier).
     */
    public BezierCurve(int x1, int y1, int x2, int y2, int ctrlX, int ctrlY,
                       Color strokeColor, Color fillColor, float strokeWidth, boolean isFilled) {
        super(x1, y1, x2, y2, strokeColor, fillColor, strokeWidth, isFilled);
        this.curveType = CurveType.QUADRATIC;
        this.ctrlX1 = ctrlX;
        this.ctrlY1 = ctrlY;
        this.ctrlX2 = ctrlX;
        this.ctrlY2 = ctrlY;
    }

    /**
     * Constructor pentru curbă cubică (Cubic Bezier).
     */
    public BezierCurve(int x1, int y1, int x2, int y2,
                       int ctrlX1, int ctrlY1, int ctrlX2, int ctrlY2,
                       Color strokeColor, Color fillColor, float strokeWidth, boolean isFilled) {
        super(x1, y1, x2, y2, strokeColor, fillColor, strokeWidth, isFilled);
        this.curveType = CurveType.CUBIC;
        this.ctrlX1 = ctrlX1;
        this.ctrlY1 = ctrlY1;
        this.ctrlX2 = ctrlX2;
        this.ctrlY2 = ctrlY2;
    }

    @Override
    public void draw(Graphics2D g2d) {
        setupGraphics(g2d);

        java.awt.Shape curveShape = buildShape();

        // 1. Umplere (opțional, dacă isFilled e true)
        if (isFilled && fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fill(curveShape);
        }

        // 2. Desenare contur curbă
        if (strokeColor != null && strokeWidth > 0) {
            g2d.setColor(strokeColor);
            g2d.draw(curveShape);
        }
    }

    /**
     * Construiește figura geometrică java.awt.Shape pe baza tipului de curbă și a motorului selectat.
     */
    public java.awt.Shape buildShape() {
        if (renderEngine == RenderEngine.MATH) {
            // Randare prin calcul matematic polinomial explicit
            if (curveType == CurveType.QUADRATIC) {
                return BezierMath.createQuadPathMath(x1, y1, ctrlX1, ctrlY1, x2, y2, 40);
            } else {
                return BezierMath.createCubicPathMath(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2, 60);
            }
        } else {
            // Randare prin Java2D QuadCurve2D / CubicCurve2D
            if (curveType == CurveType.QUADRATIC) {
                return new QuadCurve2D.Double(x1, y1, ctrlX1, ctrlY1, x2, y2);
            } else {
                return new CubicCurve2D.Double(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
            }
        }
    }

    /**
     * Desenează reperele vizuale interactive de editare (ghidaje tangente și puncte de control),
     * similar cu https://bezier.method.ac și utilitarele vectoriale profesionale.
     */
    public void drawControlGuides(Graphics2D g2d) {
        setupGraphics(g2d);

        // Stil linie punctată pentru tangente
        float[] dashPattern = {4.0f, 4.0f};
        BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f);
        g2d.setStroke(dashed);

        int handleRadius = 5;

        if (curveType == CurveType.QUADRATIC) {
            // Ghidaj de la Start la Control și de la Control la End
            g2d.setColor(new Color(100, 150, 240, 180));
            g2d.drawLine(x1, y1, ctrlX1, ctrlY1);
            g2d.drawLine(x2, y2, ctrlX1, ctrlY1);

            // Desenare manete (puncte)
            drawPointMarker(g2d, x1, y1, handleRadius, new Color(30, 144, 255), "P0 (Start)");
            drawPointMarker(g2d, ctrlX1, ctrlY1, handleRadius + 1, new Color(255, 69, 0), "P1 (Control)");
            drawPointMarker(g2d, x2, y2, handleRadius, new Color(30, 144, 255), "P2 (End)");
        } else {
            // Curba cubică: Start -> Ctrl1 și End -> Ctrl2
            g2d.setColor(new Color(100, 150, 240, 180));
            g2d.drawLine(x1, y1, ctrlX1, ctrlY1);
            g2d.drawLine(x2, y2, ctrlX2, ctrlY2);

            drawPointMarker(g2d, x1, y1, handleRadius, new Color(30, 144, 255), "P0 (Start)");
            drawPointMarker(g2d, ctrlX1, ctrlY1, handleRadius + 1, new Color(255, 69, 0), "P1 (Control 1)");
            drawPointMarker(g2d, ctrlX2, ctrlY2, handleRadius + 1, new Color(255, 140, 0), "P2 (Control 2)");
            drawPointMarker(g2d, x2, y2, handleRadius, new Color(30, 144, 255), "P3 (End)");
        }
    }

    private void drawPointMarker(Graphics2D g2d, int px, int py, int r, Color color, String label) {
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(px - r, py - r, r * 2, r * 2);
        g2d.setColor(color);
        g2d.drawOval(px - r, py - r, r * 2, r * 2);
        g2d.fillOval(px - 2, py - 2, 4, 4);
    }

    // Getters și Setters
    public CurveType getCurveType() { return curveType; }
    public void setCurveType(CurveType curveType) { this.curveType = curveType; }

    public RenderEngine getRenderEngine() { return renderEngine; }
    public void setRenderEngine(RenderEngine renderEngine) { this.renderEngine = renderEngine; }

    public int getCtrlX1() { return ctrlX1; }
    public void setCtrlX1(int ctrlX1) { this.ctrlX1 = ctrlX1; }

    public int getCtrlY1() { return ctrlY1; }
    public void setCtrlY1(int ctrlY1) { this.ctrlY1 = ctrlY1; }

    public int getCtrlX2() { return ctrlX2; }
    public void setCtrlX2(int ctrlX2) { this.ctrlX2 = ctrlX2; }

    public int getCtrlY2() { return ctrlY2; }
    public void setCtrlY2(int ctrlY2) { this.ctrlY2 = ctrlY2; }

    @Override
    public String toString() {
        return String.format("BezierCurve [%s, P0=(%d,%d), P1=(%d,%d), P2=(%d,%d), P3=(%d,%d), motor=%s]",
                curveType, x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2, renderEngine);
    }
}
