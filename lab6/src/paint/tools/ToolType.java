package paint.tools;

/**
 * Enumerație pentru toate instrumentele disponibile în redactorul grafic.
 */
public enum ToolType {
    PENCIL("Creion", "Desenare liberă cu creionul"),
    LINE("Linie", "Desenare segment de linie dreaptă"),
    RECTANGLE("Dreptunghi", "Desenare dreptunghi (contur/umplere)"),
    OVAL("Oval", "Desenare elipsă sau cerc (contur/umplere)"),
    BEZIER_QUAD("Bezier Pătratic (3 pct)", "Curbă Bezier pătratică cu 1 punct de control (QuadCurve2D/Math)"),
    BEZIER_CUBIC("Bezier Cubic (4 pct)", "Curbă Bezier cubică cu 2 puncte de control (CubicCurve2D/Math)"),
    BUCKET_FILL("Găleată (Flood Fill)", "Colorarea spațiilor închise (algoritm Flood Fill)"),
    ERASER("Radieră", "Ștergere porțiuni cu culoarea de fundal"),
    COLOR_PICKER("Pipetă", "Preluare culoare direct de pe pânză");

    private final String displayName;
    private final String description;

    ToolType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
