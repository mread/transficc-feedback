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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.transficc.tools.feedback.dao.JobTestResultsDao;
import com.transficc.tools.feedback.messaging.MessageBus;

public class GetLatestJobBuildInformationFactory
{
    private final JenkinsFacade jenkinsFacade;
    private final MessageBus messageBus;
    private final JobTestResultsDao jobTestResultsDao;
    private final Set<String> jobNamesForTestResultsToPersist;

    public GetLatestJobBuildInformationFactory(final JenkinsFacade jenkinsFacade,
                                               final MessageBus messageBus,
                                               final String[] jobNamesForTestResultsToPersist,
                                               final JobTestResultsDao jobTestResultsDao)
    {
        this.jenkinsFacade = jenkinsFacade;
        this.messageBus = messageBus;
        this.jobNamesForTestResultsToPersist = new HashSet<>(jobNamesForTestResultsToPersist.length);
        this.jobTestResultsDao = jobTestResultsDao;
        Collections.addAll(this.jobNamesForTestResultsToPersist, jobNamesForTestResultsToPersist);
    }


    public GetLatestJobBuildInformation create(final Job job, final JobService jobService)
    {
        return new GetLatestJobBuildInformation(messageBus, jobService, job, jenkinsFacade, jobNamesForTestResultsToPersist.contains(job.getName()), jobTestResultsDao);
    }

}
