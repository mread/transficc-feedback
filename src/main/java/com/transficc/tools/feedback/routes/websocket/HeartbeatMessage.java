package com.transficc.tools.feedback.routes.websocket;

public class HeartbeatMessage
{
    private final long currentServerTime;
    private final long serverStartUpTime;

    public HeartbeatMessage(final long currentServerTime, final long serverStartUpTime)
    {
        this.currentServerTime = currentServerTime;
        this.serverStartUpTime = serverStartUpTime;
    }
}
