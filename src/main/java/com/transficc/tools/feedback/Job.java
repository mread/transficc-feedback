package com.transficc.tools.feedback;

import java.util.Arrays;

import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.jenkins.domain.JobStatus;
import com.transficc.tools.jenkins.domain.JobsTestResults;

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
    private volatile int buildNumber = 1;
    private volatile double jobCompletionPercentage;
    private volatile String[] comments = new String[0];
    private volatile boolean building;
    private volatile JobsTestResults jobsTestResults;

    public Job(final String name, final String url, final int priority, final JobStatus jobStatus, final boolean shouldDisplayCommentsForJob)
    {
        this.name = name;
        this.url = url;
        this.priority = priority;
        this.jobStatus = jobStatus;
        this.shouldDisplayCommentsForJob = shouldDisplayCommentsForJob;
    }

    public void maybeUpdateAndPublish(final String revision,
                                      final JobStatus jobStatus,
                                      final int buildNumber,
                                      final double jobCompletionPercentage,
                                      final MessageBus messageBus,
                                      final String[] comments,
                                      final boolean building,
                                      final JobsTestResults jobsTestResults)
    {
        if (isThereAnUpdate(revision, jobStatus, buildNumber, jobCompletionPercentage, building))
        {
            this.jobsTestResults = jobsTestResults;
            this.revision = "".equals(revision) ? this.revision : revision;
            this.jobStatus = jobStatus;
            this.buildNumber = buildNumber;
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

    public PublishableJob createPublishable()
    {
        final String revision = this.revision.length() > GIT_HASH_LENGTH ? this.revision.substring(0, GIT_HASH_LENGTH) : this.revision;
        return new PublishableJob(name, url, priority, revision, jobStatus, buildNumber, jobCompletionPercentage, comments, building, jobsTestResults);
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
               ", jobCompletionPercentage=" + jobCompletionPercentage +
               ", comments=" + Arrays.toString(comments) +
               '}';
    }

}
