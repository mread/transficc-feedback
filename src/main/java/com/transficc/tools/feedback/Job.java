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

import java.util.Arrays;

import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;


import static com.transficc.tools.feedback.JenkinsFacade.JobStatus;
import static com.transficc.tools.feedback.JenkinsFacade.TestResults;

public class Job
{
    private static final String[] NO_COMMENTS = new String[0];
    private static final int GIT_HASH_LENGTH = 7;
    private final String name;
    private final String url;
    private final int priority;
    private volatile String revision = "";
    private volatile JobStatus jobStatus;
    private final boolean shouldDisplayCommentsForJob;
    private final VersionControl versionControl;
    private volatile int buildNumber = 0;
    private volatile double jobCompletionPercentage;
    private volatile String[] comments = new String[0];
    private volatile boolean building;
    private volatile TestResults jobsTestResults;
    private volatile long timestamp;

    public Job(final String name, final String url, final int priority, final JobStatus jobStatus, final boolean shouldDisplayCommentsForJob, final VersionControl versionControl)
    {
        this.name = name;
        this.url = url;
        this.priority = priority;
        this.jobStatus = jobStatus;
        this.shouldDisplayCommentsForJob = shouldDisplayCommentsForJob;
        this.versionControl = versionControl;
    }

    public void maybeUpdateAndPublish(final String revision,
                                      final JobStatus jobStatus,
                                      final int buildNumber,
                                      final long timestamp,
                                      final double jobCompletionPercentage,
                                      final MessageBus messageBus,
                                      final String[] comments,
                                      final boolean building,
                                      final TestResults jobsTestResults)
    {
        if (isThereAnUpdate(revision, jobStatus, buildNumber, jobCompletionPercentage, building))
        {
            this.jobsTestResults = jobsTestResults;
            this.revision = "".equals(revision) ? this.revision : revision;
            this.jobStatus = jobStatus;
            this.buildNumber = buildNumber;
            this.timestamp = timestamp;
            this.jobCompletionPercentage = jobCompletionPercentage;
            this.comments = shouldDisplayCommentsForJob ? comments : NO_COMMENTS;
            this.building = building;
            messageBus.sendUpdate(this);
        }
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public JobStatus getJobStatus()
    {
        return jobStatus;
    }

    public PublishableJob createPublishable()
    {
        final String revision = getRevision();
        return new PublishableJob(name, url, priority, revision, jobStatus, buildNumber, timestamp, jobCompletionPercentage, comments, building, jobsTestResults);
    }

    private String getRevision()
    {
        final String calculatedRevision;
        switch (versionControl)
        {
            case GIT:
                calculatedRevision = revision.length() > GIT_HASH_LENGTH ? revision.substring(0, GIT_HASH_LENGTH) : revision;
                break;
            case SVN:
            default:
                calculatedRevision = revision;
        }
        return calculatedRevision;
    }

    private boolean isThereAnUpdate(final String revision, final JobStatus jobStatus, final int buildNumber, final double jobCompletionPercentage, final boolean building)
    {
        return !this.revision.equals(revision) || this.jobStatus != jobStatus || this.buildNumber != buildNumber ||
               (this.jobCompletionPercentage != jobCompletionPercentage && !(this.jobCompletionPercentage > 100 && jobCompletionPercentage > 100)) || this.building != building;
    }

    @Override
    public String toString()
    {
        return "Job{" +
               "name='" + name + '\'' +
               ", url='" + url + '\'' +
               ", priority=" + priority +
               ", revision='" + revision + '\'' +
               ", jobStatus=" + jobStatus +
               ", buildNumber=" + buildNumber +
               ", timestamp=" + timestamp +
               ", jobCompletionPercentage=" + jobCompletionPercentage +
               ", comments=" + Arrays.toString(comments) +
               '}';
    }

}
