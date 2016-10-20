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
package com.transficc.tools.feedback.routes.websocket;

import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.routes.JobStatusSnapshot;
import com.transficc.tools.feedback.util.ClockService;
import com.transficc.tools.feedback.util.SafeSerialisation;


import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.WebSocketFrame;

public class WebSocketFrameHandler implements Handler<WebSocketFrame>
{
    private final String sessionId;
    private final EventBus eventBus;
    private final SafeSerialisation safeSerialisation;
    private final ClockService clockService;
    private final JobStatusSnapshot jobStatusSnapshot;
    private final long startUpTime;

    public WebSocketFrameHandler(final String sessionId,
                                 final EventBus eventBus,
                                 final SafeSerialisation safeSerialisation,
                                 final ClockService clockService,
                                 final JobStatusSnapshot jobStatusSnapshot,
                                 final long startUpTime)
    {
        this.sessionId = sessionId;
        this.eventBus = eventBus;
        this.safeSerialisation = safeSerialisation;
        this.clockService = clockService;
        this.jobStatusSnapshot = jobStatusSnapshot;
        this.startUpTime = startUpTime;
    }

    @Override
    public void handle(final WebSocketFrame frame)
    {
        final String payload = frame.textData();
        if ("--heartbeat--".equals(payload))
        {
            eventBus.send(sessionId, safeSerialisation.serisalise(OutboundWebSocketFrame.heartbeat(new HeartbeatMessage(clockService.currentTimeMillis(), startUpTime))));
        }
        else if ("snapshot".equals(payload))
        {
            for (final PublishableJob job : jobStatusSnapshot.getPublishableJobs())
            {
                eventBus.send(sessionId, safeSerialisation.serisalise(OutboundWebSocketFrame.jobUpdate(job)));
            }
        }
    }
}
