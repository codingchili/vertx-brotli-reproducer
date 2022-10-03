package com.codingchili.vertx.brotli;

import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple HTTP server that uses compression (gzip, br, deflate) as requested by client.
 */
public class CompressingHttpServer {
    public static final String WEBROOT = "src/test/resources/web";
    private static final String KEYSTORE_PATH = "config/keystore.jks";
    private static final String KEYSTORE_PASS = "secret";
    private static final String INDEX_PAGE = "index.html";
    private static final Logger logger = Logger.getLogger(
            CompressingHttpServer.class.getSimpleName());

    public static Future<HttpServer> create(Vertx vertx) {
        var promise = Promise.<HttpServer>promise();
        var router = Router.router(vertx);

        router.route("/*").handler(
                StaticHandler.create(WEBROOT)
                        .setCachingEnabled(false)
                        .setSendVaryHeader(false)
                        .setFilesReadOnly(false)
                        .setIndexPage(INDEX_PAGE));

        vertx.createHttpServer(createOptions())
                .requestHandler(router)
                .listen(-1, (done) -> {
                    if (done.succeeded()) {
                        var port = done.result().actualPort();
                        logger.info("server listening on https://localhost:%d".formatted(port));
                        promise.complete(done.result());
                    } else {
                        logger.log(Level.WARNING, "failed to listen", done.cause());
                        promise.fail(done.cause());
                    }
                });
        return promise.future();
    }

    private static HttpServerOptions createOptions() {
        return new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions()
                        .setPath(KEYSTORE_PATH)
                        .setPassword(KEYSTORE_PASS)
                ).setCompressionSupported(true)
                .addCompressor(StandardCompressionOptions.brotli())
                .addCompressor(StandardCompressionOptions.gzip());
    }
}