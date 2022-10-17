# vertx-brotli-reproducer

Issue: https://github.com/eclipse-vertx/vert.x/issues/4501

### Description
When using the vertx http/2 server with brotli compression enabled as per the docs, certain files gets cut off. 
I have a project that loads 40 resources on the index page, only two of these fail (one font, one js file).

https://vertx.io/docs/vertx-core/java/#_http_compression_algorithms

### Tests
Note: Built for JDK 17, the bundled version of Gradle does not support JDK 18.

```shell
./gradlew test
./gradlew test --info

# or ..

./mvnw test
```

This starts a HTTP/2 TLS server with GZIP/BR compression enabled.
The webserver uses the webroot `src/test/resources/web`.

This will run testcases for both compression algorithms, for each file

1. request file from server with tested compression algorithm using `Accept-Encoding`.
2. verify server responds with expected `Content-Encoding`.
3. Decompress the body using local algorithm.
4. Load the requested file from disk and compare with the decompressed body.

The following test cases are expected to fail when running `BrotliCompressionTest`.

- `verifyHttpResourceDecompressionForFont`
- `verifyHttpResourceDecompressionForAppView`

The `BrotliCompressionTest` also locally verifies that compressing a file from disk and decompressing it returns in identical output for all tested files.
The HTTP server uses the default compression parameters from StandardCompressionOptions.

### Browser verification

The server can be started from `com.codingchili.vertx.brotli.Main` to test using the browser. Instructions
for reproducing/verifying are placed in the index.html.

### Expected output

```shell
$ ./gradlew test
> Task :test

com.codingchili.vertx.brotli.BrotliCompressionTest > verifyHttpResourceDecompressionForFont FAILED
    java.lang.RuntimeException at BrotliCompressionTest.java:66

com.codingchili.vertx.brotli.BrotliCompressionTest > verifyHttpResourceDecompressionForAppView FAILED
    java.lang.RuntimeException at BrotliCompressionTest.java:66

8 tests completed, 2 failed

> Task :test FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///C:/Users/chili/Documents/vertx-http-brotli/build/reports/tests/test/index.html

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 4s
4 actionable tasks: 1 executed, 3 up-to-date

```

Run with `--info` for logging and stack traces, unfortunately brotli
decompression does not return any detailed error message only a `com.aayushatharva.brotli4j.decoder.DecoderJNI.Status` enum value.
