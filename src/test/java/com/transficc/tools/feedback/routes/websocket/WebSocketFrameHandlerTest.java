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
package com.transficc.tools.feedback.routes.websocket;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transficc.tools.feedback.JenkinsFacade;
import com.transficc.tools.feedback.Job;
import com.transficc.tools.feedback.JobRepository;
import com.transficc.tools.feedback.VersionControl;
import com.transficc.tools.feedback.util.SafeSerialisation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;

public class WebSocketFrameHandlerTest
{
    private static final String SESSION_ID = "Session543543";
    private static final int CLOCK_TIME = 23432;
    private static final int START_UP_TIME = 34543;
    private final EventBus eventBus = Mockito.mock(EventBus.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SafeSerialisation safeSerialisation = new SafeSerialisation(objectMapper);
    private final JobRepository jobRepository = new JobRepository();
    private final WebSocketFrameHandler handler = new WebSocketFrameHandler(SESSION_ID, eventBus, safeSerialisation, () -> CLOCK_TIME, jobRepository, START_UP_TIME);

    @Before
    public void setUp() throws Exception
    {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Test
    public void shouldSendAHeartBeatMessageWhenReceivingAHeartBeat()
    {
        //given
        final WebSocketFrameImpl frame = new WebSocketFrameImpl("--heartbeat--");

        //when
        handler.handle(frame);

        //then
        verify(eventBus).send(SESSION_ID, safeSerialisation.serisalise(OutboundWebSocketFrame.heartbeat(new HeartbeatMessage(CLOCK_TIME, START_UP_TIME))));
    }

    @Test
    public void shouldSendASnapshotForAllJobsWhenReceivingASnapshotRequest()
    {
        //given
        final Job job1 = new Job("blah", "http://www.google.com", 1, JenkinsFacade.JobStatus.DISABLED, false, VersionControl.GIT);
        final Job job2 = new Job("Judd", "http://www.google.com", 1, JenkinsFacade.JobStatus.SUCCESS, true, VersionControl.GIT);
        jobRepository.put("blah", job1);
        jobRepository.put("Judd", job2);
        final WebSocketFrameImpl frame = new WebSocketFrameImpl("snapshot");

        //when
        handler.handle(frame);

        //then
        final InOrder inOrder = Mockito.inOrder(eventBus);
        inOrder.verify(eventBus).send(SESSION_ID, safeSerialisation.serisalise(OutboundWebSocketFrame.jobUpdate(job2.createPublishable())));
        inOrder.verify(eventBus).send(SESSION_ID, safeSerialisation.serisalise(OutboundWebSocketFrame.jobUpdate(job1.createPublishable())));
    }

    @Test
    public void shouldDoNothingIfUnknownWebSocketFrame()
    {
        final WebSocketFrameImpl frame = new WebSocketFrameImpl("unknown");

        //when
        handler.handle(frame);

        verifyZeroInteractions(eventBus);
    }
}