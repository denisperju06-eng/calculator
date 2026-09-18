package core;
import java.awt.Color;

/**
 * Enumerare ce reprezintă starea curentă a aplicației rezidente și a conexiunii de rețea.
 * Utilizată pentru actualizarea pictogramei din System Tray și a interfeței grafice.
 * 
 * Cerința d: Modificarea pictogramei aplicației în dependență de starea aplicației sau datelor citite.
 */
public enum AppStatus {
    IDLE("Inactiv", new Color(128, 128, 128)),
    FETCHING("Preluare date din rețea...", new Color(30, 144, 255)),
    SUCCESS("Date actualizate cu succes", new Color(46, 139, 87)),
    ERROR("Eroare la preluarea datelor", new Color(220, 20, 60)),
    OFFLINE("Fără conexiune la Internet", new Color(178, 34, 34));

    private final String descriptionRo;
    private final Color badgeColor;

    AppStatus(String descriptionRo, Color badgeColor) {
        this.descriptionRo = descriptionRo;
        this.badgeColor = badgeColor;
    }

    public String getDescriptionRo() {
        return descriptionRo;
    }

    public Color getBadgeColor() {
        return badgeColor;
    }
}
