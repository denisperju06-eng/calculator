package core;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Planificator periodic bazat pe java.util.Timer și java.util.TimerTask.
 * Permite configurarea dinamică a intervalului de recitire și execuția repetată.
 * 
 * Cerința c: Setarea perioadei de recitire a informației din Internet și citirea repetată a informației.
 */
public class PeriodicScheduler {

    public static final int DEFAULT_INTERVAL_SECONDS = 30;
    public static final int MIN_INTERVAL_SECONDS = 5;
    public static final int MAX_INTERVAL_SECONDS = 3600;

    private int intervalSeconds;
    private int secondsUntilNextFetch;
    private boolean isRunning = false;

    private Timer timer;
    private TimerTask fetchTask;
    private TimerTask countdownTask;

    private final Runnable fetchAction;
    private final CountdownListener countdownListener;

    @FunctionalInterface
    public interface CountdownListener {
        void onTick(int secondsRemaining);
    }

    public PeriodicScheduler(Runnable fetchAction, CountdownListener countdownListener) {
        this(DEFAULT_INTERVAL_SECONDS, fetchAction, countdownListener);
    }

    public PeriodicScheduler(int initialIntervalSeconds, Runnable fetchAction, CountdownListener countdownListener) {
        this.intervalSeconds = sanitizeInterval(initialIntervalSeconds);
        this.secondsUntilNextFetch = this.intervalSeconds;
        this.fetchAction = fetchAction;
        this.countdownListener = countdownListener;
    }

    /**
     * Pornește execuția periodică cu citire imediată la start.
     */
    public synchronized void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        reschedule();
    }

    /**
     * Oprește planificatorul și anulează sarcinile active.
     */
    public synchronized void stop() {
        isRunning = false;
        cancelCurrentTasks();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Modifică dinamic perioada de recitire și replanifică sarcinile.
     * @param newIntervalSeconds noul interval în secunde
     */
    public synchronized void setIntervalSeconds(int newIntervalSeconds) {
        int sanitized = sanitizeInterval(newIntervalSeconds);
        if (this.intervalSeconds == sanitized && isRunning) {
            return;
        }
        this.intervalSeconds = sanitized;
        this.secondsUntilNextFetch = sanitized;

        if (isRunning) {
            reschedule();
        }
    }

    public synchronized int getIntervalSeconds() {
        return intervalSeconds;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    /**
     * Declanșează o recitire forțată imediată și resetează cronometrul.
     */
    public synchronized void triggerImmediate() {
        if (!isRunning) {
            // Executăm direct acțiunea dacă timer-ul nu rulează
            new Thread(fetchAction, "ImmediateFetchThread").start();
            return;
        }
        // Anulăm sarcinile vechi și le repornim imediat
        reschedule();
    }

    private synchronized void reschedule() {
        cancelCurrentTasks();

        if (timer != null) {
            timer.cancel();
        }
        // Cream un Timer daemon cu nume descriptiv
        timer = new Timer("ResidentDataFetchTimer", true);
        secondsUntilNextFetch = intervalSeconds;

        // 1. Sarcina periodică de preluare a datelor
        fetchTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    fetchAction.run();
                } catch (Exception e) {
                    System.err.println("[Scheduler] Eroare la execuția acțiunii de fetch: " + e.getMessage());
                } finally {
                    synchronized (PeriodicScheduler.this) {
                        secondsUntilNextFetch = intervalSeconds;
                    }
                }
            }
        };

        // 2. Sarcina de numărătoare inversă la fiecare secundă
        countdownTask = new TimerTask() {
            @Override
            public void run() {
                int remaining;
                synchronized (PeriodicScheduler.this) {
                    if (secondsUntilNextFetch > 0) {
                        secondsUntilNextFetch--;
                    }
                    remaining = secondsUntilNextFetch;
                }
                if (countdownListener != null) {
                    try {
                        countdownListener.onTick(remaining);
                    } catch (Exception ignored) {}
                }
            }
        };

        long periodMillis = (long) intervalSeconds * 1000L;
        // Planificăm citirea: prima execuție imediat (delay 0), apoi repetat la fiecare periodMillis
        timer.scheduleAtFixedRate(fetchTask, 0, periodMillis);
        // Planificăm numărătoarea inversă la fiecare 1000 ms
        timer.scheduleAtFixedRate(countdownTask, 1000, 1000);
    }

    private synchronized void cancelCurrentTasks() {
        if (fetchTask != null) {
            fetchTask.cancel();
            fetchTask = null;
        }
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    private int sanitizeInterval(int seconds) {
        if (seconds < MIN_INTERVAL_SECONDS) return MIN_INTERVAL_SECONDS;
        if (seconds > MAX_INTERVAL_SECONDS) return MAX_INTERVAL_SECONDS;
        return seconds;
    }
}
