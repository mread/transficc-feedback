package com.transficc.tools.feedback.routes;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.transficc.tools.feedback.messaging.PublishableIteration;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.messaging.PublishableStatus;
import com.transficc.tools.jenkins.SafeSerisalisation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;


public final class WebSocketPublisher implements Handler<ServerWebSocket>
{
    private final Deque<String> sessions = new ConcurrentLinkedDeque<>();
    private final EventBus eventBus;
    private final SafeSerisalisation safeSerisalisation;

    public WebSocketPublisher(final EventBus eventBus, final SafeSerisalisation safeSerisalisation)
    {
        this.eventBus = eventBus;
        this.safeSerisalisation = safeSerisalisation;
    }

    public void onJobUpdate(final PublishableJob job)
    {
        sendMessage("jobUpdate", job);
    }

    public void onStatusUpdate(final PublishableStatus statusUpdate)
    {
        sendMessage("statusUpdate", statusUpdate);
    }

    public void onIterationUpdate(final PublishableIteration iterationUpdate)
    {
        sendMessage("iterationUpdate", iterationUpdate);
    }

    private void sendMessage(final String type, final Object content)
    {
        final Iterator<String> iterator = sessions.iterator();
        final String outbound = safeSerisalisation.serisalise(new Outbound(type, content));
        while (iterator.hasNext())
        {
            eventBus.send(iterator.next(), outbound);
        }
    }

    @Override
    public void handle(final ServerWebSocket socket)
    {
        final String id = socket.textHandlerID();
        sessions.addLast(id);
        socket.closeHandler(event -> sessions.remove(id));
        socket.frameHandler(frame -> eventBus.send(id, safeSerisalisation.serisalise(new Outbound("heartBeat", System.currentTimeMillis()))));
    }

    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Serialised object")
    private static final class Outbound
    {
        private String type;
        private Object value;

        private Outbound(final String type, final Object value)
        {
            this.type = type;
            this.value = value;
        }
    }
}
