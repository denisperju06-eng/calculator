package paint.ui;

import paint.model.BezierCurve;
import paint.model.FreehandStroke;
import paint.model.Line;
import paint.model.Oval;
import paint.model.Rectangle;
import paint.model.Shape;
import paint.tools.FloodFill;
import paint.tools.ToolType;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Pânza de desen (Canvas) a redactorului grafic.
 * Gestionează imaginea raster (BufferedImage), randarea polimorfică a figurilor (Shape),
 * istoricul de Undo/Redo și interacțiunea cu mouse-ul pentru toate instrumentele.
 */
public class PaintCanvas extends JPanel {

    private BufferedImage canvasImage;
    private Graphics2D canvasGraphics;

    // Stive de Undo/Redo
    private final Deque<BufferedImage> undoStack = new ArrayDeque<>();
    private final Deque<BufferedImage> redoStack = new ArrayDeque<>();
    private static final int MAX_UNDO_STEPS = 30;

    // Stare instrumente și stil
    private ToolType currentTool = ToolType.PENCIL;
    private Color primaryColor = Color.BLACK;
    private Color secondaryColor = Color.WHITE;
    private float strokeWidth = 3.0f;
    private boolean isFilled = false;
    private BezierCurve.RenderEngine bezierEngine = BezierCurve.RenderEngine.JAVA2D;

    // Figură curentă în curs de desenare (preview)
    private Shape previewShape = null;
    private FreehandStroke currentStroke = null;

    // Mașină de stări pentru curbe Bezier
    private enum BezierPhase { IDLE, SETTING_CTRL1, SETTING_CTRL2 }
    private BezierPhase bezierPhase = BezierPhase.IDLE;
    private BezierCurve activeBezier = null;

    // Coordonate mouse inițiale
    private int startX, startY;

    // Componente conexe
    private StatusBar statusBar;
    private ColorBar colorBar;
    private boolean modified = false;

    public PaintCanvas(int width, int height, Color bgColor) {
        setBackground(new Color(220, 224, 230)); // Fundal exterior de spațiu de lucru
        initCanvas(width, height, bgColor);
        setupMouseListeners();
        setupKeyListeners();
        setFocusable(true);
    }

    /**
     * Inițializează pânza cu dimensiunea și culoarea de fundal specificate.
     */
    public void initCanvas(int width, int height, Color bgColor) {
        int w = Math.max(10, width);
        int h = Math.max(10, height);

        canvasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = canvasImage.createGraphics();

        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvasGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Umplem cu fundalul inițial
        canvasGraphics.setColor(bgColor != null ? bgColor : Color.WHITE);
        canvasGraphics.fillRect(0, 0, w, h);

        undoStack.clear();
        redoStack.clear();
        modified = false;

        setPreferredSize(new Dimension(w, h));
        revalidate();
        repaint();

        if (statusBar != null) {
            statusBar.setCanvasSize(w, h);
        }
    }

    /**
     * Încarcă o imagine existentă pe pânză.
     */
    public void setImage(BufferedImage newImg) {
        if (newImg == null) return;
        saveUndoState();

        int w = newImg.getWidth();
        int h = newImg.getHeight();

        canvasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = canvasImage.createGraphics();
        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvasGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        canvasGraphics.drawImage(newImg, 0, 0, null);

        undoStack.clear();
        redoStack.clear();
        modified = false;

        setPreferredSize(new Dimension(w, h));
        revalidate();
        repaint();

        if (statusBar != null) {
            statusBar.setCanvasSize(w, h);
        }
    }

    public BufferedImage getImage() {
        return canvasImage;
    }

    /**
     * Salvează o copie a imaginii curente în stiva de Undo.
     */
    public void saveUndoState() {
        if (canvasImage == null) return;

        if (undoStack.size() >= MAX_UNDO_STEPS) {
            undoStack.removeLast();
        }
        undoStack.push(cloneBufferedImage(canvasImage));
        redoStack.clear();
        modified = true;
    }

    /**
     * Anulează ultima acțiune (Undo).
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(cloneBufferedImage(canvasImage));
            BufferedImage prev = undoStack.pop();
            setCanvasImageDirect(prev);
            resetActiveState();
            repaint();
            if (statusBar != null) {
                statusBar.setMessage("Acțiune anulată (Undo).");
            }
        }
    }

    /**
     * Reface ultima acțiune anulată (Redo).
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(cloneBufferedImage(canvasImage));
            BufferedImage next = redoStack.pop();
            setCanvasImageDirect(next);
            resetActiveState();
            repaint();
            if (statusBar != null) {
                statusBar.setMessage("Acțiune refăcută (Redo).");
            }
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    private void setCanvasImageDirect(BufferedImage img) {
        canvasImage = img;
        canvasGraphics = canvasImage.createGraphics();
        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvasGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        revalidate();
        if (statusBar != null) {
            statusBar.setCanvasSize(img.getWidth(), img.getHeight());
        }
    }

    private static BufferedImage cloneBufferedImage(BufferedImage source) {
        ColorModel cm = source.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = source.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Resetează stările tranzitorii de desenare.
     */
    public void resetActiveState() {
        previewShape = null;
        currentStroke = null;
        activeBezier = null;
        bezierPhase = BezierPhase.IDLE;
        if (statusBar != null) {
            statusBar.setToolHint(currentTool);
        }
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Anulare desenare Bezier în curs
                    if (bezierPhase != BezierPhase.IDLE) {
                        resetActiveState();
                        repaint();
                        if (statusBar != null) {
                            statusBar.setMessage("Desenarea curbei Bezier a fost anulată.");
                        }
                    }
                }
            }
        });
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int x = e.getX();
                int y = e.getY();

                // Verificăm dacă suntem în interiorul pânzei
                if (!isInsideCanvas(x, y)) {
                    return;
                }

                startX = x;
                startY = y;

                switch (currentTool) {
                    case PENCIL:
                        saveUndoState();
                        currentStroke = new FreehandStroke(x, y, primaryColor, strokeWidth);
                        currentStroke.draw(canvasGraphics);
                        repaint();
                        break;

                    case ERASER:
                        saveUndoState();
                        // Radiera desenează cu Culoarea 2 (fundal) cu grosime mai mare dacă se dorește
                        currentStroke = new FreehandStroke(x, y, secondaryColor, Math.max(8.0f, strokeWidth * 2));
                        currentStroke.draw(canvasGraphics);
                        repaint();
                        break;

                    case LINE:
                        previewShape = new Line(x, y, x, y, primaryColor, strokeWidth);
                        break;

                    case RECTANGLE:
                        previewShape = new Rectangle(x, y, x, y, primaryColor, secondaryColor, strokeWidth, isFilled);
                        break;

                    case OVAL:
                        previewShape = new Oval(x, y, x, y, primaryColor, secondaryColor, strokeWidth, isFilled);
                        break;

                    case BUCKET_FILL:
                        // Colorare spații închise (Flood Fill)
                        saveUndoState();
                        Color fillColor = SwingUtilities.isRightMouseButton(e) ? secondaryColor : primaryColor;
                        boolean filled = FloodFill.fill(canvasImage, x, y, fillColor, 12);
                        if (filled) {
                            repaint();
                            if (statusBar != null) {
                                statusBar.setMessage(String.format("Zonă colorată cu succes la (%d, %d).", x, y));
                            }
                        }
                        break;

                    case COLOR_PICKER:
                        // Preluare culoare din imagine
                        int rgb = canvasImage.getRGB(x, y);
                        Color picked = new Color(rgb, true);
                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (colorBar != null) colorBar.setSecondaryColor(picked);
                            setSecondaryColor(picked);
                            if (statusBar != null) statusBar.setMessage("Culoare 2 setată din pânză: " + picked);
                        } else {
                            if (colorBar != null) colorBar.setPrimaryColor(picked);
                            setPrimaryColor(picked);
                            if (statusBar != null) statusBar.setMessage("Culoare 1 setată din pânză: " + picked);
                        }
                        break;

                    case BEZIER_QUAD:
                        handleBezierQuadPress(x, y, e);
                        break;

                    case BEZIER_CUBIC:
                        handleBezierCubicPress(x, y, e);
                        break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int x = Math.max(0, Math.min(canvasImage.getWidth() - 1, e.getX()));
                int y = Math.max(0, Math.min(canvasImage.getHeight() - 1, e.getY()));

                switch (currentTool) {
                    case PENCIL:
                    case ERASER:
                        if (currentStroke != null) {
                            currentStroke = null;
                            repaint();
                        }
                        break;

                    case LINE:
                    case RECTANGLE:
                    case OVAL:
                        if (previewShape != null) {
                            previewShape.setX2(x);
                            previewShape.setY2(y);
                            saveUndoState();
                            previewShape.draw(canvasGraphics);
                            previewShape = null;
                            repaint();
                        }
                        break;

                    case BEZIER_QUAD:
                        if (bezierPhase == BezierPhase.IDLE && activeBezier != null) {
                            // Linia de bază a fost trasată, trecem la configurarea punctului de control P1
                            activeBezier.setX2(x);
                            activeBezier.setY2(y);
                            // Inițializăm P1 la mijlocul distanței
                            activeBezier.setCtrlX1((activeBezier.getX1() + x) / 2);
                            activeBezier.setCtrlY1((activeBezier.getY1() + y) / 2);
                            bezierPhase = BezierPhase.SETTING_CTRL1;
                            if (statusBar != null) {
                                statusBar.setMessage("Bezier Pătratic: Deplasați mouse-ul pentru a curba, apoi faceți clic pentru a fixa.");
                            }
                            repaint();
                        }
                        break;

                    case BEZIER_CUBIC:
                        if (bezierPhase == BezierPhase.IDLE && activeBezier != null) {
                            // Linia de bază a fost trasată, trecem la configurarea punctului P1
                            activeBezier.setX2(x);
                            activeBezier.setY2(y);
                            int dx = x - activeBezier.getX1();
                            int dy = y - activeBezier.getY1();
                            activeBezier.setCtrlX1(activeBezier.getX1() + dx / 3);
                            activeBezier.setCtrlY1(activeBezier.getY1() + dy / 3);
                            activeBezier.setCtrlX2(activeBezier.getX1() + 2 * dx / 3);
                            activeBezier.setCtrlY2(activeBezier.getY1() + 2 * dy / 3);
                            bezierPhase = BezierPhase.SETTING_CTRL1;
                            if (statusBar != null) {
                                statusBar.setMessage("Bezier Cubic: Fixați Punctul de Control 1 (P1) cu un clic.");
                            }
                            repaint();
                        }
                        break;

                    default:
                        break;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (statusBar != null) {
                    if (isInsideCanvas(x, y)) {
                        statusBar.setCoordinates(x, y);
                    } else {
                        statusBar.setCoordinates(-1, -1);
                    }
                }

                // Ajustare dinamică a punctelor de control Bezier la mișcarea mouse-ului
                if (currentTool == ToolType.BEZIER_QUAD && bezierPhase == BezierPhase.SETTING_CTRL1 && activeBezier != null) {
                    activeBezier.setCtrlX1(x);
                    activeBezier.setCtrlY1(y);
                    repaint();
                } else if (currentTool == ToolType.BEZIER_CUBIC && activeBezier != null) {
                    if (bezierPhase == BezierPhase.SETTING_CTRL1) {
                        activeBezier.setCtrlX1(x);
                        activeBezier.setCtrlY1(y);
                        repaint();
                    } else if (bezierPhase == BezierPhase.SETTING_CTRL2) {
                        activeBezier.setCtrlX2(x);
                        activeBezier.setCtrlY2(y);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (statusBar != null) {
                    statusBar.setCoordinates(x, y);
                }

                switch (currentTool) {
                    case PENCIL:
                    case ERASER:
                        if (currentStroke != null) {
                            currentStroke.addPoint(x, y);
                            // Desenare incrementală direct pe pânză
                            currentStroke.draw(canvasGraphics);
                            repaint();
                        }
                        break;

                    case LINE:
                    case RECTANGLE:
                    case OVAL:
                        if (previewShape != null) {
                            previewShape.setX2(x);
                            previewShape.setY2(y);
                            repaint();
                        }
                        break;

                    case BEZIER_QUAD:
                        if (bezierPhase == BezierPhase.IDLE && activeBezier != null) {
                            activeBezier.setX2(x);
                            activeBezier.setY2(y);
                            activeBezier.setCtrlX1((activeBezier.getX1() + x) / 2);
                            activeBezier.setCtrlY1((activeBezier.getY1() + y) / 2);
                            repaint();
                        }
                        break;

                    case BEZIER_CUBIC:
                        if (bezierPhase == BezierPhase.IDLE && activeBezier != null) {
                            activeBezier.setX2(x);
                            activeBezier.setY2(y);
                            repaint();
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    private void handleBezierQuadPress(int x, int y, MouseEvent e) {
        if (bezierPhase == BezierPhase.IDLE) {
            // Pasul 1: Începe trasarea liniei de bază (Start -> End)
            activeBezier = new BezierCurve(x, y, x, y, x, y, primaryColor, secondaryColor, strokeWidth, isFilled);
            activeBezier.setCurveType(BezierCurve.CurveType.QUADRATIC);
            activeBezier.setRenderEngine(bezierEngine);
        } else if (bezierPhase == BezierPhase.SETTING_CTRL1) {
            // Pasul 2: Punctul de control P1 a fost fixat, finalizăm curba!
            activeBezier.setCtrlX1(x);
            activeBezier.setCtrlY1(y);
            saveUndoState();
            activeBezier.draw(canvasGraphics);
            resetActiveState();
            repaint();
            if (statusBar != null) {
                statusBar.setMessage("Curba Bezier pătratică a fost desenată cu succes.");
            }
        }
    }

    private void handleBezierCubicPress(int x, int y, MouseEvent e) {
        if (bezierPhase == BezierPhase.IDLE) {
            // Pasul 1: Începe trasarea liniei de bază
            activeBezier = new BezierCurve(x, y, x, y, x, y, x, y, primaryColor, secondaryColor, strokeWidth, isFilled);
            activeBezier.setCurveType(BezierCurve.CurveType.CUBIC);
            activeBezier.setRenderEngine(bezierEngine);
        } else if (bezierPhase == BezierPhase.SETTING_CTRL1) {
            // Pasul 2: Punctul P1 a fost fixat, trecem la P2
            activeBezier.setCtrlX1(x);
            activeBezier.setCtrlY1(y);
            bezierPhase = BezierPhase.SETTING_CTRL2;
            if (statusBar != null) {
                statusBar.setMessage("Bezier Cubic: Fixați Punctul de Control 2 (P2) cu un clic.");
            }
            repaint();
        } else if (bezierPhase == BezierPhase.SETTING_CTRL2) {
            // Pasul 3: Punctul P2 a fost fixat, finalizăm curba!
            activeBezier.setCtrlX2(x);
            activeBezier.setCtrlY2(y);
            saveUndoState();
            activeBezier.draw(canvasGraphics);
            resetActiveState();
            repaint();
            if (statusBar != null) {
                statusBar.setMessage("Curba Bezier cubică a fost desenată cu succes.");
            }
        }
    }

    public boolean isInsideCanvas(int x, int y) {
        return canvasImage != null && x >= 0 && x < canvasImage.getWidth() && y >= 0 && y < canvasImage.getHeight();
    }

    /**
     * Curăță pânza cu culoarea specificată (de ex. Culoare 2 sau Alb).
     */
    public void clearCanvas(Color clearColor) {
        if (canvasImage == null) return;
        saveUndoState();
        canvasGraphics.setColor(clearColor != null ? clearColor : Color.WHITE);
        canvasGraphics.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        resetActiveState();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (canvasImage == null) return;

        Graphics2D g2d = (Graphics2D) g;

        // 1. Desenăm imaginea raster a pânzei
        g2d.drawImage(canvasImage, 0, 0, null);

        // 2. Desenăm conturul exterior al pânzei (ramă fină)
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());

        // 3. Desenăm preview-ul figurilor în curs de trasare
        if (previewShape != null) {
            previewShape.draw(g2d);
        }

        // 4. Desenăm curba Bezier interactivă cu reperele de control
        if (activeBezier != null) {
            activeBezier.draw(g2d);
            activeBezier.drawControlGuides(g2d);
        }
    }

    // Getters și Setters
    public ToolType getCurrentTool() { return currentTool; }
    public void setCurrentTool(ToolType currentTool) {
        this.currentTool = currentTool;
        resetActiveState();
        updateCursor();
    }

    private void updateCursor() {
        if (currentTool == ToolType.COLOR_PICKER) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (currentTool == ToolType.PENCIL || currentTool == ToolType.ERASER) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (currentTool == ToolType.BUCKET_FILL) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    public Color getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(Color primaryColor) { this.primaryColor = primaryColor; }

    public Color getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(Color secondaryColor) { this.secondaryColor = secondaryColor; }

    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    public boolean isFilled() { return isFilled; }
    public void setFilled(boolean filled) { isFilled = filled; }

    public BezierCurve.RenderEngine getBezierEngine() { return bezierEngine; }
    public void setBezierEngine(BezierCurve.RenderEngine bezierEngine) { this.bezierEngine = bezierEngine; }

    public boolean isModified() { return modified; }
    public void setModified(boolean modified) { this.modified = modified; }

    public void setStatusBar(StatusBar statusBar) {
        this.statusBar = statusBar;
        if (statusBar != null && canvasImage != null) {
            statusBar.setCanvasSize(canvasImage.getWidth(), canvasImage.getHeight());
        }
    }

    public void setColorBar(ColorBar colorBar) {
        this.colorBar = colorBar;
    }
}
