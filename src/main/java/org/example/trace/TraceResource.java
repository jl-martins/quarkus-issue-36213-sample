package org.example.trace;

import io.opentelemetry.api.trace.Span;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/traces")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TraceResource {
    private final TraceCache traceCache;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> createTrace() {
        final String traceId = Span.current().getSpanContext().getTraceId();
        return Uni.createFrom().item(Response.ok(traceId).build());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getTrace(@PathParam("id") String id) {
        return traceCache.getTrace(id).map(spans -> spans.isEmpty()
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.ok(new TraceView(spans)).build());
    }
}
