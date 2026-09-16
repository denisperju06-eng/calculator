package paint.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.BitSet;

/**
 * Implementare de performanță înaltă pentru algoritmul Flood Fill (Găleată de vopsea / Bucket).
 * Folosește o coadă BFS iterativă cu un BitSet compact pentru a preveni StackOverflowError
 * și a colora suprafețe mari în milisecunde fără alocare excesivă de memorie.
 */
public final class FloodFill {

    private FloodFill() {
        // Clasă utilitară
    }

    /**
     * Umple spațiul închis pornind de la punctul (startX, startY) cu o nouă culoare.
     *
     * @param image      imaginea raster BufferedImage
     * @param startX     coordonata X de start
     * @param startY     coordonata Y de start
     * @param fillColor  noua culoare de umplere
     * @param tolerance  toleranță cromatică (0 pentru potrivire exactă)
     * @return true dacă s-a efectuat vreo modificare, false altfel
     */
    public static boolean fill(BufferedImage image, int startX, int startY, Color fillColor, int tolerance) {
        if (image == null || fillColor == null) {
            return false;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (startX < 0 || startX >= width || startY < 0 || startY >= height) {
            return false;
        }

        int targetColor = image.getRGB(startX, startY);
        int replacementColor = fillColor.getRGB();

        // Dacă culoarea țintă este deja culoarea dorită, nu facem nimic
        if (matches(targetColor, replacementColor, tolerance)) {
            return false;
        }

        int totalPixels = width * height;
        BitSet visited = new BitSet(totalPixels);

        // Coadă circulară sau array indexat pentru BFS
        int[] queue = new int[totalPixels];
        int head = 0;
        int tail = 0;

        int startIdx = startY * width + startX;
        queue[tail++] = startIdx;
        visited.set(startIdx);

        // Direcții 4-vecini (dreapta, stânga, jos, sus)
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (head < tail) {
            int currentIdx = queue[head++];
            int cx = currentIdx % width;
            int cy = currentIdx / width;

            image.setRGB(cx, cy, replacementColor);

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int neighborIdx = ny * width + nx;
                    if (!visited.get(neighborIdx)) {
                        int neighborColor = image.getRGB(nx, ny);
                        if (matches(neighborColor, targetColor, tolerance)) {
                            visited.set(neighborIdx);
                            queue[tail++] = neighborIdx;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Verifică dacă două culori sunt egale în limita toleranței specificate.
     */
    private static boolean matches(int c1, int c2, int tolerance) {
        if (c1 == c2) {
            return true;
        }
        if (tolerance <= 0) {
            return false;
        }

        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >>> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        return Math.abs(a1 - a2) <= tolerance &&
               Math.abs(r1 - r2) <= tolerance &&
               Math.abs(g1 - g2) <= tolerance &&
               Math.abs(b1 - b2) <= tolerance;
    }
}
