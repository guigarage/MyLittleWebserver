package com.guigarage.webserver;

/**
 * Created by hendrikebbers on 23.06.16.
 */
public class WebServer {

    public static void main(String[] args) {
        while (true) {
            HttpRequest request = getNextRequest();
            request.sendResponse("Hello from Server!");
        }
    }






    //IMPL...

    private static MyLittleWebservice webserver = new MyLittleWebservice();

    public static HttpRequest getNextRequest() {
        return webserver.getNextRequest();
    }
}
