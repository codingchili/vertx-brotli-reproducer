package com.codingchili.vertx.brotli;

import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class BrotliCompressionTest extends HttpServerCompressionTest {

    @Override
    protected CharSequence acceptedContentType() {
        return HttpHeaderValues.BR;
    }

    public Buffer decompress(Buffer buffer) throws Exception {
        // decompression not handled by client - implemented here.
        var result = DirectDecompress.decompress(buffer.getBytes());
        var status = result.getResultStatus();

        if (status.equals(DecoderJNI.Status.DONE) || status.equals(DecoderJNI.Status.OK)) {
            return Buffer.buffer(
                    result.getDecompressedDataByteBuf()
            );
        } else {
            throw new RuntimeException("brotli decompress returned status " + status.name());
        }
    }
}
