package paint;

import paint.ui.ColorBar;
import paint.ui.PaintCanvas;
import paint.ui.PaintMenuBar;
import paint.ui.StatusBar;
import paint.ui.ToolBar;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Fereastra principală (JFrame) a aplicației Redactor Grafic.
 * Asamblează toate componentele: Meniu, Bară de Instrumente, Pânză de Desen cu Scroll,
 * Bară de Culori și Bară de Stare.
 * Implementează cerințele a, b, c, d, e, f din fișierul conditii.md.
 */
public class PaintApp extends JFrame {

    private static final String APP_NAME = "Redactor Grafic POO (Java Paint)";

    private final PaintCanvas canvas;
    private final ToolBar toolBar;
    private final ColorBar colorBar;
    private final StatusBar statusBar;

    private File currentFile = null;

    public PaintApp() {
        super(APP_NAME);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        // 1. Inițializare componente principale
        statusBar = new StatusBar();
        colorBar = new ColorBar();
        canvas = new PaintCanvas(800, 600, Color.WHITE);

        canvas.setStatusBar(statusBar);
        canvas.setColorBar(colorBar);

        // Conectare modificări culori din ColorBar către Canvas
        colorBar.addColorChangeListener(new ColorBar.ColorChangeListener() {
            @Override
            public void onPrimaryColorChanged(Color newColor) {
                canvas.setPrimaryColor(newColor);
            }

            @Override
            public void onSecondaryColorChanged(Color newColor) {
                canvas.setSecondaryColor(newColor);
            }
        });

        toolBar = new ToolBar(canvas);
        setJMenuBar(new PaintMenuBar(this));

        // 2. Structurare Layout
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);

        // Pânza centrată într-un JScrollPane
        JPanel canvasWrapper = new JPanel(new GridBagLayout());
        canvasWrapper.setBackground(new Color(210, 215, 222));
        canvasWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        canvasWrapper.add(canvas, new GridBagConstraints());

        JScrollPane scrollPane = new JScrollPane(canvasWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Partea inferioară: ColorBar + StatusBar
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.add(colorBar);
        bottomContainer.add(statusBar);
        add(bottomContainer, BorderLayout.SOUTH);

        // 3. Gestionare închidere fereastră
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });

        updateTitle();
    }

    public PaintCanvas getCanvas() {
        return canvas;
    }

    public void updateTitle() {
        String fileName = (currentFile != null) ? currentFile.getName() : "Fără titlu";
        String modifiedMarker = canvas.isModified() ? " *" : "";
        setTitle(String.format("%s - [%s]%s", APP_NAME, fileName, modifiedMarker));
    }

    /**
     * Cerința a: Crearea unei imagini noi.
     */
    public void newImage() {
        if (!promptSaveIfModified()) {
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(800, 50, 4000, 50));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(600, 50, 4000, 50));
        String[] bgOptions = {"Alb", "Culoare 2 (Fundal)", "Transparent"};
        JComboBox<String> bgCombo = new JComboBox<>(bgOptions);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Lățime (px):"), gbc);
        gbc.gridx = 1;
        panel.add(widthSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Înălțime (px):"), gbc);
        gbc.gridx = 1;
        panel.add(heightSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Culoare fundal:"), gbc);
        gbc.gridx = 1;
        panel.add(bgCombo, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Imagine nouă", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int w = (Integer) widthSpinner.getValue();
            int h = (Integer) heightSpinner.getValue();
            Color bg;
            int bgIdx = bgCombo.getSelectedIndex();
            if (bgIdx == 0) {
                bg = Color.WHITE;
            } else if (bgIdx == 1) {
                bg = canvas.getSecondaryColor();
            } else {
                bg = new Color(0, 0, 0, 0); // Transparent
            }

            canvas.initCanvas(w, h, bg);
            currentFile = null;
            updateTitle();
            statusBar.setMessage("Imagine nouă creată.");
        }
    }

    /**
     * Cerința a: Deschiderea unei imagini de pe disc.
     */
    public void openImage() {
        if (!promptSaveIfModified()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Deschide imagine");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Toate formatele suportate (*.png, *.jpg, *.jpeg, *.bmp, *.gif)", "png", "jpg", "jpeg", "bmp", "gif"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG (*.jpg, *.jpeg)", "jpg", "jpeg"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP (*.bmp)", "bmp"));
        chooser.setAcceptAllFileFilterUsed(true);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) {
                    JOptionPane.showMessageDialog(this, "Fișierul selectat nu este o imagine validă sau formatul nu este suportat.", "Eroare deschidere", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                canvas.setImage(img);
                currentFile = file;
                updateTitle();
                statusBar.setMessage("Imagine deschisă cu succes: " + file.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Eroare la citirea imaginii: " + ex.getMessage(), "Eroare I/O", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Cerința a: Salvarea imaginii.
     */
    public boolean saveImage() {
        if (currentFile == null) {
            return saveImageAs();
        }
        return saveToFile(currentFile);
    }

    /**
     * Cerința a: Salvarea imaginii cu dialog de alegere fișier și format.
     */
    public boolean saveImageAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvează imagine ca...");
        chooser.setSelectedFile(new File(currentFile != null ? currentFile.getName() : "desen.png"));

        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG (*.png)", "png");
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG (*.jpg)", "jpg");
        FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("BMP (*.bmp)", "bmp");

        chooser.addChoosableFileFilter(pngFilter);
        chooser.addChoosableFileFilter(jpgFilter);
        chooser.addChoosableFileFilter(bmpFilter);
        chooser.setFileFilter(pngFilter);

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            String path = targetFile.getAbsolutePath();

            // Asigurăm extensia corectă dacă utilizatorul a omis-o
            if (!path.matches(".*\\.(?i)(png|jpg|jpeg|bmp)$")) {
                if (chooser.getFileFilter() == jpgFilter) {
                    targetFile = new File(path + ".jpg");
                } else if (chooser.getFileFilter() == bmpFilter) {
                    targetFile = new File(path + ".bmp");
                } else {
                    targetFile = new File(path + ".png");
                }
            }

            if (saveToFile(targetFile)) {
                currentFile = targetFile;
                updateTitle();
                return true;
            }
        }
        return false;
    }

    private boolean saveToFile(File file) {
        String name = file.getName().toLowerCase();
        String format = "png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            format = "jpg";
        } else if (name.endsWith(".bmp")) {
            format = "bmp";
        }

        BufferedImage imgToSave = canvas.getImage();
        // Pentru JPG, eliminăm canalul alpha dacă există (deoarece JPEG nu suportă transparență)
        if ("jpg".equals(format) && imgToSave.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgbImg = new BufferedImage(imgToSave.getWidth(), imgToSave.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = rgbImg.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgbImg.getWidth(), rgbImg.getHeight());
            g.drawImage(imgToSave, 0, 0, null);
            g.dispose();
            imgToSave = rgbImg;
        }

        try {
            boolean written = ImageIO.write(imgToSave, format, file);
            if (written) {
                canvas.setModified(false);
                updateTitle();
                statusBar.setMessage("Imagine salvată: " + file.getAbsolutePath());
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Formatul " + format.toUpperCase() + " nu are scriitor disponibil în Java ImageIO.", "Eroare salvare", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Eroare la scrierea fișierului: " + ex.getMessage(), "Eroare I/O", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Dialog de confirmare înainte de acțiuni distructive dacă există modificări nesalvate.
     */
    private boolean promptSaveIfModified() {
        if (!canvas.isModified()) {
            return true;
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Imaginea a fost modificată. Doriți să salvați modificările?",
                "Modificări nesalvate",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (option == JOptionPane.YES_OPTION) {
            return saveImage();
        } else if (option == JOptionPane.NO_OPTION) {
            return true;
        } else {
            return false; // Cancel
        }
    }

    public void resizeCanvasDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(4, 4, 4, 4);

        int currentW = canvas.getImage().getWidth();
        int currentH = canvas.getImage().getHeight();

        JSpinner wSpinner = new JSpinner(new SpinnerNumberModel(currentW, 10, 4000, 10));
        JSpinner hSpinner = new JSpinner(new SpinnerNumberModel(currentH, 10, 4000, 10));

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Lățime nouă (px):"), gbc);
        gbc.gridx = 1; panel.add(wSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Înălțime nouă (px):"), gbc);
        gbc.gridx = 1; panel.add(hSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Redimensionează pânza", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int newW = (Integer) wSpinner.getValue();
            int newH = (Integer) hSpinner.getValue();

            canvas.saveUndoState();
            BufferedImage oldImg = canvas.getImage();
            BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = newImg.createGraphics();
            g2d.setColor(canvas.getSecondaryColor());
            g2d.fillRect(0, 0, newW, newH);
            g2d.drawImage(oldImg, 0, 0, null);
            g2d.dispose();

            canvas.setImage(newImg);
            statusBar.setMessage(String.format("Pânză redimensionată la %d x %d px.", newW, newH));
        }
    }

    public void invertColors() {
        BufferedImage img = canvas.getImage();
        if (img == null) return;
        canvas.saveUndoState();

        int w = img.getWidth();
        int h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);
                int inverted = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, inverted);
            }
        }
        canvas.repaint();
        statusBar.setMessage("Culorile imaginii au fost inversate.");
    }

    public void convertToGrayscale() {
        BufferedImage img = canvas.getImage();
        if (img == null) return;
        canvas.saveUndoState();

        int w = img.getWidth();
        int h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int grayRgb = (a << 24) | (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, grayRgb);
            }
        }
        canvas.repaint();
        statusBar.setMessage("Imaginea a fost convertită în tonuri de gri.");
    }

    public void exitApp() {
        if (promptSaveIfModified()) {
            dispose();
            System.exit(0);
        }
    }
}
