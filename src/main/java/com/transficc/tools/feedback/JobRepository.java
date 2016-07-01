package com.transficc.tools.feedback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.transficc.tools.feedback.messaging.PublishableJob;

public class JobRepository
{
    private final Map<String, Job> jobNameToJob = new ConcurrentHashMap<>();

    public List<PublishableJob> getPublishableJobs()
    {
        return jobNameToJob.values().
                stream().
                map(Job::createPublishable).
                sorted((job1, job2) ->
                       {
                           final int comparePriority = Integer.compare(job2.getPriority(), job1.getPriority());
                           final int compareJobStatus = job1.getJobStatus().compareTo(job2.getJobStatus());
                           return comparePriority == 0 ? compareJobStatus != 0 && job1.getPriority() == 0 ? compareJobStatus : job1.getName().compareTo(job2.getName()) : comparePriority;
                       }).
                collect(Collectors.toList());
    }

    public boolean contains(final String jobName)
    {
        return jobNameToJob.containsKey(jobName);
    }

    public void put(final String jobName, final Job job)
    {
        jobNameToJob.put(jobName, job);
    }

    public void remove(final String jobName)
    {
        jobNameToJob.remove(jobName);
    }
}
