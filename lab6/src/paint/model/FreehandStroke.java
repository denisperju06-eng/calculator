package paint.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasa derivată 'FreehandStroke' pentru desenare liberă (creion / pensulă).
 * Moștenește clasa de bază Shape și acumulează puncte consecutive pentru o traiectorie netedă.
 */
public class FreehandStroke extends Shape {
    private static final long serialVersionUID = 1L;

    private final List<Point> points = new ArrayList<>();

    public FreehandStroke(int startX, int startY, Color strokeColor, float strokeWidth) {
        super(startX, startY, startX, startY, strokeColor, null, strokeWidth, false);
        points.add(new Point(startX, startY));
    }

    public void addPoint(int x, int y) {
        points.add(new Point(x, y));
        // Actualizăm punctul final
        this.x2 = x;
        this.y2 = y;
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (points.isEmpty() || strokeColor == null || strokeWidth <= 0) {
            return;
        }

        setupGraphics(g2d);
        g2d.setColor(strokeColor);

        if (points.size() == 1) {
            Point p = points.get(0);
            int r = Math.max(1, Math.round(strokeWidth / 2f));
            g2d.fillOval(p.x - r, p.y - r, r * 2, r * 2);
            return;
        }

        Path2D.Double path = new Path2D.Double();
        Point first = points.get(0);
        path.moveTo(first.x, first.y);

        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i);
            path.lineTo(p.x, p.y);
        }

        g2d.draw(path);
    }
}
