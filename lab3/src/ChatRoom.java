import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Clasa ce reprezintă o cameră de chat (Chat Room).
 * Gestionează participanții conectați la cameră și istoricul mesajelor din acea cameră.
 * Este thread-safe pentru acces concurent din mai multe fire de execuție.
 */
public class ChatRoom {
    private final String name;
    private final Set<ClientHandler> participants = Collections.synchronizedSet(new HashSet<>());
    private final List<Message> history = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_IN_MEMORY_HISTORY = 300;

    public ChatRoom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Adaugă un client la camera curentă.
     */
    public void addParticipant(ClientHandler client) {
        participants.add(client);
    }

    /**
     * Elimină un client din camera curentă.
     */
    public void removeParticipant(ClientHandler client) {
        participants.remove(client);
    }

    /**
     * Returnează o copie a setului de clienți din cameră.
     */
    public Set<ClientHandler> getParticipants() {
        synchronized (participants) {
            return new HashSet<>(participants);
        }
    }

    /**
     * Returnează numărul de participanți din cameră.
     */
    public int getParticipantCount() {
        return participants.size();
    }

    /**
     * Returnează lista numelor de utilizatori activi în această cameră.
     */
    public List<String> getParticipantUsernames() {
        List<String> list = new ArrayList<>();
        synchronized (participants) {
            for (ClientHandler client : participants) {
                if (client.getUsername() != null && !client.getUsername().trim().isEmpty()) {
                    list.add(client.getUsername());
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Adaugă un mesaj în istoricul camerei (păstrând maxim MAX_IN_MEMORY_HISTORY mesaje).
     */
    public void addMessage(Message message) {
        synchronized (history) {
            if (history.size() >= MAX_IN_MEMORY_HISTORY) {
                history.remove(0);
            }
            history.add(message);
        }
    }

    /**
     * Returnează o copie a întregului istoric din memorie.
     */
    public List<Message> getHistory() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    /**
     * Încarcă un lot de mesaje în istoric (de exemplu la citirea din fișierul de persistență).
     */
    public void loadHistory(List<Message> loadedHistory) {
        synchronized (history) {
            history.clear();
            history.addAll(loadedHistory);
        }
    }

    @Override
    public String toString() {
        return name + " (" + getParticipantCount() + " online)";
    }
}
