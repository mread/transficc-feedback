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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.transficc.functionality.Result;
import com.transficc.tools.feedback.dao.JobTestResultsDao;
import com.transficc.tools.feedback.messaging.MessageBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLatestJobBuildInformation implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GetLatestJobBuildInformation.class);
    private final Job job;
    private final boolean shouldPersistTestResults;
    private final MessageBus messageBus;
    private final JenkinsFacade jenkinsFacade;
    private final JobService jobService;
    private final JobTestResultsDao jobTestResultsDao;

    public GetLatestJobBuildInformation(final MessageBus messageBus,
                                        final JobService jobService,
                                        final Job job,
                                        final JenkinsFacade jenkinsFacade,
                                        final boolean shouldPersistTestResults,
                                        final JobTestResultsDao jobTestResultsDao)
    {
        this.messageBus = messageBus;
        this.jenkinsFacade = jenkinsFacade;
        this.jobService = jobService;
        this.job = job;
        this.shouldPersistTestResults = shouldPersistTestResults;
        this.jobTestResultsDao = jobTestResultsDao;
    }

    @Override
    public void run()
    {
        try
        {
            final Result<Integer, JenkinsFacade.LatestBuildInformation> latestBuildInformation = jenkinsFacade.getLatestBuildInformation(job.getName(), job.getJobStatus());
            latestBuildInformation.consume(statusCode ->
                                           {
                                               if (statusCode == 404)
                                               {
                                                   jobService.onJobNotFound(job.getName());
                                               }
                                               else
                                               {
                                                   LOGGER.error("Received status code {} whilst trying to get build information for job: {}", statusCode, job.getName());
                                               }
                                           },
                                           buildInformation ->
                                           {
                                               job.maybeUpdateAndPublish(buildInformation.getRevision(),
                                                                         buildInformation.getJobStatus(),
                                                                         buildInformation.getNumber(),
                                                                         buildInformation.getTimestamp(),
                                                                         buildInformation.getJobCompletionPercentage(),
                                                                         messageBus,
                                                                         buildInformation.getComments(),
                                                                         buildInformation.isBuilding(),
                                                                         buildInformation.getTestResults());

                                               if (job.hasJustCompleted() && shouldPersistTestResults)
                                               {
                                                   final JenkinsFacade.TestResults testResults = buildInformation.getTestResults();
                                                   final int total = testResults.getFailCount() + testResults.getPassCount() + testResults.getSkipCount();
                                                   final ZonedDateTime startTime = ZonedDateTime.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(buildInformation.getTimestamp()),
                                                                                                                            ZoneOffset.UTC), ZoneOffset.UTC);
                                                   jobTestResultsDao.addTestResults(job.getName(), buildInformation.getRevision(), total, testResults.getPassCount(), testResults.getFailCount(),
                                                                                    startTime, buildInformation.getDuration());
                                               }
                                           });
        }
        catch (final RuntimeException e)
        {
            LOGGER.error("An exception occurred whilst trying to gather build information", e);
        }
    }

    boolean isShouldPersistTestResults()
    {
        return shouldPersistTestResults;
    }
}
