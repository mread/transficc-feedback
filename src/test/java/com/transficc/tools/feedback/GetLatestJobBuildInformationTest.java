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
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildChangeSet;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.transficc.tools.feedback.dao.JobTestResultsDao;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.routes.websocket.FrameType;
import com.transficc.tools.feedback.routes.websocket.OutboundWebSocketFrame;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class GetLatestJobBuildInformationTest
{

    @Mock
    private JenkinsServer jenkins;
    @Mock
    private JobWithDetails jobWithDetails;
    @Mock
    private Build lastBuild;
    @Mock
    private JobService jobService;
    @Mock
    private JobTestResultsDao jobTestResultsDao;

    private GetLatestJobBuildInformation jobChecker;
    private BlockingQueue<OutboundWebSocketFrame> messageBusQueue;
    private String jobName;
    private MessageBus messageBus;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        messageBusQueue = new LinkedBlockingQueue<>();
        jobName = "Tom is the best";
        given(jobWithDetails.isBuildable()).willReturn(true);
        messageBus = new MessageBus(messageBusQueue);
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), false, jobTestResultsDao);
    }

    @Test
    public void shouldPushJobUpdateToTheMessageBus() throws Exception
    {
        //Given
        final String jobUrl = "tom-url";
        final String revision = "5435dsd";
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), 5L, 10, 0);

        //when
        jobChecker.run();

        //then
        final OutboundWebSocketFrame actualJob = messageBusQueue.take();
        assertThat(actualJob.getType(), is(FrameType.JOB_UPDATE));
        assertThat(actualJob.getValue(), is(new PublishableJob(jobName, jobUrl, 0, revision, JenkinsFacade.JobStatus.SUCCESS, 0, 5L, 50.0, new String[0], false,
                                                               new JenkinsFacade.TestResults(1, 1, 2))));
        verifyZeroInteractions(jobService);
    }

    @Test
    public void shouldNotPublishAnUpdateIfNothingHasChanged() throws Exception
    {
        //given
        final String revision = "";
        setupJobExpectations(revision, false, Optional.empty(), 10, 1, 0);

        //when
        jobChecker.run();

        //then
        assertThat(messageBusQueue.size(), is(0));
        verifyZeroInteractions(jobService);
    }

    @Test
    public void shouldRemoveJobIfNotFoundOnJenkinsServer() throws IOException
    {
        given(jenkins.getJob(jobName)).willReturn(null);

        jobChecker.run();

        verify(jobService).onJobNotFound(jobName);
    }

    @Test
    public void shouldDoNothingIfJobDoesNotHaveABuild() throws IOException
    {
        given(jenkins.getJob(jobName)).willReturn(jobWithDetails);
        given(jobWithDetails.getLastBuild()).willReturn(Build.BUILD_HAS_NEVER_RAN);

        jobChecker.run();

        assertThat(messageBusQueue.size(), is(0));
        verifyZeroInteractions(jobService);
    }

    @Test
    public void shouldPersistTestResultsIfJobJustCompletedAndHasPersistenceEnabled() throws IOException, InterruptedException
    {
        //Given
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), true, jobTestResultsDao);
        final String revision = "5435dsd";
        final ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());
        final long epochMilli = timestamp.toInstant().toEpochMilli();
        setupJobExpectations(revision, true, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, 0);

        //when
        jobChecker.run();

        final int duration = 30;
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, duration);
        jobChecker.run();

        //then
        verify(jobTestResultsDao).addTestResults(jobName, revision, 4, 1, 1, timestamp, duration);
    }

    @Test
    public void shouldNotPersistTestResultsIfJobJustCompletedAndDoesNotPersistenceEnabled() throws IOException
    {
        //Given
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), false, jobTestResultsDao);
        final String revision = "5435dsd";
        final ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());
        final long epochMilli = timestamp.toInstant().toEpochMilli();
        setupJobExpectations(revision, true, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, 0);

        //when
        jobChecker.run();

        final int duration = 30;
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, duration);
        jobChecker.run();

        //then
        verifyZeroInteractions(jobTestResultsDao);
    }

    @Test
    public void shouldNotPersistTestResultsIfJobHasNotJustCompletedAndHasPersistenceEnabled() throws IOException
    {
        //Given
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), true, jobTestResultsDao);
        final String revision = "5435dsd";
        final ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());
        final long epochMilli = timestamp.toInstant().toEpochMilli();
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, 0);

        //when
        jobChecker.run();

        final int duration = 30;
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, duration);
        jobChecker.run();

        //then
        verifyZeroInteractions(jobTestResultsDao);
    }

    @Test
    public void shouldNotPersistTestResultsIfJobHasNotJustCompletedAndDoesNotHavePersistenceEnabled() throws IOException
    {
        //Given
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), false, jobTestResultsDao);
        final String revision = "5435dsd";
        final ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());
        final long epochMilli = timestamp.toInstant().toEpochMilli();
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, 0);

        //when
        jobChecker.run();

        final int duration = 30;
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, duration);
        jobChecker.run();

        //then
        verifyZeroInteractions(jobTestResultsDao);
    }

    @Test
    public void shouldNotPersistTestResultsIfJobHasJustStartedAndHasPersistenceEnabled() throws IOException
    {
        //Given
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), true, jobTestResultsDao);
        final String revision = "5435dsd";
        final ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());
        final long epochMilli = timestamp.toInstant().toEpochMilli();
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, 0);

        //when
        jobChecker.run();

        final int duration = 30;
        setupJobExpectations(revision, true, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, duration);
        jobChecker.run();

        //then
        verifyZeroInteractions(jobTestResultsDao);
    }

    @Test
    public void shouldNotPersistTestResultsIfJobHasJustStartedAndDoesNotHavePersistenceEnabled() throws IOException
    {
        //Given
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jobService, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT),
                                                           new JenkinsFacade(jenkins, null, null, () -> 10, VersionControl.GIT), false, jobTestResultsDao);
        final String revision = "5435dsd";
        final ZonedDateTime timestamp = ZonedDateTime.now(Clock.systemUTC());
        final long epochMilli = timestamp.toInstant().toEpochMilli();
        setupJobExpectations(revision, false, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, 0);

        //when
        jobChecker.run();

        final int duration = 30;
        setupJobExpectations(revision, true, Optional.of(new Tests(1, 2, 4)), epochMilli, 10, duration);
        jobChecker.run();

        //then
        verifyZeroInteractions(jobTestResultsDao);
    }

    private void setupJobExpectations(final String revision, final boolean isBuilding, final Optional<Tests> testReport, final long timestamp, final int estimatedDuration,
                                      final int duration) throws IOException
    {
        final Map<Object, Object> revisionActions = new HashMap<>();
        final Map<Object, Object> revisions = new HashMap<>();
        revisions.put("SHA1", revision);
        revisionActions.put("lastBuiltRevision", revisions);
        final Map<Object, Object> testResults = new HashMap<>();
        testReport.ifPresent(test ->
                             {
                                 testResults.put("failCount", test.failCount);
                                 testResults.put("skipCount", test.skipCount);
                                 testResults.put("totalCount", test.totalCount);
                                 testResults.put("urlName", "testReport");
                             });
        final List<Map<Object, Object>> actions = Arrays.asList(revisionActions, testResults);
        final BuildChangeSet buildChangeSet = new BuildChangeSet();
        buildChangeSet.setItems(Collections.emptyList());

        given(jenkins.getJob(jobName)).willReturn(jobWithDetails);
        given(jobWithDetails.getLastBuild()).willReturn(lastBuild);
        given(lastBuild.details()).willReturn(new MessageBuilder<>(BuildWithDetails.class)
                                                      .setField("actions", actions)
                                                      .setField("building", isBuilding)
                                                      .setField("changeSet", buildChangeSet)
                                                      .setField("result", BuildResult.SUCCESS)
                                                      .setField("timestamp", timestamp)
                                                      .setField("duration", duration)
                                                      .setField("estimatedDuration", estimatedDuration)
                                                      .build());
    }

    private static final class Tests
    {
        private final int failCount;
        private final int skipCount;
        private final int totalCount;

        private Tests(final int failCount, final int skipCount, final int totalCount)
        {
            this.failCount = failCount;
            this.skipCount = skipCount;
            this.totalCount = totalCount;
        }
    }
}
