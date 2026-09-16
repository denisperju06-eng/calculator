package paint.ui;

import paint.PaintApp;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

/**
 * Bara de meniu principală a aplicației Paint.
 * Asigură accesul la operațiunile de fișier (Nou, Deschidere, Salvare, Salvare ca),
 * funcțiile de editare (Undo, Redo, Curățare), transformări ale imaginii și documentație.
 */
public class PaintMenuBar extends JMenuBar {

    private final PaintApp app;

    public PaintMenuBar(PaintApp app) {
        this.app = app;
        initFileMenu();
        initEditMenu();
        initImageMenu();
        initHelpMenu();
    }

    private void initFileMenu() {
        JMenu fileMenu = new JMenu("Fișier");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem itemNew = new JMenuItem("Nou...");
        itemNew.setMnemonic(KeyEvent.VK_N);
        itemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKey));
        itemNew.addActionListener(e -> app.newImage());
        fileMenu.add(itemNew);

        JMenuItem itemOpen = new JMenuItem("Deschide...");
        itemOpen.setMnemonic(KeyEvent.VK_D);
        itemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKey));
        itemOpen.addActionListener(e -> app.openImage());
        fileMenu.add(itemOpen);

        JMenuItem itemSave = new JMenuItem("Salvează");
        itemSave.setMnemonic(KeyEvent.VK_S);
        itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKey));
        itemSave.addActionListener(e -> app.saveImage());
        fileMenu.add(itemSave);

        JMenuItem itemSaveAs = new JMenuItem("Salvează ca...");
        itemSaveAs.setMnemonic(KeyEvent.VK_A);
        itemSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKey | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        itemSaveAs.addActionListener(e -> app.saveImageAs());
        fileMenu.add(itemSaveAs);

        fileMenu.addSeparator();

        JMenuItem itemExit = new JMenuItem("Ieșire");
        itemExit.setMnemonic(KeyEvent.VK_I);
        itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcutKey));
        itemExit.addActionListener(e -> app.exitApp());
        fileMenu.add(itemExit);

        add(fileMenu);
    }

    private void initEditMenu() {
        JMenu editMenu = new JMenu("Editare");
        editMenu.setMnemonic(KeyEvent.VK_E);

        int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem itemUndo = new JMenuItem("Anulează (Undo)");
        itemUndo.setMnemonic(KeyEvent.VK_U);
        itemUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKey));
        itemUndo.addActionListener(e -> app.getCanvas().undo());
        editMenu.add(itemUndo);

        JMenuItem itemRedo = new JMenuItem("Refă (Redo)");
        itemRedo.setMnemonic(KeyEvent.VK_R);
        itemRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutKey));
        itemRedo.addActionListener(e -> app.getCanvas().redo());
        editMenu.add(itemRedo);

        editMenu.addSeparator();

        JMenuItem itemClear = new JMenuItem("Curăță pânza");
        itemClear.setMnemonic(KeyEvent.VK_C);
        itemClear.addActionListener(e -> app.getCanvas().clearCanvas(app.getCanvas().getSecondaryColor()));
        editMenu.add(itemClear);

        add(editMenu);
    }

    private void initImageMenu() {
        JMenu imgMenu = new JMenu("Imagine");
        imgMenu.setMnemonic(KeyEvent.VK_I);

        JMenuItem itemResize = new JMenuItem("Redimensionează pânza...");
        itemResize.addActionListener(e -> app.resizeCanvasDialog());
        imgMenu.add(itemResize);

        JMenuItem itemInvert = new JMenuItem("Inversează culorile");
        itemInvert.addActionListener(e -> app.invertColors());
        imgMenu.add(itemInvert);

        JMenuItem itemGrayscale = new JMenuItem("Convertește în tonuri de gri");
        itemGrayscale.addActionListener(e -> app.convertToGrayscale());
        imgMenu.add(itemGrayscale);

        add(imgMenu);
    }

    private void initHelpMenu() {
        JMenu helpMenu = new JMenu("Ajutor");
        helpMenu.setMnemonic(KeyEvent.VK_A);

        JMenuItem itemBezierGuide = new JMenuItem("Ghid Curbe Bezier...");
        itemBezierGuide.addActionListener(e -> showBezierGuideDialog());
        helpMenu.add(itemBezierGuide);

        JMenuItem itemAbout = new JMenuItem("Despre Redactor Grafic...");
        itemAbout.addActionListener(e -> showAboutDialog());
        helpMenu.add(itemAbout);

        add(helpMenu);
    }

    private void showBezierGuideDialog() {
        String msg = "<html><body style='width: 420px; font-family: sans-serif;'>"
                + "<h2 style='color: #0055AA;'>Ghid Desenare Curbe Bezier</h2>"
                + "<p>Aplicația suportă două tipuri de curbe Bezier și două motoare de calcul:</p>"
                + "<h3>1. Curbă Bezier Pătratică (3 puncte - QuadCurve2D / Math):</h3>"
                + "<ul>"
                + "<li><b>Pasul 1:</b> Apăsați și trageți mouse-ul pentru linia de bază (Start P0 -&gt; Final P2).</li>"
                + "<li><b>Pasul 2:</b> Deplasați mouse-ul pentru a curba linia și faceți un <b>clic</b> pentru a fixa punctul de control P1.</li>"
                + "</ul>"
                + "<h3>2. Curbă Bezier Cubică (4 puncte - CubicCurve2D / Math):</h3>"
                + "<ul>"
                + "<li><b>Pasul 1:</b> Apăsați și trageți linia de bază (Start P0 -&gt; Final P3).</li>"
                + "<li><b>Pasul 2:</b> Deplasați mouse-ul și faceți clic pentru a fixa <b>Punctul de Control 1 (P1)</b>.</li>"
                + "<li><b>Pasul 3:</b> Deplasați mouse-ul și faceți clic pentru a fixa <b>Punctul de Control 2 (P2)</b>.</li>"
                + "</ul>"
                + "<p><i>Referință recomandată în cerințe: <a href='https://bezier.method.ac'>https://bezier.method.ac</a></i></p>"
                + "<p><b>Tasta ESC:</b> Anulează desenarea curbei aflate în desfășurare.</p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(app, msg, "Ghid Curbe Bezier", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String msg = "<html><body style='width: 400px; font-family: sans-serif;'>"
                + "<h2 style='color: #0055AA;'>Redactor Grafic (Java Paint)</h2>"
                + "<p><b>Laboratorul 6 - Paradigma Orientată pe Obiecte (POO)</b></p>"
                + "<hr/>"
                + "<p><b>Arhitectură POO implementată:</b></p>"
                + "<ul>"
                + "<li><b>Clasă de bază abstractă:</b> <code>Shape</code></li>"
                + "<li><b>Subclase derivate:</b> <code>Line</code>, <code>Rectangle</code>, <code>Oval</code>, <code>BezierCurve</code>, <code>FreehandStroke</code></li>"
                + "<li><b>Bară de instrumente:</b> Creion, Linie, Dreptunghi, Oval, Bezier (3P/4P), Găleată, Radieră, Pipetă</li>"
                + "<li><b>Bară de culori:</b> Culoare 1 (Desen) & Culoare 2 (Fundal/Umplere), Paletă 28 culori, Selector JColorChooser</li>"
                + "<li><b>Colorare spații închise:</b> Algoritm Flood Fill de înaltă performanță (BFS iterativ)</li>"
                + "<li><b>Curbe Bezier:</b> QuadCurve2D, CubicCurve2D și formule matematice Bernstein</li>"
                + "<li><b>Operațiuni imagini:</b> Nou, Deschidere, Salvare (PNG, JPG, BMP)</li>"
                + "<li><b>Istoric:</b> Undo/Redo stivă de până la 30 pași</li>"
                + "</ul>"
                + "</body></html>";
        JOptionPane.showMessageDialog(app, msg, "Despre Redactor Grafic", JOptionPane.INFORMATION_MESSAGE);
    }
}
