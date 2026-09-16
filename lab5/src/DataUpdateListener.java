/**
 * Interfață de tip Observer (Ascultător) pentru notificarea componentelor UI
 * și a pictogramei din System Tray cu privire la actualizările de date.
 * 
 * Ilustrează Observer Pattern din Paradigma Orientată pe Obiecte.
 */
public interface DataUpdateListener {

    /**
     * Apelat când s-au recepționat cu succes date meteorologice noi.
     */
    void onWeatherDataUpdated(WeatherData data);

    /**
     * Apelat când s-au recepționat cu succes date valutare noi.
     */
    void onCurrencyDataUpdated(CurrencyData data);

    /**
     * Apelat la schimbarea stării aplicației (ex: încărcare, succes, eroare).
     */
    void onStatusChanged(AppStatus status, String message);

    /**
     * Apelat la fiecare secundă pentru afișarea numărătorii inverse până la următoarea citire.
     */
    void onNextUpdateCountdown(int secondsRemaining);

    /**
     * Apelat când utilizatorul schimbă intervalul de citire a datelor.
     */
    void onIntervalChanged(int intervalSeconds);

    /**
     * Apelat când se comută între modul Vreme și Curs Valutar.
     */
    void onServiceTypeChanged(DataServiceType newType);
}
