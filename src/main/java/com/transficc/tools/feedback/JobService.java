package com.transficc.tools.feedback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.transficc.tools.feedback.messaging.JobError;
import com.transficc.tools.feedback.messaging.MessageBus;

public class JobService
{
    private final JobRepository jobRepository;
    private final MessageBus messageBus;
    private final JenkinsFacade jenkinsFacade;
    private final Map<String, ScheduledFuture> jobNameToScheduledRunnable = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService;

    public JobService(final JobRepository jobRepository,
                      final MessageBus messageBus,
                      final JenkinsFacade jenkinsFacade, final ScheduledExecutorService scheduledExecutorService)
    {
        this.jobRepository = jobRepository;
        this.messageBus = messageBus;
        this.jenkinsFacade = jenkinsFacade;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public void onError(final JobError jobError, final String jobName)
    {
        switch (jobError)
        {
            case NOT_FOUND:
                final ScheduledFuture future = jobNameToScheduledRunnable.remove(jobName);
                if (future != null)
                {
                    future.cancel(true);
                }
                jobRepository.remove(jobName);
                break;
            default:
                throw new IllegalArgumentException("Unhandled error: " + jobError);
        }
    }

    public void add(final Job job)
    {
        final GetLatestJobBuildInformation statusChecker = new GetLatestJobBuildInformation(messageBus, this, job, jenkinsFacade);
        final String jobName = job.getName();
        jobRepository.put(jobName, job);
        jobNameToScheduledRunnable.put(jobName, scheduledExecutorService.scheduleAtFixedRate(statusChecker, 0, 5, TimeUnit.SECONDS));
    }

    public boolean jobExists(final String jobName)
    {
        return jobRepository.contains(jobName);
    }
}
