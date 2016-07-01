package com.transficc.tools.feedback;

import com.transficc.infrastructure.collections.Result;
import com.transficc.tools.jenkins.Jenkins;
import com.transficc.tools.jenkins.domain.JobStatus;
import com.transficc.tools.jenkins.serialized.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobFinder implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinder.class);
    private final JobService jobService;
    private final Jenkins jenkins;
    private final JobPrioritiesRepository jobPrioritiesRepository;
    private final String masterJobName;

    public JobFinder(final JobService jobService,
                     final Jenkins jenkins, final JobPrioritiesRepository jobPrioritiesRepository, final String masterJobName)
    {
        this.jobService = jobService;
        this.jenkins = jenkins;
        this.jobPrioritiesRepository = jobPrioritiesRepository;
        this.masterJobName = masterJobName;
    }

    @Override
    public void run()
    {
        final Result<Integer, Jobs> result = jenkins.getAllJobs();
        result.consume(statusCode -> LOGGER.error("Received status code {} when trying to obtain jobs", statusCode),
                       jobs ->
                               jobs.getJobs().
                                       stream().
                                       filter(job -> !jobService.jobExists(job.getName())).
                                       map(job -> new Job(job.getName(), job.getUrl(), jobPrioritiesRepository.getPriorityForJob(job.getName()),
                                                          JobStatus.parse(job.getColor()), masterJobName.equals(job.getName()))).
                                       forEach(jobService::add));
    }
}
