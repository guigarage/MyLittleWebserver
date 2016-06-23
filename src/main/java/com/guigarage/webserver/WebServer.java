package com.guigarage.webserver;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hendrikebbers on 23.06.16.
 */
public class WebServer {

    public static void main(String[] args) throws Exception {
        List<Future<?>> tasks = new CopyOnWriteArrayList<>();

        Lock taskLock = new ReentrantLock();

        Condition taskRemoved = taskLock.newCondition();

        Condition taskAdded = taskLock.newCondition();

        ExecutorService openTaskCheck = Executors.newSingleThreadExecutor();
        openTaskCheck.execute(() -> {
            while (true) {
                taskLock.lock();
                try {
                    tasks.forEach(f -> {
                        if (f.isDone()) {
                            tasks.remove(f);
                            taskRemoved.signal();
                        }
                    });
                    taskAdded.await();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    taskLock.unlock();
                }
            }
        });


        ExecutorService requestExecutor = Executors.newCachedThreadPool();
        while (true) {
            taskLock.lock();
            try {
                while (tasks.size() > 100) {
                    taskRemoved.await();
                }
                final HttpRequest request = getNextRequest();
                Runnable r = () -> {
                    request.sendResponse("Hello from Server! Time: " + LocalTime.now());
                };
                Future<?> f = requestExecutor.submit(r);
                tasks.add(f);
                taskAdded.signal();
            } finally {
                taskLock.unlock();
            }
        }
    }


    //IMPL...

    private static MyLittleWebservice webserver = new MyLittleWebservice();

    public static HttpRequest getNextRequest() {
        return webserver.getNextRequest();
    }
}
