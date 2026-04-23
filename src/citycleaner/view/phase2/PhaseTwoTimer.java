package citycleaner.view.phase2;

/**
 * Countdown timer used by Phase 2.
 */
public class PhaseTwoTimer {
    private final long durationMs;
    private long startMs;
    private boolean running;

    public PhaseTwoTimer(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public void start() {
        running = true;
        startMs = System.currentTimeMillis();
    }

    public void reset() {
        running = false;
        startMs = 0L;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFinished() {
        return running && getRemainingMillis() <= 0L;
    }

    public long getRemainingMillis() {
        if (!running) {
            return durationMs;
        }

        long elapsed = System.currentTimeMillis() - startMs;
        long remaining = durationMs - elapsed;
        return Math.max(0L, remaining);
    }

    public int getRemainingSecondsCeil() {
        long remaining = getRemainingMillis();
        return (int) Math.ceil(remaining / 1000.0);
    }
}
