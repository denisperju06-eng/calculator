package paint.tools;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasă utilitară matematică pentru calculul curbelor Bezier.
 * Implementează explicit formulele polinoamelor Bernstein pentru:
 * 1. Curbe Bezier pătratice (3 puncte de control):
 *    B(t) = (1-t)² * P0 + 2(1-t)t * P1 + t² * P2,  t ∈ [0, 1]
 *
 * 2. Curbe Bezier cubice (4 puncte de control):
 *    B(t) = (1-t)³ * P0 + 3(1-t)²t * P1 + 3(1-t)t² * P2 + t³ * P3,  t ∈ [0, 1]
 */
public final class BezierMath {

    private BezierMath() {
        // Clasă utilitară statică
    }

    /**
     * Calculează un punct pe o curbă Bezier pătratică pentru parametrul t ∈ [0, 1].
     */
    public static Point2D.Double computeQuadPoint(double t,
                                                 double x0, double y0,
                                                 double cx, double cy,
                                                 double x1, double y1) {
        double u = 1.0 - t;
        double tt = t * t;
        double uu = u * u;

        double x = uu * x0 + 2 * u * t * cx + tt * x1;
        double y = uu * y0 + 2 * u * t * cy + tt * y1;

        return new Point2D.Double(x, y);
    }

    /**
     * Calculează un punct pe o curbă Bezier cubică pentru parametrul t ∈ [0, 1].
     */
    public static Point2D.Double computeCubicPoint(double t,
                                                  double x0, double y0,
                                                  double cx1, double cy1,
                                                  double cx2, double cy2,
                                                  double x1, double y1) {
        double u = 1.0 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        double x = uuu * x0 + 3 * uu * t * cx1 + 3 * u * tt * cx2 + ttt * x1;
        double y = uuu * y0 + 3 * uu * t * cy1 + 3 * u * tt * cy2 + ttt * y1;

        return new Point2D.Double(x, y);
    }

    /**
     * Generează o listă de puncte eșantionate matematic pe curba pătratică.
     */
    public static List<Point2D.Double> sampleQuadCurve(double x0, double y0,
                                                       double cx, double cy,
                                                       double x1, double y1,
                                                       int segments) {
        int n = Math.max(10, segments);
        List<Point2D.Double> points = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            points.add(computeQuadPoint(t, x0, y0, cx, cy, x1, y1));
        }
        return points;
    }

    /**
     * Generează o listă de puncte eșantionate matematic pe curba cubică.
     */
    public static List<Point2D.Double> sampleCubicCurve(double x0, double y0,
                                                        double cx1, double cy1,
                                                        double cx2, double cy2,
                                                        double x1, double y1,
                                                        int segments) {
        int n = Math.max(10, segments);
        List<Point2D.Double> points = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            points.add(computeCubicPoint(t, x0, y0, cx1, cy1, cx2, cy2, x1, y1));
        }
        return points;
    }

    /**
     * Construiește un Path2D prin interpolare matematică discretă a curbei pătratice.
     */
    public static Path2D.Double createQuadPathMath(double x0, double y0,
                                                  double cx, double cy,
                                                  double x1, double y1,
                                                  int segments) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x0, y0);
        int n = Math.max(16, segments);
        for (int i = 1; i <= n; i++) {
            double t = (double) i / n;
            Point2D.Double p = computeQuadPoint(t, x0, y0, cx, cy, x1, y1);
            path.lineTo(p.x, p.y);
        }
        return path;
    }

    /**
     * Construiește un Path2D prin interpolare matematică discretă a curbei cubice.
     */
    public static Path2D.Double createCubicPathMath(double x0, double y0,
                                                   double cx1, double cy1,
                                                   double cx2, double cy2,
                                                   double x1, double y1,
                                                   int segments) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x0, y0);
        int n = Math.max(24, segments);
        for (int i = 1; i <= n; i++) {
            double t = (double) i / n;
            Point2D.Double p = computeCubicPoint(t, x0, y0, cx1, cy1, cx2, cy2, x1, y1);
            path.lineTo(p.x, p.y);
        }
        return path;
    }
}
