package com.transficc.tools.feedback.routes.websocket;

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

    public WebSocketFrameHandler(final String sessionId, final EventBus eventBus, final SafeSerialisation safeSerialisation, final ClockService clockService)
    {
        this.sessionId = sessionId;
        this.eventBus = eventBus;
        this.safeSerialisation = safeSerialisation;
        this.clockService = clockService;
    }

    @Override
    public void handle(final WebSocketFrame frame)
    {
        eventBus.send(sessionId, safeSerialisation.serisalise(new OutboundWebSocketFrame("heartBeat", clockService.currentTimeMillis())));
    }
}
