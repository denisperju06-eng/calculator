import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Model de date imutabil pentru ratele de schimb valutar preluate prin HTTP.
 * 
 * Cerința a: Conectarea la un serviciu Internet pentru obținerea schimbului valutar.
 */
public class CurrencyData {
    public enum Trend {
        RISING("▲ Creștere", "+"),
        FALLING("▼ Scădere", "-"),
        STABLE("— Stabil", "=");

        private final String label;
        private final String sign;

        Trend(String label, String sign) {
            this.label = label;
            this.sign = sign;
        }

        public String getLabel() {
            return label;
        }

        public String getSign() {
            return sign;
        }
    }

    private final String baseCurrency;
    private final String targetCurrency;
    private final double currentRate;
    private final double previousRate;
    private final Map<String, Double> allRates;
    private final LocalDateTime timestamp;

    public CurrencyData(String baseCurrency, String targetCurrency, double currentRate, 
                        double previousRate, Map<String, Double> allRates, LocalDateTime timestamp) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.currentRate = currentRate;
        this.previousRate = previousRate > 0 ? previousRate : currentRate;
        this.allRates = allRates != null ? Collections.unmodifiableMap(new LinkedHashMap<>(allRates)) : Collections.emptyMap();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public double getCurrentRate() {
        return currentRate;
    }

    public double getPreviousRate() {
        return previousRate;
    }

    public Map<String, Double> getAllRates() {
        return allRates;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));
    }

    public double getDifference() {
        return currentRate - previousRate;
    }

    public Trend getTrend() {
        double diff = getDifference();
        if (Math.abs(diff) < 0.0001) {
            return Trend.STABLE;
        } else if (diff > 0) {
            return Trend.RISING;
        } else {
            return Trend.FALLING;
        }
    }

    public String getFormattedRate() {
        return String.format("%.4f", currentRate);
    }

    public String getTraySummary() {
        return "1 " + baseCurrency + " = " + getFormattedRate() + " " + targetCurrency + " " + getTrend().getSign();
    }

    @Override
    public String toString() {
        return "CurrencyData{" +
                "base='" + baseCurrency + '\'' +
                ", target='" + targetCurrency + '\'' +
                ", rate=" + currentRate +
                ", trend=" + getTrend() +
                ", time=" + getFormattedTimestamp() +
                '}';
    }
}
