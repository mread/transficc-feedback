package com.transficc.tools.feedback.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FeedbackProperties
{
    private final Properties properties;

    public FeedbackProperties(final Properties properties)
    {
        this.properties = properties;
    }

    public String getVertxCacheDir()
    {
        return properties.getProperty("feedback.vertx.cachedir");
    }

    public Map<String, Integer> getJobsWithPriorities()
    {
        final String[] jobNames = getArrayProperty("feedback.job.name");
        final String[] jobPriorities = getArrayProperty("feedback.job.priority");
        if (jobNames.length != jobPriorities.length)
        {
            throw new RuntimeException("There must be the same number of job names and priorities");
        }

        final Map<String, Integer> jobNameToPriority = new HashMap<>();
        for (int i = 0; i < jobNames.length; i++)
        {
            jobNameToPriority.put(jobNames[i], Integer.parseInt(jobPriorities[i]));
        }
        return jobNameToPriority;
    }

    public String getJenkinsUrl()
    {
        return properties.getProperty("feedback.jenkins.url");
    }

    public int getFeedbackPort()
    {
        return getInteger("feedback.port");
    }

    public String getMasterJobName()
    {
        return properties.getProperty("feedback.job.master");
    }

    private int getInteger(final String key)
    {
        return Integer.parseInt(properties.getProperty(key));
    }

    private String[] getArrayProperty(final String key)
    {
        final String property = properties.getProperty(key);
        return property.split(",");
    }

}
