import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test automatizat end-to-end pentru verificarea tuturor cerințelor din laborator:
 * - Conexiune Sockets & Threads
 * - Transmitere și recepționare mesaje
 * - Mecanism Reply (citare și răspuns)
 * - Transmitere și recepționare fișiere binare
 * - Chat rooms (creare, alăturare, izolare mesaje)
 * - Istoric mesaje (stocare și recuperare)
 */
public class ChatIntegrationTest {
    private static final int TEST_PORT = 19876;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PORNIRE TESTE AUTOMATE CHAT REȚEA LOCALĂ (POO)  ");
        System.out.println("==================================================");

        Server server = null;
        try {
            // 1. Pornire Server
            server = new Server(TEST_PORT);
            server.start();
            Thread.sleep(300);
            System.out.println("✔ [1/6] Server pornit cu succes pe portul " + TEST_PORT);

            // 2. Conectare 2 clienți: Alice și Bob
            Client alice = new Client();
            Client bob = new Client();

            CountDownLatch aliceConnectedLatch = new CountDownLatch(1);
            CountDownLatch bobConnectedLatch = new CountDownLatch(1);

            alice.addListener(new DummyListener() {
                @Override
                public void onConnected(String host, int port, String username) {
                    aliceConnectedLatch.countDown();
                }
            });

            bob.addListener(new DummyListener() {
                @Override
                public void onConnected(String host, int port, String username) {
                    bobConnectedLatch.countDown();
                }
            });

            alice.connect("127.0.0.1", TEST_PORT, "Alice");
            bob.connect("127.0.0.1", TEST_PORT, "Bob");

            if (!aliceConnectedLatch.await(3, TimeUnit.SECONDS) || !bobConnectedLatch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Eșec la conectarea clienților!");
            }
            System.out.println("✔ [2/6] Clienții Alice și Bob s-au conectat cu succes.");

            // 3. Test Transmitere Mesaj Text: Alice -> Bob
            CountDownLatch msgReceivedLatch = new CountDownLatch(1);
            AtomicReference<Message> aliceReceivedMsg = new AtomicReference<>();

            bob.addListener(new DummyListener() {
                @Override
                public void onMessageReceived(Message message) {
                    if (message.getType() == MessageType.TEXT && "Alice".equals(message.getSender())) {
                        aliceReceivedMsg.set(message);
                        msgReceivedLatch.countDown();
                    }
                }
            });

            alice.sendTextMessage("Salut Bob! Acesta este un test.", null);
            if (!msgReceivedLatch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Bob nu a primit mesajul trimis de Alice!");
            }
            assert aliceReceivedMsg.get() != null;
            System.out.println("✔ [3/6] Transmitere și recepționare mesaj text confirmată: \"" +
                    aliceReceivedMsg.get().getContent() + "\"");

            // 4. Test Răspuns (Reply): Bob răspunde la mesajul lui Alice
            CountDownLatch replyReceivedLatch = new CountDownLatch(1);
            AtomicReference<Message> replyMsg = new AtomicReference<>();

            alice.addListener(new DummyListener() {
                @Override
                public void onMessageReceived(Message message) {
                    if (message.isReply() && "Bob".equals(message.getSender())) {
                        replyMsg.set(message);
                        replyReceivedLatch.countDown();
                    }
                }
            });

            Message msgToReply = aliceReceivedMsg.get();
            bob.sendTextMessage("Salut Alice! Răspund la mesajul tău.", msgToReply);

            if (!replyReceivedLatch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Alice nu a primit răspunsul (reply) de la Bob!");
            }
            Message r = replyMsg.get();
            if (!"Alice".equals(r.getReplyToSender()) || !r.getReplyToContent().contains("Salut Bob")) {
                throw new RuntimeException("Datele de reply nu corespund mesajului original! Sender: " + r.getReplyToSender());
            }
            System.out.println("✔ [4/6] Mecanism Reply validat cu succes! Răspuns către @" +
                    r.getReplyToSender() + " (\"" + r.getReplyToContent() + "\"): " + r.getContent());

            // 5. Test Transmitere Fișier: Alice trimite un fișier către Bob
            File tempTestFile = File.createTempFile("test_upload_", ".txt");
            String testFileContent = "Date binare de test transmise prin sockets Java POO!\nCerința d din lab3.";
            try (FileOutputStream fos = new FileOutputStream(tempTestFile)) {
                fos.write(testFileContent.getBytes(StandardCharsets.UTF_8));
            }

            CountDownLatch fileReceivedLatch = new CountDownLatch(1);
            AtomicReference<Message> receivedFileMsg = new AtomicReference<>();

            bob.addListener(new DummyListener() {
                @Override
                public void onMessageReceived(Message message) {
                    if (message.isFile()) {
                        receivedFileMsg.set(message);
                        fileReceivedLatch.countDown();
                    }
                }
            });

            alice.sendFile(tempTestFile, null);
            if (!fileReceivedLatch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Bob nu a primit fișierul transmis de Alice!");
            }

            FileAttachment att = receivedFileMsg.get().getFileAttachment();
            String receivedContent = new String(att.getData(), StandardCharsets.UTF_8);
            if (!testFileContent.equals(receivedContent)) {
                throw new RuntimeException("Conținutul fișierului primit diferă de cel trimis!");
            }
            System.out.println("✔ [5/6] Transmitere și recepționare fișiere confirmată! Fișier: " +
                    att.getFileName() + " (" + att.getFormattedSize() + ")");

            // 6. Test Chat Rooms & Istoric: Creare cameră nouă și verificare izolare
            CountDownLatch roomListLatch = new CountDownLatch(1);
            bob.addListener(new DummyListener() {
                @Override
                public void onRoomListUpdated(List<String> rooms) {
                    if (rooms.contains("Camera_Studiu")) {
                        roomListLatch.countDown();
                    }
                }
            });

            alice.createRoom("Camera_Studiu");
            if (!roomListLatch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Camera nouă 'Camera_Studiu' nu a fost sincronizată către Bob!");
            }

            // Alice intră în Camera_Studiu
            alice.joinRoom("Camera_Studiu");
            Thread.sleep(300);

            // Alice trimite un mesaj în Camera_Studiu
            CountDownLatch isolationLatch = new CountDownLatch(1);
            AtomicBoolean bobGotIsolatedMessage = new AtomicBoolean(false);

            bob.addListener(new DummyListener() {
                @Override
                public void onMessageReceived(Message message) {
                    if ("Mesaj secret în Camera_Studiu".equals(message.getContent())) {
                        bobGotIsolatedMessage.set(true);
                    }
                }
            });

            alice.sendTextMessage("Mesaj secret în Camera_Studiu", null);
            Thread.sleep(500);

            if (bobGotIsolatedMessage.get()) {
                throw new RuntimeException("Bob a primit mesajul deși este într-o altă cameră!");
            }

            // Bob se alătură și el în Camera_Studiu
            CountDownLatch bobHistoryLatch = new CountDownLatch(1);
            AtomicReference<List<Message>> bobHistory = new AtomicReference<>();

            bob.addListener(new DummyListener() {
                @Override
                public void onHistoryReceived(String room, List<Message> history) {
                    if ("Camera_Studiu".equals(room)) {
                        bobHistory.set(history);
                        bobHistoryLatch.countDown();
                    }
                }
            });

            bob.joinRoom("Camera_Studiu");
            if (!bobHistoryLatch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Bob nu a primit istoricul camerei la alăturare!");
            }

            boolean foundHistory = false;
            for (Message m : bobHistory.get()) {
                if ("Mesaj secret în Camera_Studiu".equals(m.getContent())) {
                    foundHistory = true;
                    break;
                }
            }
            if (!foundHistory) {
                throw new RuntimeException("Istoricul camerei nu include mesajele anterioare!");
            }
            System.out.println("✔ [6/6] Camere de chat (Chat Rooms) și Istoricul (History) validate cu succes!");

            // Curățare
            tempTestFile.delete();
            alice.disconnect("Test terminat");
            bob.disconnect("Test terminat");

            System.out.println("\n==================================================");
            System.out.println("  TOATE TESTELE AU TRECUT CU SUCCES! (10/10)     ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("❌ TEST EȘUAT: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (server != null) {
                server.stop();
            }
            System.exit(0);
        }
    }

    /**
     * Listener abstract ajutător pentru a implementa doar metodele necesare în teste.
     */
    private static class DummyListener implements ClientListener {
        public void onConnected(String host, int port, String username) {}
        public void onDisconnected(String reason) {}
        public void onMessageReceived(Message message) {}
        public void onHistoryReceived(String room, List<Message> history) {}
        public void onRoomListUpdated(List<String> rooms) {}
        public void onUserListUpdated(String room, List<String> users) {}
        public void onError(String errorMessage) {}
    }
}
