package com.transficc.tools.feedback;

import com.transficc.tools.feedback.messaging.MessageBus;

public class BreakingNewsService
{
    private final MessageBus messageBus;
    private volatile String status = null;

    public BreakingNewsService(final MessageBus messageBus)
    {
        this.messageBus = messageBus;
    }

    public String status()
    {
        return status;
    }

    public void status(final String status)
    {
        this.status = status;
        messageBus.statusUpdate(status);
    }
}
