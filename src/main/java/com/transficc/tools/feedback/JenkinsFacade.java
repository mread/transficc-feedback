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

public class JenkinsFacade
{
    private final JenkinsServer jenkins;
    private final JobPrioritiesRepository jobPrioritiesRepository;
    private final String masterJobName;
    private final ClockService clockService;

    public JenkinsFacade(final JenkinsServer jenkins, final JobPrioritiesRepository jobPrioritiesRepository, final String masterJobName, final ClockService clockService)
    {
        this.jenkins = jenkins;
        this.jobPrioritiesRepository = jobPrioritiesRepository;
        this.masterJobName = masterJobName;
        this.clockService = clockService;
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
                                                              masterJobName.equals(job.getName())))
                                          .collect(Collectors.toList()));
        }
        catch (final IOException e)
        {
            return Result.error(500);
        }
    }

    public Result<Integer, LatestBuildInformation> getLatestBuildInformation(final String jobName, final JobStatus previousJobStatus)
    {
        try
        {
            final JobWithDetails job = jenkins.getJob(jobName);
            if (job == null || job.getLastBuild().equals(Build.BUILD_HAS_NEVER_RAN))
            {
                return Result.error(400);
            }
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
            return Result.success(new LatestBuildInformation(revision, jobStatus, buildDetails.getNumber(), jobCompletionPercentage, comments, buildDetails.isBuilding(), testResults));
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
