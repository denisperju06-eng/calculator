package paint.model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Clasa derivată 'Line' pentru desenarea unui segment de dreaptă.
 * Moștenește clasa de bază Shape.
 */
public class Line extends Shape {
    private static final long serialVersionUID = 1L;

    public Line(int x1, int y1, int x2, int y2, Color strokeColor, float strokeWidth) {
        super(x1, y1, x2, y2, strokeColor, null, strokeWidth, false);
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (strokeColor == null || strokeWidth <= 0) {
            return;
        }
        setupGraphics(g2d);
        g2d.setColor(strokeColor);
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public String toString() {
        return String.format("Line [(%d, %d) -> (%d, %d), grosime: %.1f]", x1, y1, x2, y2, strokeWidth);
    }
}
