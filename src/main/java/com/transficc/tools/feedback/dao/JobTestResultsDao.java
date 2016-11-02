package com.transficc.tools.feedback.dao;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class JobTestResultsDao
{
    private final JdbcTemplate jdbcTemplate;

    public JobTestResultsDao(final DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void addTestResults(final String revision, final int totalTests, final int passed, final int failed, final ZonedDateTime startTime, final long duration)
    {
        jdbcTemplate.update("INSERT INTO test_run (revision, total, passed, failed, start_time, duration) VALUES (?, ?, ?, ?, ?, ?)",
                            revision, totalTests, passed, failed, Timestamp.from(startTime.toInstant()), duration);
    }

    TestEntry getTestResult(final String revision)
    {
        return jdbcTemplate.queryForObject("SELECT id, revision, total, passed, failed, start_time, duration FROM test_run WHERE revision = ?",
                                           (rs, num) ->
                                           {

                                               final int id = rs.getInt("id");
                                               final int testTotal = rs.getInt("total");
                                               final int passedTests = rs.getInt("passed");
                                               final int failedTests = rs.getInt("failed");
                                               final Timestamp startTime = rs.getTimestamp("start_time");
                                               final int duration = rs.getInt("duration");
                                               return new TestEntry(id, revision, testTotal, passedTests, failedTests, startTime.toInstant().getEpochSecond(), duration);
                                           },
                                           revision);
    }

    static final class TestEntry
    {
        private final int id;
        private final String revision;
        private final int totalTests;
        private final int passedTests;
        private final int failedTests;
        private final long startTime;
        private final int duration;

        private TestEntry(final int id, final String revision, final int totalTests, final int passedTests, final int failedTests, final long startTime, final int duration)
        {
            this.id = id;
            this.revision = revision;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.failedTests = failedTests;
            this.startTime = startTime;
            this.duration = duration;
        }

        public int getId()
        {
            return id;
        }

        public String getRevision()
        {
            return revision;
        }

        public int getTotalTests()
        {
            return totalTests;
        }

        public int getPassedTests()
        {
            return passedTests;
        }

        public int getFailedTests()
        {
            return failedTests;
        }

        public long getStartTime()
        {
            return startTime;
        }

        public int getDuration()
        {
            return duration;
        }
    }
}
