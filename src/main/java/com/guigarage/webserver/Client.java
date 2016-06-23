package com.guigarage.webserver;

import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by hendrikebbers on 23.06.16.
 */
public class Client {

    public static void main(String[] args) {

        for(int i = 0; i<10; i++) {
            ExecutorService parallelService = Executors.newFixedThreadPool(1000);

            IntStream.range(1, Integer.MAX_VALUE).forEach(e -> {
                parallelService.execute(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        URL url = new URL("http://localhost:8080/hello");
                        System.out.println(IOUtils.toString(url.openStream()) + " - Request took " + (System.currentTimeMillis() - startTime) + " ms");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            });
        }

    }

}
