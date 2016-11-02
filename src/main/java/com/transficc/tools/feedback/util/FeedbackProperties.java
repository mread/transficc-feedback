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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.transficc.functionality.Optionality;
import com.transficc.tools.feedback.FeedbackMain;
import com.transficc.tools.feedback.VersionControl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedbackProperties
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackProperties.class);
    private final Properties properties;

    public FeedbackProperties(final Optional<File> propertiesFile)
    {
        this.properties = createProperties(propertiesFile);
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

    public String[] getJobNamesForTestResultsToPersist()
    {
        return getArrayProperty("feedback.test.job");
    }

    private int getInteger(final String key)
    {
        return Integer.parseInt(properties.getProperty(key));
    }

    private String[] getArrayProperty(final String key)
    {
        final String property = properties.getProperty(key);
        if (property == null)
        {
            return new String[0];
        }
        return property.split(",");
    }

    private static Properties createProperties(final Optional<File> propertiesFile)
    {
        final Properties properties = new Properties();
        Optionality.consume(propertiesFile, () ->
                            {
                                final ClassLoader classLoader = FeedbackMain.class.getClassLoader();
                                final InputStream serviceProperties = classLoader.getResourceAsStream("feedback.properties");
                                if (serviceProperties == null)
                                {
                                    throw new IllegalArgumentException("No feedback.properties file found on classpath. " +
                                                                       "Specify the location of one as the first argument, or put it on your classpath");
                                }
                                try
                                {
                                    properties.load(serviceProperties);
                                }
                                catch (final IOException e)
                                {
                                    LOGGER.error("Failed to load properties", e);
                                }
                            },
                            (File file) ->
                            {
                                try (final FileInputStream inStream = new FileInputStream(file))
                                {
                                    properties.load(inStream);
                                }
                                catch (final IOException e)
                                {
                                    LOGGER.error("Failed to load properties", e);
                                }

                            });
        return properties;

    }
}
