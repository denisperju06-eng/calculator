package paint.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;

/**
 * Clasa de bază abstractă 'Shape' pentru toate figurile geometrice desenabile.
 * Utilizează principiile fundamentale ale Programării Orientate pe Obiecte (POO):
 * - Încapsulare: câmpuri protejate/private cu metode accesor (getters/setters).
 * - Abstracție: metoda abstractă draw(Graphics2D g2d) care definește contractul de desenare.
 * - Polimorfism: fiecare figură derivată implementează propria logică de randare grafică.
 */
public abstract class Shape implements Serializable {
    private static final long serialVersionUID = 1L;

    // Coordonatele figurii (start și end)
    protected int x1;
    protected int y1;
    protected int x2;
    protected int y2;

    // Proprietăți de stil
    protected Color strokeColor;
    protected Color fillColor;
    protected float strokeWidth;
    protected boolean isFilled;
    protected boolean isAntialiased = true;

    /**
     * Constructor complet pentru o figură geometrică.
     *
     * @param x1          coordonata X de start
     * @param y1          coordonata Y de start
     * @param x2          coordonata X de final
     * @param y2          coordonata Y de final
     * @param strokeColor culoarea de contur
     * @param fillColor   culoarea de umplere
     * @param strokeWidth grosimea liniei de contur
     * @param isFilled    indică dacă figura este plină (umplută)
     */
    public Shape(int x1, int y1, int x2, int y2, Color strokeColor, Color fillColor, float strokeWidth, boolean isFilled) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.strokeWidth = Math.max(1.0f, strokeWidth);
        this.isFilled = isFilled;
    }

    /**
     * Metodă abstractă pentru desenarea figurii.
     * Fiecare subclasă (Line, Rectangle, Oval, BezierCurve, etc.) o implementează specific.
     *
     * @param g2d contextul grafic Graphics2D
     */
    public abstract void draw(Graphics2D g2d);

    /**
     * Configurează proprietățile de bază ale contextului Graphics2D înainte de desenare:
     * anti-aliasing pentru linii fine și capete rotunjite pentru îmbinări.
     *
     * @param g2d contextul grafic Graphics2D
     */
    protected void setupGraphics(Graphics2D g2d) {
        if (isAntialiased) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        }
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    // Metode ajutătoare pentru calculul casetei de încadrare (bounding box)
    public int getMinX() {
        return Math.min(x1, x2);
    }

    public int getMinY() {
        return Math.min(y1, y2);
    }

    public int getWidth() {
        return Math.abs(x2 - x1);
    }

    public int getHeight() {
        return Math.abs(y2 - y1);
    }

    // Getters și Setters
    public int getX1() { return x1; }
    public void setX1(int x1) { this.x1 = x1; }

    public int getY1() { return y1; }
    public void setY1(int y1) { this.y1 = y1; }

    public int getX2() { return x2; }
    public void setX2(int x2) { this.x2 = x2; }

    public int getY2() { return y2; }
    public void setY2(int y2) { this.y2 = y2; }

    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }

    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }

    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = Math.max(1.0f, strokeWidth); }

    public boolean isFilled() { return isFilled; }
    public void setFilled(boolean filled) { isFilled = filled; }

    public boolean isAntialiased() { return isAntialiased; }
    public void setAntialiased(boolean antialiased) { isAntialiased = antialiased; }
}
