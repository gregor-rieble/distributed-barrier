package de.gregblog.barrier;

public class BarrierException extends RuntimeException {
    private final boolean shutdown;

    public BarrierException(String message, Exception cause) {
        this(message, false, cause);
    }

    public BarrierException(String message, boolean shutdown, Exception cause) {
        super(message, cause);
        this.shutdown = shutdown;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
