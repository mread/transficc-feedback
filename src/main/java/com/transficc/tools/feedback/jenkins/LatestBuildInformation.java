package com.transficc.tools.feedback.jenkins;

import java.util.Optional;

import com.transficc.infrastructure.collections.Optionality;
import com.transficc.tools.feedback.JobStatus;
import com.transficc.tools.feedback.JobsTestResults;
import com.transficc.tools.feedback.jenkins.serialized.JobTestResults;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class LatestBuildInformation
{
    private final String revision;
    private final JobStatus jobStatus;
    private final int number;
    private final double jobCompletionPercentage;
    private final String[] comments;
    private final boolean building;
    private final JobsTestResults testResults;

    public LatestBuildInformation(final String revision,
                                  final JobStatus jobStatus,
                                  final int number,
                                  final double jobCompletionPercentage,
                                  final String[] comments,
                                  final boolean building,
                                  final Optional<JobTestResults> testResults)
    {

        this.revision = revision;
        this.jobStatus = jobStatus;
        this.number = number;
        this.jobCompletionPercentage = jobCompletionPercentage;
        this.comments = comments;
        this.building = building;
        this.testResults = Optionality.fold(testResults, () -> null, JobTestResults::convert);
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

    public JobsTestResults getTestResults()
    {
        return testResults;
    }
}
