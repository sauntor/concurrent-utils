package com.lingcreative.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An efficient worker for waiting many {@link AwaitCapable} objects.
 */
public class AwaitingService {

    private static final AtomicLong ID = new AtomicLong();

    private final long id = ID.incrementAndGet();

    private String name = AwaitingService.class.getSimpleName() + "-" + id;

    private Logger logger = LoggerFactory.getLogger(name);

    private volatile int threads  = 1;
    private volatile long timeWhenEmpty = 73;
    private volatile TimeUnit timeUnitWhenEmpty = TimeUnit.MILLISECONDS;
    private volatile long timeWhileWaiting = 1;
    private volatile TimeUnit timeUnitWhileWaiting = TimeUnit.NANOSECONDS;

    private volatile boolean started = false;
    private volatile boolean stopping = false;
    private volatile boolean stopped = false;

    private ExecutorService waitingService;

    private Set<AwaitCapableObject> awaitCapableObjects = new ConcurrentSkipListSet<>();

    public void start() {
        if (started) {
            return;
        }
        started = true;
        waitingService = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            waitingService.submit(new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            });
        }
    }

    public void waitFor(AwaitCapableObject object) {
        if (stopping) {
            return;
        }
        awaitCapableObjects.add(object);
    }

    public void cancel(AwaitCapableObject object) {
        awaitCapableObjects.remove(object);
    }
    private void loop() {
        while (!stopping) {
            try {
                if (awaitCapableObjects.isEmpty()) {
                    Thread.sleep(timeUnitWhenEmpty.toNanos(timeWhenEmpty));
                }
                tryWait();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
    private void tryWait() throws InterruptedException {
        for (AwaitCapableObject awaitCapableObject : awaitCapableObjects) {
            boolean handled = false;
            try {
                awaitCapableObject.start();
                if (awaitCapableObject.await(timeWhileWaiting, timeUnitWhileWaiting)) {
                    handled = true;
                    awaitCapableObject.success();
                }
            } catch (InterruptedException e) {
                if (Thread.currentThread().isInterrupted() && stopping) {
                    logger.info("Stopped.");
                    throw e;
                }
                logger.info("Unknown interruption!", e);
            } catch (Throwable throwable) {
                handled = awaitCapableObject.exception(throwable);
                logger.debug("Handle exception with: {}", !handled ? "continue" : "done", throwable);
            } finally {
                if (handled) {
                    awaitCapableObjects.remove(awaitCapableObject);
                    logger.info("{} is completed", awaitCapableObject);
                }
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    @PreDestroy
    public void stop() {
        if (stopping) {
            return;
        }
        stopping = true;
        waitingService.shutdown();
        for (AwaitCapableObject object : awaitCapableObjects) {
            object.cancel();
        }
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setTimeWhenEmpty(long timeWhenEmpty) {
        this.timeWhenEmpty = timeWhenEmpty;
    }

    public void setTimeUnitWhenEmpty(TimeUnit timeUnitWhenEmpty) {
        this.timeUnitWhenEmpty = timeUnitWhenEmpty;
    }

    public void setTimeWhileWaiting(long timeWhileWaiting) {
        this.timeWhileWaiting = timeWhileWaiting;
    }

    public void setTimeUnitWhileWaiting(TimeUnit timeUnitWhileWaiting) {
        this.timeUnitWhileWaiting = timeUnitWhileWaiting;
    }

    public static interface CancelHandler {
        void accept(AwaitCapable throwable);
    }

    public static interface ExceptionHandler {
        boolean test(Throwable throwable);
    }

    public static interface Operation {
        void on() throws  Throwable;
    }

    public static interface AwaitCapable {
        boolean await(long time, TimeUnit unit) throws InterruptedException;
    }

    public static interface PlainExceptionHandler {
        void test(Throwable throwable);
    }
}
