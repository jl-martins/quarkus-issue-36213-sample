package org.example.trace;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Builder;

import java.util.Map;

@Builder
public record SpanView(String name, Kind kind, String traceId, String spanId,
                       String parentId, Status status, Map<String, Object> attributes) {
    public enum Kind {
        INTERNAL,
        SERVER,
        CLIENT,
        PRODUCER,
        CONSUMER,
        @JsonEnumDefaultValue
        UNKNOWN
    }

    public record Status(Code code, String description) {
        public enum Code {
            UNSET,
            OK,
            ERROR,
            @JsonEnumDefaultValue
            UNKNOWN
        }
    }
}
