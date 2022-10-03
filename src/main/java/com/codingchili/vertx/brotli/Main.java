package com.codingchili.vertx.brotli;

import io.vertx.core.Vertx;

import java.awt.*;
import java.net.URI;

/**
 * Use for browser testing, otherwise run tests.
 */
public class Main {

    public static void main(String[] args) {
        CompressingHttpServer.create(Vertx.vertx()).onSuccess(server -> {
            try {
                var port = server.actualPort();
                // open default browser.
                Desktop.getDesktop().browse(
                        URI.create("https://localhost:%d/".formatted(port))
                );
            } catch (Exception e) {
                // no desktop available.
            }
        });
    }

}
