package com.transficc.tools.feedback.jenkins;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.transficc.infrastructure.collections.Result;
import com.transficc.tools.feedback.HttpClientFacade;
import com.transficc.tools.feedback.JobStatus;
import com.transficc.tools.feedback.jenkins.serialized.Action;
import com.transficc.tools.feedback.jenkins.serialized.Branch;
import com.transficc.tools.feedback.jenkins.serialized.BuildInfo;
import com.transficc.tools.feedback.jenkins.serialized.Item;
import com.transficc.tools.feedback.jenkins.serialized.JobTestResults;
import com.transficc.tools.feedback.jenkins.serialized.Jobs;
import com.transficc.tools.feedback.jenkins.serialized.LastBuildInformation;
import com.transficc.tools.feedback.util.ClockService;

public class Jenkins
{
    private static final String JOBS_DATA_URL = "/api/json?tree=jobs[name,url,color,lastBuild[number,url]]";
    private static final String BUILD_DATA_URL = "/api/json?tree=estimatedDuration,building,url,timestamp,changeSet[items[id,comment]],actions[buildsByBranchName[revision[SHA1]]]";
    private static final String BRANCH_NAME = "refs/remotes/origin/master";
    private final ClockService clockService;
    private final HttpClientFacade httpClient;
    private final String jobsUrl;

    public Jenkins(final ClockService clockService,
                   final HttpClientFacade httpClient,
                   final String url)
    {
        this.clockService = clockService;
        this.httpClient = httpClient;
        this.jobsUrl = url + JOBS_DATA_URL;
    }

    public Result<Integer, LatestBuildInformation> getLatestBuildInformation(final String jobUrl)
    {
        final Result<Integer, com.transficc.tools.feedback.jenkins.serialized.Job> jenkinsJobResult = getLatestBuildFor(jobUrl);
        return jenkinsJobResult.fold((Function<Integer, Result<Integer, LatestBuildInformation>>)Result::error,
                                     job ->
                                     {
                                         final LastBuildInformation lastBuild = job.getLastBuild();
                                         final boolean isThereALastBuild = lastBuild != null;
                                         if (!isThereALastBuild)
                                         {
                                             return Result.error(400);
                                         }
                                         final Optional<JobTestResults> testResults = getTestResultsFor(lastBuild.getUrl());
                                         final Result<Integer, Build> lastBuildInformation = getBuildInformationFor(lastBuild.getUrl());
                                         return lastBuildInformation.fold(
                                                 (Function<Integer, Result<Integer, LatestBuildInformation>>)Result::error,
                                                 build ->
                                                 {
                                                     final long currentTimeMillis = clockService.currentMillis();
                                                     final double jobCompletionPercentage = (double)(currentTimeMillis - build.getTimestamp()) / build.getEstimatedDuration() * 100;
                                                     return Result.success(new LatestBuildInformation(build.getRevision(), JobStatus.parse(job.getColor()), lastBuild.getNumber(),
                                                                                                      jobCompletionPercentage, build.getComments(), build.isBuilding(), testResults));
                                                 });
                                     });
    }

    public Result<Integer, Jobs> getAllJobs()
    {
        return httpClient.get(jobsUrl, Jobs.class);
    }

    private Result<Integer, com.transficc.tools.feedback.jenkins.serialized.Job> getLatestBuildFor(final String jobUrl)
    {
        return httpClient.get(jobUrl, com.transficc.tools.feedback.jenkins.serialized.Job.class);
    }

    private Optional<JobTestResults> getTestResultsFor(final String url)
    {
        final String testResultUrl = url + "testReport/api/json?tree=passCount,failCount,skipCount,duration";
        final Result<Integer, JobTestResults> testResults = httpClient.get(testResultUrl, JobTestResults.class);
        return testResults.fold((Function<Integer, Optional<JobTestResults>>)error -> Optional.empty(), Optional::of);
    }

    private Result<Integer, Build> getBuildInformationFor(final String url)
    {
        final Result<Integer, BuildInfo> result = httpClient.get(url + BUILD_DATA_URL, BuildInfo.class);
        return result.map(buildInfo ->
                          {
                              final String revision;
                              final String[] comments;

                              if (buildInfo.getChangeSet() == null || buildInfo.getChangeSet().getItems().length == 0)
                              {
                                  revision = findRevisionFrom(buildInfo.getActions());
                                  comments = new String[0];
                              }
                              else
                              {
                                  final Item[] changeSetItems = buildInfo.getChangeSet().getItems();
                                  revision = changeSetItems[changeSetItems.length - 1].getId();
                                  comments = Arrays.stream(changeSetItems).filter(item -> item.getComment() != null).map(Item::getComment).toArray(String[]::new);
                              }
                              return new Build(revision, comments, buildInfo.getTimestamp(), buildInfo.getEstimatedDuration(), buildInfo.isBuilding());
                          });
    }

    private String findRevisionFrom(final Action[] actions)
    {
        for (final Action action : actions)
        {
            if (action.hasData())
            {
                final Branch branch = action.get(BRANCH_NAME);
                return branch.getRevision();
            }
        }
        return "";
    }
}
