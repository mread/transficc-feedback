package com.transficc.tools.feedback;

import com.transficc.tools.feedback.messaging.MessageBus;

public class IterationRepository
{
    private final MessageBus messageBus;
    //TODO: get the iteration value persisted (redis?)
    private volatile String iteration = "it1";

    public IterationRepository(final MessageBus messageBus)
    {
        this.messageBus = messageBus;
    }

    public String iteration()
    {
        return iteration;
    }

    public void iteration(final String iteration)
    {
        this.iteration = iteration;
        messageBus.iterationUpdate(iteration);
    }
}
