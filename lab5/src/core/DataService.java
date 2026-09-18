package core;
/**
 * Interfață generică ce definește contractul pentru serviciile de preluare a datelor prin Internet.
 * Aplică principiul de Abstracție și permite utilizarea polimorfică a diferitelor surse de date.
 * 
 * Cerința a: Conectarea la un serviciu Internet pentru obținerea informației (vreme/valută).
 * 
 * @param <T> tipul de date returnat (ex: WeatherData, CurrencyData)
 */
public interface DataService<T> {

    /**
     * Efectuează cererea HTTP prin java.net.HttpURLConnection și returnează datele parsate.
     * @return instanța cu datele preluate
     * @throws Exception în caz de eroare de rețea, timeout sau răspuns invalid
     */
    T fetchData() throws Exception;

    /**
     * Returnează tipul serviciului curent (WEATHER sau CURRENCY).
     */
    DataServiceType getServiceType();

    /**
     * Numele descriptiv al serviciului.
     */
    String getServiceName();

    /**
     * Un rezumat succint al ultimelor date preluate, potrivit pentru afișare în status bar sau tray.
     */
    String getSummary();
}
