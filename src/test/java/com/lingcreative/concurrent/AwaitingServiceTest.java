package com.lingcreative.concurrent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class AwaitingServiceTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService taskExecutor;
    private AwaitingService awaitingService;

    @Before
    public void init() {
        awaitingService = new AwaitingService();
        taskExecutor = Executors.newFixedThreadPool(15);
    }

    @After
    public void destroy() throws InterruptedException {
        taskExecutor.awaitTermination(15, TimeUnit.MINUTES);
        awaitingService.stop();
    }

    @Test
    public void run() throws Exception {
        awaitingService.start();
        for (int i = 0; i < 9; i++) {
            long sleep = (long) (15 * (Math.random() + 0.1d));
            wait(i, sleep);
        }

        taskExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(5000L);
                AwaitingServiceTest.this.stop();
                return null;
            }
        });

        awaitingService.stop();
    }

    private void task(final int number, final long sleep, final CountDownLatch latch) {
        taskExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                print("Begin %d, sleep = %d", number, sleep);
                Thread.sleep(sleep);
                latch.countDown();
                print("End %d, sleep = %d", number, sleep);
                return null;
            }
        });
    }

    private void wait(final int number, final long sleep) {
        CountDownLatch latch = new CountDownLatch(1);
        AwaitCapableObject waitMe = AwaitCapableObject.builder()
                .countDown(latch)
                .success(new AwaitingService.Operation() {
                    @Override
                    public void on() throws Throwable {
                        print("Success %d, sleep = %d", number, sleep);
                    }
                })
                .build();

        awaitingService.waitFor(waitMe);

        task(number, sleep, latch);
    }

    private void stop() {
        taskExecutor.shutdown();
        awaitingService.stop();
    }

    private void print(String s, Object... args) {
        System.err.printf(Thread.currentThread().getName() + " - " + s + "\n", args);
        System.err.flush();
    }

}
