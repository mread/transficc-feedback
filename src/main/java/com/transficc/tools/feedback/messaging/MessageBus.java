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

    public void jobRemoved(final String jobName)
    {
        webSocketPublisher.onJobRemoved(jobName);
    }
}
