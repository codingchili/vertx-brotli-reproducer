package com.codingchili.vertx.brotli;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

@RunWith(VertxUnitRunner.class)
public class GzipCompressionTest extends HttpServerCompressionTest {

    @Override
    protected CharSequence acceptedContentType() {
        return HttpHeaderValues.GZIP;
    }

    @Override
    protected Buffer decompress(Buffer buffer) throws Exception {
        var gzip = new GZIPInputStream(new ByteArrayInputStream(buffer.getBytes()));
        return Buffer.buffer(gzip.readAllBytes());
    }
}
