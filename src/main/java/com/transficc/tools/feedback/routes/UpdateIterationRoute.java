package com.transficc.tools.feedback.routes;

import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.feedback.util.SafeSerialisation;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class UpdateIterationRoute implements Handler<RoutingContext>
{
    private final IterationRepository iterationRepository;
    private final SafeSerialisation safeSerialisation;

    public UpdateIterationRoute(final IterationRepository iterationRepository, final SafeSerialisation safeSerialisation)
    {
        this.iterationRepository = iterationRepository;
        this.safeSerialisation = safeSerialisation;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final IterationUpdate iteration = safeSerialisation.deserialise(routingContext.getBodyAsString(), IterationUpdate.class);
        iterationRepository.iteration(iteration.iteration);
        routingContext.
                response().
                setStatusCode(201).
                end();
    }

    private static class IterationUpdate
    {
        private String iteration;
    }
}
