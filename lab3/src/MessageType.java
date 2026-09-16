/**
 * Tipul de mesaj transmis în rețeaua locală de chat.
 * Folosit pentru a identifica acțiunea și conținutul fiecărui pachet transmis prin Socket.
 */
public enum MessageType {
    TEXT,               // Mesaj text normal de chat
    FILE,               // Mesaj ce conține un fișier atașat
    REPLY,              // Mesaj de tip răspuns (reply) la alt mesaj
    SYSTEM,             // Notificare de sistem (ex: utilizator s-a conectat/deconectat)
    CONNECT,            // Handshake inițial la conectarea clientului (nume utilizator)
    DISCONNECT,         // Notificare de deconectare a unui client
    JOIN_ROOM,          // Cerere de alăturare la o cameră de chat
    LEAVE_ROOM,         // Părăsirea camerei curente
    CREATE_ROOM,        // Cerere de creare a unei noi camere de chat
    ROOM_LIST,          // Trimiterea listei actualizate de camere către clienți
    USER_LIST,          // Trimiterea listei de utilizatori din camera curentă
    HISTORY_REQUEST,    // Cerere pentru istoricul mesajelor dintr-o cameră
    HISTORY_RESPONSE    // Răspuns de la server conținând istoricul mesajelor
}
