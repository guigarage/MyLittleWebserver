package com.guigarage.webserver;

import spark.Request;

import java.util.Objects;

/**
 * Created by hendrikebbers on 23.06.16.
 */
public class HttpRequest {

    private final Request request;

    private final MyLittleWebservice webservice;

    public HttpRequest(final Request request, final MyLittleWebservice webservice) {
        this.request = Objects.requireNonNull(request, "request");
        this.webservice = Objects.requireNonNull(webservice, "webservice");
    }

    public void sendResponse(final String message) {
        webservice.handled(request, message);
    }
}
