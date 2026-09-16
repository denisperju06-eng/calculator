import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/**
 * Serviciu de preluare a datelor meteorologice de la API-ul public Open-Meteo.
 * Folosește java.net.HttpURLConnection conform cerințelor din laborator.
 * 
 * Cerința a: Conectarea la un serviciu Internet pentru obținerea informației meteo.
 */
public class WeatherService implements DataService<WeatherData> {

    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 8000;

    private CityLocation currentLocation;
    private WeatherData lastFetchedData;

    public WeatherService() {
        // Orașul implicit este Chișinău
        this(new CityLocation("Chișinău", 47.0105, 28.8638, "Moldova"));
    }

    public WeatherService(CityLocation location) {
        this.currentLocation = location != null ? location : new CityLocation("Chișinău", 47.0105, 28.8638, "Moldova");
    }

    public CityLocation getCurrentLocation() {
        return currentLocation;
    }

    public synchronized void setCurrentLocation(CityLocation location) {
        if (location != null) {
            this.currentLocation = location;
        }
    }

    @Override
    public WeatherData fetchData() throws Exception {
        // Construim URL-ul pentru Open-Meteo API
        String urlString = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m",
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );

        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "ResidentWeatherTray/1.0 (Java POO Lab5)");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error de la serverul meteo: cod " + responseCode);
            }

            String responseBody = readStreamToString(connection.getInputStream());
            Map<String, Object> root = SimpleJson.parseObject(responseBody);

            Map<String, Object> currentMap = SimpleJson.getMap(root, "current");
            if (currentMap == null) {
                throw new IOException("Răspunsul JSON nu conține obiectul 'current'");
            }

            double temperature = SimpleJson.getDouble(currentMap, "temperature_2m", 0.0);
            double humidity = SimpleJson.getDouble(currentMap, "relative_humidity_2m", 0.0);
            int weatherCode = SimpleJson.getInt(currentMap, "weather_code", 0);
            double windSpeed = SimpleJson.getDouble(currentMap, "wind_speed_10m", 0.0);

            WeatherData data = new WeatherData(
                    currentLocation.getName(),
                    temperature,
                    humidity,
                    windSpeed,
                    weatherCode,
                    LocalDateTime.now()
            );

            this.lastFetchedData = data;
            return data;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public DataServiceType getServiceType() {
        return DataServiceType.WEATHER;
    }

    @Override
    public String getServiceName() {
        return "Open-Meteo Weather API";
    }

    @Override
    public String getSummary() {
        if (lastFetchedData != null) {
            return lastFetchedData.getTraySummary();
        }
        return currentLocation.getName() + ": Date neinițializate";
    }

    public WeatherData getLastFetchedData() {
        return lastFetchedData;
    }

    private String readStreamToString(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
