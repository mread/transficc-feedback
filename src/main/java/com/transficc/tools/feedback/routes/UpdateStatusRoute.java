package com.transficc.tools.feedback.routes;

import com.transficc.tools.feedback.BreakingNewsService;
import com.transficc.tools.feedback.util.SafeSerialisation;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class UpdateStatusRoute implements Handler<RoutingContext>
{
    private final BreakingNewsService breakingNewsService;
    private final SafeSerialisation safeSerialisation;

    public UpdateStatusRoute(final BreakingNewsService breakingNewsService, final SafeSerialisation safeSerialisation)
    {
        this.breakingNewsService = breakingNewsService;
        this.safeSerialisation = safeSerialisation;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final StatusUpdate statusUpdate = safeSerialisation.deserialise(routingContext.getBodyAsString(), StatusUpdate.class);
        breakingNewsService.status(statusUpdate.message);
        routingContext.
                response().
                setStatusCode(201)
                .end();
    }

    private static class StatusUpdate
    {
        private String message;
    }
}
