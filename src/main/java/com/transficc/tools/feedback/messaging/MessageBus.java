package com.transficc.tools.feedback.messaging;

import java.util.concurrent.BlockingQueue;

import com.transficc.tools.feedback.Job;
import com.transficc.tools.feedback.routes.WebSocketPublisher;

public class MessageBus
{
    private final BlockingQueue<PublishableJob> jobUpdates;
    private final WebSocketPublisher webSocketPublisher;

    public MessageBus(final BlockingQueue<PublishableJob> jobUpdates, final WebSocketPublisher webSocketPublisher)
    {
        this.jobUpdates = jobUpdates;
        this.webSocketPublisher = webSocketPublisher;
    }

    public void sendUpdate(final Job job)
    {
        if (!jobUpdates.offer(job.createPublishable()))
        {
            throw new IllegalStateException("Failed to add job to the queue");
        }
    }

    public void iterationUpdate(final String iteration)
    {
        webSocketPublisher.onIterationUpdate(new PublishableIteration(iteration));
    }

    public void statusUpdate(final String status)
    {
        webSocketPublisher.onStatusUpdate(new PublishableStatus(status));
    }
}
