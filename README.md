# vertx-brotli-reproducer

### Description
When using the vertx http/2 server with brotli compression enabled as per the docs, certain files gets cut off. 
I have a project that loads 40 resources on the index page, only two of these fail (one font, one js file).

### Tests
Note: Built for JDK 17, the bundled version of Gradle does not support JDK 18.

```shell
./gradlew test

# or ..

./gradlew test --info
```

This starts a HTTP/2 TLS server with GZIP/BR compression enabled.
The webserver uses the webroot `src/test/resources/web`.

This will run the following 3 test cases, for each compression algorithm.

* #1 download /index.html
* #2 download /app.view.js
* #3 download /font/Quantico-Regular.ttf

Tests #2 and #3 is expected to fail for `br`.

### Browser verification

The server can be started from `com.codingchili.vertx.brotli.Main` to test using the browser. 

