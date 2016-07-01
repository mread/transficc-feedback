package com.transficc.tools.feedback.routes;

import com.transficc.tools.feedback.BreakingNewsService;
import com.transficc.tools.feedback.util.SafeSerisalisation;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class UpdateStatusRoute implements Handler<RoutingContext>
{
    private final BreakingNewsService breakingNewsService;
    private final SafeSerisalisation safeSerisalisation;

    public UpdateStatusRoute(final BreakingNewsService breakingNewsService, final SafeSerisalisation safeSerisalisation)
    {
        this.breakingNewsService = breakingNewsService;
        this.safeSerisalisation = safeSerisalisation;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final StatusUpdate statusUpdate = safeSerisalisation.deserialise(routingContext.getBodyAsString(), StatusUpdate.class);
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
