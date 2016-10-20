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
