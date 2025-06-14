# Quarkus Issue #36213 workaround

This branch gives an example of how a [ConfigurableSpanExporterProvider](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi/1.44.1/io/opentelemetry/sdk/autoconfigure/spi/traces/ConfigurableSpanExporterProvider.html)
can be used to work around the issue described in [the README from the main branch](https://github.com/jl-martins/quarkus-issue-36213-sample/blob/main/README.md).

Please refer to the aforementioned README to learn about the main classes in this project and the problem that this
workaround solves.

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

5. Click `Explore` in the left panel, select "Tempo" in the data source dropdown and search for the trace id - The trace
is shown, confirming that the default span exporter is being used alongside RedisSpanExporter

<sup>
* the trace cache has an expiration time of 10 minutes, so this step must be executed up to 10 minutes after the trace
is exported, otherwise, it will return a <code>404 Not Found</code> response
</sup>