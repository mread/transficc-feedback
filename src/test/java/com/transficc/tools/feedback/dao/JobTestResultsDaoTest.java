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
        final int totalTests = 100;
        final int passed = 98;
        final int failed = 2;
        final int duration = 20;
        final ZonedDateTime startTime = ZonedDateTime.now(Clock.systemUTC()).minusSeconds(duration);

        dao.addTestResults(revision, totalTests, passed, failed, startTime, duration);

        final JobTestResultsDao.TestEntry testResult = dao.getTestResult(revision);
        assertThat(testResult.getRevision(), is(revision));
        assertThat(testResult.getTotalTests(), is(totalTests));
        assertThat(testResult.getPassedTests(), is(passed));
        assertThat(testResult.getFailedTests(), is(failed));
        assertThat(testResult.getStartTime(), is(startTime.toInstant().getEpochSecond()));
        assertThat(testResult.getDuration(), is(duration));
    }
}