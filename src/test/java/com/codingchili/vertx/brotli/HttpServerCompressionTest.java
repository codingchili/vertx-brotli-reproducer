package com.codingchili.vertx.brotli;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.Paths;
import java.util.logging.Logger;

@RunWith(VertxUnitRunner.class)
public abstract class HttpServerCompressionTest {
    private static final String HOST = "localhost";
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private Vertx vertx;
    private HttpServer server;
    private TestContext test;

    /**
     * @return string matching the accepted content type for the compression being tested.
     */
    protected abstract CharSequence acceptedContentType();

    /**
     * @param buffer a compressed buffer to decode matching the accepted content type.
     * @return decompressed buffer.
     */
    protected abstract Buffer decompress(Buffer buffer) throws Exception;

    @Before
    public void setUp(TestContext test) {
        this.test = test;
        this.vertx = Vertx.vertx();

        var async = test.async();
        createServer(vertx).onSuccess(server -> {
            this.server = server;
            async.complete();
        }).onFailure(test::fail);
    }

    @After
    public void teardown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void requestIndexPage() {
        requestAndCompare("/index.html");
    }

    @Test
    public void requestWebFont() {
        requestAndCompare("/font/Quantico-Regular.ttf");
    }

    @Test
    public void requestAppViewJs() {
        requestAndCompare("/app-view.js");
    }

    private void requestAndCompare(String webrootFile) {
        var async = test.async();
        CompositeFuture.all(requestFile(webrootFile), loadFile(webrootFile))
                .onSuccess(futures -> {
                    var http = futures.<Buffer>resultAt(0);
                    var disk = futures.<Buffer>resultAt(1);
                    logger.info("=== " + this.getClass().getSimpleName() + " ===" +
                            "\ncomparing file " + webrootFile +
                            "\n\tbytes from disk: " + disk.getBytes().length +
                            "\n\tbytes from http: " + http.getBytes().length
                    );
                    test.assertEquals(disk.length(), http.length());
                    async.complete();
                }).onFailure(test::fail);
    }

    private Future<Buffer> loadFile(String path) {
        return vertx.fileSystem().readFile(
                Paths.get(CompressingHttpServer.WEBROOT, path).toAbsolutePath().toString()
        );
    }

    private Future<Buffer> requestFile(String uri) {
        var promise = Promise.<io.vertx.core.buffer.Buffer>promise();

        createHttpClient()
                .request(HttpMethod.GET, server.actualPort(), HOST, uri)
                .compose(request -> {
                    request.putHeader(HttpHeaderNames.ACCEPT_ENCODING, acceptedContentType());
                    return request.send();
                })
                .compose(response -> {
                    //response.headers().forEach((key, value) -> logger.info("[%s] = '%s'".formatted(key, value)));
                    test.assertEquals(
                            acceptedContentType().toString(),
                            response.getHeader(HttpHeaderNames.CONTENT_ENCODING)
                    );
                    return response.body();
                })
                .onSuccess(body -> {
                    try {
                        // decompress the result if not handled by client.
                        promise.complete(decompress(body));
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }).onFailure(test::fail);

        return promise.future();
    }

    private HttpClient createHttpClient() {
        return vertx.createHttpClient(new HttpClientOptions()
                .setSsl(true)
                .setTrustAll(true)
                .setVerifyHost(false)
        );
    }

    protected Future<HttpServer> createServer(Vertx vertx) {
        return CompressingHttpServer.create(vertx);
    }
}
