import java.awt.Color;

/**
 * Enumerare ce descrie condițiile meteorologice preluate prin API-ul HTTP.
 * Permite adaptarea pictogramei din System Tray în funcție de vremea curentă.
 * 
 * Cerința d: Modificarea pictogramei aplicației în dependență de datele citite.
 */
public enum WeatherCondition {
    CLEAR_SUNNY("Senin", "☀️", new Color(255, 180, 0)),
    PARTLY_CLOUDY("Parțial înnorat", "⛅", new Color(135, 206, 250)),
    OVERCAST("Înnorat", "☁️", new Color(160, 160, 160)),
    FOG("Ceață", "🌫️", new Color(192, 192, 192)),
    RAIN("Ploaie", "🌧️", new Color(30, 144, 255)),
    SNOW("Ninsoare", "❄️", new Color(175, 238, 238)),
    THUNDERSTORM("Furtună", "⛈️", new Color(138, 43, 226)),
    UNKNOWN("Necunoscut", "❓", new Color(128, 128, 128));

    private final String descriptionRo;
    private final String emoji;
    private final Color themeColor;

    WeatherCondition(String descriptionRo, String emoji, Color themeColor) {
        this.descriptionRo = descriptionRo;
        this.emoji = emoji;
        this.themeColor = themeColor;
    }

    public String getDescriptionRo() {
        return descriptionRo;
    }

    public String getEmoji() {
        return emoji;
    }

    public Color getThemeColor() {
        return themeColor;
    }

    /**
     * Mapează codul internațional WMO la o valoare din enumerare.
     * @param wmoCode codul meteo WMO returnat de serviciul de prognoză
     * @return instanța corespunzătoare de WeatherCondition
     */
    public static WeatherCondition fromWmoCode(int wmoCode) {
        if (wmoCode == 0) {
            return CLEAR_SUNNY;
        } else if (wmoCode == 1 || wmoCode == 2) {
            return PARTLY_CLOUDY;
        } else if (wmoCode == 3) {
            return OVERCAST;
        } else if (wmoCode == 45 || wmoCode == 48) {
            return FOG;
        } else if ((wmoCode >= 51 && wmoCode <= 67) || (wmoCode >= 80 && wmoCode <= 82)) {
            return RAIN;
        } else if ((wmoCode >= 71 && wmoCode <= 77) || (wmoCode >= 85 && wmoCode <= 86)) {
            return SNOW;
        } else if (wmoCode >= 95 && wmoCode <= 99) {
            return THUNDERSTORM;
        } else {
            return UNKNOWN;
        }
    }
}
