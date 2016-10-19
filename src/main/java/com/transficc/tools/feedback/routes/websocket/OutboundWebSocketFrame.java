package com.transficc.tools.feedback.routes.websocket;

import com.transficc.tools.feedback.messaging.PublishableIteration;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.messaging.PublishableStatus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Serialised object")
public final class OutboundWebSocketFrame
{
    private final String type;
    private final Object value;

    private OutboundWebSocketFrame(final String type, final Object value)
    {
        this.type = type;
        this.value = value;
    }

    public static OutboundWebSocketFrame jobUpdate(final PublishableJob job)
    {
        return new OutboundWebSocketFrame("jobUpdate", job);
    }

    public static OutboundWebSocketFrame statusUpdate(final PublishableStatus status)
    {
        return new OutboundWebSocketFrame("statusUpdate", status);
    }

    public static OutboundWebSocketFrame iterationUpdate(final PublishableIteration iteration)
    {
        return new OutboundWebSocketFrame("iterationUpdate", iteration);
    }

    public static OutboundWebSocketFrame heartbeat(final HeartbeatMessage heartbeatMessage)
    {
        return new OutboundWebSocketFrame("heartBeat", heartbeatMessage);
    }
}
