package com.transficc.tools.feedback;

import java.util.Map;

public class JobPrioritiesRepository
{
    private final Map<String, Integer> jobsWithPriorities;

    public JobPrioritiesRepository(final Map<String, Integer> jobsWithPriorities)
    {
        this.jobsWithPriorities = jobsWithPriorities;
    }

    public int getPriorityForJob(final String jobName)
    {
        return jobsWithPriorities.getOrDefault(jobName, 0);
    }
}
