import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model de date imutabil ce reține informațiile meteorologice preluate prin HTTP.
 * 
 * Cerința a: Conectarea la un serviciu Internet pentru obținerea informației meteo.
 */
public class WeatherData {
    private final String locationName;
    private final double temperature;
    private final double humidity;
    private final double windSpeed;
    private final int weatherCode;
    private final WeatherCondition condition;
    private final LocalDateTime timestamp;

    public WeatherData(String locationName, double temperature, double humidity, 
                       double windSpeed, int weatherCode, LocalDateTime timestamp) {
        this.locationName = locationName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
        this.condition = WeatherCondition.fromWmoCode(weatherCode);
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getLocationName() {
        return locationName;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getFormattedTemperature() {
        return String.format("%.1f°C", temperature);
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public WeatherCondition getCondition() {
        return condition;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));
    }

    /**
     * Text succint pentru pictograma din tray și tooltip.
     */
    public String getTraySummary() {
        return locationName + ": " + getFormattedTemperature() + " (" + condition.getDescriptionRo() + ")";
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "location='" + locationName + '\'' +
                ", temp=" + temperature +
                ", condition=" + condition +
                ", humidity=" + humidity +
                ", wind=" + windSpeed +
                ", time=" + getFormattedTimestamp() +
                '}';
    }
}
