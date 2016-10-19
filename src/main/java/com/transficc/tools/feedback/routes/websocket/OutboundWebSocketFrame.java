package com.transficc.tools.feedback.routes.websocket;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Serialised object")
public final class OutboundWebSocketFrame
{
    private final String type;
    private final Object value;

    public OutboundWebSocketFrame(final String type, final Object value)
    {
        this.type = type;
        this.value = value;
    }
}
