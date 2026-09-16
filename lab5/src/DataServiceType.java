/**
 * Tipul de serviciu de date activ în aplicație.
 * Permite comutarea între monitorizarea vremii și monitorizarea cursului valutar.
 */
public enum DataServiceType {
    WEATHER("Prognoză Meteo", "🌡️"),
    CURRENCY("Curs Valutar", "💱");

    private final String displayName;
    private final String icon;

    DataServiceType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
