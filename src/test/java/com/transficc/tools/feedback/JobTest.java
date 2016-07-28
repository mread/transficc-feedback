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
package com.transficc.tools.feedback;

import com.transficc.tools.feedback.messaging.MessageBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


import static com.transficc.tools.feedback.JenkinsFacade.*;

public class JobTest
{
    @Mock
    private MessageBus messageBus;
    private final Job job = new Job("tom", "url", 3, JobStatus.SUCCESS, false);

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        job.maybeUpdateAndPublish("revision1", JobStatus.SUCCESS, 1, 110, messageBus, new String[0], false, null);
        verify(messageBus).sendUpdate(any(Job.class));
    }

    @Test
    public void shouldNotPublishIfJobHasAlreadyCompleted()
    {
        job.maybeUpdateAndPublish("revision1", JobStatus.SUCCESS, 1, 1120, messageBus, new String[0], false, null);
        verifyNoMoreInteractions(messageBus);
    }

    @Test
    public void shouldPublishIfANewRunHasStarted()
    {
        job.maybeUpdateAndPublish("revision2", JobStatus.SUCCESS, 2, 0, messageBus, new String[0], false, null);
        verify(messageBus, times(2)).sendUpdate(job);
    }

    @Test
    public void shouldPublishIfAnUpdateForARunIsReceived()
    {
        job.maybeUpdateAndPublish("revision2", JobStatus.SUCCESS, 2, 10, messageBus, new String[0], false, null);
        job.maybeUpdateAndPublish("revision2", JobStatus.SUCCESS, 2, 20, messageBus, new String[0], false, null);
        verify(messageBus, times(3)).sendUpdate(job);
    }

    @Test
    public void shouldPublishIfJobCompletes()
    {
        job.maybeUpdateAndPublish("revision2", JobStatus.SUCCESS, 2, 10, messageBus, new String[0], false, null);
        job.maybeUpdateAndPublish("revision2", JobStatus.SUCCESS, 2, 110, messageBus, new String[0], false, null);
        verify(messageBus, times(3)).sendUpdate(job);
    }
}