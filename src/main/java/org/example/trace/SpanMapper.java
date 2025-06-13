package org.example.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.Map;

import static org.mapstruct.MappingConstants.ComponentModel.CDI;

@Mapper(componentModel = CDI)
public abstract class SpanMapper {

    @Mapping(target = "parentId", source = "parentSpanContext")
    @Mapping(target = "status.code", source = "status.statusCode")
    public abstract SpanView spanDataToSpanView(SpanData spanData);

    protected final String parentSpanContextToParentId(SpanContext parentSpanContext) {
        return parentSpanContext.isValid() ? parentSpanContext.getSpanId() : null;
    }

    protected final Map<String, Object> attributesToAttributeMap(Attributes attributes) {
        Map<String, Object> attributeMap = new HashMap<>();
        attributes.forEach((attributeKey, value) -> attributeMap.put(attributeKey.getKey(), value));
        return attributeMap;
    }
}
