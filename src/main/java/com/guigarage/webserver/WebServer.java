package com.guigarage.webserver;

import java.time.LocalTime;

/**
 * Created by hendrikebbers on 23.06.16.
 */
public class WebServer {

    public static void main(String[] args) throws Exception{
        while (true) {
            final HttpRequest request = getNextRequest();
            Runnable r = () -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                request.sendResponse("Hello from Server! Time: " + LocalTime.now());
            };
            new Thread(r).start();
        }
    }






    //IMPL...

    private static MyLittleWebservice webserver = new MyLittleWebservice();

    public static HttpRequest getNextRequest() {
        return webserver.getNextRequest();
    }
}
