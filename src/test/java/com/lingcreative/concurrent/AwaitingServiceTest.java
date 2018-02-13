package com.lingcreative.concurrent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AwaitingServiceTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService taskExecutor;
    private AwaitingService awaitingService;

    private int objects = 9;
    private CountDownLatch finishCountDown = new CountDownLatch(objects);

    @Before
    public void init() {
        awaitingService = new AwaitingService();
        taskExecutor = Executors.newFixedThreadPool(15);
    }

    @After
    public void destroy() throws InterruptedException {
        taskExecutor.shutdown();
        awaitingService.stop();
    }

    @Test
    public void run() throws Exception {
        print("~~~~~~~~~~~~~~~~~~");
        awaitingService.start();
        for (int i = 0; i < objects; i++) {
            long sleep = 1L * (long) (15 * (Math.random() + 0.1d));
            wait(i, sleep);
        }

        finishCountDown.await();
        print("^^^^^^^^^^^^^^^^^^");
    }

    private void task(final int number, final long sleep, final CountDownLatch latch) {
        taskExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                print("Begin %d, sleep = %d", number, sleep);
                Thread.sleep(sleep);
                latch.countDown();
                print("End %d, sleep = %d", number, sleep);
                finishCountDown.countDown();
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
