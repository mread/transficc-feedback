package com.transficc.tools.feedback.routes;

import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.feedback.JobRepository;
import com.transficc.tools.feedback.BreakingNewsService;
import com.transficc.tools.jenkins.SafeSerialisation;


import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;

public final class Routes
{
    private Routes()
    {
    }

    public static void setup(final HttpServer server,
                             final JobRepository jobRepository,
                             final IterationRepository iterationRepository,
                             final BreakingNewsService breakingNewsService,
                             final WebSocketPublisher webSocketPublisher,
                             final Router router,
                             final SafeSerialisation safeSerialisation)
    {
        final TemplateEngine engine = HandlebarsTemplateEngine.create();
        router.route().handler(BodyHandler.create());
        router.route("/static/*").handler(StaticHandler.create().setWebRoot("static").setCachingEnabled(true));
        router.get("/").handler(new IndexViewRoute(engine, jobRepository, iterationRepository, breakingNewsService));
        router.put("/iteration").handler(new UpdateIterationRoute(iterationRepository, safeSerialisation)).consumes("*/json");
        router.put("/status").handler(new UpdateStatusRoute(breakingNewsService, safeSerialisation));
        server.websocketHandler(webSocketPublisher);
        server.requestHandler(router::accept);
    }
}
