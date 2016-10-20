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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.offbytwo.jenkins.JenkinsServer;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.routes.websocket.OutboundWebSocketFrame;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;


import static com.transficc.tools.feedback.JenkinsFacade.JobStatus;

public class JobFinderTest
{
    @Mock
    private JenkinsServer jenkins;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    @Mock
    private ScheduledFuture scheduledFuture;
    private JobFinder jobFinder;
    private JobRepository jobRepository;

    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        BDDMockito.given(scheduledExecutorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).willReturn(scheduledFuture);
        jobRepository = new JobRepository();
        final LinkedBlockingQueue<OutboundWebSocketFrame> messageBusQueue = new LinkedBlockingQueue<>();
        final MessageBus messageBus = new MessageBus(messageBusQueue);
        jobFinder = new JobFinder(new JobService(jobRepository, messageBus, null, scheduledExecutorService),
                                  new JenkinsFacade(jenkins, new JobPrioritiesRepository(Collections.emptyMap()), "", () -> 10, VersionControl.GIT));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldOnlyAddEachJobOnce() throws IOException
    {
        final Map<String, com.offbytwo.jenkins.model.Job> result1 = new HashMap<>();
        result1.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Tom").setField("url", "stuff.com").build());
        result1.put("Chinar", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Chinar").setField("url", "stuff.com").build());
        final Map<String, com.offbytwo.jenkins.model.Job> result2 = new HashMap<>();
        result2.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Tom").setField("url", "stuff.com").build());
        BDDMockito.given(jenkins.getJobs()).willReturn(result1, result2);

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        MatcherAssert.assertThat(publishableJobs.size(), Is.is(2));
        MatcherAssert.assertThat(publishableJobs.get(0).getName(), Is.is("Chinar"));
        MatcherAssert.assertThat(publishableJobs.get(0).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.DISABLED));
        MatcherAssert.assertThat(publishableJobs.get(1).getName(), Is.is("Tom"));
        MatcherAssert.assertThat(publishableJobs.get(1).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(1).getJobStatus(), Is.is(JobStatus.DISABLED));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddJobAsTheyAreCreated() throws IOException
    {
        final Map<String, com.offbytwo.jenkins.model.Job> result1 = new HashMap<>();
        result1.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Chinar").setField("url", "stuff.com").build());
        final Map<String, com.offbytwo.jenkins.model.Job> result2 = new HashMap<>();
        result2.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Tom").setField("url", "stuff.com").build());
        BDDMockito.given(jenkins.getJobs()).willReturn(result1, result2);

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        MatcherAssert.assertThat(publishableJobs.size(), Is.is(2));
        MatcherAssert.assertThat(publishableJobs.get(0).getName(), Is.is("Chinar"));
        MatcherAssert.assertThat(publishableJobs.get(0).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.DISABLED));
        MatcherAssert.assertThat(publishableJobs.get(1).getName(), Is.is("Tom"));
        MatcherAssert.assertThat(publishableJobs.get(1).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(1).getJobStatus(), Is.is(JobStatus.DISABLED));
    }
}