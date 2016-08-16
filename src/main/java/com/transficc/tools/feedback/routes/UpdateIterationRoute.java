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

import com.transficc.portals.DecodingHandler;
import com.transficc.portals.PortalRequest;
import com.transficc.portals.ResponseHelper;
import com.transficc.tools.feedback.IterationRepository;


import io.vertx.ext.web.RoutingContext;

public class UpdateIterationRoute implements DecodingHandler<UpdateIterationRoute.IterationUpdate>
{
    private final IterationRepository iterationRepository;

    public UpdateIterationRoute(final IterationRepository iterationRepository)
    {
        this.iterationRepository = iterationRepository;
    }

    @Override
    public void handle(final RoutingContext event, final IterationUpdate value)
    {
        iterationRepository.iteration(value.iteration);
        ResponseHelper.ok(event.response());
    }

    public static final class IterationUpdate implements PortalRequest
    {
        private String iteration;
    }
}
