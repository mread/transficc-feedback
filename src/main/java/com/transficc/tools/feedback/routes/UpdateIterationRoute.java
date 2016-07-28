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

import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.feedback.util.SafeSerialisation;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class UpdateIterationRoute implements Handler<RoutingContext>
{
    private final IterationRepository iterationRepository;
    private final SafeSerialisation safeSerialisation;

    public UpdateIterationRoute(final IterationRepository iterationRepository, final SafeSerialisation safeSerialisation)
    {
        this.iterationRepository = iterationRepository;
        this.safeSerialisation = safeSerialisation;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final IterationUpdate iteration = safeSerialisation.deserialise(routingContext.getBodyAsString(), IterationUpdate.class);
        iterationRepository.iteration(iteration.iteration);
        routingContext.
                response().
                setStatusCode(201).
                end();
    }

    private static class IterationUpdate
    {
        private String iteration;
    }
}
