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
package com.transficc.tools.feedback.routes;

import com.transficc.tools.feedback.BreakingNewsService;
import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.feedback.JobRepository;
import com.transficc.tools.feedback.util.SafeSerialisation;


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
