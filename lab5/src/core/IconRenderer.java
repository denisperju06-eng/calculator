package core;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

/**
 * Generator dinamic de pictograme pentru bara System Tray.
 * Generează pictograme vectoriale în memorie (fără dependențe de fișiere externe),
 * adaptate stării conexiunii sau condițiilor meteo / valutare curente.
 * 
 * Cerința d: Modificarea pictogramei aplicației în dependență de starea aplicației sau datelor citite.
 */
public class IconRenderer {

    public static final int ICON_SIZE = 32; // Dimensiune optimă pentru High-DPI (Retina / Windows scale)

    /**
     * Creează o pictogramă adaptată pentru datele meteorologice.
     */
    public static Image createWeatherIcon(WeatherCondition condition, double temperature) {
        BufferedImage image = createBlankCanvas();
        Graphics2D g2 = setupGraphics(image);

        // Desenăm simbolul meteo
        switch (condition) {
            case CLEAR_SUNNY:
                drawSun(g2);
                break;
            case PARTLY_CLOUDY:
                drawPartlyCloudy(g2);
                break;
            case OVERCAST:
                drawCloud(g2, new Color(130, 140, 150));
                break;
            case RAIN:
                drawRain(g2);
                break;
            case SNOW:
                drawSnow(g2);
                break;
            case THUNDERSTORM:
                drawThunderstorm(g2);
                break;
            case FOG:
                drawFog(g2);
                break;
            default:
                drawSun(g2);
                break;
        }

        // Desenăm insigna cu temperatura în colțul din dreapta-jos
        drawTemperatureBadge(g2, temperature);

        g2.dispose();
        return image;
    }

    /**
     * Creează o pictogramă adaptată pentru datele de curs valutar.
     */
    public static Image createCurrencyIcon(CurrencyData.Trend trend, String targetCurrency) {
        BufferedImage image = createBlankCanvas();
        Graphics2D g2 = setupGraphics(image);

        // Desenăm o monedă de aur ca bază
        g2.setColor(new Color(245, 180, 0));
        g2.fillOval(3, 3, 26, 26);
        g2.setColor(new Color(180, 120, 0));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(3, 3, 26, 26);

        // Desenăm simbolul valutei în centru
        g2.setColor(new Color(80, 50, 0));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String symbol = getCurrencySymbol(targetCurrency);
        int strW = g2.getFontMetrics().stringWidth(symbol);
        g2.drawString(symbol, 16 - (strW / 2), 20);

        // Desenăm indicatorul de trend (săgeată sus verde / săgeată jos roșie)
        if (trend == CurrencyData.Trend.RISING) {
            drawTrendArrow(g2, true);
        } else if (trend == CurrencyData.Trend.FALLING) {
            drawTrendArrow(g2, false);
        }

        g2.dispose();
        return image;
    }

    /**
     * Creează o pictogramă pentru starea aplicației (încărcare, eroare, offline, inactiv).
     */
    public static Image createStatusIcon(AppStatus status) {
        BufferedImage image = createBlankCanvas();
        Graphics2D g2 = setupGraphics(image);

        switch (status) {
            case FETCHING:
                drawLoadingSpinner(g2);
                break;
            case ERROR:
            case OFFLINE:
                drawErrorWarning(g2);
                break;
            case IDLE:
            default:
                drawIdleIcon(g2);
                break;
        }

        g2.dispose();
        return image;
    }

    // ==================== Metode de desenare ====================

    private static BufferedImage createBlankCanvas() {
        return new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    private static Graphics2D setupGraphics(BufferedImage img) {
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    private static void drawSun(Graphics2D g2) {
        g2.setColor(new Color(255, 170, 0));
        // Raze
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = 14, cy = 14, r = 7;
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            int x1 = (int) (cx + Math.cos(rad) * (r + 2));
            int y1 = (int) (cy + Math.sin(rad) * (r + 2));
            int x2 = (int) (cx + Math.cos(rad) * (r + 5));
            int y2 = (int) (cy + Math.sin(rad) * (r + 5));
            g2.drawLine(x1, y1, x2, y2);
        }
        // Corpul soarelui
        g2.setColor(new Color(255, 200, 0));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(230, 140, 0));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
    }

    private static void drawPartlyCloudy(Graphics2D g2) {
        // Soare în fundal
        g2.setColor(new Color(255, 190, 0));
        g2.fillOval(4, 4, 13, 13);
        // Nor în prim plan
        drawCloud(g2, new Color(180, 190, 200));
    }

    private static void drawCloud(Graphics2D g2, Color color) {
        g2.setColor(color);
        // Bule ce formează un nor pufos
        g2.fillOval(6, 11, 14, 12);
        g2.fillOval(13, 7, 13, 13);
        g2.fillOval(19, 11, 11, 11);
        g2.fillRoundRect(8, 14, 21, 9, 6, 6);

        // Contur subtil
        g2.setColor(color.darker());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(8, 14, 21, 9, 6, 6);
    }

    private static void drawRain(Graphics2D g2) {
        drawCloud(g2, new Color(100, 115, 130));
        // Picături de ploaie
        g2.setColor(new Color(0, 150, 255));
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(10, 24, 8, 28);
        g2.drawLine(16, 24, 14, 28);
        g2.drawLine(22, 24, 20, 28);
    }

    private static void drawSnow(Graphics2D g2) {
        drawCloud(g2, new Color(140, 155, 170));
        // Fulgi de nea
        g2.setColor(new Color(200, 240, 255));
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.drawString("*", 9, 28);
        g2.drawString("*", 15, 29);
        g2.drawString("*", 21, 28);
    }

    private static void drawThunderstorm(Graphics2D g2) {
        drawCloud(g2, new Color(70, 75, 85));
        // Fulger galben
        g2.setColor(new Color(255, 215, 0));
        GeneralPath bolt = new GeneralPath();
        bolt.moveTo(17, 19);
        bolt.lineTo(13, 25);
        bolt.lineTo(16, 25);
        bolt.lineTo(14, 30);
        bolt.lineTo(20, 23);
        bolt.lineTo(17, 23);
        bolt.closePath();
        g2.fill(bolt);
    }

    private static void drawFog(Graphics2D g2) {
        g2.setColor(new Color(170, 180, 190));
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(6, 10, 26, 10);
        g2.drawLine(4, 15, 28, 15);
        g2.drawLine(7, 20, 25, 20);
        g2.drawLine(5, 25, 27, 25);
    }

    private static void drawLoadingSpinner(Graphics2D g2) {
        g2.setColor(new Color(0, 122, 255));
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(6, 6, 20, 20, 45, 270);

        // Vârf de săgeată pentru rotație
        g2.drawLine(24, 9, 26, 14);
        g2.drawLine(21, 13, 26, 14);
    }

    private static void drawErrorWarning(Graphics2D g2) {
        // Triunghi roșu de avertizare
        g2.setColor(new Color(230, 40, 40));
        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(16, 4);
        triangle.lineTo(29, 27);
        triangle.lineTo(3, 27);
        triangle.closePath();
        g2.fill(triangle);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("!", 14, 24);
    }

    private static void drawIdleIcon(Graphics2D g2) {
        g2.setColor(new Color(100, 110, 120));
        g2.fillOval(5, 5, 22, 22);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("P", 12, 20);
    }

    private static void drawTrendArrow(Graphics2D g2, boolean rising) {
        int ax = 20, ay = rising ? 3 : 18;
        if (rising) {
            g2.setColor(new Color(0, 180, 60));
            GeneralPath path = new GeneralPath();
            path.moveTo(ax + 5, ay);
            path.lineTo(ax + 10, ay + 8);
            path.lineTo(ax, ay + 8);
            path.closePath();
            g2.fill(path);
        } else {
            g2.setColor(new Color(220, 30, 30));
            GeneralPath path = new GeneralPath();
            path.moveTo(ax, ay);
            path.lineTo(ax + 10, ay);
            path.lineTo(ax + 5, ay + 8);
            path.closePath();
            g2.fill(path);
        }
    }

    private static void drawTemperatureBadge(Graphics2D g2, double temp) {
        String str = Math.round(temp) + "°";
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        int strW = g2.getFontMetrics().stringWidth(str);

        int bx = 32 - strW - 4;
        int by = 21;
        // Fundal semi-transparent pentru lizibilitate maximă
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRoundRect(bx - 1, by, strW + 3, 11, 4, 4);

        // Text alb sau galben
        g2.setColor(Color.WHITE);
        g2.drawString(str, bx, by + 9);
    }

    private static String getCurrencySymbol(String curr) {
        if ("EUR".equalsIgnoreCase(curr)) return "€";
        if ("USD".equalsIgnoreCase(curr)) return "$";
        if ("GBP".equalsIgnoreCase(curr)) return "£";
        if ("MDL".equalsIgnoreCase(curr)) return "L";
        if ("RON".equalsIgnoreCase(curr)) return "L";
        return curr != null && curr.length() > 0 ? curr.substring(0, 1) : "$";
    }
}
