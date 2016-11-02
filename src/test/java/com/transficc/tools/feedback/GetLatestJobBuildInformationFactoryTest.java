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

import com.transficc.tools.feedback.dao.JobTestResultsDao;
import com.transficc.tools.feedback.messaging.MessageBus;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GetLatestJobBuildInformationFactoryTest
{
    private final JenkinsFacade jenkinsFacade = Mockito.mock(JenkinsFacade.class);
    private final MessageBus messageBus = Mockito.mock(MessageBus.class);
    private final JobTestResultsDao jobTestResultsDao = Mockito.mock(JobTestResultsDao.class);
    private final GetLatestJobBuildInformationFactory factory = new GetLatestJobBuildInformationFactory(jenkinsFacade, messageBus, new String[]{"SpecialJob"}, jobTestResultsDao);

    @Test
    public void shouldCreateARunnableThatShouldPersistsTestInformation()
    {
        final GetLatestJobBuildInformation buildInformation = factory.create(new Job("SpecialJob", "google.com", 1, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT), null);

        assertThat(buildInformation.isShouldPersistTestResults(), is(true));
    }

    @Test
    public void shouldCreateARunnableThatShouldNotPersistsTestInformation()
    {
        final GetLatestJobBuildInformation buildInformation = factory.create(new Job("AnotherJob", "google.com", 1, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT), null);

        assertThat(buildInformation.isShouldPersistTestResults(), is(false));
    }
}