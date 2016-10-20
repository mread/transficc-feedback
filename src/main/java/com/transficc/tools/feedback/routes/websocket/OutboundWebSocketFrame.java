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

import com.transficc.tools.feedback.messaging.PublishableIteration;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.messaging.PublishableStatus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Serialised object")
public final class OutboundWebSocketFrame
{
    private final FrameType type;
    private final Object value;

    private OutboundWebSocketFrame(final FrameType type, final Object value)
    {
        this.type = type;
        this.value = value;
    }

    public FrameType getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }

    public static OutboundWebSocketFrame jobUpdate(final PublishableJob job)
    {
        return new OutboundWebSocketFrame(FrameType.JOB_UPDATE, job);
    }

    public static OutboundWebSocketFrame jobDeleted(final String jobName)
    {
        return new OutboundWebSocketFrame(FrameType.JOB_DELETED, jobName);
    }

    public static OutboundWebSocketFrame statusUpdate(final PublishableStatus status)
    {
        return new OutboundWebSocketFrame(FrameType.STATUS_UPDATE, status);
    }

    public static OutboundWebSocketFrame iterationUpdate(final PublishableIteration iteration)
    {
        return new OutboundWebSocketFrame(FrameType.ITERATION_UPDATE, iteration);
    }

    public static OutboundWebSocketFrame heartbeat(final HeartbeatMessage heartbeatMessage)
    {
        return new OutboundWebSocketFrame(FrameType.HEARTBEAT, heartbeatMessage);
    }
}
