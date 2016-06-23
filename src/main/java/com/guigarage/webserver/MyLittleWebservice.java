package com.guigarage.webserver;

import spark.Request;
import spark.Response;
import spark.Route;
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
public class MyLittleWebservice implements Route {

    private final IdentityHashMap<Request, Request> waitingRequests = new IdentityHashMap();

    private final Map<Request, String> handledRequests = new HashMap<>();

    private final Lock synchronizer = new ReentrantLock();

    private final Condition handleCondition = synchronizer.newCondition();

    private final Condition incomingCondition = synchronizer.newCondition();

    public MyLittleWebservice() {
        Spark.get("/hello", this);
    }

    @Override
    public synchronized Object handle(Request request, Response response) {
        synchronizer.lock();
        try {
            waitingRequests.put(request, request);
            incomingCondition.signalAll();

            while (!handledRequests.containsKey(request)) {
                try {
                    handleCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            return handledRequests.get(request);
        } finally {
            synchronizer.unlock();
        }
    }

    public void handled(Request request, String responseContent) {
        synchronizer.lock();
        try {
            waitingRequests.remove(request);
            handledRequests.put(request, responseContent);
            handleCondition.signalAll();
        } finally {
            synchronizer.unlock();
        }
    }

    public HttpRequest getNextRequest() {
        synchronizer.lock();
        try {
            while (waitingRequests.isEmpty()) {
                try {
                    incomingCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return new HttpRequest(waitingRequests.keySet().iterator().next(), this);
        } finally {
            synchronizer.unlock();
        }
    }
}
