package core;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Fereastra grafică principală (Dashboard) a aplicației rezidente.
 * Implementează ascunderea în System Tray și afișarea detaliată a datelor meteo și valutare.
 * 
 * Cerința b: Ascunderea aplicației de pe bara Start cu afișarea în System tray.
 * Cerința c: Setarea perioadei de recitire și afișarea repetată a informației.
 */
public class DashboardFrame extends JFrame implements DataUpdateListener {

    public interface DashboardListener {
        void onRefreshRequested();
        void onServiceTypeChanged(DataServiceType type);
        void onCityChanged(CityLocation city);
        void onCurrencyChanged(String baseCurrency, String targetCurrency);
        void onIntervalChanged(int seconds);
        void onOpenSettingsRequested();
        void onHideToTrayRequested();
        void onExitRequested();
    }

    private final DashboardListener listener;
    private final HotkeyManager hotkeyManager;

    // Componente Header
    private JLabel titleLabel;
    private JLabel statusBadgeLabel;
    private JLabel lastUpdatedLabel;
    private JRadioButton weatherRadio;
    private JRadioButton currencyRadio;

    // Componente CardLayout
    private CardLayout cardLayout;
    private JPanel cardsContainer;

    // Componente Card Meteo
    private JComboBox<CityLocation> cityComboBox;
    private JLabel weatherIconLabel;
    private JLabel temperatureLabel;
    private JLabel conditionLabel;
    private JLabel humidityLabel;
    private JLabel windLabel;

    // Componente Card Valută
    private JComboBox<String> baseCurrencyCombo;
    private JComboBox<String> targetCurrencyCombo;
    private JLabel rateDisplayLabel;
    private JLabel trendLabel;
    private JTable ratesTable;
    private DefaultTableModel ratesTableModel;

    // Componente Footer / Controale
    private JLabel countdownLabel;
    private JButton refreshButton;
    private JButton hideToTrayButton;
    private JButton settingsButton;
    private JLabel hotkeysBannerLabel;

    public DashboardFrame(DashboardListener listener, HotkeyManager hotkeyManager) {
        super("Monitor Rezident - Date Meteo & Valutare (POO Lab 5)");
        this.listener = listener;
        this.hotkeyManager = hotkeyManager;

        initComponents();

        // Comportament rezident: la apăsarea pe 'X', fereastra NU închide aplicația,
        // ci o ascunde în System Tray conform Cerinței b!
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hideToTray();
            }
        });

        pack();
        setSize(680, 560);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // 1. Panoul Superior (Header)
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Panoul Central (CardLayout: Meteo vs Valută)
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.add(createWeatherPanel(), DataServiceType.WEATHER.name());
        cardsContainer.add(createCurrencyPanel(), DataServiceType.CURRENCY.name());
        add(cardsContainer, BorderLayout.CENTER);

        // 3. Panoul Inferior (Footer cu cronometru, butoane și banner hotkeys)
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 6));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        titleLabel = new JLabel("Monitor Rezident Internet");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        leftTop.add(titleLabel);

        statusBadgeLabel = new JLabel(" Pornire... ");
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setBackground(new Color(220, 220, 220));
        statusBadgeLabel.setForeground(Color.BLACK);
        statusBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusBadgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        leftTop.add(statusBadgeLabel);

        header.add(leftTop, BorderLayout.WEST);

        // Selector radio pentru mod de lucru
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        weatherRadio = new JRadioButton("🌡️ Meteo", true);
        currencyRadio = new JRadioButton("💱 Curs Valutar", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(weatherRadio);
        bg.add(currencyRadio);

        weatherRadio.addActionListener(e -> {
            cardLayout.show(cardsContainer, DataServiceType.WEATHER.name());
            if (listener != null) listener.onServiceTypeChanged(DataServiceType.WEATHER);
        });
        currencyRadio.addActionListener(e -> {
            cardLayout.show(cardsContainer, DataServiceType.CURRENCY.name());
            if (listener != null) listener.onServiceTypeChanged(DataServiceType.CURRENCY);
        });

        modePanel.add(weatherRadio);
        modePanel.add(currencyRadio);
        header.add(modePanel, BorderLayout.EAST);

        lastUpdatedLabel = new JLabel("Ultima actualizare: Niciuna", SwingConstants.LEFT);
        lastUpdatedLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lastUpdatedLabel.setForeground(Color.GRAY);
        lastUpdatedLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 6, 6));
        header.add(lastUpdatedLabel, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createWeatherPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Selector oraș
        JPanel topSelect = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topSelect.add(new JLabel("Alege Orașul:"));
        cityComboBox = new JComboBox<>(CityLocation.getDefaultCities().toArray(new CityLocation[0]));
        cityComboBox.addActionListener(e -> {
            CityLocation loc = (CityLocation) cityComboBox.getSelectedItem();
            if (loc != null && listener != null) {
                listener.onCityChanged(loc);
            }
        });
        topSelect.add(cityComboBox);
        panel.add(topSelect, BorderLayout.NORTH);

        // Card central cu vremea
        JPanel mainCard = new JPanel(new GridBagLayout());
        mainCard.setBackground(new Color(245, 248, 252));
        mainCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 235), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        weatherIconLabel = new JLabel("☀️", SwingConstants.CENTER);
        weatherIconLabel.setFont(new Font("SansSerif", Font.PLAIN, 64));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
        mainCard.add(weatherIconLabel, gbc);

        temperatureLabel = new JLabel("--.-°C", SwingConstants.LEFT);
        temperatureLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        temperatureLabel.setForeground(new Color(30, 60, 110));
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1;
        mainCard.add(temperatureLabel, gbc);

        conditionLabel = new JLabel("Se încarcă...", SwingConstants.LEFT);
        conditionLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        conditionLabel.setForeground(new Color(80, 90, 100));
        gbc.gridx = 1; gbc.gridy = 1;
        mainCard.add(conditionLabel, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        mainCard.add(new JSeparator(), gbc);

        JPanel details = new JPanel(new GridLayout(1, 2, 20, 5));
        details.setOpaque(false);
        humidityLabel = new JLabel("💧 Umiditate aer: --%");
        humidityLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        windLabel = new JLabel("💨 Viteza vântului: -- km/h");
        windLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        details.add(humidityLabel);
        details.add(windLabel);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        mainCard.add(details, gbc);

        panel.add(mainCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCurrencyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Selector valute
        JPanel topSelect = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topSelect.add(new JLabel("Valută de bază:"));
        String[] bases = {"EUR", "USD", "GBP"};
        baseCurrencyCombo = new JComboBox<>(bases);
        topSelect.add(baseCurrencyCombo);

        topSelect.add(new JLabel("Valută țintă:"));
        String[] targets = {"MDL", "RON", "USD", "EUR", "GBP", "CHF"};
        targetCurrencyCombo = new JComboBox<>(targets);
        topSelect.add(targetCurrencyCombo);

        java.awt.event.ActionListener currencyChangeAction = e -> {
            String b = (String) baseCurrencyCombo.getSelectedItem();
            String t = (String) targetCurrencyCombo.getSelectedItem();
            if (listener != null && b != null && t != null) {
                listener.onCurrencyChanged(b, t);
            }
        };
        baseCurrencyCombo.addActionListener(currencyChangeAction);
        targetCurrencyCombo.addActionListener(currencyChangeAction);

        panel.add(topSelect, BorderLayout.NORTH);

        // Card central cu cursul curent și tabelul tuturor ratelor
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));

        JPanel rateCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        rateCard.setBackground(new Color(254, 251, 243));
        rateCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 220, 190), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        rateDisplayLabel = new JLabel("1 EUR = --.---- MDL");
        rateDisplayLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        rateDisplayLabel.setForeground(new Color(110, 70, 10));
        rateCard.add(rateDisplayLabel);

        trendLabel = new JLabel("— Stabil");
        trendLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        rateCard.add(trendLabel);

        centerContainer.add(rateCard, BorderLayout.NORTH);

        // Tabel cu toate ratele disponibile
        String[] cols = {"Valută", "Rată de schimb", "Echivalent pentru 100 unități"};
        ratesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        ratesTable = new JTable(ratesTableModel);
        ratesTable.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(ratesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Tabel Cursuri Valutare Directe"));
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(10, 4, 4, 4)
        ));

        // Rândul 1: Cronometru & Butoane
        JPanel controlsRow = new JPanel(new BorderLayout(10, 4));

        countdownLabel = new JLabel("⏳ Următoarea citire: calculare...", SwingConstants.LEFT);
        countdownLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        countdownLabel.setForeground(new Color(70, 70, 70));
        controlsRow.add(countdownLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        refreshButton = new JButton("Actualizează Acum");
        refreshButton.setToolTipText("Forțează citirea din rețea (Hotkey: Ctrl+Shift+R)");
        refreshButton.addActionListener(e -> {
            if (listener != null) listener.onRefreshRequested();
        });
        buttons.add(refreshButton);

        settingsButton = new JButton("Setări & Hotkeys");
        settingsButton.addActionListener(e -> {
            if (listener != null) listener.onOpenSettingsRequested();
        });
        buttons.add(settingsButton);

        hideToTrayButton = new JButton("Ascunde în Tray");
        hideToTrayButton.setToolTipText("Ascunde fereastra în System Tray (Aplicația rămâne activă în fundal)");
        hideToTrayButton.addActionListener(e -> hideToTray());
        buttons.add(hideToTrayButton);

        controlsRow.add(buttons, BorderLayout.EAST);
        footer.add(controlsRow);
        footer.add(Box.createVerticalStrut(8));

        // Rândul 2: Banner combinatii de taste (Cerința e)
        hotkeysBannerLabel = new JLabel("Taste rapide active: Ctrl+Shift+R (Refresh) | Ctrl+Shift+D (Arată/Ascunde) | Ctrl+Shift+M (Mod) | Ctrl+Shift+Q (Ieșire)", SwingConstants.CENTER);
        hotkeysBannerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hotkeysBannerLabel.setForeground(new Color(110, 110, 110));
        hotkeysBannerLabel.setAlignmentX(CENTER_ALIGNMENT);
        footer.add(hotkeysBannerLabel);

        return footer;
    }

    /**
     * Ascunde fereastra în System Tray și afișează o notificare de confirmare.
     * Cerința b: Ascunderea aplicației de pe bara Start cu afișarea în System tray.
     */
    public void hideToTray() {
        setVisible(false);
        if (listener != null) {
            listener.onHideToTrayRequested();
        }
    }

    public void showFromTray() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    public void toggleVisibility() {
        if (isVisible()) {
            hideToTray();
        } else {
            showFromTray();
        }
    }

    // ==================== Implementare DataUpdateListener ====================

    @Override
    public void onWeatherDataUpdated(WeatherData data) {
        if (data == null) return;
        SwingUtilities.invokeLater(() -> {
            temperatureLabel.setText(data.getFormattedTemperature());
            conditionLabel.setText(data.getCondition().getDescriptionRo());
            weatherIconLabel.setText(data.getCondition().getEmoji());
            humidityLabel.setText("💧 Umiditate aer: " + String.format("%.0f%%", data.getHumidity()));
            windLabel.setText("💨 Viteza vântului: " + String.format("%.1f km/h", data.getWindSpeed()));
            lastUpdatedLabel.setText("Ultima actualizare meteo: " + data.getFormattedTimestamp() + " (" + data.getLocationName() + ")");
        });
    }

    @Override
    public void onCurrencyDataUpdated(CurrencyData data) {
        if (data == null) return;
        SwingUtilities.invokeLater(() -> {
            rateDisplayLabel.setText("1 " + data.getBaseCurrency() + " = " + data.getFormattedRate() + " " + data.getTargetCurrency());

            CurrencyData.Trend trend = data.getTrend();
            trendLabel.setText(trend.getLabel());
            if (trend == CurrencyData.Trend.RISING) {
                trendLabel.setForeground(new Color(0, 140, 50));
            } else if (trend == CurrencyData.Trend.FALLING) {
                trendLabel.setForeground(new Color(200, 30, 30));
            } else {
                trendLabel.setForeground(Color.DARK_GRAY);
            }

            // Actualizăm tabelul tuturor ratelor
            ratesTableModel.setRowCount(0);
            Map<String, Double> map = data.getAllRates();
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                double rate = entry.getValue();
                double hundred = rate * 100.0;
                ratesTableModel.addRow(new Object[]{
                        entry.getKey(),
                        String.format("%.4f", rate),
                        String.format("%.2f %s", hundred, entry.getKey())
                });
            }

            lastUpdatedLabel.setText("Ultima actualizare valutară: " + data.getFormattedTimestamp());
        });
    }

    @Override
    public void onStatusChanged(AppStatus status, String message) {
        SwingUtilities.invokeLater(() -> {
            statusBadgeLabel.setText(" " + status.getDescriptionRo() + " ");
            statusBadgeLabel.setBackground(status.getBadgeColor());
            statusBadgeLabel.setForeground(Color.WHITE);
            if (message != null && !message.isEmpty()) {
                statusBadgeLabel.setToolTipText(message);
            }
        });
    }

    @Override
    public void onNextUpdateCountdown(int secondsRemaining) {
        SwingUtilities.invokeLater(() -> {
            countdownLabel.setText("⏳ Următoarea citire din Internet în: " + secondsRemaining + "s");
        });
    }

    @Override
    public void onIntervalChanged(int intervalSeconds) {
        SwingUtilities.invokeLater(() -> {
            countdownLabel.setText("⏳ Următoarea citire în: " + intervalSeconds + "s (Interval: " + intervalSeconds + "s)");
        });
    }

    @Override
    public void onServiceTypeChanged(DataServiceType newType) {
        SwingUtilities.invokeLater(() -> {
            if (newType == DataServiceType.WEATHER) {
                weatherRadio.setSelected(true);
                cardLayout.show(cardsContainer, DataServiceType.WEATHER.name());
            } else {
                currencyRadio.setSelected(true);
                cardLayout.show(cardsContainer, DataServiceType.CURRENCY.name());
            }
        });
    }

    public void updateCitySelection(CityLocation city) {
        if (cityComboBox != null && city != null) {
            cityComboBox.setSelectedItem(city);
        }
    }

    public void updateCurrencySelection(String targetCurrency) {
        if (targetCurrencyCombo != null && targetCurrency != null) {
            targetCurrencyCombo.setSelectedItem(targetCurrency);
        }
    }
}
