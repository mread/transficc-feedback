/*
 * Copyright 2016 TransFICC Ltd.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */
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
