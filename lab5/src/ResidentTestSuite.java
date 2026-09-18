import core.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.KeyStroke;

/**
 * Suită automată de teste pentru validarea tuturor cerințelor din POO Lab 5:
 * a. Conectarea la servicii Internet (WeatherService, CurrencyService) prin HttpURLConnection
 * b. Integrarea rezidentă în System Tray (TrayManager)
 * c. Setarea și replanificarea dinamică a intervalului cu java.util.Timer (PeriodicScheduler)
 * d. Generarea dinamică a pictogramelor pentru toate stările (IconRenderer)
 * e. Înregistrarea, reconfigurarea și declanșarea combinațiilor de taste (HotkeyManager)
 */
public class ResidentTestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("\n========== Lansare Suită de Teste Automate Lab 5 ==========\n");

        testWeatherService();
        testCurrencyService();
        testPeriodicScheduler();
        testIconRenderer();
        testHotkeyManager();
        testSimpleJson();

        System.out.println("\n===========================================================");
        System.out.println("  REZULTAT FINAL TESTE: " + testsPassed + " Trecute, " + testsFailed + " Eșuate");
        System.out.println("===========================================================\n");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testWeatherService() {
        System.out.println("--- Test 1: Conectare HTTP Meteo (Open-Meteo API) ---");
        try {
            WeatherService service = new WeatherService(new CityLocation("Chișinău", 47.0105, 28.8638, "Moldova"));
            WeatherData data = service.fetchData();

            assertTrue("WeatherData nu este nul", data != null);
            assertTrue("Numele orașului este Chișinău", "Chișinău".equals(data.getLocationName()));
            assertTrue("Temperatura este în interval rezonabil (-50°C .. 50°C)", data.getTemperature() > -50 && data.getTemperature() < 50);
            assertTrue("Condiția meteo este definită", data.getCondition() != null);
            assertTrue("Rezumatul pentru tray este generat corect", data.getTraySummary() != null && data.getTraySummary().contains("Chișinău"));
            System.out.println("      Date obținute: " + data);
        } catch (Exception e) {
            assertTrue("Eroare la fetchWeather: " + e.getMessage(), false);
        }
    }

    private static void testCurrencyService() {
        System.out.println("\n--- Test 2: Conectare HTTP Curs Valutar (Exchange Rates API) ---");
        try {
            CurrencyService service = new CurrencyService("EUR", "MDL");
            CurrencyData data = service.fetchData();

            assertTrue("CurrencyData nu este nul", data != null);
            assertTrue("Valuta de bază este EUR", "EUR".equals(data.getBaseCurrency()));
            assertTrue("Valuta țintă este MDL", "MDL".equals(data.getTargetCurrency()));
            assertTrue("Rata EUR/MDL este pozitivă (> 15)", data.getCurrentRate() > 15.0);
            assertTrue("Tabelul cu rate conține valute multiple", data.getAllRates().containsKey("USD") && data.getAllRates().containsKey("RON"));
            assertTrue("Trendul este determinat", data.getTrend() != null);
            System.out.println("      Date obținute: " + data);
        } catch (Exception e) {
            assertTrue("Eroare la fetchCurrency: " + e.getMessage(), false);
        }
    }

    private static void testPeriodicScheduler() {
        System.out.println("\n--- Test 3: Planificator Periodic (java.util.Timer) & Modificare Interval ---");
        try {
            AtomicInteger fetchCount = new AtomicInteger(0);
            AtomicInteger tickCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);

            PeriodicScheduler scheduler = new PeriodicScheduler(
                    5, // 5 secunde
                    () -> {
                        fetchCount.incrementAndGet();
                        latch.countDown();
                    },
                    secondsRemaining -> {
                        tickCount.incrementAndGet();
                    }
            );

            scheduler.start();
            assertTrue("Schedulerul a pornit", scheduler.isRunning());
            assertTrue("Intervalul inițial este 5s", scheduler.getIntervalSeconds() == 5);

            // Așteptăm prima execuție
            boolean completed = latch.await(2, TimeUnit.SECONDS);
            assertTrue("Prima execuție a avut loc la start", fetchCount.get() >= 1);

            // Modificăm intervalul dinamic la 10 secunde (Cerința c)
            scheduler.setIntervalSeconds(10);
            assertTrue("Intervalul a fost actualizat la 10s", scheduler.getIntervalSeconds() == 10);

            // Declanșare manuală imediată
            scheduler.triggerImmediate();
            Thread.sleep(200);
            assertTrue("Trigger manual a crescut numărul de execuții", fetchCount.get() >= 2);

            scheduler.stop();
            assertTrue("Schedulerul s-a oprit", !scheduler.isRunning());
        } catch (Exception e) {
            assertTrue("Eroare la PeriodicScheduler: " + e.getMessage(), false);
        }
    }

    private static void testIconRenderer() {
        System.out.println("\n--- Test 4: Generare Dinamică Pictograme System Tray (IconRenderer) ---");
        try {
            // Testare pictograme pentru fiecare stare meteo
            for (WeatherCondition cond : WeatherCondition.values()) {
                Image img = IconRenderer.createWeatherIcon(cond, 21.5);
                assertTrue("Pictogramă meteo generată pentru " + cond.name(), img != null && img.getWidth(null) == 32);
            }

            // Testare pictograme valută
            Image currRising = IconRenderer.createCurrencyIcon(CurrencyData.Trend.RISING, "EUR");
            Image currFalling = IconRenderer.createCurrencyIcon(CurrencyData.Trend.FALLING, "USD");
            assertTrue("Pictogramă valută creștere generată", currRising != null);
            assertTrue("Pictogramă valută scădere generată", currFalling != null);

            // Testare pictograme stare
            for (AppStatus status : AppStatus.values()) {
                Image img = IconRenderer.createStatusIcon(status);
                assertTrue("Pictogramă stare generată pentru " + status.name(), img != null);
            }
        } catch (Exception e) {
            assertTrue("Eroare la IconRenderer: " + e.getMessage(), false);
        }
    }

    private static void testHotkeyManager() {
        System.out.println("\n--- Test 5: Manager Combinații de Taste (HotkeyManager & Rebind) ---");
        try {
            HotkeyManager manager = new HotkeyManager();
            AtomicBoolean actionTriggered = new AtomicBoolean(false);

            KeyStroke initialKey = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
            manager.registerHotkey("TEST_ACTION", "Test Refresh", "Descriere", initialKey, () -> {
                actionTriggered.set(true);
            });

            HotkeyBinding binding = manager.getBinding("TEST_ACTION");
            assertTrue("Combinația a fost înregistrată", binding != null);
            assertTrue("DisplayText este F5", "F5".equals(binding.getDisplayText()));

            // Simulare simulare tastă KeyEvent
            KeyEvent testEvent = new KeyEvent(
                    new java.awt.Canvas(),
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    KeyEvent.VK_F5,
                    KeyEvent.CHAR_UNDEFINED
            );
            boolean dispatched = manager.dispatchKeyEvent(testEvent);
            assertTrue("Dispecerul a interceptat KeyEvent pentru F5", dispatched);

            // Reconfigurare Hotkey (Cerința e)
            KeyStroke newKey = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
            boolean rebound = manager.rebindHotkey("TEST_ACTION", newKey);
            assertTrue("Rebind la Ctrl+R a reușit", rebound);
            assertTrue("Noul display text conține R", binding.getDisplayText().contains("R"));

            manager.shutdown();
        } catch (Exception e) {
            assertTrue("Eroare la HotkeyManager: " + e.getMessage(), false);
        }
    }

    private static void testSimpleJson() {
        System.out.println("\n--- Test 6: Parser JSON Minimalist (SimpleJson) ---");
        try {
            String sampleJson = "{\"status\":\"ok\", \"code\": 200, \"value\": 19.45, \"enabled\": true, \"nested\": {\"city\": \"Chisinau\"}}";
            java.util.Map<String, Object> map = SimpleJson.parseObject(sampleJson);

            assertTrue("Cheia 'status' este 'ok'", "ok".equals(SimpleJson.getString(map, "status", "")));
            assertTrue("Cheia 'code' este 200", SimpleJson.getInt(map, "code", 0) == 200);
            assertTrue("Cheia 'value' este 19.45", Math.abs(SimpleJson.getDouble(map, "value", 0.0) - 19.45) < 0.001);
            assertTrue("Nested object conține 'city'='Chisinau'", "Chisinau".equals(SimpleJson.getString(SimpleJson.getMap(map, "nested"), "city", "")));
        } catch (Exception e) {
            assertTrue("Eroare la SimpleJson: " + e.getMessage(), false);
        }
    }
}
