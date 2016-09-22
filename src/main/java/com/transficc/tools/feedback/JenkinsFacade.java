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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildChangeSetItem;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.transficc.functionality.Result;
import com.transficc.tools.feedback.util.ClockService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsFacade.class);
    private final JenkinsServer jenkins;
    private final JobPrioritiesRepository jobPrioritiesRepository;
    private final String masterJobName;
    private final ClockService clockService;
    private final VersionControl versionControl;

    public JenkinsFacade(final JenkinsServer jenkins,
                         final JobPrioritiesRepository jobPrioritiesRepository,
                         final String masterJobName,
                         final ClockService clockService,
                         final VersionControl versionControl)
    {
        this.jenkins = jenkins;
        this.jobPrioritiesRepository = jobPrioritiesRepository;
        this.masterJobName = masterJobName;
        this.clockService = clockService;
        this.versionControl = versionControl;
    }

    public Result<Integer, List<Job>> getAllJobs(final Predicate<String> filter)
    {
        try
        {
            final Map<String, com.offbytwo.jenkins.model.Job> jobs = jenkins.getJobs();
            return Result.success(jobs.values()
                                          .stream()
                                          .filter(job -> filter.test(job.getName()))
                                          .map(job -> new Job(job.getName(), job.getUrl(), jobPrioritiesRepository.getPriorityForJob(job.getName()), JobStatus.DISABLED,
                                                              masterJobName.equals(job.getName()), versionControl))
                                          .collect(Collectors.toList()));
        }
        catch (final IOException e)
        {
            LOGGER.warn("Received an error trying to get jobs", e);
            return Result.error(500);
        }
    }

    public Result<Integer, LatestBuildInformation> getLatestBuildInformation(final String jobName, final JobStatus previousJobStatus)
    {
        try
        {
            final JobWithDetails job = jenkins.getJob(jobName);
            if (job == null)
            {
                return Result.error(404);
            }
            else if (job.getLastBuild().equals(Build.BUILD_HAS_NEVER_RAN))
            {
                return Result.error(400);
            }
            else
            {
                final BuildWithDetails buildDetails = job.getLastBuild().details();
                final String revision = getRevision(buildDetails);
                final JobStatus jobStatus = !job.isBuildable() ? JobStatus.DISABLED : JobStatus.parse(buildDetails.getResult(), previousJobStatus);
                final double jobCompletionPercentage = (double)(clockService.currentTimeMillis() - buildDetails.getTimestamp()) / buildDetails.getEstimatedDuration() * 100;
                final List<String> commentList = buildDetails
                        .getChangeSet()
                        .getItems()
                        .stream()
                        .map(BuildChangeSetItem::getComment)
                        .collect(Collectors.toList());
                final String[] comments = new String[commentList.size()];
                commentList.toArray(comments);
                final TestResults testResults = getTestResults(buildDetails);
                return Result.success(new LatestBuildInformation(revision,
                                                                 jobStatus,
                                                                 buildDetails.getNumber(),
                                                                 buildDetails.getTimestamp(),
                                                                 jobCompletionPercentage,
                                                                 comments,
                                                                 buildDetails.isBuilding(),
                                                                 testResults));
            }
        }
        catch (final IOException e)
        {
            return Result.error(500);
        }
    }

    private static String getRevision(final BuildWithDetails buildDetails)
    {
        for (final Object entries : buildDetails.getActions())
        {
            final Map<String, String> lastBuildRevision = ((Map<String, Map<String, String>>)entries).get("lastBuiltRevision");
            if (lastBuildRevision != null)
            {
                return lastBuildRevision.get("SHA1");
            }
        }
        return "";
    }

    private static TestResults getTestResults(final BuildWithDetails buildDetails)
    {
        for (final Object entries : buildDetails.getActions())
        {
            final Map<String, Object> maps = (Map<String, Object>)entries;
            if ("testReport".equals(maps.get("urlName")))
            {
                final int failCount = (int)maps.get("failCount");
                final int skipCount = (int)maps.get("skipCount");
                final int totalCount = (int)maps.get("totalCount");
                final int passCount = totalCount - failCount - skipCount;
                return new TestResults(passCount, failCount, skipCount);
            }
        }
        return null;
    }

    public static final class LatestBuildInformation
    {
        private final String revision;
        private final JobStatus jobStatus;
        private final int number;
        private final double jobCompletionPercentage;
        private final String[] comments;
        private final boolean building;
        private final TestResults testResults;
        private long timestamp;

        public LatestBuildInformation(final String revision,
                                      final JobStatus jobStatus,
                                      final int number,
                                      final long timestamp,
                                      final double jobCompletionPercentage,
                                      final String[] comments,
                                      final boolean building,
                                      final TestResults testResults)
        {
            this.revision = revision;
            this.jobStatus = jobStatus;
            this.number = number;
            this.timestamp = timestamp;
            this.jobCompletionPercentage = jobCompletionPercentage;
            this.comments = comments;
            this.building = building;
            this.testResults = testResults;
        }

        public String getRevision()
        {
            return revision;
        }

        public JobStatus getJobStatus()
        {
            return jobStatus;
        }

        public int getNumber()
        {
            return number;
        }

        public double getJobCompletionPercentage()
        {
            return jobCompletionPercentage;
        }

        public String[] getComments()
        {
            return comments;
        }

        public boolean isBuilding()
        {
            return building;
        }

        public TestResults getTestResults()
        {
            return testResults;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }

    public static class TestResults
    {
        private final int passCount;
        private final int failCount;
        private final int skipCount;

        public TestResults(final int passCount, final int failCount, final int skipCount)
        {
            this.passCount = passCount;
            this.failCount = failCount;
            this.skipCount = skipCount;
        }

        public int getPassCount()
        {

            return passCount;
        }

        public int getFailCount()
        {
            return failCount;
        }

        public int getSkipCount()
        {
            return skipCount;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final TestResults that = (TestResults)o;

            if (passCount != that.passCount)
            {
                return false;
            }
            if (failCount != that.failCount)
            {
                return false;
            }
            return skipCount == that.skipCount;

        }

        @Override
        public int hashCode()
        {
            int result = passCount;
            result = 31 * result + failCount;
            result = 31 * result + skipCount;
            return result;
        }
    }

    public enum JobStatus
    {
        //This order is important (Enum.compareTo is used in JobRepository)
        ERROR,
        DISABLED,
        SUCCESS;

        private static JobStatus parse(final BuildResult result, final JobStatus previousStatus)
        {
            if (result == null)
            {
                return previousStatus;
            }

            final JobStatus output;

            switch (result)
            {
                case ABORTED:
                case FAILURE:
                case UNSTABLE:
                    output = ERROR;
                    break;
                case SUCCESS:
                    output = SUCCESS;
                    break;
                case NOT_BUILT:
                    output = DISABLED;
                    break;
                case BUILDING:
                case REBUILDING:
                default:
                    output = previousStatus;
                    break;
            }
            return output;
        }
    }
}
