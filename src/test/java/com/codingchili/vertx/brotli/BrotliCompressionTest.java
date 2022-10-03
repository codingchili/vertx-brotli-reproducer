package com.codingchili.vertx.brotli;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class BrotliCompressionTest extends HttpServerCompressionTest {

    static {
        Brotli4jLoader.ensureAvailability();
    }

    @Override
    protected CharSequence acceptedContentType() {
        return HttpHeaderValues.BR;
    }

    @Test
    public void localCompressDecompressAppView() {
        verifyCompressDecompressFor("/app-view.js");
    }

    @Test
    public void localCompressDecompressFont() {
        verifyCompressDecompressFor("/font/Quantico-Regular.ttf");
    }

    private void verifyCompressDecompressFor(String webFile) {
        var async = test.async();

        loadFile(webFile).onSuccess(file -> {
            try {
                var compressed = Encoder.compress(file.getBytes());
                var decompressed = DirectDecompress.decompress(compressed);

                test.assertEquals(decompressed.getResultStatus(), DecoderJNI.Status.DONE);
                test.assertEquals(
                        file.getByteBuf(),
                        decompressed.getDecompressedDataByteBuf()
                );

                async.complete();
            } catch (Exception e) {
                test.fail(e);
            }
        }).onFailure(test::fail);
    }

    public Buffer decompress(Buffer buffer) throws Exception {
        // decompression not handled by client - implemented here.
        var result = DirectDecompress.decompress(buffer.getBytes());
        var status = result.getResultStatus();

        if (status.equals(DecoderJNI.Status.DONE)) {
            return Buffer.buffer(
                    result.getDecompressedDataByteBuf()
            );
        } else {
            throw new RuntimeException("brotli decompress returned status " + status.name());
        }
    }
}
