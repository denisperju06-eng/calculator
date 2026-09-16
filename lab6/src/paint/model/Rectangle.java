package paint.model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Clasa derivată 'Rectangle' pentru desenarea dreptunghiurilor.
 * Moștenește clasa de bază Shape și suportă atât contur cât și umplere interioară.
 */
public class Rectangle extends Shape {
    private static final long serialVersionUID = 1L;

    public Rectangle(int x1, int y1, int x2, int y2, Color strokeColor, Color fillColor, float strokeWidth, boolean isFilled) {
        super(x1, y1, x2, y2, strokeColor, fillColor, strokeWidth, isFilled);
    }

    @Override
    public void draw(Graphics2D g2d) {
        setupGraphics(g2d);

        int x = getMinX();
        int y = getMinY();
        int width = getWidth();
        int height = getHeight();

        // 1. Umplere interioară dacă opțiunea este activată
        if (isFilled && fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fillRect(x, y, width, height);
        }

        // 2. Desenare contur
        if (strokeColor != null && strokeWidth > 0) {
            g2d.setColor(strokeColor);
            g2d.drawRect(x, y, width, height);
        }
    }

    @Override
    public String toString() {
        return String.format("Rectangle [x=%d, y=%d, w=%d, h=%d, plin=%b]", getMinX(), getMinY(), getWidth(), getHeight(), isFilled);
    }
}
