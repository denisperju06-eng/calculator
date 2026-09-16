import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model de date imutabil pentru definirea unei locații geografice (oraș)
 * utilizate în interogările serviciului meteo.
 */
public class CityLocation {
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String country;

    public CityLocation(String name, double latitude, double longitude, String country) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return name + " (" + country + ")";
    }

    /**
     * Returnează o listă predefinită de orașe populare din regiune și internațional.
     */
    public static List<CityLocation> getDefaultCities() {
        List<CityLocation> list = new ArrayList<>();
        list.add(new CityLocation("Chișinău", 47.0105, 28.8638, "Moldova"));
        list.add(new CityLocation("Bălți", 47.7617, 27.9289, "Moldova"));
        list.add(new CityLocation("București", 44.4268, 26.1025, "România"));
        list.add(new CityLocation("Iași", 47.1585, 27.6014, "România"));
        list.add(new CityLocation("Cluj-Napoca", 46.7712, 23.6236, "România"));
        list.add(new CityLocation("Kiev", 50.4501, 30.5234, "Ucraina"));
        list.add(new CityLocation("Berlin", 52.5200, 13.4050, "Germania"));
        list.add(new CityLocation("Paris", 48.8566, 2.3522, "Franța"));
        list.add(new CityLocation("Londra", 51.5074, -0.1278, "Marea Britanie"));
        list.add(new CityLocation("Roma", 41.9028, 12.4964, "Italia"));
        list.add(new CityLocation("Tokyo", 35.6762, 139.6503, "Japonia"));
        list.add(new CityLocation("New York", 40.7128, -74.0060, "SUA"));
        return Collections.unmodifiableList(list);
    }
}
