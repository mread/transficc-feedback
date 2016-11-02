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
package com.transficc.tools.feedback.dao;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JobTestResultsDaoTest extends DaoTest
{
    private JobTestResultsDao dao;

    @Before
    public void setup()
    {
        dao = new JobTestResultsDao(getDataSource());
    }

    @Test
    public void shouldBeAbleToInsertTestResults()
    {
        final String revision = "Test124";
        final String jobName = "TomJob";
        final int totalTests = 100;
        final int passed = 98;
        final int failed = 2;
        final int duration = 20;
        final ZonedDateTime startTime = ZonedDateTime.now(Clock.systemUTC()).minusSeconds(duration);

        dao.addTestResults(jobName, revision, totalTests, passed, failed, startTime, duration);

        final JobTestResultsDao.TestEntry testResult = dao.getTestResult(revision);
        assertThat(testResult.getRevision(), is(revision));
        assertThat(testResult.getJobName(), is(jobName));
        assertThat(testResult.getTotalTests(), is(totalTests));
        assertThat(testResult.getPassedTests(), is(passed));
        assertThat(testResult.getFailedTests(), is(failed));
        assertThat(testResult.getStartTime(), is(startTime.toInstant().getEpochSecond()));
        assertThat(testResult.getDuration(), is(duration));
    }
}