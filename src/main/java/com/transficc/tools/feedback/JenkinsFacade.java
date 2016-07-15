package com.transficc.tools.feedback;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.transficc.functionality.Result;
import com.transficc.tools.jenkins.Jenkins;
import com.transficc.tools.jenkins.domain.JobsTestResults;
import com.transficc.tools.jenkins.serialized.Jobs;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class JenkinsFacade
{
    private final Jenkins jenkins;
    private final JobPrioritiesRepository jobPrioritiesRepository;
    private final String masterJobName;

    public JenkinsFacade(final Jenkins jenkins, final JobPrioritiesRepository jobPrioritiesRepository, final String masterJobName)
    {
        this.jenkins = jenkins;
        this.jobPrioritiesRepository = jobPrioritiesRepository;
        this.masterJobName = masterJobName;
    }

    public Result<Integer, List<Job>> getAllJobs(final Predicate<String> filter)
    {
        final Result<Integer, Jobs> result = jenkins.getAllJobs();
        return result.fold(Result::error,
                           jobs ->
                                   Result.success(jobs.getJobs().
                                           stream().
                                           filter(job -> filter.test(job.getName())).
                                           map(job -> new Job(job.getName(), job.getUrl(), jobPrioritiesRepository.getPriorityForJob(job.getName()),
                                                              JobStatus.DISABLED, masterJobName.equals(job.getName()))).
                                           collect(Collectors.toList())));

    }

    public Result<Integer, LatestBuildInformation> getLatestBuildInformation(final String jobUrl)
    {
        final Result<Integer, com.transficc.tools.jenkins.domain.LatestBuildInformation> latestBuildInformation = jenkins.getLatestBuildInformation(jobUrl);
        return latestBuildInformation.fold(Result::error,
                                           buildInformation ->
                                           {
                                               final JobsTestResults testResults = buildInformation.getTestResults();
                                               return Result.success(new LatestBuildInformation(buildInformation.getRevision(),
                                                                                                JobStatus.parse(buildInformation.getJobStatus()),
                                                                                                buildInformation.getNumber(),
                                                                                                buildInformation.getJobCompletionPercentage(),
                                                                                                buildInformation.getComments(),
                                                                                                buildInformation.isBuilding(),
                                                                                                new TestResults(testResults.getPassCount(),
                                                                                                                testResults.getFailCount(),
                                                                                                                testResults.getSkipCount(),
                                                                                                                testResults.getDuration())));
                                           }
        );
    }

    @SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
    public static final class LatestBuildInformation
    {
        private final String revision;
        private final JobStatus jobStatus;
        private final int number;
        private final double jobCompletionPercentage;
        private final String[] comments;
        private final boolean building;
        private final TestResults testResults;

        public LatestBuildInformation(final String revision,
                                      final JobStatus jobStatus,
                                      final int number,
                                      final double jobCompletionPercentage,
                                      final String[] comments, final boolean building, final TestResults testResults)
        {
            this.revision = revision;
            this.jobStatus = jobStatus;
            this.number = number;
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
    }

    public static class TestResults
    {
        private final int passCount;
        private final int failCount;
        private final int skipCount;
        private final double duration;

        public TestResults(final int passCount, final int failCount, final int skipCount, final double duration)
        {
            this.passCount = passCount;
            this.failCount = failCount;
            this.skipCount = skipCount;
            this.duration = duration;
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

        public double getDuration()
        {
            return duration;
        }
    }

    public enum JobStatus
    {
        //This order is important (Enum.compareTo is used in JobRepository)
        ERROR,
        DISABLED,
        SUCCESS;

        public static JobStatus parse(final com.transficc.tools.jenkins.domain.JobStatus jobStatus)
        {
            switch (jobStatus)
            {
                case SUCCESS:
                    return SUCCESS;
                case DISABLED:
                    return DISABLED;
                case ERROR:
                    return ERROR;
                default:
                    throw new IllegalArgumentException("Unknown job status " + jobStatus);
            }
        }
    }
}
