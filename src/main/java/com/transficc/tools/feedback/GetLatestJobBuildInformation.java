package com.transficc.tools.feedback;

import com.transficc.functionality.Result;
import com.transficc.tools.feedback.messaging.JobError;
import com.transficc.tools.feedback.messaging.MessageBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLatestJobBuildInformation implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GetLatestJobBuildInformation.class);
    private final Job job;
    private final MessageBus messageBus;
    private final String jobUrl;
    private final JenkinsFacade jenkinsFacade;
    private final JobService jobService;

    public GetLatestJobBuildInformation(final MessageBus messageBus,
                                        final JobService jobService, final Job job, final JenkinsFacade jenkinsFacade)
    {
        this.messageBus = messageBus;
        this.jenkinsFacade = jenkinsFacade;
        this.jobService = jobService;
        this.job = job;
        this.jobUrl = this.job.getUrl() + "/api/json?tree=name,url,color,lastBuild[number,url]";
    }

    @Override
    public void run()
    {
        try
        {
            final Result<Integer, JenkinsFacade.LatestBuildInformation> latestBuildInformation = jenkinsFacade.getLatestBuildInformation(job.getName(), job.getJobStatus());
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
        catch (final RuntimeException e)
        {
            LOGGER.error("An exception occurred whilst trying to gather build information", e);
        }
    }

}
