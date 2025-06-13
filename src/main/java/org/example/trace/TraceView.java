package org.example.trace;

import java.util.Set;

public record TraceView(Set<SpanView> spans) {
}
