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
package com.transficc.tools.feedback.messaging;

import java.util.Arrays;

import com.transficc.tools.feedback.JenkinsFacade;


import static com.transficc.tools.feedback.JenkinsFacade.JobStatus;

public class PublishableJob
{
    private final String name;
    private final String url;
    private final int priority;
    private final String revision;
    private final JobStatus jobStatus;
    private final int buildNumber;
    private final long timestamp;
    private final double jobCompletionPercentage;
    private final String[] comments;
    private final boolean shouldHideProgressBar;
    private final boolean shouldBeFullScreen;
    private final boolean isMasterJob;
    private final JenkinsFacade.TestResults jobsTestResults;
    private final boolean shouldHideTestResults;

    public PublishableJob(final String name,
                          final String url,
                          final int priority,
                          final String revision,
                          final JobStatus jobStatus,
                          final int buildNumber,
                          final long timestamp,
                          final double jobCompletionPercentage,
                          final String[] comments,
                          final boolean building,
                          final JenkinsFacade.TestResults jobsTestResults)
    {
        this.name = name;
        this.url = url;
        this.priority = priority;
        this.revision = revision;
        this.jobStatus = jobStatus;
        this.buildNumber = buildNumber;
        this.timestamp = timestamp;
        this.jobCompletionPercentage = jobCompletionPercentage;
        this.comments = comments.clone();
        this.shouldHideProgressBar = !building;
        this.shouldBeFullScreen = priority > 0;
        this.isMasterJob = priority == 100;
        this.shouldHideTestResults = jobsTestResults == null;
        this.jobsTestResults = jobsTestResults;
    }

    public JenkinsFacade.TestResults getJobsTestResults()
    {
        return jobsTestResults;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public int getPriority()
    {
        return priority;
    }

    public String getRevision()
    {
        return revision;
    }

    public JobStatus getJobStatus()
    {
        return jobStatus;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public double getJobCompletionPercentage()
    {
        return jobCompletionPercentage;
    }

    public boolean isShouldHideProgressBar()
    {
        return shouldHideProgressBar;
    }

    public boolean isShouldBeFullScreen()
    {
        return shouldBeFullScreen;
    }

    public boolean isMasterJob()
    {
        return isMasterJob;
    }

    public boolean isShouldHideTestResults()
    {
        return shouldHideTestResults;
    }

    public String[] getComments()
    {
        return comments;
    }

    @Override
    public String toString()
    {
        return "PublishableJob{" +
               "name='" + name + '\'' +
               ", url='" + url + '\'' +
               ", priority=" + priority +
               ", revision='" + revision + '\'' +
               ", jobStatus=" + jobStatus +
               ", buildNumber=" + buildNumber +
               ", timestamp=" + timestamp +
               ", jobCompletionPercentage=" + jobCompletionPercentage +
               ", comments=" + Arrays.toString(comments) +
               ", shouldHideProgressBar=" + shouldHideProgressBar +
               ", shouldBeFullScreen=" + shouldBeFullScreen +
               ", testResults=" + jobsTestResults +
               '}';
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

        final PublishableJob that = (PublishableJob)o;

        if (priority != that.priority)
        {
            return false;
        }
        if (buildNumber != that.buildNumber)
        {
            return false;
        }
        if (timestamp != that.timestamp)
        {
            return false;
        }
        if (Double.compare(that.jobCompletionPercentage, jobCompletionPercentage) != 0)
        {
            return false;
        }
        if (shouldHideProgressBar != that.shouldHideProgressBar)
        {
            return false;
        }
        if (shouldBeFullScreen != that.shouldBeFullScreen)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null)
        {
            return false;
        }
        if (revision != null ? !revision.equals(that.revision) : that.revision != null)
        {
            return false;
        }
        if (jobStatus != that.jobStatus)
        {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.deepEquals(comments, that.comments))
        {
            return false;
        }
        return jobsTestResults != null ? jobsTestResults.equals(that.jobsTestResults) : that.jobsTestResults == null;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + priority;
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (jobStatus != null ? jobStatus.hashCode() : 0);
        result = 31 * result + buildNumber;
        // TODO: timestamp in hashCode?
        temp = Double.doubleToLongBits(jobCompletionPercentage);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(comments);
        result = 31 * result + (shouldHideProgressBar ? 1 : 0);
        result = 31 * result + (shouldBeFullScreen ? 1 : 0);
        result = 31 * result + (jobsTestResults != null ? jobsTestResults.hashCode() : 0);
        return result;
    }
}
