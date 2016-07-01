package com.transficc.tools.feedback.messaging;

import java.util.concurrent.BlockingQueue;

import com.transficc.tools.feedback.routes.WebSocketPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageSubscriber implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSubscriber.class);
    private final BlockingQueue<PublishableJob> messageQueue;
    private final WebSocketPublisher webSocketPublisher;
    private volatile boolean isRunning;

    public MessageSubscriber(final BlockingQueue<PublishableJob> messageQueue, final WebSocketPublisher webSocketPublisher)
    {
        this.messageQueue = messageQueue;
        this.webSocketPublisher = webSocketPublisher;
        this.isRunning = false;
    }

    @Override
    public void run()
    {
        isRunning = true;
        LOGGER.info("Message bus subscriber starting");
        while (isRunning)
        {
            try
            {
                final PublishableJob job = messageQueue.take();
                webSocketPublisher.onJobUpdate(job);
            }
            catch (final InterruptedException e)
            {
                isRunning = false;
                LOGGER.info("Message bus subscriber stopping", e);
            }
        }
    }
}
