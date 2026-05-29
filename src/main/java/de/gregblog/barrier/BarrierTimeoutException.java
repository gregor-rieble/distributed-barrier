package de.gregblog.barrier;

public class BarrierTimeoutException extends BarrierException {
    public BarrierTimeoutException(String message, Exception cause) {
        super(message, cause);
    }
}
