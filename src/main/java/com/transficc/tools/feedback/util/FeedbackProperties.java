/*
 * Copyright 2016 TransFICC Ltd.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */
package com.transficc.tools.feedback.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.transficc.tools.feedback.VersionControl;

public class FeedbackProperties
{
    private final Properties properties;

    public FeedbackProperties(final Properties properties)
    {
        this.properties = properties;
    }

    public String getVertxCacheDir()
    {
        return "cache";
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

    public String getJenkinsUsername()
    {
        return properties.getProperty("feedback.jenkins.username");
    }

    public String getJenkinsPassword()
    {
        return properties.getProperty("feedback.jenkins.password");
    }

    public int getFeedbackPort()
    {
        return getInteger("feedback.port");
    }

    public String getMasterJobName()
    {
        return properties.getProperty("feedback.job.master");
    }

    public VersionControl getVersionControl()
    {
        return VersionControl.valueOf(properties.getProperty("feedback.versioncontrol").toUpperCase());
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
