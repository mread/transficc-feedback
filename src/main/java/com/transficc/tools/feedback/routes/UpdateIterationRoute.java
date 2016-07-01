package com.transficc.tools.feedback.routes;

import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.jenkins.SafeSerisalisation;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class UpdateIterationRoute implements Handler<RoutingContext>
{
    private final IterationRepository iterationRepository;
    private final SafeSerisalisation safeSerisalisation;

    public UpdateIterationRoute(final IterationRepository iterationRepository, final SafeSerisalisation safeSerisalisation)
    {
        this.iterationRepository = iterationRepository;
        this.safeSerisalisation = safeSerisalisation;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final IterationUpdate iteration = safeSerisalisation.deserialise(routingContext.getBodyAsString(), IterationUpdate.class);
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
