package core;
/**
 * Interfață ascultător pentru evenimente legate de modificarea listei de Favorite.
 * Aplică șablonul de proiectare Observer.
 */
public interface FavoritesListener {

    /**
     * Apelat atunci când lista de favorite s-a modificat (adăugare, ștergere, editare).
     */
    void onFavoritesChanged();
}
