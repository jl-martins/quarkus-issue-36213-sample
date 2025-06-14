package org.example.trace;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniSubscriber;
import io.smallrye.mutiny.subscription.UniSubscription;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// N.B.: This class is not a CDI bean because of https://github.com/quarkusio/quarkus/issues/36213
@RequiredArgsConstructor
@Slf4j
public class RedisSpanExporter implements SpanExporter {
    private final RedisSpanExporter.Config config;
    private final SpanMapper spanMapper;
    private final TraceCache traceCache;

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        CompletableResultCode result = new CompletableResultCode();
        log.debug("Exporting {} span(s) to Redis", spans.size());
        Multi.createFrom().iterable(spans)
                .group().by(SpanData::getTraceId, spanMapper::spanDataToSpanView)
                .onItem().transformToUni(group -> {
                    String traceId = group.key();
                    Uni<List<SpanView>> spanViews = group.collect().asList();
                    return spanViews.chain(item -> traceCache.addSpans(traceId, item));
                })
                .merge(config.maxConcurrency())
                .collect().with(Collectors.summingInt(Integer::intValue))
                .subscribe().withSubscriber(new ExportSubscriber(result));

        return result;
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Builder
    public record Config(int maxConcurrency) {
        public static final String CONFIG_PREFIX = "otel.exporter.redis";

        public static Config fromProperties(ConfigProperties properties) {
            return Config.builder()
                    .maxConcurrency(properties.getInt(configKey("max-concurrency"), 4))
                    .build();
        }

        private static String configKey(String propertyName) {
            return CONFIG_PREFIX + '.' + propertyName;
        }
    }

    private static record ExportSubscriber(CompletableResultCode result) implements UniSubscriber<Integer> {

        @Override
        public void onSubscribe(UniSubscription uniSubscription) {
            // nothing to do here
        }

        @Override
        public void onItem(Integer exportedSpanCount) {
            log.debug("Succesfully exported {} span(s) to Redis", exportedSpanCount);
            result.succeed();
        }

        @Override
        public void onFailure(Throwable throwable) {
            log.error("Failed to export spans to Redis", throwable);
            result.fail();
        }
    }
}
