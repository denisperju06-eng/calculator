package core;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Controlerul central al aplicației rezidente (Arhitectură MVC / Mediator).
 * Coordonează serviciile HTTP de rețea, planificatorul periodic (Timer),
 * pictograma și meniul din System Tray, combinatiile de taste (Hotkeys) și fereastra Dashboard.
 * 
 * Îndeplinește toate cerințele laboratorului de POO:
 * a. Conectarea la un serviciu Internet pentru obținerea informației (vreme/valută)
 * b. Ascunderea aplicației de pe bara Start cu afișarea în System tray
 * c. Setarea perioadei de recitire a informației din Internet și citirea repetată
 * d. Modificarea pictogramei aplicației în dependență de starea aplicației sau datelor citite
 * e. Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident
 */
public class ResidentApp implements TrayManager.TrayActionListener, DashboardFrame.DashboardListener {

    private final WeatherService weatherService;
    private final CurrencyService currencyService;
    private final PeriodicScheduler scheduler;
    private final HotkeyManager hotkeyManager;
    private final TrayManager trayManager;
    private final DashboardFrame dashboardFrame;
    private final ExecutorService networkExecutor;

    private DataServiceType activeType = DataServiceType.WEATHER;
    private AppStatus currentStatus = AppStatus.IDLE;
    private boolean isMac;

    public ResidentApp() {
        this.isMac = System.getProperty("os.name", "").toLowerCase().contains("mac");
        this.networkExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "NetworkWorkerThread");
            t.setDaemon(true);
            return t;
        });

        // 1. Inițializăm serviciile de rețea HTTP (Cerința a)
        this.weatherService = new WeatherService();
        this.currencyService = new CurrencyService();

        // 2. Inițializăm managerul de combinații de taste (Cerința e)
        this.hotkeyManager = new HotkeyManager();
        setupDefaultHotkeys();

        // 3. Inițializăm managerul System Tray (Cerința b & d)
        this.trayManager = new TrayManager(this);

        // 4. Inițializăm fereastra grafică (Dashboard)
        this.dashboardFrame = new DashboardFrame(this, hotkeyManager);

        // 5. Inițializăm planificatorul periodic cu java.util.Timer (Cerința c)
        this.scheduler = new PeriodicScheduler(
                PeriodicScheduler.DEFAULT_INTERVAL_SECONDS,
                this::fetchActiveData,
                this::handleCountdownTick
        );
    }

    /**
     * Înregistrează combinațiile de taste implicite active pentru aplicația rezidentă.
     * Cerința e: Setarea combinațiilor de taste active.
     */
    private void setupDefaultHotkeys() {
        int mask = isMac ? KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK : 
                          KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK;

        // Hotkey 1: Actualizează acum
        hotkeyManager.registerHotkey(
                "REFRESH",
                "Actualizare Forțată Date",
                "Interoghează imediat serviciul HTTP activ din Internet",
                KeyStroke.getKeyStroke(KeyEvent.VK_R, mask),
                this::onRefreshRequested
        );

        // Hotkey 2: Afișează / Ascunde fereastra din tray
        hotkeyManager.registerHotkey(
                "TOGGLE_WINDOW",
                "Arată/Ascunde Tablou",
                "Afișează sau minimizează tabloul de comandă în System Tray",
                KeyStroke.getKeyStroke(KeyEvent.VK_D, mask),
                this::onToggleDashboard
        );

        // Hotkey 3: Comută între Vreme și Valută
        hotkeyManager.registerHotkey(
                "TOGGLE_MODE",
                "Comutare Mod de Date",
                "Comută între monitorizarea vremii și cursul valutar",
                KeyStroke.getKeyStroke(KeyEvent.VK_M, mask),
                this::toggleActiveType
        );

        // Hotkey 4: Deschide configurarea setărilor și a tastelor
        hotkeyManager.registerHotkey(
                "SETTINGS",
                "Deschide Setări & Taste",
                "Afișează fereastra de configurare a intervalului și tastelor rapide",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, mask),
                this::onOpenSettingsRequested
        );

        // Hotkey 5: Închidere completă a aplicației rezidente
        hotkeyManager.registerHotkey(
                "QUIT",
                "Închidere Aplicație",
                "Oprește aplicația rezidentă și o elimină din System Tray",
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask),
                this::onExitRequested
        );
    }

    /**
     * Pornește ciclul de viață al aplicației rezidente.
     */
    public void start() {
        // 1. Adăugăm pictograma în System Tray
        boolean trayAdded = trayManager.initialize();
        if (!trayAdded) {
            System.out.println("[ResidentApp] SystemTray nu este disponibil. Aplicația va rula în mod fereastră normală.");
            dashboardFrame.setVisible(true);
        } else {
            // Conform Cerinței b, aplicația rezidentă pornește minimizată în System Tray
            // sau deschide fereastra la prima rulare și poate fi ascunsă
            dashboardFrame.setVisible(true);
            trayManager.showNotification(
                    "Monitor Rezident Pornit",
                    "Aplicația rulează în System Tray. Folosiți clic pe iconiță sau Ctrl+Shift+D pentru afișare.",
                    TrayIcon.MessageType.INFO
            );
        }

        // 2. Afișăm în consolă ghidul de comenzi și taste rapide
        hotkeyManager.printHotkeysHelp();

        // 3. Pornim planificatorul periodic (Cerința c)
        scheduler.start();
    }

    /**
     * Execută interogarea HTTP asincron pentru serviciul curent activ.
     * Cerința a: Conectarea la un serviciu Internet pentru obținerea informației.
     * Cerința d: Modificarea pictogramei aplicației în dependență de datele citite.
     */
    public void fetchActiveData() {
        networkExecutor.submit(() -> {
            updateStatus(AppStatus.FETCHING, "Preluare date din Internet (" + activeType.getDisplayName() + ")...");
            try {
                if (activeType == DataServiceType.WEATHER) {
                    WeatherData data = weatherService.fetchData();
                    updateStatus(AppStatus.SUCCESS, "Date meteo actualizate: " + data.getTraySummary());

                    // Actualizăm UI și pictograma din System Tray
                    dashboardFrame.onWeatherDataUpdated(data);
                    trayManager.updateWeatherData(data);

                    System.out.println("[HTTP Success] Meteo: " + data.getTraySummary());
                } else {
                    CurrencyData data = currencyService.fetchData();
                    updateStatus(AppStatus.SUCCESS, "Date valutare actualizate: " + data.getTraySummary());

                    // Actualizăm UI și pictograma din System Tray
                    dashboardFrame.onCurrencyDataUpdated(data);
                    trayManager.updateCurrencyData(data);

                    System.out.println("[HTTP Success] Valută: " + data.getTraySummary());
                }
            } catch (Exception e) {
                String errorMsg = "Eroare rețea: " + e.getMessage();
                System.err.println("[HTTP Error] " + errorMsg);
                updateStatus(AppStatus.ERROR, errorMsg);
                trayManager.showNotification("Eroare Conexiune", errorMsg, TrayIcon.MessageType.WARNING);
            }
        });
    }

    private void updateStatus(AppStatus status, String message) {
        this.currentStatus = status;
        dashboardFrame.onStatusChanged(status, message);
        trayManager.updateStatus(status, message);
    }

    private void handleCountdownTick(int secondsRemaining) {
        dashboardFrame.onNextUpdateCountdown(secondsRemaining);
    }

    public void toggleActiveType() {
        if (activeType == DataServiceType.WEATHER) {
            onServiceTypeSelected(DataServiceType.CURRENCY);
        } else {
            onServiceTypeSelected(DataServiceType.WEATHER);
        }
    }

    // ==================== Callback-uri TrayActionListener & DashboardListener ====================

    @Override
    public void onToggleDashboard() {
        dashboardFrame.toggleVisibility();
    }

    @Override
    public void onRefreshRequested() {
        System.out.println("[ResidentApp] Declanșare citire manuală forțată...");
        scheduler.triggerImmediate();
    }

    @Override
    public void onServiceTypeSelected(DataServiceType type) {
        if (type != null && type != activeType) {
            this.activeType = type;
            dashboardFrame.onServiceTypeChanged(type);
            System.out.println("[ResidentApp] Schimbat mod de lucru în: " + type.getDisplayName());
            // Declanșăm imediat citirea pentru noul mod selectat
            scheduler.triggerImmediate();
        }
    }

    @Override
    public void onServiceTypeChanged(DataServiceType type) {
        onServiceTypeSelected(type);
    }

    @Override
    public void onIntervalSelected(int seconds) {
        scheduler.setIntervalSeconds(seconds);
        dashboardFrame.onIntervalChanged(seconds);
        System.out.println("[ResidentApp] Intervalul de recitire a fost setat la: " + seconds + " secunde.");
        trayManager.showNotification("Interval Actualizat", "Perioada de recitire este acum de " + seconds + " secunde.", TrayIcon.MessageType.INFO);
    }

    @Override
    public void onIntervalChanged(int seconds) {
        onIntervalSelected(seconds);
    }

    @Override
    public void onCitySelected(CityLocation location) {
        weatherService.setCurrentLocation(location);
        dashboardFrame.updateCitySelection(location);
        System.out.println("[ResidentApp] Oraș selectat: " + location.getName());
        if (activeType == DataServiceType.WEATHER) {
            scheduler.triggerImmediate();
        }
    }

    @Override
    public void onCityChanged(CityLocation city) {
        onCitySelected(city);
    }

    @Override
    public void onCurrencyChanged(String baseCurrency, String targetCurrency) {
        currencyService.setBaseCurrency(baseCurrency);
        currencyService.setTargetCurrency(targetCurrency);
        System.out.println("[ResidentApp] Valute selectate: " + baseCurrency + " -> " + targetCurrency);
        if (activeType == DataServiceType.CURRENCY) {
            scheduler.triggerImmediate();
        }
    }

    @Override
    public void onTargetCurrencySelected(String currencyCode) {
        currencyService.setTargetCurrency(currencyCode);
        dashboardFrame.updateCurrencySelection(currencyCode);
        System.out.println("[ResidentApp] Valută țintă selectată: " + currencyCode);
        if (activeType == DataServiceType.CURRENCY) {
            scheduler.triggerImmediate();
        }
    }

    @Override
    public void onOpenSettingsRequested() {
        SwingUtilities.invokeLater(() -> {
            SettingsDialog dialog = new SettingsDialog(dashboardFrame, hotkeyManager, scheduler, () -> {
                dashboardFrame.onIntervalChanged(scheduler.getIntervalSeconds());
            });
            dialog.setVisible(true);
        });
    }

    @Override
    public void onHideToTrayRequested() {
        trayManager.showNotification(
                "Aplicație Minimizată",
                "Aplicația rulează în System Tray. Faceți clic pe pictogramă pentru redeschidere.",
                TrayIcon.MessageType.INFO
        );
    }

    @Override
    public void onExitRequested() {
        System.out.println("[ResidentApp] Oprire aplicație rezidentă...");
        scheduler.stop();
        trayManager.remove();
        hotkeyManager.shutdown();
        dashboardFrame.dispose();
        networkExecutor.shutdownNow();
        System.out.println("[ResidentApp] Aplicație oprită cu succes. La revedere!");
        System.exit(0);
    }
}
