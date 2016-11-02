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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.transficc.tools.feedback.messaging.MessageBus;

public class JobService
{
    private final JobRepository jobRepository;
    private final MessageBus messageBus;
    private final Map<String, ScheduledFuture<?>> jobNameToScheduledRunnable;
    private final ScheduledExecutorService scheduledExecutorService;
    private final GetLatestJobBuildInformationFactory getLatestJobBuildInformationFactory;

    public JobService(final JobRepository jobRepository, final MessageBus messageBus, final ScheduledExecutorService scheduledExecutorService,
                      final GetLatestJobBuildInformationFactory getLatestJobBuildInformationFactory)
    {
        this.jobRepository = jobRepository;
        this.messageBus = messageBus;
        this.scheduledExecutorService = scheduledExecutorService;
        this.getLatestJobBuildInformationFactory = getLatestJobBuildInformationFactory;
        jobNameToScheduledRunnable = new ConcurrentHashMap<>();
    }

    public void onJobNotFound(final String jobName)
    {
        final ScheduledFuture<?> future = jobNameToScheduledRunnable.remove(jobName);
        if (future != null)
        {
            future.cancel(true);
        }
        jobRepository.remove(jobName);
        messageBus.jobRemoved(jobName);
    }

    public void add(final Job job)
    {
        final GetLatestJobBuildInformation statusChecker = getLatestJobBuildInformationFactory.create(job, this);
        final String jobName = job.getName();
        jobRepository.put(jobName, job);
        final ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(statusChecker, 0, 5, TimeUnit.SECONDS);
        jobNameToScheduledRunnable.put(jobName, scheduledFuture);
    }

    public boolean jobExists(final String jobName)
    {
        return jobRepository.contains(jobName);
    }
}
