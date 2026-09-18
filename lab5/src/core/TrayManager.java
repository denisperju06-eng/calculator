package core;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/**
 * Gestionează pictogramele, meniul contextual și notificările din System Tray.
 * Asigură rularea rezidentă a aplicației în bara de stare/notificare a sistemului de operare.
 * 
 * Cerința b: Ascunderea aplicației de pe bara Start cu afișarea în System tray.
 * Cerința d: Modificarea pictogramei aplicației în dependență de starea aplicației sau datelor citite.
 */
public class TrayManager {

    public interface TrayActionListener {
        void onToggleDashboard();
        void onRefreshRequested();
        void onServiceTypeSelected(DataServiceType type);
        void onIntervalSelected(int seconds);
        void onCitySelected(CityLocation location);
        void onTargetCurrencySelected(String currencyCode);
        void onOpenSettingsRequested();
        void onExitRequested();
    }

    private final TrayActionListener listener;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private PopupMenu popupMenu;

    private MenuItem titleHeaderItem;
    private MenuItem toggleWindowItem;
    private MenuItem refreshItem;
    private Menu modeMenu;
    private Menu intervalMenu;
    private Menu cityMenu;
    private Menu currencyMenu;
    private CheckboxMenuItem notificationsCheckbox;

    private boolean notificationsEnabled = true;
    private DataServiceType currentType = DataServiceType.WEATHER;

    public TrayManager(TrayActionListener listener) {
        this.listener = listener;
    }

    /**
     * Inițializează și adaugă pictograma rezidentă în System Tray.
     * @return true dacă System Tray este suportat și pictograma a fost adăugată cu succes.
     */
    public boolean initialize() {
        if (!SystemTray.isSupported()) {
            System.err.println("[TrayManager] SystemTray nu este suportat pe această platformă.");
            return false;
        }

        try {
            systemTray = SystemTray.getSystemTray();
            popupMenu = createPopupMenu();

            // Pictogramă inițială
            Image initialImage = IconRenderer.createStatusIcon(AppStatus.FETCHING);
            trayIcon = new TrayIcon(initialImage, "ResidentInfo - Pornire...", popupMenu);
            trayIcon.setImageAutoSize(true);

            // Clic pe pictogramă deschide sau ascunde fereastra principală
            trayIcon.addActionListener(e -> {
                if (listener != null) {
                    listener.onToggleDashboard();
                }
            });

            // Pentru compatibilitate pe diverse platforme (dublu clic sau clic stânga)
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                        // Pe macOS click stânga deschide meniul popup nativ; pe Windows/Linux acționează ca toggle
                        String os = System.getProperty("os.name", "").toLowerCase();
                        if (!os.contains("mac")) {
                            if (listener != null) {
                                listener.onToggleDashboard();
                            }
                        }
                    }
                }
            });

            systemTray.add(trayIcon);
            System.out.println("[TrayManager] Pictograma rezidentă a fost adăugată în System Tray cu succes.");
            return true;
        } catch (AWTException e) {
            System.err.println("[TrayManager] Eroare la adăugarea pictogramei în System Tray: " + e.getMessage());
            return false;
        }
    }

    /**
     * Construiește meniul contextual pentru System Tray.
     */
    private PopupMenu createPopupMenu() {
        PopupMenu menu = new PopupMenu();

        // 1. Antet cu datele curente
        titleHeaderItem = new MenuItem("ResidentInfo Monitor [Pornire...]");
        titleHeaderItem.setEnabled(false);
        menu.add(titleHeaderItem);
        menu.addSeparator();

        // 2. Control fereastră
        toggleWindowItem = new MenuItem("Arată / Ascunde Tabloul (Ctrl+Shift+D)");
        toggleWindowItem.addActionListener(e -> {
            if (listener != null) listener.onToggleDashboard();
        });
        menu.add(toggleWindowItem);

        // 3. Forțare actualizare
        refreshItem = new MenuItem("Actualizează Acum (Ctrl+Shift+R)");
        refreshItem.addActionListener(e -> {
            if (listener != null) listener.onRefreshRequested();
        });
        menu.add(refreshItem);
        menu.addSeparator();

        // 4. Mod de lucru (Vreme vs Curs Valutar)
        modeMenu = new Menu("Mod de Lucru");
        MenuItem weatherModeItem = new MenuItem("🌡️ Prognoză Meteo");
        weatherModeItem.addActionListener(e -> {
            currentType = DataServiceType.WEATHER;
            if (listener != null) listener.onServiceTypeSelected(DataServiceType.WEATHER);
        });
        MenuItem currencyModeItem = new MenuItem("💱 Curs Valutar");
        currencyModeItem.addActionListener(e -> {
            currentType = DataServiceType.CURRENCY;
            if (listener != null) listener.onServiceTypeSelected(DataServiceType.CURRENCY);
        });
        modeMenu.add(weatherModeItem);
        modeMenu.add(currencyModeItem);
        menu.add(modeMenu);

        // 5. Schimbare interval recitire
        intervalMenu = new Menu("Interval Recitire");
        addIntervalMenuItem(intervalMenu, "10 secunde", 10);
        addIntervalMenuItem(intervalMenu, "30 secunde (Implicit)", 30);
        addIntervalMenuItem(intervalMenu, "1 minut", 60);
        addIntervalMenuItem(intervalMenu, "5 minute", 300);
        addIntervalMenuItem(intervalMenu, "15 minute", 900);
        menu.add(intervalMenu);

        // 6. Submeniu Orașe
        cityMenu = new Menu("Alege Orașul");
        for (CityLocation city : CityLocation.getDefaultCities()) {
            MenuItem cityItem = new MenuItem(city.getName() + " (" + city.getCountry() + ")");
            cityItem.addActionListener(e -> {
                if (listener != null) listener.onCitySelected(city);
            });
            cityMenu.add(cityItem);
        }
        menu.add(cityMenu);

        // 7. Submeniu Valute
        currencyMenu = new Menu("Valută Țintă (Baza: EUR)");
        String[] currencies = {"MDL", "RON", "USD", "GBP", "CHF"};
        for (String c : currencies) {
            MenuItem currItem = new MenuItem(c);
            currItem.addActionListener(e -> {
                if (listener != null) listener.onTargetCurrencySelected(c);
            });
            currencyMenu.add(currItem);
        }
        menu.add(currencyMenu);
        menu.addSeparator();

        // 8. Opțiuni suplimentare
        notificationsCheckbox = new CheckboxMenuItem("Notificări Balon active", true);
        notificationsCheckbox.addItemListener(e -> {
            notificationsEnabled = notificationsCheckbox.getState();
        });
        menu.add(notificationsCheckbox);

        MenuItem settingsItem = new MenuItem("Configurare Hotkeys & Setări...");
        settingsItem.addActionListener(e -> {
            if (listener != null) listener.onOpenSettingsRequested();
        });
        menu.add(settingsItem);
        menu.addSeparator();

        // 9. Ieșire din aplicația rezidentă
        MenuItem exitItem = new MenuItem("Ieșire din Aplicație");
        exitItem.addActionListener(e -> {
            if (listener != null) listener.onExitRequested();
        });
        menu.add(exitItem);

        return menu;
    }

    private void addIntervalMenuItem(Menu parent, String label, int seconds) {
        MenuItem item = new MenuItem(label);
        item.addActionListener(e -> {
            if (listener != null) {
                listener.onIntervalSelected(seconds);
            }
        });
        parent.add(item);
    }

    /**
     * Actualizează pictograma din tray și tooltip-ul pe baza datelor meteo.
     * Cerința d: Modificarea pictogramei în dependență de datele citite.
     */
    public void updateWeatherData(WeatherData data) {
        if (trayIcon == null || data == null) return;

        SwingUtilities.invokeLater(() -> {
            Image iconImage = IconRenderer.createWeatherIcon(data.getCondition(), data.getTemperature());
            trayIcon.setImage(iconImage);
            String tooltip = data.getTraySummary() + " | " + data.getFormattedTimestamp();
            if (tooltip.length() > 63) {
                tooltip = tooltip.substring(0, 60) + "..."; // Limitare AWT tooltip pe anumite OS
            }
            trayIcon.setToolTip(tooltip);
            titleHeaderItem.setLabel(data.getTraySummary());
        });
    }

    /**
     * Actualizează pictograma din tray și tooltip-ul pe baza datelor valutare.
     * Cerința d: Modificarea pictogramei în dependență de datele citite.
     */
    public void updateCurrencyData(CurrencyData data) {
        if (trayIcon == null || data == null) return;

        SwingUtilities.invokeLater(() -> {
            Image iconImage = IconRenderer.createCurrencyIcon(data.getTrend(), data.getTargetCurrency());
            trayIcon.setImage(iconImage);
            String tooltip = data.getTraySummary() + " | " + data.getFormattedTimestamp();
            if (tooltip.length() > 63) {
                tooltip = tooltip.substring(0, 60) + "...";
            }
            trayIcon.setToolTip(tooltip);
            titleHeaderItem.setLabel(data.getTraySummary());
        });
    }

    /**
     * Actualizează pictograma când starea aplicației se schimbă (ex: încărcare, eroare).
     */
    public void updateStatus(AppStatus status, String message) {
        if (trayIcon == null) return;

        SwingUtilities.invokeLater(() -> {
            if (status == AppStatus.FETCHING || status == AppStatus.ERROR || status == AppStatus.OFFLINE) {
                Image iconImage = IconRenderer.createStatusIcon(status);
                trayIcon.setImage(iconImage);
            }
            if (message != null && !message.isEmpty()) {
                titleHeaderItem.setLabel(message);
            }
        });
    }

    /**
     * Afișează o notificare de tip balon (System Tray Notification).
     */
    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null && notificationsEnabled) {
            SwingUtilities.invokeLater(() -> {
                trayIcon.displayMessage(title, message, type);
            });
        }
    }

    public void remove() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }

    public boolean isSupported() {
        return SystemTray.isSupported();
    }
}
