package com.transficc.tools.feedback;

import java.util.List;

import com.transficc.functionality.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobFinder implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinder.class);
    private final JobService jobService;
    private final JenkinsFacade jenkinsFacade;

    public JobFinder(final JobService jobService, final JenkinsFacade jenkinsFacade)
    {
        this.jobService = jobService;
        this.jenkinsFacade = jenkinsFacade;
    }

    @Override
    public void run()
    {
        final Result<Integer, List<Job>> result = jenkinsFacade.getAllJobs(name -> !jobService.jobExists(name));
        result.consume(statusCode -> LOGGER.error("Received status code {} when trying to obtain jobs", statusCode),
                       jobs ->
                               jobs.stream()
                                       .forEach(jobService::add));
    }
}
