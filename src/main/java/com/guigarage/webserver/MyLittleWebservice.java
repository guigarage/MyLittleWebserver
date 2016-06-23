package com.guigarage.webserver;

import spark.Request;
import spark.Spark;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hendrikebbers on 23.06.16.
 */
public class MyLittleWebservice {

    private final IdentityHashMap<Request, Request> waitingRequests = new IdentityHashMap();

    private final Map<Request, String> handledRequests = new HashMap<>();

    private final Lock requestLock = new ReentrantLock();

    private final Lock responseLock = new ReentrantLock();

    private final Condition incomingCondition = requestLock.newCondition();

    private final Condition handleCondition = responseLock.newCondition();

    public MyLittleWebservice() {

        int maxThreads = 200;
        int minThreads = 10;
        int timeOutMillis = 30;
        Spark.threadPool(maxThreads, minThreads, timeOutMillis);

        Spark.port(8080);

        Spark.get("/hello", (request, response) -> handle(request));

        Spark.awaitInitialization();
    }

    private String handle(Request request) {
        requestLock.lock();
        try {
            System.out.println("Received Request " + request.toString());
            waitingRequests.put(request, request);
            incomingCondition.signalAll();
        } finally {
            requestLock.unlock();
        }


        responseLock.lock();
        try {
            while (!handledRequests.containsKey(request)) {
                try {
                    handleCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("Send Response for  Request " + request.toString());
            return handledRequests.remove(request);
        } finally {
            responseLock.unlock();
        }
    }

    public void handled(Request request, String responseContent) {
        System.out.println("Response " + request.toString() + " handled");

        requestLock.lock();
        try {
            waitingRequests.remove(request);
        } finally {
            requestLock.unlock();
        }

        responseLock.lock();
        try {
            handledRequests.put(request, responseContent);
            handleCondition.signalAll();
        } finally {
            responseLock.unlock();
        }
    }

    public HttpRequest getNextRequest() {
        requestLock.lock();
        try {
            while (waitingRequests.isEmpty()) {
                try {
                    incomingCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Request request = waitingRequests.keySet().iterator().next();
            System.out.println("Will handle Response " + request.toString());
            return new HttpRequest(waitingRequests.remove(request), this);
        } finally {
            requestLock.unlock();
        }
    }
}
