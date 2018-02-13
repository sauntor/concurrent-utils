package com.lingcreative.concurrent;

import com.lingcreative.concurrent.AwaitingService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;

public class AwaitCapableObject implements AwaitingService.AwaitCapable, Comparable<AwaitCapableObject> {

    private static final AtomicLong ID = new AtomicLong();

    private final long id = ID.incrementAndGet();

    private volatile boolean started = false;
    private AwaitingService.AwaitCapable awaitCapable;
    private AwaitingService.Operation onSuccess;
    private AwaitingService.Operation onStart;
    private AwaitingService.ExceptionHandler onError;

    private AwaitingService.CancelHandler onCancel;

    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        return awaitCapable.await(time, unit);
    }

    public void success() throws Throwable {
        if (onSuccess != null) {
            onSuccess.on();
        }
    }

    public void cancel() {
        if (onCancel != null) {
            onCancel.accept(awaitCapable);
        }
    }
    public void start() throws Throwable {
        if (started) {
            return;
        }
        started = true;
        if (onStart != null) {
            onStart.on();
        }
    }

    public boolean exception(Throwable e) {
        if (onError != null) {
            return onError.test(e);
        }
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int compareTo(AwaitCapableObject o) {
        long r = id - (o == null ? 0 : o.id);
        return r > 0 ? 1 : (r < 0 ? -1 : 0);
    }

    public static class Builder {

        private AwaitCapableObject object = new AwaitCapableObject();

        public Builder success(AwaitingService.Operation operation) {
            object.onSuccess = operation;
            return this;
        }

        public Builder exception(final AwaitingService.PlainExceptionHandler consumer) {
            object.onError = new AwaitingService.ExceptionHandler() {
                @Override
                public boolean test(Throwable throwable) {
                    consumer.test(throwable);
                    return true;
                }
            };
            return this;
        }

        public Builder start(AwaitingService.Operation operation) {
            object.onStart = operation;
            return this;
        }

        public Builder await(AwaitingService.AwaitCapable await) {
            object.awaitCapable = await;
            return this;
        }

        public Builder countDown(final CountDownLatch countDownLatch) {
            object.awaitCapable = new AwaitingService.AwaitCapable() {
                @Override
                public boolean await(long time, TimeUnit unit) throws InterruptedException {
                    return countDownLatch.await(time, unit);
                }
            };
            object.onCancel = new AwaitingService.CancelHandler() {
                @Override
                public void accept(AwaitingService.AwaitCapable throwable) {
                    while (countDownLatch.getCount() > 0) {
                        countDownLatch.countDown();
                    }
                }
            };
            return this;
        }

        public Builder condition(final Condition condition, final boolean signalAll) {
            object.awaitCapable = new AwaitingService.AwaitCapable() {
                @Override
                public boolean await(long time, TimeUnit unit) throws InterruptedException {
                    return condition.await(time, unit);
                }
            };
            object.onCancel = new AwaitingService.CancelHandler() {
                @Override
                public void accept(AwaitingService.AwaitCapable throwable) {
                    if (signalAll) {
                        condition.signalAll();
                    }
                }
            };
            return this;
        }

        public AwaitCapableObject build() {
            return object;
        }
    }
}
