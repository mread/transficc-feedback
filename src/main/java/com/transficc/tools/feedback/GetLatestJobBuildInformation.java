package com.transficc.tools.feedback;

import com.transficc.infrastructure.collections.Result;
import com.transficc.tools.feedback.messaging.JobError;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.jenkins.Jenkins;
import com.transficc.tools.jenkins.LatestBuildInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLatestJobBuildInformation implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GetLatestJobBuildInformation.class);
    private final Job job;
    private final MessageBus messageBus;
    private final String jobUrl;
    private final Jenkins jenkins;
    private final JobService jobService;

    public GetLatestJobBuildInformation(final MessageBus messageBus,
                                        final Jenkins jenkins, final JobService jobService, final Job job)
    {
        this.messageBus = messageBus;
        this.jenkins = jenkins;
        this.jobService = jobService;
        this.job = job;
        this.jobUrl = this.job.getUrl() + "/api/json?tree=name,url,color,lastBuild[number,url]";
    }

    @Override
    public void run()
    {
        final Result<Integer, LatestBuildInformation> latestBuildInformation = jenkins.getLatestBuildInformation(jobUrl);
        latestBuildInformation.consume(statusCode ->
                                       {
                                           if (statusCode == 404)
                                           {
                                               jobService.onError(JobError.NOT_FOUND, job.getName());
                                           }
                                           else
                                           {
                                               LOGGER.error("Received status code {} whilst trying to get build information for job: {}", statusCode, job.getName());
                                           }
                                       },
                                       buildInformation ->
                                       {
                                           job.maybeUpdateAndPublish(buildInformation.getRevision(),
                                                                     buildInformation.getJobStatus(),
                                                                     buildInformation.getNumber(),
                                                                     buildInformation.getJobCompletionPercentage(),
                                                                     messageBus,
                                                                     buildInformation.getComments(),
                                                                     buildInformation.isBuilding(),
                                                                     buildInformation.getTestResults());
                                       });
    }

}
