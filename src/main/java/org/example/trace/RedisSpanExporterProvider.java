package org.example.trace;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import jakarta.enterprise.inject.spi.CDI;

public class RedisSpanExporterProvider implements ConfigurableSpanExporterProvider {
    public static final String EXPORTER_NAME = "redis";

    @Override
    public SpanExporter createExporter(ConfigProperties properties) {
        RedisSpanExporter.Config config = RedisSpanExporter.Config.fromProperties(properties);
        SpanMapper spanMapper = CDI.current().select(SpanMapper.class).get();
        TraceCache traceCache = CDI.current().select(TraceCache.class).get();
        return new RedisSpanExporter(config, spanMapper, traceCache);
    }

    @Override
    public String getName() {
        return EXPORTER_NAME;
    }
}
