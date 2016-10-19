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

import java.util.List;

import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.feedback.BreakingNewsService;
import com.transficc.tools.feedback.messaging.PublishableJob;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;

public class IndexViewRoute implements Handler<RoutingContext>
{
    private static final String TEMPLATE = "templates/index.hbs";
    private final TemplateEngine engine;
    private final JobStatusSnapshot jobStatusSnapshot;
    private final IterationRepository iterationRepository;
    private final BreakingNewsService breakingNewsService;
    private final long startUpTime;

    public IndexViewRoute(final TemplateEngine engine, final JobStatusSnapshot jobStatusSnapshot,
                          final IterationRepository iterationRepository, final BreakingNewsService breakingNewsService, final long startUpTime)
    {
        this.engine = engine;
        this.jobStatusSnapshot = jobStatusSnapshot;
        this.iterationRepository = iterationRepository;
        this.breakingNewsService = breakingNewsService;
        this.startUpTime = startUpTime;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final List<PublishableJob> jobs = jobStatusSnapshot.getPublishableJobs();
        routingContext.put("jobs", jobs);
        routingContext.put("iteration", iterationRepository.iteration());
        routingContext.put("status", breakingNewsService.status());
        routingContext.put("startUpTime", startUpTime);
        engine.render(routingContext, TEMPLATE,
                      res ->
                      {
                          if (res.succeeded())
                          {
                              routingContext.response().end(res.result());
                          }
                          else
                          {
                              routingContext.fail(res.cause());
                          }
                      });
    }
}
