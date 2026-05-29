package de.gregblog.barrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class BarrierService {
    private static final Logger LOG = LoggerFactory.getLogger(BarrierService.class);

    private final BarrierProperties properties;
    private final ConfigurableApplicationContext applicationContext;

    private CyclicBarrier barrier;
    private final AtomicInteger completions = new AtomicInteger(0);
    private volatile boolean shutdown = false;

    public BarrierService(
        BarrierProperties properties,
        ConfigurableApplicationContext applicationContext
    ) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        createBarrier(null);
    }

    public void await(Integer timeoutSeconds) throws BarrierException {
        final var timeout = Optional.ofNullable(timeoutSeconds)
            .orElseGet(properties::defaultTimeoutSeconds);

        final var barrierInstance = getBarrier();

        try {
            final var waiting = barrierInstance.getNumberWaiting();
            LOG.info("Party {}/{} entering barrier with timeout of {} seconds", waiting + 1, properties.numParties(), timeout);
            barrierInstance.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            shutdown = true;
            Thread.currentThread().interrupt();
            throw new BarrierException("Shutdown requested while waiting for barrier", e);
        } catch (BrokenBarrierException e) {
            if (shutdown) {
                throw new BarrierException("Shutdown requested while waiting for barrier", true, e);
            } else {
                throw new PartyLeftBarrierException("Broken barrier, another waiting party might have left", e);
            }
        } catch (TimeoutException e) {
            throw new BarrierTimeoutException("Waiting for barrier timed out after " + timeout + " seconds", e);
        } finally {
            if (!shutdown) {
                createBarrier(barrierInstance);
            }
        }
    }

    @EventListener(ContextClosedEvent.class)
    public synchronized void interruptWaitingParties() {
        shutdown = true;
        this.barrier.reset();
    }

    private synchronized void createBarrier(CyclicBarrier currentBarrier) {
        if (this.barrier == currentBarrier) {
            this.barrier = new CyclicBarrier(properties.numParties(), this::onCompletion);
            LOG.info("Initialized barrier with {} parties", properties.numParties());

            final var neededCompletions = properties.shutdownAfterCompletions();
            if (neededCompletions > 0) {
                LOG.info("Application will shutdown after {} completions. Current completion count: {}/{}", neededCompletions, completions.get(), neededCompletions);
            }
        }
    }

    private synchronized CyclicBarrier getBarrier() {
        return barrier;
    }

    private void onCompletion() {
        LOG.info("Barrier completed, all {} parties arrived", properties.numParties());

        final var numCompletions = completions.incrementAndGet();
        final var neededCompletions = properties.shutdownAfterCompletions();

        if (neededCompletions <= 0) {
            LOG.info("Barrier completed successfully {} times", numCompletions);
        } else {
            LOG.info("Barrier completed successfully {}/{} times", numCompletions, neededCompletions);
        }

        if (neededCompletions > 0 && numCompletions >= neededCompletions) {
            LOG.info("Reached {}/{} completions, shutting down application...", neededCompletions, neededCompletions);

            Thread.ofPlatform().start(() -> {
                int exitCode = SpringApplication.exit(applicationContext, () -> 0);
                System.exit(exitCode);
            });
        }
    }
}
