# Quarkus Issue #36213 sample

Sample project for Quarkus issue [#36213](https://github.com/quarkusio/quarkus/issues/36213).

This project has an example of a custom [SpanExporter](https://javadoc.io/doc/io.opentelemetry/opentelemetry-sdk/1.44.1/io/opentelemetry/sdk/package-summary.html)
(RedisSpanExporter) that caches OpenTelemetry spans in Redis and a REST resource (TraceResource) to create new traces
and retrieve them from Redis.

RedisSpanExporter is a CDI bean, which, as of Quarkus 3.23.3, causes Quarkus to use it instead of the [default
exporter](https://quarkus.io/guides/opentelemetry#default-2). My goal (which I believe to be the same as that of the
author of issue [#36213](https://github.com/quarkusio/quarkus/issues/36213)) is to be able to have a custom span
exporter that is a CDI bean and can be used alongside the default span exporter instead of replacing it.

## How to run?

You can run the application using:

```shell script
./mvnw quarkus:dev
```

## How to test?

1. Create a new trace with `curl -X POST http://localhost:8080/traces` - the trace id will be returned in the response
body
2. Copy the trace id
3. Fetch the trace with `curl http://localhost:8080/traces/{id}` by replacing `{id}` with the id of the trace - the
response should contain the trace, which is retrieved from Redis, showing that RedisSpanExporter exported it with
success<sup>*</sup>
4. Go to the Grafana endpoint indicated in the following log:

   ```text
   Dev Service Lgtm started, config: {grafana.endpoint=...}
   ```

5. Click `Explore` in the left panel, select "Tempo" in the data source dropdown and search for the trace id - Grafana
will indicate that the trace could not be found
6. Delete RedisSpanExporter
7. Restart the app
8. Create a new trace and search for it in Grafana - the trace is now shown because the custom span exporter was deleted
and Quarkus is now using the default span exporter

<sup>
* the trace cache has an expiration time of 10 minutes, so this step must be executed up to 10 minutes after the trace
is exported, otherwise, it will return a <code>404 Not Found</code> response
</sup>