package core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serviciu de preluare a ratelor de schimb valutar de la API-ul public Open Exchange Rates.
 * Folosește java.net.HttpURLConnection conform cerințelor din laborator.
 * 
 * Cerința a: Conectarea la un serviciu Internet pentru obținerea schimbului valutar.
 */
public class CurrencyService implements DataService<CurrencyData> {

    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 8000;

    private String baseCurrency;
    private String targetCurrency;
    private double previousRate = 0.0;
    private CurrencyData lastFetchedData;

    public CurrencyService() {
        this("EUR", "MDL");
    }

    public CurrencyService(String baseCurrency, String targetCurrency) {
        this.baseCurrency = baseCurrency != null ? baseCurrency.toUpperCase() : "EUR";
        this.targetCurrency = targetCurrency != null ? targetCurrency.toUpperCase() : "MDL";
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public synchronized void setBaseCurrency(String baseCurrency) {
        if (baseCurrency != null && !baseCurrency.trim().isEmpty()) {
            this.baseCurrency = baseCurrency.trim().toUpperCase();
        }
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public synchronized void setTargetCurrency(String targetCurrency) {
        if (targetCurrency != null && !targetCurrency.trim().isEmpty()) {
            this.targetCurrency = targetCurrency.trim().toUpperCase();
        }
    }

    @Override
    public CurrencyData fetchData() throws Exception {
        String urlString = "https://open.er-api.com/v6/latest/" + baseCurrency;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "ResidentCurrencyTray/1.0 (Java POO Lab5)");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error de la serverul valutar: cod " + responseCode);
            }

            String responseBody = readStreamToString(connection.getInputStream());
            Map<String, Object> root = SimpleJson.parseObject(responseBody);

            Map<String, Object> ratesRaw = SimpleJson.getMap(root, "rates");
            if (ratesRaw == null) {
                throw new IOException("Răspunsul JSON nu conține tabelul 'rates'");
            }

            Map<String, Double> allRates = new LinkedHashMap<>();
            // Extragem valutele de interes principal
            String[] commonCurrencies = {"MDL", "RON", "USD", "EUR", "GBP", "CHF", "UAH"};
            for (String curr : commonCurrencies) {
                Double val = SimpleJson.getDouble(ratesRaw, curr, null);
                if (val != null) {
                    allRates.put(curr, val);
                }
            }

            Double currentRateVal = SimpleJson.getDouble(ratesRaw, targetCurrency, 0.0);

            double oldRate = (previousRate > 0.0) ? previousRate : currentRateVal;
            CurrencyData data = new CurrencyData(
                    baseCurrency,
                    targetCurrency,
                    currentRateVal,
                    oldRate,
                    allRates,
                    LocalDateTime.now()
            );

            // Salvăm rata curentă pentru compararea următoare (trend)
            this.previousRate = currentRateVal;
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
        return DataServiceType.CURRENCY;
    }

    @Override
    public String getServiceName() {
        return "Open Exchange Rates API (" + baseCurrency + " -> " + targetCurrency + ")";
    }

    @Override
    public String getSummary() {
        if (lastFetchedData != null) {
            return lastFetchedData.getTraySummary();
        }
        return baseCurrency + "/" + targetCurrency + ": Date neinițializate";
    }

    public CurrencyData getLastFetchedData() {
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
