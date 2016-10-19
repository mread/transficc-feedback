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

    public WebSocketFrameHandler(final String sessionId, final EventBus eventBus, final SafeSerialisation safeSerialisation, final ClockService clockService, final JobStatusSnapshot jobStatusSnapshot)
    {
        this.sessionId = sessionId;
        this.eventBus = eventBus;
        this.safeSerialisation = safeSerialisation;
        this.clockService = clockService;
        this.jobStatusSnapshot = jobStatusSnapshot;
    }

    @Override
    public void handle(final WebSocketFrame frame)
    {
        final String payload = frame.textData();
        if ("--heartbeat--".equals(payload))
        {
            eventBus.send(sessionId, safeSerialisation.serisalise(OutboundWebSocketFrame.heartbeat(clockService.currentTimeMillis())));
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
