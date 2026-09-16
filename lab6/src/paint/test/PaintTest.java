package paint.test;

import paint.model.BezierCurve;
import paint.model.FreehandStroke;
import paint.model.Line;
import paint.model.Oval;
import paint.model.Rectangle;
import paint.model.Shape;
import paint.tools.BezierMath;
import paint.tools.FloodFill;
import paint.ui.PaintCanvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Suită completă de teste unitare și de integrare pentru Redactorul Grafic (Paint).
 * Verifică:
 * 1. Ierarhia polimorfică de clase Shape (Line, Rectangle, Oval, BezierCurve, FreehandStroke).
 * 2. Algoritmul Flood Fill pentru spații închise.
 * 3. Calculele matematice Bernstein pentru curbele Bezier (Quad & Cubic).
 * 4. Stiva de Undo/Redo a pânzei.
 */
public class PaintTest {

    public static void main(String[] args) {
        System.out.println("=== Începere Teste Unitare Redactor Grafic ===");
        int passed = 0;
        int total = 0;

        total++; if (testShapeHierarchy()) passed++;
        total++; if (testFloodFillClosedShape()) passed++;
        total++; if (testFloodFillBoundaryIntegrity()) passed++;
        total++; if (testBezierMathFormulas()) passed++;
        total++; if (testBezierEnginesRendering()) passed++;
        total++; if (testCanvasUndoRedo()) passed++;

        System.out.println(String.format("\nRezultat final: %d / %d teste trecute cu succes!", passed, total));
        if (passed == total) {
            System.out.println(">>> TOATE TESTELE AU FOST VALIDATE CU SUCCES! <<<");
        } else {
            System.err.println(">>> UNELE TESTE AU EȘUAT! <<<");
            System.exit(1);
        }
    }

    private static boolean testShapeHierarchy() {
        System.out.print("Test 1: Ierarhie polimorfică Shape (Line, Rectangle, Oval, Freehand)... ");
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        Shape[] shapes = new Shape[] {
                new Line(10, 10, 50, 50, Color.RED, 2.0f),
                new Rectangle(20, 20, 80, 80, Color.BLUE, Color.YELLOW, 3.0f, true),
                new Oval(60, 60, 120, 120, Color.GREEN, Color.CYAN, 2.0f, true),
                new FreehandStroke(5, 5, Color.BLACK, 1.0f)
        };

        FreehandStroke stroke = (FreehandStroke) shapes[3];
        stroke.addPoint(15, 25);
        stroke.addPoint(30, 45);

        for (Shape s : shapes) {
            s.draw(g2d);
        }
        g2d.dispose();

        System.out.println("PASSED");
        return true;
    }

    private static boolean testFloodFillClosedShape() {
        System.out.print("Test 2: Flood Fill pe o zonă închisă de un dreptunghi... ");
        int w = 100, h = 100;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Umplem cu fundal alb
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);

        // Desenăm un dreptunghi negru contur
        g2d.setColor(Color.BLACK);
        g2d.drawRect(20, 20, 60, 60);
        g2d.dispose();

        // Punctul (50, 50) este în interiorul dreptunghiului
        Color fillCol = Color.RED;
        boolean result = FloodFill.fill(img, 50, 50, fillCol, 0);

        if (!result) {
            System.err.println("FAILED: FloodFill a returnat false");
            return false;
        }

        // Verificăm că pixelul interior s-a colorat în roșu
        int insideRgb = img.getRGB(50, 50);
        if (insideRgb != fillCol.getRGB()) {
            System.err.println("FAILED: Pixelul din interior nu este roșu!");
            return false;
        }

        // Verificăm că pixelul exterior (5, 5) a rămas alb
        int outsideRgb = img.getRGB(5, 5);
        if (outsideRgb != Color.WHITE.getRGB()) {
            System.err.println("FAILED: Pixelul din exterior s-a modificat!");
            return false;
        }

        System.out.println("PASSED");
        return true;
    }

    private static boolean testFloodFillBoundaryIntegrity() {
        System.out.print("Test 3: Integritate barieră Flood Fill... ");
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 50, 50);

        // Linie de separare pe coloana x=25
        g.setColor(Color.BLACK);
        g.drawLine(25, 0, 25, 49);
        g.dispose();

        // Umplem jumătatea stângă cu albastru
        FloodFill.fill(img, 10, 25, Color.BLUE, 0);

        // Verificăm stânga (10, 25) este albastru
        if (img.getRGB(10, 25) != Color.BLUE.getRGB()) {
            System.err.println("FAILED: Stânga nu este albastră!");
            return false;
        }

        // Verificăm dreapta (35, 25) este în continuare alb
        if (img.getRGB(35, 25) != Color.WHITE.getRGB()) {
            System.err.println("FAILED: Vopseaua a curs dincolo de barieră!");
            return false;
        }

        System.out.println("PASSED");
        return true;
    }

    private static boolean testBezierMathFormulas() {
        System.out.print("Test 4: Formule matematice polinoame Bernstein Bezier... ");
        // Curba Pătratică: P0=(0,0), P1=(50, 100), P2=(100, 0)
        // La t = 0 => (0, 0)
        Point2D.Double pStart = BezierMath.computeQuadPoint(0.0, 0, 0, 50, 100, 100, 0);
        if (Math.abs(pStart.x - 0.0) > 1e-6 || Math.abs(pStart.y - 0.0) > 1e-6) {
            System.err.println("FAILED: Pătratic t=0 invalid");
            return false;
        }

        // La t = 1 => (100, 0)
        Point2D.Double pEnd = BezierMath.computeQuadPoint(1.0, 0, 0, 50, 100, 100, 0);
        if (Math.abs(pEnd.x - 100.0) > 1e-6 || Math.abs(pEnd.y - 0.0) > 1e-6) {
            System.err.println("FAILED: Pătratic t=1 invalid");
            return false;
        }

        // La t = 0.5 => B(0.5) = 0.25*(0,0) + 0.5*(50, 100) + 0.25*(100, 0) = (25 + 25, 50) = (50, 50)
        Point2D.Double pMid = BezierMath.computeQuadPoint(0.5, 0, 0, 50, 100, 100, 0);
        if (Math.abs(pMid.x - 50.0) > 1e-6 || Math.abs(pMid.y - 50.0) > 1e-6) {
            System.err.println("FAILED: Pătratic t=0.5 așteptat (50, 50), obținut " + pMid);
            return false;
        }

        // Curba Cubică: P0=(0,0), P1=(0, 100), P2=(100, 100), P3=(100, 0)
        Point2D.Double cStart = BezierMath.computeCubicPoint(0.0, 0, 0, 0, 100, 100, 100, 100, 0);
        Point2D.Double cEnd = BezierMath.computeCubicPoint(1.0, 0, 0, 0, 100, 100, 100, 100, 0);
        if (Math.abs(cStart.x) > 1e-6 || Math.abs(cEnd.x - 100.0) > 1e-6) {
            System.err.println("FAILED: Cubic start/end invalid");
            return false;
        }

        List<Point2D.Double> samples = BezierMath.sampleCubicCurve(0, 0, 0, 100, 100, 100, 100, 0, 20);
        if (samples.size() != 21) {
            System.err.println("FAILED: Eșantionare curba cubică invalidă");
            return false;
        }

        System.out.println("PASSED");
        return true;
    }

    private static boolean testBezierEnginesRendering() {
        System.out.print("Test 5: Randare curbe Bezier cu motoarele Java2D și Math... ");
        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // 1. Pătratică cu Java2D
        BezierCurve qJava = new BezierCurve(20, 150, 280, 150, 150, 20, Color.BLACK, null, 2.0f, false);
        qJava.setRenderEngine(BezierCurve.RenderEngine.JAVA2D);
        qJava.draw(g2d);

        // 2. Pătratică cu Math
        BezierCurve qMath = new BezierCurve(20, 150, 280, 150, 150, 20, Color.RED, null, 2.0f, false);
        qMath.setRenderEngine(BezierCurve.RenderEngine.MATH);
        qMath.draw(g2d);

        // 3. Cubică cu Java2D
        BezierCurve cJava = new BezierCurve(20, 250, 280, 250, 80, 50, 220, 50, Color.BLUE, Color.GREEN, 2.0f, true);
        cJava.setRenderEngine(BezierCurve.RenderEngine.JAVA2D);
        cJava.draw(g2d);

        // 4. Cubică cu Math
        BezierCurve cMath = new BezierCurve(20, 250, 280, 250, 80, 50, 220, 50, Color.MAGENTA, null, 2.0f, false);
        cMath.setRenderEngine(BezierCurve.RenderEngine.MATH);
        cMath.draw(g2d);

        // 5. Test repere vizuale interactive (guides)
        cJava.drawControlGuides(g2d);

        g2d.dispose();
        System.out.println("PASSED");
        return true;
    }

    private static boolean testCanvasUndoRedo() {
        System.out.print("Test 6: Funcționalitate Undo / Redo pe Canvas... ");
        PaintCanvas canvas = new PaintCanvas(100, 100, Color.WHITE);

        // Inițial pânza e complet albă
        int initialPixel = canvas.getImage().getRGB(10, 10);

        // Modificare 1: Desenăm un punct negru
        canvas.saveUndoState();
        canvas.getImage().setRGB(10, 10, Color.BLACK.getRGB());

        if (canvas.getImage().getRGB(10, 10) != Color.BLACK.getRGB()) {
            System.err.println("FAILED: Modificarea nu s-a aplicat");
            return false;
        }

        // Test Undo: ar trebui să revină la alb
        canvas.undo();
        if (canvas.getImage().getRGB(10, 10) != initialPixel) {
            System.err.println("FAILED: Undo nu a restabilit starea inițială");
            return false;
        }

        // Test Redo: ar trebui să revină la negru
        canvas.redo();
        if (canvas.getImage().getRGB(10, 10) != Color.BLACK.getRGB()) {
            System.err.println("FAILED: Redo nu a refăcut modificarea");
            return false;
        }

        System.out.println("PASSED");
        return true;
    }
}
