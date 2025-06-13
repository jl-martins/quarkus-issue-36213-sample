package org.example.trace;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ExpireArgs;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.set.ReactiveSetCommands;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Slf4j
public class TraceCache {
    private static final String CACHE_KEY_PREFIX = "trace";
    private static final char CACHE_KEY_SEPARATOR = '#';
    private static final ExpireArgs EXPIRE_NX = new ExpireArgs().nx();

    private final TraceCache.Config config;
    private final ReactiveSetCommands<String, SpanView> setCommands;
    private final ReactiveKeyCommands<String> keyCommands;

    @Inject
    public TraceCache(TraceCache.Config config, ReactiveRedisDataSource dataSource) {
        this.config = config;
        this.setCommands = dataSource.set(SpanView.class);
        this.keyCommands = dataSource.key();
    }

    private static String getCacheKey(String traceId) {
        return CACHE_KEY_PREFIX + CACHE_KEY_SEPARATOR + traceId;
    }

    public Uni<Integer> addSpans(String traceId, Collection<SpanView> spans) {
        String key = getCacheKey(traceId);
        return setCommands.sadd(key, spans.toArray(SpanView[]::new))
                .onItem().call(addedSpanCount -> {
                    log.debug("Added {} span(s) to {}", addedSpanCount, key);
                    return setExpirationTimeIfAdded(key, addedSpanCount);
                });
    }

    public Uni<Set<SpanView>> getTrace(String traceId) {
        String key = getCacheKey(traceId);
        return setCommands.smembers(key);
    }

    private Uni<Boolean> setExpirationTimeIfAdded(String key, int addedSpanCount) {
        return addedSpanCount > 0 ? setExpirationTime(key) : Uni.createFrom().item(false);
    }

    private Uni<Boolean> setExpirationTime(String key) {
        log.debug("Setting expiration time of key {} if not already set", key);
        return keyCommands.expire(key, config.expireAfterWrite(), EXPIRE_NX)
                .onItem().invoke(didSet -> {
                    if (didSet) {
                        log.debug("Expiration time of {} was set to {}", key, config.expireAfterWrite());
                    } else {
                        log.debug("Didn't set expiration time of {} because key doesn't exist or already has a timeout", key);
                    }
                });
    }

    @ConfigMapping(prefix = "cache.\"" + CACHE_KEY_PREFIX + '"')
    public interface Config {
        @WithDefault("10m")
        Duration expireAfterWrite();
    }
}
